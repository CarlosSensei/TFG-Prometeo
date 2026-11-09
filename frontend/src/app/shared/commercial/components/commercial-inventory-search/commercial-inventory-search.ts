
import { Component, DestroyRef, inject, output, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Subject, switchMap, timer, of, map, catchError } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommercialLookupService } from '../../services/commercial-lookup.service';
import { InventoryItem } from '../../models/inventory-item.model';

@Component({
  selector: 'app-commercial-inventory-search',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './commercial-inventory-search.html',
  styleUrl: './commercial-inventory-search.scss'
})
export class CommercialInventorySearch {

  private readonly lookup = inject(CommercialLookupService);
  private readonly destroyRef = inject(DestroyRef);

  private readonly search$ = new Subject<string>();

  readonly inventoryQuery = signal('');
  readonly inventoryResults = signal<InventoryItem[]>([]);

  readonly searchingInventory = signal(false);
  readonly searchCompleted = signal(false);
  readonly searchError = signal<string | null>(null);

  readonly itemSelected = output<InventoryItem>();
  readonly searchFailed = output<string>();
  readonly searchStarted = output<void>();

  constructor() {
    this.setupAutocomplete();
  }

  private setupAutocomplete(): void {

    this.search$.pipe(
      switchMap(term => {

        this.inventoryResults.set([]);
        this.searchCompleted.set(false);
        this.searchError.set(null);

        if (term.length < 2) {
          this.searchingInventory.set(false);
          return of(null);
        }

        this.searchingInventory.set(true);
        this.searchStarted.emit();

        return timer(300).pipe(

          switchMap(() =>
            this.lookup.searchInventory(term)
          ),

          map(page =>
            page.content.filter(item => item.active)
          ),

          catchError(err => {

            console.error(
              'Error buscando artículos:',
              err
            );

            this.searchError.set(
              'No se ha podido consultar el inventario.'
            );

            this.searchFailed.emit(
              'No se ha podido buscar artículos.'
            );

            return of([] as InventoryItem[]);
          })
        );
      }),

      takeUntilDestroyed(this.destroyRef)
    )
    .subscribe(results => {

      if (results === null) {
        this.searchingInventory.set(false);
        return;
      }

      this.inventoryResults.set(results);
      this.searchingInventory.set(false);
      this.searchCompleted.set(true);
    });
  }

  updateInventoryQuery(value: string): void {

    this.inventoryQuery.set(value);

    this.inventoryResults.set([]);
    this.searchError.set(null);
    this.searchCompleted.set(false);

    // Cancela inmediatamente la búsqueda anterior.
    this.search$.next(value.trim());
  }

  addItem(item: InventoryItem): void {

    if (!item.active) {
      return;
    }

    this.itemSelected.emit(item);

    this.updateInventoryQuery('');
  }
}
