import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription } from 'rxjs';
import { RouterLink } from '@angular/router';
import { Card } from '../../../../shared/ui/card/card';
import { DashboardApiService } from '../../services/dashboard-api.service';
import { DashboardSummary, DashboardCalendar, DashboardCalendarAction } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [Card, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard {
  private readonly api = inject(DashboardApiService);
  private readonly destroyRef = inject(DestroyRef);
  readonly data = signal<DashboardSummary | null>(null);
  readonly calendarData = signal<DashboardCalendar | null>(null);
  readonly calendarLoading = signal(false);
  readonly calendarError = signal<string | null>(null);
  readonly displayedMonth = signal(new Date(new Date().getFullYear(), new Date().getMonth(), 1));
  readonly selectedDay = signal<string | null>(null);
  readonly weekdays = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  private calendarRequest?: Subscription;

  readonly monthTitle = computed(() => new Intl.DateTimeFormat('es-ES', {
    month: 'long', year: 'numeric'
  }).format(this.displayedMonth()));

  readonly calendarDays = computed(() => {
    const month = this.displayedMonth();
    const first = new Date(month.getFullYear(), month.getMonth(), 1);
    const offset = (first.getDay() + 6) % 7;
    const count = new Date(month.getFullYear(), month.getMonth() + 1, 0).getDate();
    const grouped = new Map<string, number>();
    for (const action of this.calendarData()?.monthActions ?? []) {
      const date = action.scheduledAt.slice(0, 10);
      grouped.set(date, (grouped.get(date) ?? 0) + 1);
    }
    const today = this.localDateKey(new Date());
    return Array.from({ length: Math.ceil((offset + count) / 7) * 7 }, (_, index) => {
      const date = new Date(month.getFullYear(), month.getMonth(), index - offset + 1);
      const key = this.localDateKey(date);
      return { key, number: date.getDate(), currentMonth: date.getMonth() === month.getMonth(),
        count: grouped.get(key) ?? 0, today: key === today };
    });
  });

  readonly shownActions = computed(() => {
    const data = this.calendarData();
    if (!data) return [];
    const day = this.selectedDay();
    return day ? data.monthActions.filter(item => item.scheduledAt.slice(0, 10) === day)
      : data.nextActions;
  });

  readonly actionsTitle = computed(() => {
    const key = this.selectedDay();
    if (!key) return 'Próximas acciones';
    const [year, month, day] = key.split('-').map(Number);
    return new Intl.DateTimeFormat('es-ES', { day: 'numeric', month: 'long' })
      .format(new Date(year, month - 1, day));
  });
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly number = new Intl.NumberFormat('es-ES');
  readonly currency = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' });
  readonly metrics = computed(() => {
    const d = this.data();
    if (!d) return [];
    return [
      { label: 'Clientes activos', value: this.number.format(d.activeClients), detail: 'Base comercial', link: '/clients' },
      { label: 'Presupuestos abiertos', value: this.number.format(d.openQuotes), detail: 'Borradores, enviados y aceptados', link: '/quotes' },
      { label: 'Pedidos en curso', value: this.number.format(d.activeOrders), detail: 'Pendientes de entrega', link: '/orders' },
      { label: 'Seguimientos pendientes', value: this.number.format(d.pendingFollowUps), detail: `${d.overdueFollowUps} vencidos`, link: '/followups' },
      { label: 'Conversión de presupuestos', value: `${d.conversionRate.toFixed(1)} %`, detail: `${d.convertedQuotes} convertidos de ${d.decidedQuotes} resueltos`, link: '/quotes' },
      { label: 'Ventas entregadas', value: this.currency.format(d.deliveredRevenue), detail: 'Importe acumulado (IVA incluido)', link: '/orders' }
    ];
  });

  constructor() {
    this.destroyRef.onDestroy(() => this.calendarRequest?.unsubscribe());
    this.refresh();
  }

  private localDateKey(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  changeMonth(offset: number): void {
    const date = this.displayedMonth();
    this.displayedMonth.set(new Date(date.getFullYear(), date.getMonth() + offset, 1));
    this.selectedDay.set(null);
    this.loadCalendar();
  }

  selectDay(key: string): void {
    this.selectedDay.set(this.selectedDay() === key ? null : key);
  }

  clearDay(): void { this.selectedDay.set(null); }

  actionLabel(type: string): string {
    const names: Record<string, string> = {
      CALL: 'Llamada', EMAIL: 'Correo', WHATSAPP: 'WhatsApp', SMS: 'SMS',
      REMINDER: 'Recordatorio', OTHER: 'Otra acción'
    };
    return names[type] ?? type;
  }

  loadCalendar(): void {
    this.calendarRequest?.unsubscribe();
    const date = this.displayedMonth();
    this.calendarLoading.set(true);
    this.calendarError.set(null);
    this.calendarRequest = this.api.calendar(date.getFullYear(), date.getMonth() + 1)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: result => { this.calendarData.set(result); this.calendarLoading.set(false); },
        error: () => {
          this.calendarData.set(null);
          this.calendarError.set('No se ha podido cargar el calendario.');
          this.calendarLoading.set(false);
        }
      });
  }

  refresh(): void {
    this.loading.set(true);
    this.error.set(null);
    this.loadCalendar();
    this.api.summary().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: response => { this.data.set(response); this.loading.set(false); },
      error: () => { this.error.set('No se pudo cargar el dashboard. Comprueba el backend y vuelve a intentarlo.'); this.loading.set(false); }
    });
  }

  formatDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '—' :
      new Intl.DateTimeFormat('es-ES', { dateStyle: 'short', timeStyle: 'short' }).format(date);
  }

  activityLink(type: 'QUOTE' | 'ORDER', id: number): string {
    return type === 'QUOTE' ? '/quotes' : '/orders';
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      DRAFT: 'Borrador', SENT: 'Enviado', ACCEPTED: 'Aceptado', REJECTED: 'Rechazado',
      CONVERTED: 'Convertido', EXPIRED: 'Caducado', PENDING: 'Pendiente',
      CONFIRMED: 'Confirmado', IN_PROGRESS: 'En curso', READY: 'Listo',
      DELIVERED: 'Entregado', CANCELLED: 'Cancelado'
    };
    return labels[status] ?? status;
  }
}
