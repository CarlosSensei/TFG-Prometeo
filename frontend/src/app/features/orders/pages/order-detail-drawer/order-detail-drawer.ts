
import { Component, effect, inject, input, output, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { forkJoin, of, catchError, switchMap, map, Subscription} from 'rxjs';
import { OrderApiService } from '../../services/order-api.service';
import { ClientApiService } from '../../../clients/services/client-api.service';
import { Client } from '../../../clients/models/client.model';
import { SellerSummary } from '../../../quotes/models/seller-summary.model';
import { Drawer } from '../../../../shared/ui/drawer/drawer';
import { Badge } from '../../../../shared/ui/badge/badge';
import { OrderCommercialInfo } from '../../components/order-commercial-info/order-commercial-info';
import { Order, OrderStatus } from '../../models/order.model';
import { OrderDetailItems } from '../../components/order-detail-items/order-detail-items';
import { OrderWorkflow, OrderWorkflowAction } from '../../components/order-workflow/order-workflow';
import { CommercialTotalsComponent } from '../../../../shared/commercial/components/commercial-totals/commercial-totals';
import { formatOrderDate, formatOrderStatus, orderStatusVariant } from '../../utils/order-presentation';

@Component({
  selector: 'app-order-detail-drawer',
  standalone: true,
  imports: [ Drawer, Badge, OrderCommercialInfo, OrderDetailItems, OrderWorkflow, CommercialTotalsComponent],
  templateUrl: './order-detail-drawer.html',
  styleUrl: './order-detail-drawer.scss'
})
export class OrderDetailDrawer {

  // ======================================
  // SERVICIOS
  // ======================================

  private readonly orderApi = inject(OrderApiService);
  private readonly clientApi = inject(ClientApiService);
  private readonly http = inject(HttpClient);
  private actionRequest?: Subscription;

  // ======================================
  // INPUTS / OUTPUTS
  // ======================================

  readonly orderId = input<number | null>(null);
  readonly closed = output<void>();
  readonly updated = output<void>();
  readonly clientRequested = output<number>();

  // ======================================
  // ESTADO
  // ======================================

  readonly order = signal<Order | null>(null);
  readonly client = signal<Client | null>(null);
  readonly seller = signal<SellerSummary | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly processing = signal(false);
  readonly actionError = signal<string | null>(null);
  readonly actionSuccess = signal<string | null>(null);

  // ======================================
  // PRESENTACIÓN
  // ======================================

  readonly formatDate = formatOrderDate;
  readonly formatStatus = formatOrderStatus;
  readonly statusVariant = orderStatusVariant;

  // ======================================
  // CARGA DEL DETALLE
  // ======================================

  constructor() {

    effect((onCleanup) => {

      const id = this.orderId();

      this.actionRequest?.unsubscribe();

      this.order.set(null);
      this.client.set(null);
      this.seller.set(null);

      this.error.set(null);
      this.actionError.set(null);
      this.actionSuccess.set(null);

      this.loading.set(false);
      this.processing.set(false);

      if (id === null) {
        return;
      }

      this.loading.set(true);

      const subscription = this.orderApi
        .getById(id)
        .pipe(switchMap(order => {

            // Los datos comerciales complementarios
            // no deben bloquear el detalle del pedido.

            const clientRequest = this.clientApi
              .getById(order.clientId)
              .pipe(catchError(() => of(null)));

            const sellerRequest = order.sellerId != null
              ? this.http.get<SellerSummary>(`/api/users/${order.sellerId}`)
                  .pipe(catchError(() => of(null))) : of(null);

            return forkJoin({client: clientRequest, seller: sellerRequest}).pipe(
              map(data => ({
                order,
                client: data.client,
                seller: data.seller
              }))
            );
          })

        )
        .subscribe({

          next: data => {

            this.order.set(data.order);
            this.client.set(data.client);
            this.seller.set(data.seller);
            this.loading.set(false);
          },

          error: err => {

            console.error('Error cargando pedido:', err);
            this.error.set('No se ha podido cargar el detalle del pedido.');
            this.loading.set(false);
          }
        });

      onCleanup(() => {
        subscription.unsubscribe();
        this.actionRequest?.unsubscribe();
      });
    });
  }

  // ======================================
  // CERRAR
  // ======================================

  close(): void {
    this.closed.emit();
  }

  // ======================================
  // EJECUTAR WORKFLOW
  // ======================================

  executeWorkflow(action: OrderWorkflowAction): void {

    const current = this.order();

    if (!current || !current.active || this.processing()) {
      return;
    }

    const allowed: Record<OrderStatus, OrderWorkflowAction[]> = {
      PENDING: ['confirm', 'cancel'],
      CONFIRMED: ['startPreparation', 'cancel'],
      IN_PROGRESS: ['ready', 'cancel'],
      READY: ['deliver', 'cancel'],
      DELIVERED: [],
      CANCELLED: []
    };

    if (!allowed[current.status].includes(action)) {
      return;
    }

    const messages: Record<OrderWorkflowAction, string> = {
      confirm: '¿Confirmar el pedido? Se descontará el stock.',
      startPreparation: '¿Iniciar la preparación de este pedido?',
      ready: '¿Marcar el pedido como listo para entregar?',
      deliver: '¿Registrar la entrega definitiva del pedido?',
      cancel: '¿Cancelar el pedido? Se devolverá el stock si corresponde.'
    };

    if (!window.confirm(messages[action])) {
      return;
    }

    const requests = {
      confirm: () => this.orderApi.confirm(current.id),
      startPreparation: () => this.orderApi.startPreparation(current.id),
      ready: () => this.orderApi.markReady(current.id),
      deliver: () => this.orderApi.deliver(current.id),
      cancel: () => this.orderApi.cancel(current.id)
    };

    this.processing.set(true);
    this.actionError.set(null);
    this.actionSuccess.set(null);

    this.actionRequest = requests[action]().subscribe({

      next: updatedOrder => {

        if (this.orderId() !== current.id) {
          return;
        }

        this.order.set(updatedOrder);
        this.processing.set(false);
        this.actionSuccess.set('Estado del pedido actualizado correctamente.');

        this.updated.emit();
      },

      error: (err: HttpErrorResponse) => {

        if (this.orderId() !== current.id) {
          return;
        }

        this.actionError.set(typeof err.error?.message === 'string'
            ? err.error.message : 'No se ha podido actualizar el pedido.'
        );

        this.processing.set(false);

        console.error('Error en workflow:', err);
      }
    });
  }



}
