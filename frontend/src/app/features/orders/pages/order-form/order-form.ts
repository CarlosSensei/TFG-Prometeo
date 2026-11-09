
import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Client } from '../../../clients/models/client.model';
import { CommercialClientSelector } from '../../../../shared/commercial/components/commercial-client-selector/commercial-client-selector';
import { CommercialInventorySearch } from '../../../../shared/commercial/components/commercial-inventory-search/commercial-inventory-search';
import { CommercialLinesEditor } from '../../../../shared/commercial/components/commercial-lines-editor/commercial-lines-editor';
import { CommercialTotalsComponent } from '../../../../shared/commercial/components/commercial-totals/commercial-totals';
import { CommercialLookupService } from '../../../../shared/commercial/services/commercial-lookup.service';
import { InventoryItem } from '../../../../shared/commercial/models/inventory-item.model';
import { CommercialLineDraft } from '../../../../shared/commercial/models/commercial-line.model';
import { calculateTotals, roundMoney } from '../../../../shared/commercial/utils/commercial-calculations';
import { OrderPayload } from '../../models/order.model';
import { OrderApiService } from '../../services/order-api.service';
import { ListNavigationStateService } from '../../../../core/services/list-navigation-state.service';
import { Button } from '../../../../shared/ui/button/button';

@Component({
  selector: 'app-order-form',
  standalone: true,
  imports: [
    ReactiveFormsModule, 
    CommercialClientSelector, 
    CommercialInventorySearch, 
    CommercialLinesEditor, 
    CommercialTotalsComponent, Button],
  templateUrl: './order-form.html',
  styleUrl: './order-form.scss'
})
export class OrderForm implements OnInit {

  // ======================================
  // DEPENDENCIAS
  // ======================================

  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly api = inject(OrderApiService);
  private readonly lookup = inject(CommercialLookupService);
  private readonly listNavigation = inject(ListNavigationStateService);

  private nextLineKey = 0;

  // ======================================
  // ESTADO
  // ======================================

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly selectedClient = signal<Client | null>(null);

  readonly lines = signal<CommercialLineDraft[]>([]);

  // ======================================
  // FORMULARIO
  // ======================================

  readonly form = this.fb.group({

    clientId: this.fb.control<number | null>(null, Validators.required),
    orderDate: this.fb.nonNullable.control(this.localDate(new Date()), Validators.required),
    expectedDeliveryDate: this.fb.nonNullable.control(''),
    notes: this.fb.nonNullable.control('', Validators.maxLength(1000))
  });

  // ======================================
  // CÁLCULO ECONÓMICO
  // ======================================

  readonly totals = computed(() => calculateTotals(this.lines()));

  // ======================================
  // VALIDACIÓN DE ARTÍCULOS
  // ======================================

  readonly validLines = computed(() => 
    this.lines().length > 0 && this.lines().every(line => {

      const gross = roundMoney(line.inventoryItem.salePrice * line.quantity);

      return (
        Number.isInteger(line.quantity) &&
        line.quantity > 0 &&
        Number.isFinite(line.discount) &&
        line.discount >= 0 &&
        line.discount <= gross &&
        line.inventoryItem.active
      );
    })
  );

  // ======================================
  // INICIALIZACIÓN
  // ======================================

  ngOnInit(): void {
    this.preselectClientFromUrl();
  }

  // ======================================
  // PRECARGA DEL CLIENTE
  // ======================================

  private preselectClientFromUrl(): void {

    const idParam = this.route.snapshot.queryParamMap.get('clientId');

    if (!idParam) {
      return;
    }

    const clientId = Number(idParam);

    if (!Number.isSafeInteger(clientId) || clientId <= 0) {
      this.error.set('El identificador de cliente no es válido.');
      return;
    }

    this.loading.set(true);

    this.lookup.getClient(clientId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({

        next: client => {

          if (!client.active) {
            this.error.set('No se puede crear un pedido para un cliente inactivo.');
          } else {
            this.selectClient(client);
          }

          this.loading.set(false);
        },

        error: () => {
          this.error.set('No se ha podido recuperar el cliente seleccionado.');
          this.loading.set(false);
        }
      });
  }

  // ======================================
  // CLIENTE
  // ======================================

  selectClient(client: Client | null): void {

    this.selectedClient.set(client);
    this.form.controls.clientId.setValue(client?.id ?? null);
  }

  // ======================================
  // ARTÍCULOS
  // ======================================

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
  }

  removeItem(key: number): void {

    this.lines.update(lines => lines.filter(line => line.key !== key));
  }

  updateQuantity(key: number, value: string): void {

    const quantity = Number(value);

    this.lines.update(lines => lines.map(line =>
        line.key === key ? { ...line, quantity } : line)
    );
  }

  updateDiscount(key: number, value: string): void {

    const discount = Number(value);

    this.lines.update(lines => lines.map(line =>
        line.key === key ? { ...line, discount } : line)
    );
  }

  // ======================================
  // GUARDAR PEDIDO
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

    if (value.clientId === null) {
      this.error.set('Selecciona un cliente.');
      return;
    }

    if (
      value.expectedDeliveryDate &&
      value.expectedDeliveryDate < value.orderDate
    ) {
      this.error.set('La entrega prevista no puede ser anterior a la fecha del pedido.');
      return;
    }

    const payload: OrderPayload = {

      clientId: value.clientId,
      orderDate: value.orderDate,
      expectedDeliveryDate: value.expectedDeliveryDate || null,
      notes: value.notes.trim() || null,

      items: this.lines().map(line => ({
        inventoryItemId: line.inventoryItem.id,
        quantity: line.quantity,
        discount: line.discount
      }))
    };

    this.saving.set(true);
    this.error.set(null);

    this.api.create(payload).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({

        next: () => {
          this.saving.set(false);

          void this.router.navigateByUrl(this.getReturnUrl());
        },

        error: (err: HttpErrorResponse) => {

          console.error('Error creando pedido:', err);
          this.saving.set(false);

          if (err.status === 400) {
            this.error.set('Revisa los datos del pedido.');
          } else if (err.status === 404) {
            this.error.set('No se ha encontrado el cliente o alguno de los artículos.');
          } else if (err.status === 409) {
            this.error.set(err.error?.message ?? 'No se ha podido completar la operación.');
          } else {
            this.error.set('No se ha podido guardar el pedido.');
          }
        }
      });
  }

  // ======================================
  // NAVEGACIÓN
  // ======================================

  goBack(): void {

    void this.router.navigateByUrl(this.getReturnUrl());
  }

  private getReturnUrl(): string {

    const requested = this.route.snapshot.queryParamMap.get('returnUrl');

    // Aceptamos únicamente rutas internas
    // conocidas de OptiFlow.

    if (requested &&
      (
        requested === '/orders' ||
        requested.startsWith('/orders?') ||
        requested === '/clients' ||
        requested.startsWith('/clients?')
      )
    ) {
      return requested;
    }

    return this.listNavigation.get('orders');
  }

  // ======================================
  // FECHAS
  // ======================================

  private localDate(date: Date): string {

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
