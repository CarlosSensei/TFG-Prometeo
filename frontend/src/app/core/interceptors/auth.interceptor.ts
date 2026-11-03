import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const auth = inject(AuthService);
  const router = inject(Router);

  // Evitamos modificar solicitudes externas
  if (!req.url.startsWith('/api/')) {
    return next(req);
  }

  const isLogin = req.url === '/api/auth/login';
  const authorization = auth.getAuthorizationHeader();

  const request = !isLogin && authorization
    ? req.clone({setHeaders: { Authorization: authorization }})
    : req;

  return next(request).pipe(catchError((error: HttpErrorResponse) => {

      if (error.status === 401 && !isLogin) {
        const returnUrl = router.url;

        auth.logout();

        void router.navigate(['/login'], {
          queryParams: { returnUrl }
        });
      }

      return throwError(() => error);
    })
  );
};
