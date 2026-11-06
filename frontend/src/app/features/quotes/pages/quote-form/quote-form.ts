import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { 
  forkJoin,
  Subscription, 
  Subject,
  timer,
  debounceTime,
  distinctUntilChanged,
  switchMap,
  map,
  catchError,
  of,
  tap
} from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { QuoteApiService } from '../../services/quote-api.service';
import { InventoryItem, QuoteLookupService } from '../../services/quote-lookup.service';
import { Quote, QuoteItemPayload, QuotePayload } from '../../models/quote.model';
import { Client } from '../../../clients/models/client.model';
import { Button } from '../../../../shared/ui/button/button';

interface QuoteLineDraft {
  key: number;
  id?: number;
  inventoryItem: InventoryItem;
  quantity: number;
  discount: number;
}

interface LineTotal {
  subtotal: number;
  tax: number;
  total: number;
}

@Component({
  selector: 'app-quote-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    Button
  ],
  templateUrl: './quote-form.html',
  styleUrl: './quote-form.scss'
})
export class QuoteForm implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  private readonly quoteApi = inject(QuoteApiService);
  private readonly lookup = inject(QuoteLookupService);

  private readonly clientSearch$ = new Subject<string>();

  private clientSearchRequest?: Subscription;
  private inventorySearchRequest?: Subscription;

  private nextLineKey = 0;

  // ======================================
  // ESTADO
  // ======================================

  readonly quoteId = signal<number | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly selectedClient = signal<Client | null>(null);
  readonly clientQuery = signal('');
  readonly clients = signal<Client[]>([]);
  readonly searchingClients = signal(false);

  readonly inventoryQuery = signal('');
  readonly inventoryResults = signal<InventoryItem[]>([]);
  readonly searchingInventory = signal(false);

  readonly lines = signal<QuoteLineDraft[]>([]);
  readonly isEditMode = computed(() => this.quoteId() !== null);

  readonly title = computed(() => 
    this.isEditMode() ? 'Editar presupuesto' : 'Nuevo presupuesto');

  // ======================================
  // FORMULARIO
  // ======================================

  readonly form = this.fb.group({
    clientId: this.fb.control<number | null>(null, Validators.required),
    quoteDate: this.fb.nonNullable.control(this.today(), Validators.required),
    expirationDate: this.fb.nonNullable.control(this.todayPlusDays(30), Validators.required),
    notes: this.fb.nonNullable.control('', Validators.maxLength(1000))
  });

  // ======================================
  // CONSTRUCTOR
  // ======================================

  constructor() {
    this.setupClientAutocomplete();
  }

  // ======================================
  // CÁLCULOS ESTIMADOS
  // ======================================

  readonly totals = computed(() => {

    let subtotal = 0;
    let tax = 0;
    let discount = 0;

    for (const line of this.lines()) {
      const calculated = this.calculateLine(line);

      subtotal += calculated.subtotal;
      tax += calculated.tax;
      discount += line.discount;
    }

    subtotal = this.roundMoney(subtotal);
    tax = this.roundMoney(tax);

    return {
      subtotal,
      tax,
      discount: this.roundMoney(discount),
      total: this.roundMoney(subtotal + tax)
    };
  });

  readonly validLines = computed(() =>
    this.lines().length > 0 &&
    this.lines().every(line =>
      Number.isInteger(line.quantity) &&
      line.quantity > 0 &&
      Number.isFinite(line.discount) &&
      line.discount >= 0 &&
      line.discount <=
        this.roundMoney(
          line.inventoryItem.salePrice * line.quantity
        ) &&
      line.inventoryItem.active
    )
  );

  // ======================================
  // INICIALIZACIÓN
  // ======================================

  ngOnInit(): void {

    // Cancelar suscripciones al destruir el componente.
    this.destroyRef.onDestroy(() => {
      this.clientSearchRequest?.unsubscribe();
      this.inventorySearchRequest?.unsubscribe();
    });

    // Comprobar si estamos editando un presupuesto.
    const idParam = this.route.snapshot.paramMap.get('id');

    // MODO CREACIÓN
    if (idParam === null) {

      // Si venimos desde la ficha de un cliente,
      // lo seleccionamos automáticamente.
      this.preselectClientFromUrl();

      return;
    }

    // MODO EDICIÓN
    const id = Number(idParam);

    if (!Number.isSafeInteger(id) || id <= 0) {
      this.error.set('Identificador de presupuesto inválido.');
      return;
    }

    this.quoteId.set(id);

    // Recuperar el presupuesto existente.
    this.loadQuote(id);
  }

  private loadQuote(id: number): void {

    this.loading.set(true);
    this.error.set(null);

    this.quoteApi.getById(id).pipe(
      switchMap(quote => {

        if (quote.status !== 'DRAFT') {
          throw new Error(
            'Solo se pueden editar presupuestos en borrador.'
          );
        }

        if (!quote.active) {
          throw new Error(
            'No se puede editar un presupuesto inactivo.'
          );
        }

        const items = quote.items ?? [];

        const inventoryRequests = items.map(item =>
          this.lookup.getInventoryItem(item.inventoryItemId)
        );

        return forkJoin({
          quote: of(quote),
          client: this.lookup.getClient(quote.clientId),
          inventory: inventoryRequests.length
            ? forkJoin(inventoryRequests)
            : of([] as InventoryItem[])
        });
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: ({ quote, client, inventory }) => {

        this.selectedClient.set(client);
        this.clientQuery.set(this.clientName(client));

        this.form.patchValue({
          clientId: quote.clientId,
          quoteDate: quote.quoteDate,
          expirationDate: quote.expirationDate ?? '',
          notes: quote.notes ?? ''
        });

        this.lines.set((quote.items ?? []).map((item, index) => ({
          key: ++this.nextLineKey,
          id: item.id,
          inventoryItem: inventory[index],
          quantity: item.quantity,
          discount: item.discount ?? 0
        })));

        this.loading.set(false);
      },
      error: err => {
        this.error.set(
          err instanceof Error && !(err instanceof HttpErrorResponse)
            ? err.message
            : 'No se ha podido cargar el presupuesto.'
        );
        this.loading.set(false);
      }
    });
  }

  private getReturnUrl(): string {
    const requested = this.route.snapshot
      .queryParamMap.get('returnUrl');

    // Solo se aceptan rutas internas del listado.
    if (
      requested &&
      (requested === '/quotes' ||
        requested.startsWith('/quotes?'))
    ) {
      return requested;
    }

    return '/quotes';
  }

  private preselectClientFromUrl(): void {

    // Solo aplicamos precarga al crear.
    // En edición mandan los datos existentes.
    if (this.isEditMode()) {
      return;
    }

    const clientIdParam = this.route.snapshot
      .queryParamMap.get('clientId');

    if (!clientIdParam) {
      return;
    }

    const clientId = Number(clientIdParam);

    if (
      !Number.isSafeInteger(clientId) ||
      clientId <= 0
    ) {
      this.error.set(
        'El identificador de cliente no es válido.'
      );
      return;
    }

    this.lookup.getClient(clientId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({

        next: client => {

          if (!client.active) {
            this.error.set(
              'No se puede crear un presupuesto para un cliente inactivo.'
            );
            return;
          }

          this.selectClient(client);
        },

        error: () => {
          this.error.set(
            'No se ha podido recuperar el cliente seleccionado.'
          );
        }
      });
  }

  // ======================================
  // BÚSQUEDA DE CLIENTES
  // ======================================

  private setupClientAutocomplete(): void {

    this.clientSearch$
      .pipe(
        switchMap(term => {

          this.searchingClients.set(term.length >= 2);

          if (term.length < 2) {
            return of([] as Client[]);
          }

          return timer(300).pipe(
            switchMap(() =>
              this.lookup.searchClients(term)
            ),
            map(page => page.content),
            catchError(err => {
              console.error('Error buscando clientes:', err);
              return of([] as Client[]);
            })
          );
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(results => {
        this.clients.set(results);
        this.searchingClients.set(false);
      });
  }

  searchClients(): void {

    const term = this.clientQuery().trim();

    this.clientSearchRequest?.unsubscribe();
    this.clients.set([]);

    if (term.length < 2) {
      return;
    }

    this.searchingClients.set(true);
    this.error.set(null);

    this.clientSearchRequest = this.lookup
      .searchClients(term)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.clients.set(page.content);
          this.searchingClients.set(false);
        },
        error: () => {
          this.error.set(
            'No se ha podido buscar clientes.'
          );
          this.searchingClients.set(false);
        }
      });
  }

  updateClientQuery(value: string): void {

    this.clientQuery.set(value);

    // Si el usuario modifica el texto,
    // la selección anterior deja de ser válida.
    this.selectedClient.set(null);
    this.form.controls.clientId.setValue(null);

    this.clients.set([]);

    this.clientSearch$.next(value.trim());
  }

  selectClient(client: Client): void {

    this.selectedClient.set(client);
    this.form.controls.clientId.setValue(client.id);
    this.clientQuery.set(this.clientName(client));
    this.clients.set([]);

    // Cancela búsquedas todavía pendientes.
    this.clientSearch$.next('');

    this.searchingClients.set(false);
  }

  clientName(client: Client): string {
    return [
      client.name,
      client.surname,
      client.lastSurname
    ].filter(Boolean).join(' ');
  }

  // ======================================
  // BÚSQUEDA DE INVENTARIO
  // ======================================

  searchInventory(): void {

    const term = this.inventoryQuery().trim();

    this.inventorySearchRequest?.unsubscribe();
    this.inventoryResults.set([]);

    if (term.length < 2) {
      return;
    }

    this.searchingInventory.set(true);
    this.error.set(null);

    this.inventorySearchRequest = this.lookup
      .searchInventory(term)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.inventoryResults.set(
            page.content.filter(item => item.active)
          );
          this.searchingInventory.set(false);
        },
        error: () => {
          this.error.set(
            'No se ha podido buscar artículos.'
          );
          this.searchingInventory.set(false);
        }
      });
  }

  updateInventoryQuery(value: string): void {
    this.inventoryQuery.set(value);
    this.inventoryResults.set([]);
  }

  addItem(item: InventoryItem): void {

    if (!item.active) {
      return;
    }

    this.lines.update(lines => [
      ...lines,
      {
        key: ++this.nextLineKey,
        inventoryItem: item,
        quantity: 1,
        discount: 0
      }
    ]);

    this.inventoryQuery.set('');
    this.inventoryResults.set([]);
  }

  // ======================================
  // EDICIÓN DE LÍNEAS
  // ======================================

  updateQuantity(key: number, value: string): void {

    const quantity = Number(value);

    this.lines.update(lines => lines.map(line => line.key === key 
      ? { ...line, quantity } : line)
    );
  }

  updateDiscount(key: number, value: string): void {

    const discount = Number(value);

    this.lines.update(lines => lines.map(line => line.key === key 
      ? { ...line, discount } : line)
    );
  }

  removeItem(key: number): void {
    this.lines.update(lines => lines.filter(line => line.key !== key));
  }

  calculateLine(line: QuoteLineDraft): LineTotal {

    const gross =
      line.inventoryItem.salePrice * line.quantity;

    const subtotal = this.roundMoney(
      Math.max(0, gross - line.discount)
    );

    const tax = this.roundMoney(
      subtotal * line.inventoryItem.taxRate / 100
    );

    return {
      subtotal,
      tax,
      total: this.roundMoney(subtotal + tax)
    };
  }

  // ======================================
  // GUARDADO
  // ======================================

  save(): void {

    this.form.markAllAsTouched();

    if (
      this.form.invalid ||
      !this.validLines() ||
      this.loading() ||
      this.saving()
    ) {
      return;
    }

    const value = this.form.getRawValue();

    if (value.clientId === null || value.expirationDate < value.quoteDate) {
      this.error.set('Revisa el cliente y las fechas del presupuesto.');
      return;
    }

    const items: QuoteItemPayload[] = this.lines().map(line => ({
      ...(line.id != null ? { id: line.id } : {}),
      inventoryItemId: line.inventoryItem.id,
      quantity: line.quantity,
      discount: line.discount
    }));

    const payload: QuotePayload = {
      clientId: value.clientId,
      status: 'DRAFT',
      quoteDate: value.quoteDate,
      expirationDate: value.expirationDate,
      notes: value.notes.trim() || null,
      items
    };

    this.saving.set(true);
    this.error.set(null);

    const id = this.quoteId();

    const request = id === null
      ? this.quoteApi.create(payload)
      : this.quoteApi.update(id, payload);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.saving.set(false);
        void this.router.navigateByUrl(this.getReturnUrl());
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);

        if (err.status === 400) {
          this.error.set('Los datos del presupuesto no son válidos.');
        } else if (err.status === 404) {
          this.error.set('No se ha encontrado el presupuesto, cliente o artículo.');
        } else if (err.status === 409) {
          this.error.set('El presupuesto ya no se puede modificar.');
        } else {
          this.error.set('No se ha podido guardar el presupuesto.');
        }

        console.error('Error guardando presupuesto:', err);
      }
    });
  }

  goBack(): void {
    void this.router.navigate(['/quotes']);
  }

  // ======================================
  // UTILIDADES
  // ======================================

  private roundMoney(value: number): number {
    return Math.round((value + Number.EPSILON) * 100) / 100;
  }

  private today(): string {
    return this.localDate(new Date());
  }

  private todayPlusDays(days: number): string {
    const date = new Date();
    date.setDate(date.getDate() + days);
    return this.localDate(date);
  }

  private localDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
