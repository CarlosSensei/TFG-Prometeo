import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {

  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly showPassword = signal(false);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  constructor() {
    if (this.auth.isAuthenticated()) {
      void this.router.navigate(['/dashboard']);
    }
  }

  togglePassword(): void {
    this.showPassword.update(value => !value);
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const credentials = {
      email: this.form.controls.email.value.trim(),
      password: this.form.controls.password.value
    };

    this.auth.login(credentials).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: () => {
        const requestedUrl =
          this.route.snapshot.queryParamMap.get('returnUrl');

        // Evitar redirecciones externas
        const returnUrl =
          requestedUrl?.startsWith('/') &&
          !requestedUrl.startsWith('//') &&
          !requestedUrl.startsWith('/\\') &&
          !requestedUrl.startsWith('/login')
            ? requestedUrl
            : '/dashboard';

        void this.router.navigateByUrl(returnUrl);
      },

      error: (err: HttpErrorResponse) => {
        if (err.status === 400) {
          this.error.set(
            'Revisa el formato de los datos introducidos.'
          );
        } else if (err.status === 401) {
          this.error.set(
            'Correo electrónico o contraseña incorrectos.'
          );
        } else if (err.status === 0) {
          this.error.set(
            'No se pudo conectar con el servidor.'
          );
        } else {
          this.error.set(
            'No se pudo iniciar sesión. Inténtalo de nuevo.'
          );
        }
      }
    });
  }
}
