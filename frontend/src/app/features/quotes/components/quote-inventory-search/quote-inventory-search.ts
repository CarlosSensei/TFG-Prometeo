import { Component, inject, output, signal, DestroyRef } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Subscription } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  InventoryItem,
  QuoteLookupService,
} from '../../services/quote-lookup.service';
import { Button } from '../../../../shared/ui/button/button';
@Component({
  selector: 'app-quote-inventory-search',
  standalone: true,
  imports: [CurrencyPipe, Button],
  templateUrl: './quote-inventory-search.html',
  styleUrl: './quote-inventory-search.scss',
})
export class QuoteInventorySearch {
  private readonly lookup = inject(QuoteLookupService);
  private readonly destroyRef = inject(DestroyRef);
  private inventorySearchRequest?: Subscription;
  readonly inventoryQuery = signal('');
  readonly inventoryResults = signal<InventoryItem[]>([]);
  readonly searchingInventory = signal(false);
  readonly itemSelected = output<InventoryItem>();
  readonly searchFailed = output<string>();
  readonly searchStarted = output<void>();
  addItem(item: InventoryItem): void {
    if (!item.active) return;
    this.itemSelected.emit(item);
    this.updateInventoryQuery('');
  }
  searchInventory(): void {
    const term = this.inventoryQuery().trim();

    this.inventorySearchRequest?.unsubscribe();
    this.inventoryResults.set([]);

    if (term.length < 2) {
      return;
    }

    this.searchingInventory.set(true);
    this.searchStarted.emit();

    this.inventorySearchRequest = this.lookup
      .searchInventory(term)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (page) => {
          this.inventoryResults.set(page.content.filter((item) => item.active));
          this.searchingInventory.set(false);
        },
        error: () => {
          this.searchFailed.emit('No se ha podido buscar artículos.');
          this.searchingInventory.set(false);
        },
      });
  }

  updateInventoryQuery(value: string): void {
    this.inventoryQuery.set(value);
    this.inventoryResults.set([]);
    this.inventorySearchRequest?.unsubscribe();
    this.searchingInventory.set(false);
  }
}
