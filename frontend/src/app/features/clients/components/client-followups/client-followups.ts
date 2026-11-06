import {
  Component,
  DestroyRef,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import {
  DataTable,
  TableColumn,
  TableRow,
} from '../../../../shared/ui/data-table/data-table';
import { Pagination } from '../../../../shared/ui/pagination/pagination';
import { ClientDocumentsApiService } from '../../services/client-documents-api.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ClientFollowUp,
  FollowUpStatus,
  FollowUpType,
} from '../../models/client-followup.model';

@Component({
  selector: 'app-client-followups',
  standalone: true,
  imports: [DataTable, Pagination],
  templateUrl: './client-followups.html',
  styleUrl: './client-followups.scss',
})
export class ClientFollowups {
  private readonly documentsApi = inject(ClientDocumentsApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly followUpRefresh = signal(0);
  readonly clientId = input.required<number>();

  constructor() {
    effect(() => {
      this.clientId();
      this.followUpPage.set(0);
      this.followUps.set([]);
      this.followUpsTotal.set(0);
      this.completingFollowUpId.set(null);
    });
    effect((onCleanup) => {
      const clientId = this.clientId();
      const page = this.followUpPage();
      this.followUpRefresh();

      this.followUpsLoading.set(true);
      this.followUpsError.set(null);

      const subscription = this.documentsApi
        .getFollowUps(clientId, page, 5)
        .subscribe({
          next: (response) => {
            this.followUps.set(response.content);
            this.followUpsTotal.set(response.totalElements);
            this.followUpsLoading.set(false);
          },
          error: () => {
            this.followUpsError.set(
              'No se han podido cargar los seguimientos.',
            );
            this.followUpsLoading.set(false);
          },
        });

      onCleanup(() => subscription.unsubscribe());
    });
  }

  readonly followUpPage = signal(0);
  readonly followUps = signal<ClientFollowUp[]>([]);
  readonly followUpsTotal = signal(0);
  readonly followUpsLoading = signal(false);
  readonly followUpsError = signal<string | null>(null);

  readonly completingFollowUpId = signal<number | null>(null);
  readonly completionResult = signal('');
  readonly completionNotes = signal('');
  readonly completing = signal(false);

  readonly followUpColumns: TableColumn[] = [
    { key: 'type', label: 'Tipo' },
    { key: 'scheduledAt', label: 'Fecha programada' },
    { key: 'status', label: 'Estado' },
    { key: 'actions', label: 'Acciones' },
  ];

  readonly followUpRows = computed<TableRow[]>(() =>
    this.followUps().map((item) => ({
      id: item.id,
      type: this.formatFollowUpType(item.type),
      scheduledAt: new Intl.DateTimeFormat('es-ES', {
        dateStyle: 'short',
        timeStyle: 'short',
      }).format(new Date(item.scheduledAt)),
      status: this.formatFollowUpStatus(item.status),
      actions: item.status === 'PENDING' ? 'Completar' : '—',
    })),
  );

  private formatFollowUpType(type: FollowUpType): string {
    const labels: Record<FollowUpType, string> = {
      CALL: 'Llamada',
      EMAIL: 'Email',
      WHATSAPP: 'WhatsApp',
      SMS: 'SMS',
      REMINDER: 'Recordatorio',
      OTHER: 'Otro',
    };
    return labels[type];
  }

  private formatFollowUpStatus(status: FollowUpStatus): string {
    const labels: Record<FollowUpStatus, string> = {
      PENDING: 'Pendiente',
      COMPLETED: 'Completado',
      CANCELLED: 'Cancelado',
    };
    return labels[status];
  }

  changeFollowUpPage(page: number): void {
    this.followUpPage.set(page);
  }

  openCompleteFollowUp(id: number): void {
    const followUp = this.followUps().find((item) => item.id === id);

    if (!followUp || followUp.status !== 'PENDING') {
      return;
    }

    this.completingFollowUpId.set(id);
    this.completionResult.set('');
    this.completionNotes.set('');
  }

  cancelCompleteFollowUp(): void {
    this.completingFollowUpId.set(null);
  }

  completeFollowUp(): void {
    const id = this.completingFollowUpId();

    if (id === null || this.completing()) {
      return;
    }

    this.completing.set(true);
    this.followUpsError.set(null);

    this.documentsApi
      .completeFollowUp(id, {
        result: this.completionResult().trim() || null,
        notes: this.completionNotes().trim() || null,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.completing.set(false);
          this.completingFollowUpId.set(null);

          // Recargar los seguimientos desde el backend
          this.followUpRefresh.update((value) => value + 1);
        },
        error: () => {
          this.completing.set(false);
          this.followUpsError.set('No se ha podido completar el seguimiento.');
        },
      });
  }

  handleFollowUpAction(event: { action: string; row: TableRow }): void {
    const id = Number(event.row['id']);

    if (event.action === 'view' && Number.isFinite(id)) {
      this.openCompleteFollowUp(id);
    }
  }
}
