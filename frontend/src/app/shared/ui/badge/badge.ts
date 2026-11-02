import { Component, input } from '@angular/core';

export type BadgeVariant =
  | 'neutral'
  | 'success'
  | 'warning'
  | 'danger'
  | 'info';

@Component({
  selector: 'app-badge',
  templateUrl: './badge.html',
  styleUrl: './badge.scss'
})
export class Badge {
  readonly variant = input<BadgeVariant>('neutral');
}
