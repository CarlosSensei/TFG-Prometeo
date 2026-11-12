import { Component, DestroyRef, effect, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Drawer } from '../../../../shared/ui/drawer/drawer';
import { Button } from '../../../../shared/ui/button/button';
import { Badge } from '../../../../shared/ui/badge/badge';
import { INVENTORY_TYPES, InventoryItem, InventoryPayload, InventoryType } from '../../models/inventory.model';
import { InventoryApiService } from '../../services/inventory-api.service';
import { InventoryStockMovements } from '../inventory-stock-movements/inventory-stock-movements';

@Component({
  selector: 'app-inventory-detail-drawer',
  standalone: true,
  imports: [Drawer, Button, Badge, InventoryStockMovements],
  templateUrl: './inventory-detail-drawer.html',
  styleUrl: './inventory-detail-drawer.scss',
})
export class InventoryDetailDrawer {
  private readonly api = inject(InventoryApiService);
  private readonly destroyRef = inject(DestroyRef);
  readonly itemId = input<number | null>(null);
  readonly creating = input(false);
  readonly closed = output<void>();
  readonly saved = output<void>();
  readonly stockUpdated = output<void>();

  readonly options = INVENTORY_TYPES;
  readonly item = signal<InventoryItem | null>(null);
  readonly editing = signal(false);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  readonly sku = signal('');
  readonly name = signal('');
  readonly type = signal<InventoryType>('FRAME');
  readonly brand = signal('');
  readonly description = signal('');
  readonly salePrice = signal(0);
  readonly taxRate = signal(21);
  readonly stock = signal(0);

  constructor() {
    effect((onCleanup) => {
      const id = this.itemId();
      const creating = this.creating();
      this.error.set(null);
      this.message.set(null);
      this.item.set(null);
      this.editing.set(creating);
      this.resetFields();

      if (creating || id === null) { this.loading.set(false); return; }
      this.loading.set(true);
      const request = this.api.findById(id).subscribe({
        next: item => { this.item.set(item); this.fillFields(item); this.loading.set(false); },
        error: err => { this.error.set(err.error?.message ?? 'No se pudo cargar el artículo.'); this.loading.set(false); },
      });
      onCleanup(() => request.unsubscribe());
    });
  }

  private resetFields(): void {
    this.sku.set(''); this.name.set(''); this.type.set('FRAME');
    this.brand.set(''); this.description.set(''); this.salePrice.set(0);
    this.taxRate.set(21); this.stock.set(0);
  }

  private fillFields(item: InventoryItem): void {
    this.sku.set(item.sku);
    this.name.set(item.name);
    this.type.set(item.type);
    this.brand.set(item.brand ?? '');
    this.description.set(item.description ?? '');
    this.salePrice.set(item.salePrice);
    this.taxRate.set(item.taxRate);
    this.stock.set(item.stock);
  }

  getTitle(): string {
    return this.creating() ? 'Nuevo artículo' : this.editing() ? 'Editar artículo' : 'Detalle del artículo';
  }

  typeLabel(value: InventoryType): string {
    return this.options.find(option => option.value === value)?.label ?? value;
  }

  edit(): void { this.editing.set(true); this.error.set(null); this.message.set(null); }
  cancelEdit(): void {
    if (this.creating()) { this.closed.emit(); return; }
    const item = this.item();
    if (item) this.fillFields(item);
    this.editing.set(false);
    this.error.set(null);
  }

  submit(): void {
    if (this.saving()) return;
    const sku = this.sku().trim();
    const name = this.name().trim();
    const price = this.salePrice();
    const tax = this.taxRate();
    const stock = this.stock();
    if (!sku || sku.length > 50 || !name || name.length > 150 ||
      this.brand().length > 100 || this.description().length > 500 ||
      !Number.isFinite(price) || price < 0 || !Number.isFinite(tax) || tax < 0 || tax > 999.99 ||
      !Number.isSafeInteger(stock) || stock < 0) {
      this.error.set('Revisa los datos: SKU, nombre, precio, IVA y stock deben ser válidos.');
      return;
    }

    const payload: InventoryPayload = {
      sku, name, type: this.type(), brand: this.brand().trim() || null,
      description: this.description().trim() || null,
      salePrice: price, taxRate: tax,
      // En edición se conserva el dato obtenido. El backend debe ignorar el
      // stock del PUT para evitar que una ficha sobrescriba movimientos concurrentes.
      stock: this.creating() ? stock : this.item()!.stock,
    };
    this.saving.set(true);
    this.error.set(null);
    const request = this.creating() ? this.api.create(payload) : this.api.update(this.itemId()!, payload);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.saving.set(false); this.saved.emit(); },
      error: err => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'No se ha podido guardar el artículo.');
      },
    });
  }

  refreshAfterMovement(): void {
    const id = this.itemId();
    if (id === null) return;
    this.api.findById(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: item => {
        if (this.itemId() !== id) return;
        this.item.set(item);
        this.fillFields(item);
        this.stockUpdated.emit();
      },
      error: err => this.error.set(err.error?.message ?? 'Movimiento guardado, pero no se pudo actualizar la ficha.'),
    });
  }

  deactivate(): void {
    const id = this.itemId();
    if (id === null || this.saving() || !window.confirm('¿Desactivar este artículo? Ya no estará disponible para nuevas operaciones.')) return;
    this.saving.set(true);
    this.error.set(null);
    this.api.deactivate(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.saving.set(false); this.saved.emit(); },
      error: err => { this.saving.set(false); this.error.set(err.error?.message ?? 'No se pudo desactivar el artículo.'); },
    });
  }
}
