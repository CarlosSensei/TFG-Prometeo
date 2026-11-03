import { Component, effect, inject, input, output, signal, computed } from '@angular/core';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { ClientQuote, ClientOrder } from '../../models/client-documents.model';
import { ClientDocumentsApiService } from '../../services/client-documents-api.service';
import { DataTable, TableColumn, TableRow } from '../../../../shared/ui/data-table/data-table';
import { Pagination } from '../../../../shared/ui/pagination/pagination';
import { Router } from '@angular/router';
import { Drawer } from '../../../../shared/ui/drawer/drawer';
import { Button } from '../../../../shared/ui/button/button';
import { Badge } from '../../../../shared/ui/badge/badge';
import { ClientApiService } from '../../services/client-api.service';
import { ClientFollowUp, FollowUpStatus, FollowUpType } from '../../models/client-followup.model';
import { Client } from '../../models/client.model';
import { ClientStats, ClientTimelineEvent } from '../../models/client-commercial.model';

// Pestañas disponibles en la ficha
type ClientTab =
  | 'summary'
  | 'timeline'
  | 'quotes'
  | 'orders'
  | 'followups';

@Component({
  selector: 'app-client-detail-drawer',
  standalone: true,
  imports: [
    Drawer,
    Button,
    Badge,
    CurrencyPipe,
    DatePipe,
    DecimalPipe,
    DataTable,
    Pagination
  ],
  templateUrl: './client-detail-drawer.html',
  styleUrl: './client-detail-drawer.scss'
})
export class ClientDetailDrawer {

  // =====================================================
  // DEPENDENCIAS
  // =====================================================

  private readonly api = inject(ClientApiService);
  private readonly router = inject(Router);

  // =====================================================
  // INPUTS / OUTPUTS
  // =====================================================

  readonly clientId = input<number | null>(null);

  readonly closed = output<void>();

  // =====================================================
  // ESTADO GENERAL
  // =====================================================

  readonly client = signal<Client | null>(null);

  readonly loading = signal(false);

  readonly error = signal<string | null>(null);

  readonly activeTab = signal<ClientTab>('summary');

  // =====================================================
  // ESTADÍSTICAS COMERCIALES
  // =====================================================

  readonly stats = signal<ClientStats | null>(null);

  readonly statsLoading = signal(false);

  readonly statsError = signal<string | null>(null);

  readonly commercialDataLoaded = signal(false);

  // =====================================================
  // TIMELINE
  // =====================================================

  readonly timeline = signal<ClientTimelineEvent[]>([]);

  readonly timelineLoading = signal(false);

  readonly timelineError = signal<string | null>(null);

  readonly timelineLoaded = signal(false);

  readonly timelinePageSize = 10;

  readonly visibleTimelineCount = signal(10);

  // =====================================================
  // FOLLOW UP
  // =====================================================

  private readonly followUpRefresh = signal(0);

  // =====================================================
  // CONSTRUCTOR / EFFECTS
  // =====================================================

  constructor() {

    /**
     * EFFECT 1
     *
     * Recupera los datos principales del cliente.
     *
     * Se ejecuta cuando cambia clientId.
     *
     * Cancela la petición anterior si se selecciona
     * otro cliente o se cierra el drawer.
     */
    effect((onCleanup) => {

      const id = this.clientId();

      this.client.set(null);
      this.error.set(null);
      this.loading.set(false);

      // Reiniciar navegación al cambiar de cliente
      this.activeTab.set('summary');

      // Reiniciar paginación de presupuestos y pedidos y follow ups
      this.quotePage.set(0);
      this.orderPage.set(0);
      this.followUpPage.set(0);
      this.completingFollowUpId.set(null);

      if (id === null) {
        return;
      }

      this.loading.set(true);

      const subscription = this.api.getById(id).subscribe({

        next: (client: Client) => {
          this.client.set(client);
          this.loading.set(false);
        },

        error: () => {
          this.error.set(
            'No se ha podido cargar la ficha del cliente.'
          );

          this.loading.set(false);
        }

      });

      onCleanup(() => {
        subscription.unsubscribe();
      });

    });

    /**
     * EFFECT 2
     *
     * Carga las estadísticas comerciales.
     *
     * Se ejecuta al abrir la pestaña Resumen
     * o cuando cambia el cliente.
     */
    effect((onCleanup) => {

      const id = this.clientId();
      const tab = this.activeTab();

      this.stats.set(null);
      this.statsError.set(null);
      this.statsLoading.set(false);
      this.commercialDataLoaded.set(false);

      if (id === null || tab !== 'summary') {
        return;
      }

      this.statsLoading.set(true);

      const subscription = this.api.getStats(id).subscribe({

        next: (data: ClientStats) => {

          this.stats.set(data);
          this.commercialDataLoaded.set(true);
          this.statsLoading.set(false);

        },

        error: () => {

          this.statsError.set(
            'No se han podido recuperar las estadísticas comerciales.'
          );

          this.statsLoading.set(false);

        }

      });

      onCleanup(() => {
        subscription.unsubscribe();
      });

    });

    /**
     * EFFECT 3
     *
     * Recupera el timeline comercial.
     *
     * Solo se consulta cuando el usuario
     * selecciona la pestaña Timeline.
     */
    effect((onCleanup) => {

      const id = this.clientId();
      const tab = this.activeTab();

      this.timeline.set([]);
      this.timelineError.set(null);
      this.timelineLoading.set(false);
      this.timelineLoaded.set(false);
      this.visibleTimelineCount.set(this.timelinePageSize);

      if (id === null || tab !== 'timeline') {
        return;
      }

      this.timelineLoading.set(true);

      const subscription = this.api.getTimeline(id).subscribe({

        next: (events: ClientTimelineEvent[]) => {

          const sortedEvents = [...events].sort(
            (a, b) =>
              Date.parse(b.date) - Date.parse(a.date)
          );

          this.timeline.set(sortedEvents);
          this.timelineLoaded.set(true);
          this.timelineLoading.set(false);

        },

        error: () => {

          this.timelineError.set(
            'No se ha podido recuperar el historial de actividad.'
          );

          this.timelineLoading.set(false);

        }

      });

      onCleanup(() => {
        subscription.unsubscribe();
      });

    });

        /**
     * EFFECT 4
     *
     * PRESUPUESTOS Y PEDIDOS
     *
     * Se ejecuta al abrir la pestaña Resumen
     * o cuando cambia el cliente.
     */
    
    effect((onCleanup) => {

      const clientId = this.clientId();
      const tab = this.activeTab();
      const quotePage = this.quotePage();
      const orderPage = this.orderPage();

      this.documentsError.set(null);
      this.documentsLoading.set(false);

      if (clientId === null) {
        this.quotes.set([]);
        this.orders.set([]);
        this.quotesTotal.set(0);
        this.ordersTotal.set(0);
        return;
      }

      if (tab !== 'quotes' && tab !== 'orders') {
        return;
      }

      this.documentsLoading.set(true);

      if (tab === 'quotes') {

        const subscription = this.documentsApi
          .getQuotes(
            clientId,
            quotePage,
            this.documentPageSize
          )
          .subscribe({
            next: response => {
              this.quotes.set(response.content);
              this.quotesTotal.set(response.totalElements);
              this.documentsLoading.set(false);
            },
            error: () => {
              this.documentsError.set(
                'No se han podido recuperar los presupuestos.'
              );
              this.documentsLoading.set(false);
            }
          });

        onCleanup(() => subscription.unsubscribe());
      }

      if (tab === 'orders') {

        const subscription = this.documentsApi
          .getOrders(
            clientId,
            orderPage,
            this.documentPageSize
          )
          .subscribe({
            next: response => {
              this.orders.set(response.content);
              this.ordersTotal.set(response.totalElements);
              this.documentsLoading.set(false);
            },
            error: () => {
              this.documentsError.set(
                'No se han podido recuperar los pedidos.'
              );
              this.documentsLoading.set(false);
            }
          });

        onCleanup(() => subscription.unsubscribe());
      }
    });

            /**
     * EFFECT 5
     *
     * SEGUIMIENTOS
     *
     * Se ejecuta al abrir la pestaña Resumen
     * o cuando cambia el cliente.
     */    
    
    effect((onCleanup) => {
      const clientId = this.clientId();
      const tab = this.activeTab();
      const page = this.followUpPage();
      const refresh = this.followUpRefresh();

      if (clientId === null || tab !== 'followups') {
        return;
      }

      this.followUpsLoading.set(true);
      this.followUpsError.set(null);

      const subscription = this.documentsApi
        .getFollowUps(clientId, page, 5)
        .subscribe({
          next: response => {
            this.followUps.set(response.content);
            this.followUpsTotal.set(response.totalElements);
            this.followUpsLoading.set(false);
          },
          error: () => {
            this.followUpsError.set(
              'No se han podido cargar los seguimientos.'
            );
            this.followUpsLoading.set(false);
          }
        });

      onCleanup(() => subscription.unsubscribe());
    });


  }

  // =====================================================
  // NAVEGACIÓN
  // =====================================================

  close(): void {
    this.closed.emit();
  }

  selectTab(tab: ClientTab): void {
    if (tab === 'quotes') {
      this.quotePage.set(0);
    }

    if (tab === 'orders') {
      this.orderPage.set(0);
    }

    this.activeTab.set(tab);
  }

  editClient(): void {

    const id = this.clientId();

    if (id === null) {
      return;
    }

    this.closed.emit();

    this.router.navigate([
      '/clients',
      id,
      'edit'
    ]);

  }

  // =====================================================
  // INFORMACIÓN DEL CLIENTE
  // =====================================================

  fullName(client: Client): string {

    return [
      client.name,
      client.surname,
      client.lastSurname
    ]
      .filter(Boolean)
      .join(' ');

  }

  display(value: string | null | undefined): string {
    return value?.trim() || '—';
  }

  // =====================================================
  // ESTADÍSTICAS COMERCIALES
  // =====================================================

  /**
   * Indica si el cliente tiene operaciones comerciales.
   */
  hasCommercialActivity(): boolean {

    const data = this.stats();

    if (!data) {
      return false;
    }

    return data.ordersCount > 0 ||
           data.quotesCount > 0;

  }

  /**
   * Devuelve el porcentaje de conversión recibido
   * del backend.
   *
   * Se asume que el backend devuelve 0-100.
   */
  conversionRate(): number {
    return this.stats()?.conversionRate ?? 0;
  }

  /**
   * Número de operaciones comerciales pendientes.
   */
  pendingCommercialActions(): number {

    const data = this.stats();

    if (!data) {
      return 0;
    }

    return (
      data.pendingFollowUps +
      data.pendingOrdersCount +
      data.openQuotesCount
    );

  }

  // =====================================================
  // TIMELINE
  // =====================================================

  /**
   * Incrementa los registros visibles en bloques de 10.
   */
  showMoreTimeline(): void {

    this.visibleTimelineCount.update(
      count => count + this.timelinePageSize
    );

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

  // =====================================================
  // CLIENT DOCUMENT
  // =====================================================

  private readonly documentsApi = inject(ClientDocumentsApiService);

  readonly documentPageSize = 5;

  readonly quotePage = signal(0);
  readonly orderPage = signal(0);

  readonly quotes = signal<ClientQuote[]>([]);
  readonly orders = signal<ClientOrder[]>([]);

  readonly quotesTotal = signal(0);
  readonly ordersTotal = signal(0);

  readonly documentsLoading = signal(false);
  readonly documentsError = signal<string | null>(null);

  readonly quoteColumns: TableColumn[] = [
    { key: 'quoteNumber', label: 'Presupuesto' },
    { key: 'quoteDate', label: 'Fecha' },
    { key: 'status', label: 'Estado' },
    { key: 'total', label: 'Importe' }
  ];

  readonly orderColumns: TableColumn[] = [
    { key: 'orderNumber', label: 'Pedido' },
    { key: 'orderDate', label: 'Fecha' },
    { key: 'status', label: 'Estado' },
    { key: 'total', label: 'Importe' }
  ];

  get quoteRows(): TableRow[] {
    return this.quotes().map(quote => ({
      id: quote.id,
      quoteNumber: quote.quoteNumber,
      quoteDate: quote.quoteDate,
      status: this.formatStatus(quote.status),
      total: this.formatMoney(quote.total)
    }));
  }

  get orderRows(): TableRow[] {
    return this.orders().map(order => ({
      id: order.id,
      orderNumber: order.orderNumber,
      orderDate: order.orderDate,
      status: this.formatStatus(order.status),
      total: this.formatMoney(order.total)
    }));
  }

  formatMoney(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }

  changeQuotePage(page: number): void {
    this.quotePage.set(page);
  }

  changeOrderPage(page: number): void {
    this.orderPage.set(page);
  }

  // =====================================================
  // FOLLOWUPS
  // =====================================================

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
    { key: 'actions', label: 'Acciones' }
  ];

  readonly followUpRows = computed<TableRow[]>(() =>
    this.followUps().map(item => ({
      id: item.id,
      type: this.formatFollowUpType(item.type),
      scheduledAt: new Intl.DateTimeFormat('es-ES', {
        dateStyle: 'short',
        timeStyle: 'short'
      }).format(new Date(item.scheduledAt)),
      status: this.formatFollowUpStatus(item.status),
      actions: item.status === 'PENDING'
        ? 'Completar'
        : '—'
    }))
  );

  private formatFollowUpType(type: FollowUpType): string {
    const labels: Record<FollowUpType, string> = {
      CALL: 'Llamada',
      EMAIL: 'Email',
      WHATSAPP: 'WhatsApp',
      SMS: 'SMS',
      REMINDER: 'Recordatorio',
      OTHER: 'Otro'
    };
    return labels[type];
  }

  private formatFollowUpStatus(status: FollowUpStatus): string {
    const labels: Record<FollowUpStatus, string> = {
      PENDING: 'Pendiente',
      COMPLETED: 'Completado',
      CANCELLED: 'Cancelado'
    };
    return labels[status];
  }

  changeFollowUpPage(page: number): void {
    this.followUpPage.set(page);
  }

  openCompleteFollowUp(id: number): void {
    const followUp = this.followUps()
      .find(item => item.id === id);

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

    this.documentsApi.completeFollowUp(id, {
      result: this.completionResult().trim() || null,
      notes: this.completionNotes().trim() || null
    }).subscribe({
      next: () => {
        this.completing.set(false);
        this.completingFollowUpId.set(null);

        // Recargar los seguimientos desde el backend
        this.followUpRefresh.update(value => value + 1);
      },
      error: () => {
        this.completing.set(false);
        this.followUpsError.set(
          'No se ha podido completar el seguimiento.'
        );
      }
    });
  }

  handleFollowUpAction(event: {
    action: string;
    row: TableRow;
  }): void {
    const id = Number(event.row['id']);

    if (event.action === 'view' && Number.isFinite(id)) {
      this.openCompleteFollowUp(id);
    }
  }



}
