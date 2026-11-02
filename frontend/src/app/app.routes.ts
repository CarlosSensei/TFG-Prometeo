import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
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
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
