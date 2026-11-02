import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-input',
  templateUrl: './input.html',
  styleUrl: './input.scss'
})
export class Input {
  readonly id = input.required<string>();
  readonly label = input<string>('');
  readonly type = input<'text' | 'email' | 'tel' | 'search'>('text');
  readonly placeholder = input('');
  readonly value = input('');
  readonly disabled = input(false);
  readonly required = input(false);
  readonly error = input('');
  readonly hint = input('');

  readonly valueChange = output<string>();

  onInput(event: Event): void {
    const element = event.target as HTMLInputElement;
    this.valueChange.emit(element.value);
  }
}
