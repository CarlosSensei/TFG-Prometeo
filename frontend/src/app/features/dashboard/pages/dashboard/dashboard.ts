import { Component } from '@angular/core';

import { Button } from '../../../../shared/ui/button/button';
import { Badge } from '../../../../shared/ui/badge/badge';
import { Card } from '../../../../shared/ui/card/card';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [Button, Badge, Card],
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
