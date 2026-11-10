import { Injectable, computed, signal } from '@angular/core';

export type ThemePreference = 'system' | 'light' | 'dark';
export type AccentPreference = 'blue' | 'teal' | 'violet' | 'orange' | 'green';
export type DensityPreference = 'comfortable' | 'compact';
export type MotionPreference = 'system' | 'full' | 'reduced';

export interface AppearanceSettings {
  theme: ThemePreference;
  accent: AccentPreference;
  density: DensityPreference;
  motion: MotionPreference;
}

const DEFAULTS: AppearanceSettings = {
  theme: 'system', accent: 'blue', density: 'comfortable', motion: 'system'
};

@Injectable({ providedIn: 'root' })
export class AppearanceSettingsService {
  private readonly key = 'optiflow_appearance_v1';
  private readonly state = signal<AppearanceSettings>(this.restore());
  readonly settings = this.state.asReadonly();
  readonly isDark = computed(() => this.resolveDark(this.state().theme));

  private readonly themeQuery = window.matchMedia('(prefers-color-scheme: dark)');
  private readonly motionQuery = window.matchMedia('(prefers-reduced-motion: reduce)');

  constructor() {
    this.apply();
    this.themeQuery.addEventListener('change', () => this.apply());
    this.motionQuery.addEventListener('change', () => this.apply());
  }

  update(patch: Partial<AppearanceSettings>): void {
    const next = { ...this.state(), ...patch };
    this.state.set(next);
    localStorage.setItem(this.key, JSON.stringify(next));
    this.apply();
  }

  reset(): void {
    this.state.set({ ...DEFAULTS });
    localStorage.removeItem(this.key);
    this.apply();
  }

  private resolveDark(theme: ThemePreference): boolean {
    return theme === 'dark' || (theme === 'system' && this.themeQuery.matches);
  }

  private apply(): void {
    const s = this.state();
    const root = document.documentElement;
    root.dataset['theme'] = this.resolveDark(s.theme) ? 'dark' : 'light';
    root.dataset['accent'] = s.accent;
    root.dataset['density'] = s.density;
    root.dataset['motion'] = s.motion === 'system'
      ? (this.motionQuery.matches ? 'reduced' : 'full')
      : s.motion;
    root.style.colorScheme = root.dataset['theme'];
  }

  private restore(): AppearanceSettings {
    try {
      const raw = JSON.parse(localStorage.getItem(this.key) ?? '{}') as Partial<AppearanceSettings>;
      return {
        theme: ['system', 'light', 'dark'].includes(raw.theme ?? '') ? raw.theme! : DEFAULTS.theme,
        accent: ['blue', 'teal', 'violet', 'orange', 'green'].includes(raw.accent ?? '') ? raw.accent! : DEFAULTS.accent,
        density: ['comfortable', 'compact'].includes(raw.density ?? '') ? raw.density! : DEFAULTS.density,
        motion: ['system', 'full', 'reduced'].includes(raw.motion ?? '') ? raw.motion! : DEFAULTS.motion
      };
    } catch {
      return { ...DEFAULTS };
    }
  }
}
