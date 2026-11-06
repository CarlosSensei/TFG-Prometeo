import { Component, effect, inject, input, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { ClientApiService } from '../../services/client-api.service';
import { Client } from '../../models/client.model';
import { ClientStats } from '../../models/client-commercial.model';

@Component({
  selector: 'app-client-summary',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, DecimalPipe],
  templateUrl: './client-summary.html',
  styleUrl: './client-summary.scss',
})
export class ClientSummary {
  private readonly api = inject(ClientApiService);
  readonly client = input.required<Client>();
  readonly stats = signal<ClientStats | null>(null);
  readonly statsLoading = signal(false);
  readonly statsError = signal<string | null>(null);
  readonly commercialDataLoaded = signal(false);

  constructor() {
    effect((onCleanup) => {
      const id = this.client().id;

      this.stats.set(null);
      this.statsError.set(null);
      this.statsLoading.set(false);
      this.commercialDataLoaded.set(false);

      this.statsLoading.set(true);

      const subscription = this.api.getStats(id).subscribe({
        next: (data: ClientStats) => {
          this.stats.set(data);
          this.commercialDataLoaded.set(true);
          this.statsLoading.set(false);
        },

        error: () => {
          this.statsError.set(
            'No se han podido recuperar las estadísticas comerciales.',
          );

          this.statsLoading.set(false);
        },
      });

      onCleanup(() => {
        subscription.unsubscribe();
      });
    });
  }

  fullName(client: Client): string {
    return [client.name, client.surname, client.lastSurname]
      .filter(Boolean)
      .join(' ');
  }

  display(value: string | null | undefined): string {
    return value?.trim() || '—';
  }

  pendingCommercialActions(): number {
    const data = this.stats();
    return data
      ? data.pendingFollowUps + data.pendingOrdersCount + data.openQuotesCount
      : 0;
  }
}
