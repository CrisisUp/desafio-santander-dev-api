import { HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

/**
 * Adds the Bearer JWT to every API request when a session exists.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;
  if (token) {
    const clone = req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) });
    return next(clone);
  }
  return next(req);
};
