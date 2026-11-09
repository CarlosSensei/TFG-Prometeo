import { Injectable } from '@angular/core';

export type ListModule =
  | 'clients'
  | 'quotes'
  | 'orders'
  | 'inventory'
  | 'followups';

@Injectable({
  providedIn: 'root'
})
export class ListNavigationStateService {

  private readonly prefix = 'optiflow:list-state:';

  save(module: ListModule, url: string): void {

    if (!this.isValidUrl(module, url)) {
      return;
    }

    sessionStorage.setItem(
      this.prefix + module,
      url
    );
  }

  get(module: ListModule): string {

    const saved = sessionStorage.getItem(
      this.prefix + module
    );

    if (saved && this.isValidUrl(module, saved)) {
      return saved;
    }

    return `/${module}`;
  }

  clear(module: ListModule): void {
    sessionStorage.removeItem(this.prefix + module);
  }

  private isValidUrl(
    module: ListModule,
    url: string
  ): boolean {

    return url === `/${module}` ||
      url.startsWith(`/${module}?`);
  }
}
