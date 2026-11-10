import { Component, inject } from '@angular/core';
import { AppearanceSettingsService, ThemePreference, AccentPreference, DensityPreference, MotionPreference } from '../../../../core/settings/appearance-settings.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  templateUrl: './settings.html',
  styleUrl: './settings.scss'
})
export class Settings {
  readonly appearance = inject(AppearanceSettingsService);
  setTheme(theme: ThemePreference): void { this.appearance.update({ theme }); }
  setAccent(accent: AccentPreference): void { this.appearance.update({ accent }); }
  setDensity(density: DensityPreference): void { this.appearance.update({ density }); }
  setMotion(motion: MotionPreference): void { this.appearance.update({ motion }); }
  reset(): void { this.appearance.reset(); }
}
