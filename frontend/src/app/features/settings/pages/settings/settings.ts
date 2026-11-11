import { Component, inject } from '@angular/core';
import { AppearanceSettingsService, ThemePreference, AccentPreference, DensityPreference, MotionPreference } from '../../../../core/settings/appearance-settings.service';
import { AvatarService } from '../../../../core/settings/avatar.service';
import { AuthService } from '../../../../core/auth/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  templateUrl: './settings.html',
  styleUrl: './settings.scss'
})
export class Settings {

  readonly avatar = inject(AvatarService);
  readonly appearance = inject(AppearanceSettingsService);
  readonly auth = inject(AuthService);

  setTheme(theme: ThemePreference): void { 
    this.appearance.update({ theme }); 
  }

  setAccent(accent: AccentPreference): void { 
    this.appearance.update({ accent }); 
  }

  setDensity(density: DensityPreference): void { 
    this.appearance.update({ density }); 
  }

  setMotion(motion: MotionPreference): void { 
    this.appearance.update({ motion }); 
  }

  reset(): void { 
    this.appearance.reset(); 
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (file) {
      this.avatar.upload(file);
    }

    input.value = '';
  }
}
