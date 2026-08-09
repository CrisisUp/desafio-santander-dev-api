import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

export interface AuthResponse {
  token: string;
  refreshToken: string;
  username: string;
  role: string;
  userId: number | null;
}

export interface Credentials {
  username: string;
  password: string;
}

const TOKEN_KEY = 'sdw_jwt';

@Injectable({ providedIn: 'root' })
export class AuthService {
  /** Current auth state, persisted in localStorage. */
  readonly auth = signal<AuthResponse | null>(this.load());

  constructor(private http: HttpClient) {}

  get token(): string | null {
    return this.auth()?.token ?? null;
  }

  get refreshToken(): string | null {
    return this.auth()?.refreshToken ?? null;
  }

  get isAuthenticated(): boolean {
    return this.auth() !== null;
  }

  login(creds: Credentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/auth/login', creds).pipe(
      tap((res) => this.save(res)),
      catchError(this.handleError)
    );
  }

  register(creds: Credentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/auth/register', creds).pipe(
      tap((res) => this.save(res)),
      catchError(this.handleError)
    );
  }

  /** Rotates the refresh token and stores the new token pair. */
  refresh(): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>('/auth/refresh', { refreshToken: this.refreshToken })
      .pipe(
        tap((res) => this.save(res)),
        catchError(this.handleError)
      );
  }

  /** Revokes the refresh token on the server, then clears local state. */
  logout(): Observable<void> {
    const rt = this.refreshToken;
    // Revoke on the server when we have a token; always clear local state.
    const revoke$ = rt
      ? this.http.post<void>('/auth/logout', { refreshToken: rt })
      : new Observable<void>((s) => { s.next(); s.complete(); });
    return revoke$.pipe(
      tap(() => this.clear()),
      catchError((err) => {
        // Even if revocation fails, end the session locally.
        this.clear();
        return throwError(() => err);
      })
    );
  }

  /** Clears local state without a server call (e.g. after a failed refresh). */
  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.auth.set(null);
  }

  private save(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, JSON.stringify(res));
    this.auth.set(res);
  }

  private load(): AuthResponse | null {
    // localStorage may be absent in test environments.
    if (typeof localStorage === 'undefined') return null;
    const raw = localStorage.getItem(TOKEN_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as AuthResponse;
    } catch {
      return null;
    }
  }

  private handleError(err: HttpErrorResponse): Observable<never> {
    const message =
      typeof err.error === 'string' && err.error
        ? err.error
        : `Erro inesperado (HTTP ${err.status}).`;
    return throwError(() => new Error(message));
  }
}
