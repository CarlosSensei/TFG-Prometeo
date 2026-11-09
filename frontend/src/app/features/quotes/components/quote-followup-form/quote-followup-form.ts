import { Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FollowUpApiService } from '../../../followups/services/followup-api.service';
import {
  FOLLOWUP_TYPES,
  FollowUpType,
  FollowUp,
} from '../../../followups/models/followup.model';
import { Button } from '../../../../shared/ui/button/button';

@Component({
  selector: 'app-quote-followup-form',
  standalone: true,
  imports: [Button],
  templateUrl: './quote-followup-form.html',
  styleUrl: './quote-followup-form.scss',
})
export class QuoteFollowUpForm {
  private readonly api = inject(FollowUpApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly quoteId = input.required<number>();
  readonly created = output<FollowUp>();
  readonly cancelled = output<void>();

  readonly types = FOLLOWUP_TYPES;
  readonly type = signal<FollowUpType>('CALL');
  readonly scheduledAt = signal('');
  readonly notes = signal('');
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  // datetime-local maneja la hora local, sin conversión a UTC.
  readonly earliest = this.localDateTimeValue(new Date());

  private localDateTimeValue(date: Date): string {
    const local = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
    return local.toISOString().slice(0, 16);
  }

  submit(): void {
    if (this.saving()) return;

    const date = this.scheduledAt();
    const notes = this.notes().trim();

    if (!date) {
      this.error.set('Selecciona una fecha y hora para el seguimiento.');
      return;
    }

    if (new Date(date).getTime() <= Date.now()) {
      this.error.set('El seguimiento debe programarse para una fecha futura.');
      return;
    }

    if (notes.length > 1000) {
      this.error.set('Las notas no pueden superar los 1000 caracteres.');
      return;
    }

    this.error.set(null);
    this.saving.set(true);

    this.api.create(this.quoteId(), {
      type: this.type(),
      scheduledAt: date.length === 16 ? `${date}:00` : date,
      notes: notes || null,
      automated: false,
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: followUp => {
          this.saving.set(false);
          this.created.emit(followUp);
        },
        error: (err: HttpErrorResponse) => {
          this.saving.set(false);
          this.error.set(
            typeof err.error?.message === 'string'
              ? err.error.message
              : 'No se ha podido crear el seguimiento.'
          );
        },
      });
  }
}
