import {
  Component,
  ElementRef,
  ViewChild,
  effect,
  input,
  output
} from '@angular/core';

@Component({
  selector: 'app-drawer',
  standalone: true,
  templateUrl: './drawer.html',
  styleUrl: './drawer.scss'
})
export class Drawer {

  readonly open = input(false);
  readonly title = input('');
  readonly closed = output<void>();
  readonly size = input<'default' | 'wide'>('default');

  @ViewChild('dialog')
  dialog?: ElementRef<HTMLDialogElement>;

  constructor() {
    effect(() => {
      const isOpen = this.open();

      // Ejecutamos después de actualizar la vista.
      queueMicrotask(() => {
        const element = this.dialog?.nativeElement;

        if (!element) return;

        if (isOpen && !element.open) {
          element.showModal();
        } else if (!isOpen && element.open) {
          element.close();
        }
      });
    });
  }

  close(): void {
    this.closed.emit();
  }

  onDialogClose(): void {
    if (this.open()) {
      this.closed.emit();
    }
  }
}
