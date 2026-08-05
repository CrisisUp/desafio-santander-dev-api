import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { User, UserPage } from './user';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly baseUrl = '/users';

  constructor(private http: HttpClient) {}

  list(page: number, size: number): Observable<UserPage> {
    return this.http.get<UserPage>(this.baseUrl, {
      params: { page, size },
    });
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

  private handleError(err: HttpErrorResponse): Observable<never> {
    // The API returns the message as the response body (string) for 404/422.
    const message =
      typeof err.error === 'string' && err.error
        ? err.error
        : `Erro inesperado (HTTP ${err.status}).`;
    return throwError(() => new Error(message));
  }
}
