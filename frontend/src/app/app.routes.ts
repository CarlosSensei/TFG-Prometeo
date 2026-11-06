import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Iniciar sesión | OptiFlow',
    loadComponent: () =>
      import('./features/auth/pages/login/login')
        .then(m => m.Login)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./core/layout/app-shell/app-shell')
        .then(m => m.AppShell),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      },
      {
        path: 'dashboard',
        title: 'Dashboard | OptiFlow',
        loadComponent: () =>
          import('./features/dashboard/pages/dashboard/dashboard')
            .then(m => m.Dashboard)
      },
      {
        path: 'clients',
        title: 'Clientes | OptiFlow',
        loadComponent: () =>
          import('./features/clients/pages/client-list/client-list')
            .then(m => m.ClientList)
      },
      {
        path: 'clients/new',
        loadComponent: () =>
          import('./features/clients/pages/client-form/client-form')
            .then(m => m.ClientForm)
      },
      {
        path: 'clients/:id/edit',
        loadComponent: () =>
          import('./features/clients/pages/client-form/client-form')
            .then(m => m.ClientForm)
      },
      {
        path: 'quotes',
        title: 'Presupuestos | OptiFlow',
        loadComponent: () =>
          import('./features/quotes/pages/quote-list/quote-list')
            .then(m => m.QuoteList)
      },
      {
        path: 'quotes/new',
        title: 'Nuevo presupuesto | OptiFlow',
        loadComponent: () =>
          import('./features/quotes/pages/quote-form/quote-form')
            .then(m => m.QuoteForm)
      },
      {
        path: 'quotes/:id/edit',
        title: 'Editar presupuesto | OptiFlow',
        loadComponent: () =>
          import('./features/quotes/pages/quote-form/quote-form')
            .then(m => m.QuoteForm)
      },
      {
        path: 'ui-playground',
        title: 'UI Playground | OptiFlow',
        loadComponent: () =>
          import('./features/ui-playground/pages/ui-playground/ui-playground')
            .then(m => m.UiPlayground)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
