import { Component, input, output } from '@angular/core';

export interface SelectOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-select',
  templateUrl: './select.html',
  styleUrl: './select.scss'
})
export class Select {
  readonly id = input.required<string>();
  readonly label = input('');
  readonly value = input('');
  readonly options = input<SelectOption[]>([]);
  readonly disabled = input(false);

  readonly valueChange = output<string>();

  onChange(event: Event): void {
    const element = event.target as HTMLSelectElement;
    this.valueChange.emit(element.value);
  }
}
