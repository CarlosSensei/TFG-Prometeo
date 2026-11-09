import { Component, DestroyRef, effect, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Drawer } from '../../../../shared/ui/drawer/drawer';
import { Button } from '../../../../shared/ui/button/button';
import { Badge, BadgeVariant } from '../../../../shared/ui/badge/badge';
import { FollowUp, FOLLOWUP_STATUSES, FOLLOWUP_TYPES, FollowUpStatus } from '../../models/followup.model';
import { FollowUpApiService } from '../../services/followup-api.service';

@Component({
  selector: 'app-followup-detail-drawer',
  standalone: true,
  imports: [Drawer, Button, Badge],
  templateUrl: './followup-detail-drawer.html',
  styleUrl: './followup-detail-drawer.scss'
})
export class FollowUpDetailDrawer {
  private readonly api = inject(FollowUpApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly followUp = input<FollowUp | null>(null);
  readonly closed = output<void>();
  readonly updated = output<void>();
  readonly completionResult = signal('');
  readonly completionNotes = signal('');
  readonly completing = signal(false);
  readonly error = signal<string | null>(null);

  readonly typeOptions = FOLLOWUP_TYPES;
  readonly statusOptions = FOLLOWUP_STATUSES;

  constructor() {
    effect(() => {
      const current = this.followUp();
      this.completionResult.set('');
      this.completionNotes.set(current?.notes ?? '');
      this.error.set(null);
    });
  }

  typeLabel(value: FollowUp['type']): string {
    return this.typeOptions.find(x => x.value === value)?.label ?? value;
  }

  statusLabel(value: FollowUpStatus): string {
    return this.statusOptions.find(x => x.value === value)?.label ?? value;
  }

  statusVariant(value: FollowUpStatus): BadgeVariant {
    return value === 'COMPLETED' ? 'success' : value === 'CANCELLED' ? 'neutral' : 'warning';
  }

  formatDateTime(value: string | null): string {
    if (!value) return '—';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '—' :
      new Intl.DateTimeFormat('es-ES', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
  }

  close(): void {
    if (this.completing()) return;
    this.closed.emit();
  }

  complete(): void {
    const current = this.followUp();
    if (!current || current.status !== 'PENDING' || !current.active || this.completing()) return;

    this.completing.set(true);
    this.error.set(null);
    this.api.complete(current.id, {
      result: this.completionResult().trim() || null,
      notes: this.completionNotes().trim() || null
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.completing.set(false);
        this.updated.emit();
      },
      error: err => {
        console.error('Error al completar seguimiento:', err);
        this.error.set(typeof err.error?.message === 'string'
          ? err.error.message : 'No se ha podido completar el seguimiento.');
        this.completing.set(false);
      }
    });
  }
}
