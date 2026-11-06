import { Component, effect, inject, input, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Button } from '../../../../shared/ui/button/button';
import { ClientApiService } from '../../services/client-api.service';
import { ClientTimelineEvent } from '../../models/client-commercial.model';

@Component({
  selector: 'app-client-timeline',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, Button],
  templateUrl: './client-timeline.html',
  styleUrl: './client-timeline.scss',
})
export class ClientTimeline {
  private readonly api = inject(ClientApiService);
  readonly clientId = input.required<number>();
  readonly timeline = signal<ClientTimelineEvent[]>([]);
  readonly timelineLoading = signal(false);
  readonly timelineError = signal<string | null>(null);
  readonly timelineLoaded = signal(false);
  readonly timelinePageSize = 10;
  readonly visibleTimelineCount = signal(10);

  constructor() {
    effect((onCleanup) => {
      const id = this.clientId();

      this.timeline.set([]);
      this.timelineError.set(null);
      this.timelineLoading.set(false);
      this.timelineLoaded.set(false);
      this.visibleTimelineCount.set(this.timelinePageSize);

      this.timelineLoading.set(true);

      const subscription = this.api.getTimeline(id).subscribe({
        next: (events: ClientTimelineEvent[]) => {
          const sortedEvents = [...events].sort(
            (a, b) => Date.parse(b.date) - Date.parse(a.date),
          );

          this.timeline.set(sortedEvents);
          this.timelineLoaded.set(true);
          this.timelineLoading.set(false);
        },

        error: () => {
          this.timelineError.set(
            'No se ha podido recuperar el historial de actividad.',
          );

          this.timelineLoading.set(false);
        },
      });

      onCleanup(() => {
        subscription.unsubscribe();
      });
    });
  }

  showMoreTimeline(): void {
    this.visibleTimelineCount.update((count) => count + this.timelinePageSize);
  }

  /**
   * Normaliza estados del backend para mostrarlos
   * en la interfaz.
   *
   * PENDING_FOLLOW_UP -> PENDING FOLLOW UP
   */
  formatStatus(status: string | null): string {
    if (!status) {
      return 'Sin estado';
    }

    return status.replaceAll('_', ' ');
  }
}
