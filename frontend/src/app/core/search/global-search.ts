import { Component, computed, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, debounceTime, distinctUntilChanged, map, of, startWith, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { GlobalSearchService, SearchResult } from './global-search.service';
import { Input } from '../../shared/ui/input/input';

@Component({
  selector: 'app-global-search',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    Input
  ],
  templateUrl: './global-search.html',
  styleUrl: './global-search.scss'
})
export class GlobalSearch {

  // ============================================
  // DEPENDENCIAS
  // ============================================

  private readonly searchService = inject(GlobalSearchService);
  private readonly router = inject(Router);

  // ============================================
  // ESTADO
  // ============================================

  readonly query = new FormControl('', {
    nonNullable: true
  });

  readonly results = signal<SearchResult[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly open = signal(false);

  // ============================================
  // CATEGORÍAS DE BÚSQUEDA
  // ============================================

  readonly categories = [
    { type: 'client', label: 'Clientes', icon: '♙' },
    { type: 'quote', label: 'Presupuestos', icon: '▤' },
    { type: 'order', label: 'Pedidos', icon: '▣' }
  ] as const;

  // Agrupa resultados por categoría sin nuevas peticiones HTTP.
  readonly groupedResults = computed(() => {
    const results = this.results();

    return {
      client: results.filter(result => result.type === 'client'),
      quote: results.filter(result => result.type === 'quote'),
      order: results.filter(result => result.type === 'order')
    };
  });

  resultsByType(type: SearchResult['type']): SearchResult[] {
    return this.groupedResults()[type];
  }

  // ============================================
  // BÚSQUEDA REACTIVA
  // ============================================

  constructor() {

    this.query.valueChanges.pipe(
      startWith(''),

      map(value => value.trim()),

      debounceTime(300),

      distinctUntilChanged(),

      switchMap(term => {

        if (term.length < 2) {
          this.loading.set(false);

          return of({
            results: [] as SearchResult[],
            error: null as string | null
          });
        }

        this.loading.set(true);
        this.error.set(null);

        return this.searchService.search(term).pipe(
          map(results => ({
            results,
            error: null as string | null
          })),

          catchError(() => of({
            results: [] as SearchResult[],
            error: 'No se ha podido realizar la búsqueda.'
          }))
        );
      }),

      takeUntilDestroyed()
    ).subscribe(({ results, error }) => {

      this.results.set(results);
      this.error.set(error);
      this.loading.set(false);

    });
  }

  // ============================================
  // NAVEGACIÓN
  // ============================================

  selectResult(result: SearchResult): void {

    this.open.set(false);
    this.query.setValue('');

    // Hasta disponer de pantallas propias de
    // presupuestos y pedidos, abrimos la ficha
    // del cliente asociado al resultado.

    void this.router.navigate(['/clients'], {
      queryParams: {
        detail: result.clientId
      }
    });
  }

  // ============================================
  // INTERFAZ
  // ============================================

  closeResults(): void {
    this.open.set(false);
  }
}
