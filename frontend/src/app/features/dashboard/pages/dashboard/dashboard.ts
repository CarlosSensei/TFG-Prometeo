
import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard {
  readonly metrics = [
    {
      label: 'Clientes activos',
      value: '1.248',
      description: 'Base de clientes'
    },
    {
      label: 'Presupuestos abiertos',
      value: '38',
      description: 'Pendientes de resolución'
    },
    {
      label: 'Pedidos en curso',
      value: '24',
      description: 'En proceso'
    },
    {
      label: 'Seguimientos pendientes',
      value: '12',
      description: 'Acciones por realizar'
    }
  ];
}
