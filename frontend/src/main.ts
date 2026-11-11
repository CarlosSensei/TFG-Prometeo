import { bootstrapApplication } from '@angular/platform-browser';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';

import { appConfig } from './app/app.config';
import { App } from './app/app';

// Registrar formatos españoles para moneda,
// fechas, números y porcentajes.
registerLocaleData(localeEs);

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
