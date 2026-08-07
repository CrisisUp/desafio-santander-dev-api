import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Transaction, TransactionPage, TransactionRequest } from './transaction';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly baseUrl = '/accounts';

  constructor(private http: HttpClient) {}

  list(accountId: number, page: number, size: number): Observable<TransactionPage> {
    const params: Record<string, string> = { page: String(page), size: String(size) };
    return this.http.get<TransactionPage>(`${this.baseUrl}/${accountId}/transactions`, { params });
  }

  create(accountId: number, tx: TransactionRequest): Observable<Transaction> {
    return this.http
      .post<Transaction>(`${this.baseUrl}/${accountId}/transactions`, tx)
      .pipe(catchError(this.handleError));
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
