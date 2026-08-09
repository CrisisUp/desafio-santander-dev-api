import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

export interface AuthResponse {
  token: string;
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

  logout(): void {
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
