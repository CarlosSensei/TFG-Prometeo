
import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { GlobalSearch } from '../../search/global-search';
import { ListNavigationStateService } from '../../services/list-navigation-state.service';
import { NotificationBell } from '../../../shared/ui/notification-bell/notification-bell';


@Component({
  selector: 'app-app-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, GlobalSearch, NotificationBell],
  templateUrl: './app-shell.html',
  styleUrl: './app-shell.scss'
})

export class AppShell {

  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly listNavigation = inject(ListNavigationStateService);

  logout(): void {
    this.auth.logout();
    void this.router.navigate(['/login']);
  }

  navigateToList(module: 'clients' | 'quotes' | 'orders' | 'inventory'): void {

    this.closeSidebar();

    const url = this.listNavigation.get(module);

    void this.router.navigateByUrl(url);
  }

  sidebarOpen = signal(false);  

  toggleSidebar(): void {
    this.sidebarOpen.update(open => !open);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }
}
