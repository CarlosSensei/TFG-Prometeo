import { Component, DestroyRef, effect, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription } from 'rxjs';
import { Button } from '../../../../shared/ui/button/button';
import { StockMovementApiService } from '../../services/stock-movement-api.service';
import { CreateStockMovement, StockMovement, StockMovementType } from '../../models/stock-movement.model';

@Component({
  selector: 'app-inventory-stock-movements',
  standalone: true,
  imports: [Button],
  templateUrl: './inventory-stock-movements.html',
  styleUrl: './inventory-stock-movements.scss',
})
export class InventoryStockMovements {
  private readonly api = inject(StockMovementApiService);
  private readonly destroyRef = inject(DestroyRef);
  private listRequest?: Subscription;

  readonly itemId = input.required<number>();
  readonly currentStock = input.required<number>();
  readonly allowMutations = input(true);
  readonly stockChanged = output<void>();

  readonly movements = signal<StockMovement[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly formError = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly formOpen = signal(false);
  readonly type = signal<StockMovementType>('ENTRY');
  readonly quantity = signal<number | null>(null);
  readonly reason = signal('');

  readonly size = 8;

  constructor() {
    effect(() => {
      const id = this.itemId();
      this.page.set(0);
      this.movements.set([]);
      this.formOpen.set(false);
      this.error.set(null);
      this.success.set(null);
      this.load(id, 0);
    });
    this.destroyRef.onDestroy(() => this.listRequest?.unsubscribe());
  }

  typeLabel(type: StockMovementType): string {
    switch (type) {
      case 'ENTRY': return 'Entrada';
      case 'EXIT': return 'Salida';
      case 'ADJUSTMENT': return 'Ajuste';
    }
  }

  load(itemId = this.itemId(), page = this.page()): void {
    this.listRequest?.unsubscribe();
    this.loading.set(true);
    this.error.set(null);
    this.listRequest = this.api.findByItem(itemId, page, this.size)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: result => {
          if (this.itemId() !== itemId) return;
          this.movements.set(result.content);
          this.page.set(result.number);
          this.totalPages.set(result.totalPages);
          this.totalElements.set(result.totalElements);
          this.loading.set(false);
        },
        error: err => {
          if (this.itemId() !== itemId) return;
          this.loading.set(false);
          this.error.set(this.errorMessage(err, 'No se pudo cargar el historial de movimientos.'));
        },
      });
  }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages() || this.loading()) return;
    this.load(this.itemId(), page);
  }

  openForm(): void {
    this.type.set('ENTRY');
    this.quantity.set(null);
    this.reason.set('');
    this.formError.set(null);
    this.success.set(null);
    this.formOpen.set(true);
  }

  cancelForm(): void {
    if (this.saving()) return;
    this.formOpen.set(false);
    this.formError.set(null);
  }

  submit(): void {
    if (this.saving()) return;
    const quantity = this.quantity();
    const reason = this.reason().trim();
    const type = this.type();
    if (quantity === null || !Number.isSafeInteger(quantity) || quantity < 0 ||
        (type !== 'ADJUSTMENT' && quantity === 0)) {
      this.formError.set(type === 'ADJUSTMENT'
        ? 'Introduce un stock final válido, entero y mayor o igual a cero.'
        : 'Introduce una cantidad entera mayor que cero.');
      return;
    }
    if (!reason || reason.length > 250) {
      this.formError.set('El motivo es obligatorio y debe tener como máximo 250 caracteres.');
      return;
    }
    if (type === 'EXIT' && quantity > this.currentStock()) {
      this.formError.set('La cantidad supera el stock disponible.');
      return;
    }
    if (type === 'ADJUSTMENT' && quantity === this.currentStock()) {
      this.formError.set('El ajuste debe modificar el stock actual.');
      return;
    }
    const itemId = this.itemId();
    const payload: CreateStockMovement = { type, quantity, reason };
    this.saving.set(true);
    this.formError.set(null);
    this.api.create(itemId, payload).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.saving.set(false);
        if (this.itemId() !== itemId) return;
        this.formOpen.set(false);
        this.success.set('Movimiento registrado correctamente.');
        this.load(itemId, 0);
        this.stockChanged.emit();
      },
      error: err => {
        this.saving.set(false);
        if (this.itemId() !== itemId) return;
        this.formError.set(this.errorMessage(err, 'No se pudo registrar el movimiento.'));
      },
    });
  }

  private errorMessage(err: any, fallback: string): string {
    const message = err?.error?.message;
    return typeof message === 'string' && message.trim() ? message : fallback;
  }
}
