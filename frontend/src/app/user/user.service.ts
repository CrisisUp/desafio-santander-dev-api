import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { User, UserPage } from './user';

export interface UniquenessCheck {
  accountNumberAvailable: boolean;
  cardNumberAvailable: boolean;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly baseUrl = '/users';

  constructor(private http: HttpClient) {}

  list(page: number, size: number, name?: string, sort?: string): Observable<UserPage> {
    const params: Record<string, string> = { page: String(page), size: String(size) };
    if (name && name.trim()) {
      params['name'] = name.trim();
    }
    if (sort) {
      params['sort'] = sort;
    }
    return this.http.get<UserPage>(this.baseUrl, { params });
  }

  get(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`).pipe(catchError(this.handleError));
  }

  create(user: User): Observable<User> {
    return this.http.post<User>(this.baseUrl, user).pipe(catchError(this.handleError));
  }

  update(id: number, user: User): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/${id}`, user).pipe(catchError(this.handleError));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(catchError(this.handleError));
  }

  /** Whether the account/card numbers are still free (excludeId = user being edited). */
  checkUniqueness(accountNumber?: string, cardNumber?: string, excludeId?: number): Observable<UniquenessCheck> {
    const params: Record<string, string> = {};
    if (accountNumber) params['accountNumber'] = accountNumber;
    if (cardNumber) params['cardNumber'] = cardNumber;
    if (excludeId != null) params['excludeId'] = String(excludeId);
    return this.http.get<UniquenessCheck>(`${this.baseUrl}/check`, { params }).pipe(catchError(this.handleError));
  }

  private handleError(err: HttpErrorResponse): Observable<never> {
    // The API returns the message as the response body (string) for 404/422.
    const message =
      typeof err.error === 'string' && err.error
        ? err.error
        : `Erro inesperado (HTTP ${err.status}).`;
    return throwError(() => new Error(message));
  }
}
