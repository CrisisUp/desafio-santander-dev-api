import { HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { Observable, from, of, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

/**
 * Adds the Bearer JWT to every API request. On a 401 (access token expired),
 * attempts a single refresh (rotating the refresh token) and retries the
 * original request once; if the refresh also fails, the session is cleared and
 * the error propagates.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  // Never intercept the auth endpoints themselves (refresh/login/logout).
  if (req.url.startsWith('/auth/')) {
    return next(req);
  }

  // Reads the CURRENT token at call time, so a retry after a refresh uses the
  // freshly rotated access token (not the expired one captured earlier).
  const addAuth = (r: HttpRequest<unknown>) => {
    const t = auth.token;
    return t ? r.clone({ headers: r.headers.set('Authorization', `Bearer ${t}`) }) : r;
  };

  return next(addAuth(req)).pipe(
    catchError((err) => {
      if (err.status === 401 && auth.refreshToken) {
        // Single in-flight refresh: concurrent 401s share one refresh call.
        return from(refreshOnce(auth)).pipe(
          switchMap(() => next(addAuth(req))), // retry once with the new token
          catchError((refreshErr) => {
            auth.clear();
            return throwError(() => refreshErr);
          })
        );
      }
      return throwError(() => err);
    })
  );
};

/**
 * Performs the refresh, guarding against concurrent calls (multiple 401s in
 * flight share a single /auth/refresh request).
 */
function refreshOnce(auth: AuthService): Promise<void> {
  if (!refreshPromise) {
    refreshPromise = auth.refresh().toPromise().then(() => undefined).finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

let refreshPromise: Promise<void> | null = null;
