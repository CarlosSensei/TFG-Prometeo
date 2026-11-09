import { Component, computed, input, output } from '@angular/core';
import { OrderStatus } from '../../models/order.model';
import { Button } from '../../../../shared/ui/button/button';

export type OrderWorkflowAction =
  | 'confirm'
  | 'startPreparation'
  | 'ready'
  | 'deliver'
  | 'cancel';

interface WorkflowAction {
  id: OrderWorkflowAction;
  label: string;
}

@Component({
  selector: 'app-order-workflow',
  standalone: true,
  imports: [Button],
  templateUrl: './order-workflow.html',
  styleUrl: './order-workflow.scss'
})
export class OrderWorkflow {

  readonly status = input.required<OrderStatus>();
  readonly active = input(true);
  readonly processing = input(false);
  readonly actionRequested = output<OrderWorkflowAction>();
  readonly primaryAction = computed<WorkflowAction | null>(() => {

    if (!this.active()) {
      return null;
    }

    switch (this.status()) {

      case 'PENDING':
        return {
          id: 'confirm',
          label: 'Confirmar pedido'
        };

      case 'CONFIRMED':
        return {
          id: 'startPreparation',
          label: 'Iniciar preparación'
        };

      case 'IN_PROGRESS':
        return {
          id: 'ready',
          label: 'Marcar como listo'
        };

      case 'READY':
        return {
          id: 'deliver',
          label: 'Registrar entrega'
        };

      default:
        return null;
    }
  });

  readonly canCancel = computed(() =>
    this.active() &&
    this.status() !== 'CANCELLED' &&
    this.status() !== 'DELIVERED'
  );

  request(action: OrderWorkflowAction): void {

    if (this.processing() || !this.active()) {
      return;
    }

    const permitted = this.primaryAction()?.id === action ||
      (action === 'cancel' && this.canCancel());

    if (permitted) {
      this.actionRequested.emit(action);
    }
  }
}
