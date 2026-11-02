import { Component, input } from '@angular/core';

@Component({
  selector: 'app-card',
  templateUrl: './card.html',
  styleUrl: './card.scss'
})
export class Card {
  readonly padded = input(true);
  readonly elevated = input(false);
}
