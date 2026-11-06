import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthUser, LoginRequest, LoginResponse } from './auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/auth';

  // Claves utilizadas en sessionStorage
  private readonly TOKEN_KEY = 'optiflow_token';
  private readonly TOKEN_TYPE_KEY = 'optiflow_token_type';
  private readonly USER_KEY = 'optiflow_user';

  // Estado privado recuperado de sessionStorage
  private readonly accessToken = signal<string | null>(
    sessionStorage.getItem(this.TOKEN_KEY)
  );

  private readonly tokenType = signal<string>(
    sessionStorage.getItem(this.TOKEN_TYPE_KEY) ?? 'Bearer'
  );

  private readonly userState = signal<AuthUser | null>(
    this.restoreUser()
  );

  // Estado público de solo lectura
  readonly user = this.userState.asReadonly();

  readonly isAuthenticated = computed(() => {
    const token = this.accessToken();
    const user = this.userState();

    return !!token && !!user && !this.isTokenExpired(token);
  });

  readonly fullName = computed(() => {
    const user = this.userState();

    if (!user) return '';

    return `${user.firstName} ${user.lastName}`.trim();
  });

  readonly initials = computed(() => {
    const user = this.userState();

    if (!user) return '';

    return (
      (user.firstName?.charAt(0) ?? '') +
      (user.lastName?.charAt(0) ?? '')
    ).toUpperCase();
  });

  // Iniciar sesión
  login(credentials: LoginRequest): Observable<LoginResponse> {

    return this.http.post<LoginResponse>(
      `${this.apiUrl}/login`,
      credentials
    ).pipe(tap(response => {

        const { token, tokenType, ...user } = response;

        // Guardar estado en memoria
        this.accessToken.set(token);
        this.tokenType.set(tokenType ?? 'Bearer');
        this.userState.set(user);

        // Persistir durante la sesión del navegador
        sessionStorage.setItem(this.TOKEN_KEY, token);
        sessionStorage.setItem(
          this.TOKEN_TYPE_KEY,
          tokenType ?? 'Bearer'
        );
        sessionStorage.setItem(
          this.USER_KEY,
          JSON.stringify(user)
        );
      })
    );
  }

  // Recuperar usuario al recargar Angular
  private restoreUser(): AuthUser | null {

    const storedUser = sessionStorage.getItem(this.USER_KEY);

    if (!storedUser) return null;

    try {
      return JSON.parse(storedUser) as AuthUser;
    } catch {
      sessionStorage.removeItem(this.USER_KEY);
      return null;
    }
  }

  // Comprobar caducidad JWT
  private isTokenExpired(token: string): boolean {

    try {
      const payloadPart = token.split('.')[1];

      if (!payloadPart) return true;

      const normalized = payloadPart
        .replace(/-/g, '+')
        .replace(/_/g, '/');

      const payload = JSON.parse(atob(normalized));

      if (typeof payload.exp !== 'number') {
        return true;
      }

      return Date.now() >= payload.exp * 1000;

    } catch {
      return true;
    }
  }

  // Cabecera utilizada por el interceptor
  getAuthorizationHeader(): string | null {

    const token = this.accessToken();

    if (!token || this.isTokenExpired(token)) {
      return null;
    }

    return `${this.tokenType()} ${token}`;
  }

  // Cerrar sesión
  logout(): void {

    // Limpiar memoria
    this.accessToken.set(null);
    this.tokenType.set('Bearer');
    this.userState.set(null);

    // Limpiar almacenamiento
    sessionStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem(this.TOKEN_TYPE_KEY);
    sessionStorage.removeItem(this.USER_KEY);
  }
}
