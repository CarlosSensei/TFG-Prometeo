import { Injectable, inject, signal, effect } from '@angular/core';
import { AuthService } from '../auth/auth.service';

@Injectable({ providedIn: 'root' })
export class AvatarService {
    private readonly auth = inject(AuthService);

    readonly image = signal<string | null>(null);
    readonly error = signal<string | null>(null);

    constructor() {
        effect(() => {
            this.auth.user()?.userId;
            this.load();
        });
    }

    private storageKey(): string {
        return `optiflow_avatar_${this.auth.user()?.userId ?? 'guest'}`;
    }

    load(): void {
        this.image.set(localStorage.getItem(this.storageKey()));
    }

    upload(file: File): void {
        this.error.set(null);

        if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
            this.error.set('Utiliza una imagen JPG, PNG o WebP.');
            return;
        }

        if (file.size > 1024 * 1024) {
            this.error.set('La imagen no debe superar 1 MB.');
            return;
        }

        const reader = new FileReader();

        reader.onload = () => {
            if (typeof reader.result !== 'string') return;

            try {
                localStorage.setItem(this.storageKey(), reader.result);
                this.image.set(reader.result);
            } catch {
                this.error.set('No se pudo guardar la imagen.');
            }
        };

        reader.onerror = () => {
            this.error.set('No se pudo leer la imagen.');
        };

        reader.readAsDataURL(file);
    }

    remove(): void {
        localStorage.removeItem(this.storageKey());
        this.image.set(null);
        this.error.set(null);
    }
}
