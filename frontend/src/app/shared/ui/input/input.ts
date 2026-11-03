import { Component, input, output, forwardRef, signal } from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR
} from '@angular/forms';


@Component({
  selector: 'app-input',
  providers: [
  {
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => Input),
    multi: true
  }],
  templateUrl: './input.html',
  styleUrl: './input.scss'
})

export class Input implements ControlValueAccessor {
  readonly id = input.required<string>();
  readonly label = input<string>('');
  readonly type = input<'text' | 'email' | 'tel' | 'search' | 'date'>('text');
  readonly placeholder = input('');
  readonly value = input('');
  readonly disabled = input(false);
  readonly required = input(false);
  readonly error = input('');
  readonly hint = input('');

  readonly valueChange = output<string>();
  readonly internalValue = signal('');
  readonly formDisabled = signal(false);

  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};

  writeValue(value: string | null): void {
    this.internalValue.set(value ?? '');
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(disabled: boolean): void {
    this.formDisabled.set(disabled);
  }

  onInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = target.value;

    this.internalValue.set(value);
    this.onChange(value);

    // Mantiene compatibilidad con los usos anteriores.
    this.valueChange.emit(value);
  }

  onBlur(): void {
    this.onTouched();
  }
}
