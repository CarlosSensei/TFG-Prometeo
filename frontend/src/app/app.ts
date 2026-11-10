import { Component, signal, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AppearanceSettingsService } from './core/settings/appearance-settings.service'

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('optiflow-frontend');
  readonly appearance = inject(AppearanceSettingsService);
}
