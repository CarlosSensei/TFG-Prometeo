import { Component, input } from '@angular/core';

@Component({
  selector: 'app-skeleton',
  standalone: true,
  templateUrl: './skeleton.html',
  styleUrl: './skeleton.scss'
})
export class Skeleton {
  readonly width = input('100%');
  readonly height = input('18px');
  readonly radius = input('var(--radius-md)');
}
