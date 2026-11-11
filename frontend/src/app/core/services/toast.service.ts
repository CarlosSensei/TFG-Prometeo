import { Injectable, signal } from '@angular/core';

export type ToastKind = 'success' | 'error' | 'info' | 'warning';
export interface ToastMessage {
  id: number;
  kind: ToastKind;
  text: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly itemsState = signal<ToastMessage[]>([]);
  readonly items = this.itemsState.asReadonly();
  private nextId = 1;
  private readonly handles = new Map<number, ReturnType<typeof setTimeout>>();

  show(text: string, kind: ToastKind = 'info', duration = 4500): number {
    const id = this.nextId++;
    this.itemsState.update(items => [...items, { id, kind, text }].slice(-4));
    if (duration > 0) {
      this.handles.set(id, setTimeout(() => this.dismiss(id), duration));
    }
    return id;
  }

  success(text: string): number { return this.show(text, 'success'); }
  error(text: string): number { return this.show(text, 'error', 6500); }
  warning(text: string): number { return this.show(text, 'warning'); }

  dismiss(id: number): void {
    const handle = this.handles.get(id);
    if (handle) clearTimeout(handle);
    this.handles.delete(id);
    this.itemsState.update(items => items.filter(item => item.id !== id));
  }
}
