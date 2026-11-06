import {
  Component,
  effect,
  inject,
  input,
  output,
  signal,
  DestroyRef,
} from '@angular/core';
import { Subject, switchMap, timer, of, map, catchError } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Client } from '../../../clients/models/client.model';
import { QuoteLookupService } from '../../services/quote-lookup.service';
@Component({
  selector: 'app-quote-client-selector',
  standalone: true,
  imports: [],
  templateUrl: './quote-client-selector.html',
  styleUrl: './quote-client-selector.scss',
})
export class QuoteClientSelector {
  private readonly lookup = inject(QuoteLookupService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly clientSearch$ = new Subject<string>();
  readonly selectedClient = input<Client | null>(null);
  readonly invalid = input(false);
  readonly clientChanged = output<Client | null>();
  readonly clientQuery = signal('');
  readonly clients = signal<Client[]>([]);
  readonly searchingClients = signal(false);
  constructor() {
    this.setupClientAutocomplete();
    effect(() => {
      const client = this.selectedClient();
      if (client) {
        this.clientQuery.set(this.clientName(client));
        this.clients.set([]);
        this.clientSearch$.next('');
      }
    });
  }
  private setupClientAutocomplete(): void {
    this.clientSearch$
      .pipe(
        switchMap((term) => {
          this.searchingClients.set(term.length >= 2);

          if (term.length < 2) {
            return of([] as Client[]);
          }

          return timer(300).pipe(
            switchMap(() => this.lookup.searchClients(term)),
            map((page) => page.content),
            catchError((err) => {
              console.error('Error buscando clientes:', err);
              return of([] as Client[]);
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((results) => {
        this.clients.set(results);
        this.searchingClients.set(false);
      });
  }

  updateClientQuery(value: string): void {
    this.clientQuery.set(value);

    // Si el usuario modifica el texto,
    // la selección anterior deja de ser válida.
    this.clientChanged.emit(null);

    this.clients.set([]);

    this.clientSearch$.next(value.trim());
  }

  selectClient(client: Client): void {
    this.clientChanged.emit(client);
    this.clientQuery.set(this.clientName(client));
    this.clients.set([]);

    // Cancela búsquedas todavía pendientes.
    this.clientSearch$.next('');

    this.searchingClients.set(false);
  }

  clientName(client: Client): string {
    return [client.name, client.surname, client.lastSurname]
      .filter(Boolean)
      .join(' ');
  }
}
