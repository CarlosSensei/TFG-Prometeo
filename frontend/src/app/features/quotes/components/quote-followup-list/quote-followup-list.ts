import { Component, DestroyRef, effect, inject, input, output, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FollowUpApiService } from '../../../followups/services/followup-api.service';
import { FollowUp, FollowUpType, FOLLOWUP_TYPES } from '../../../followups/models/followup.model';
import { Button } from '../../../../shared/ui/button/button';
import { Badge, BadgeVariant } from '../../../../shared/ui/badge/badge';
import { Pagination } from '../../../../shared/ui/pagination/pagination';

@Component({
  selector: 'app-quote-followup-list',
  standalone: true,
  imports: [Button, Badge, Pagination],
  templateUrl: './quote-followup-list.html',
  styleUrl: './quote-followup-list.scss',
})
export class QuoteFollowUpList {
  private readonly api = inject(FollowUpApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly quoteId = input.required<number>();
  readonly refreshToken = input(0);
  readonly changed = output<void>();
  readonly items = signal<FollowUp[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly actionError = signal<string | null>(null);
  readonly page = signal(0);
  readonly pageSize = 5;
  readonly totalElements = signal(0);
  readonly selected = signal<FollowUp | null>(null);
  readonly type = signal<FollowUpType>('CALL');
  readonly scheduledAt = signal('');
  readonly notes = signal('');
  readonly saving = signal(false);
  readonly types = FOLLOWUP_TYPES;

  constructor() {
    effect((onCleanup) => {
      const quoteId = this.quoteId();
      const page = this.page();
      this.refreshToken();
      this.reloadCount();
      this.loading.set(true);
      this.error.set(null);
      const sub = this.api.getByQuote(quoteId, page, this.pageSize)
        .subscribe({
          next: response => {
            this.items.set(response.content);
            this.totalElements.set(response.totalElements);
            this.loading.set(false);
          },
          error: (err: HttpErrorResponse) => {
            this.loading.set(false);
            this.error.set(err.error?.message ?? 'No se han podido recuperar los seguimientos.');
          },
        });
      onCleanup(() => sub.unsubscribe());
    });
    effect(() => {
      this.quoteId();
      this.page.set(0);
      this.selected.set(null);
      this.actionError.set(null);
    });
  }

  formatType(type: FollowUpType): string {
    return this.types.find(item => item.value === type)?.label ?? type;
  }

  formatStatus(status: FollowUp['status']): string {
    return ({ PENDING: 'Pendiente', COMPLETED: 'Completado', CANCELLED: 'Cancelado' })[status];
  }

  statusVariant(status: FollowUp['status']): BadgeVariant {
    return status === 'COMPLETED' ? 'success' : status === 'PENDING' ? 'warning' : 'neutral';
  }

  formatDate(value: string | null): string {
    if (!value) return '—';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : new Intl.DateTimeFormat('es-ES', {
      dateStyle: 'short', timeStyle: 'short',
    }).format(date);
  }

  private datetimeInput(value: string): string {
    return value.slice(0, 16);
  }

  openEdit(item: FollowUp): void {
    if (item.status !== 'PENDING' || !item.active || this.saving()) return;
    this.selected.set(item);
    this.type.set(item.type);
    this.scheduledAt.set(this.datetimeInput(item.scheduledAt));
    this.notes.set(item.notes ?? '');
    this.actionError.set(null);
  }

  closeEdit(): void {
    if (!this.saving()) {
      this.selected.set(null);
      this.actionError.set(null);
    }
  }

  setPage(page: number): void {
    this.closeEdit();
    this.page.set(page);
  }

  saveEdit(): void {
    const original = this.selected();
    if (!original || original.status !== 'PENDING' || this.saving()) return;
    const when = this.scheduledAt();
    if (!when || !Number.isFinite(new Date(when).getTime())) {
      this.actionError.set('Indica una fecha y hora válidas.');
      return;
    }
    if (new Date(when).getTime() <= Date.now()) {
      this.actionError.set('La reprogramación debe tener una fecha futura.');
      return;
    }
    if (this.notes().length > 1000) {
      this.actionError.set('Las notas no pueden superar 1000 caracteres.');
      return;
    }
    this.saving.set(true);
    this.actionError.set(null);
    this.api.update(original.id, {
      ...original,
      type: this.type(),
      scheduledAt: when.length === 16 ? `${when}:00` : when,
      notes: this.notes().trim() || null,
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.saving.set(false);
        this.selected.set(null);
        // Evitamos un refresco de toda la ficha y consultamos únicamente seguimientos.
        this.reload();
        this.changed.emit();
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.actionError.set(err.error?.message ?? 'No se ha podido actualizar el seguimiento.');
      },
    });
  }

  private readonly reloadCount = signal(0);
  reload(): void {
    this.reloadCount.update(n => n + 1);
  }
}
