import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subject, combineLatest, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { UserService } from '../user/user.service';
import { TransactionService } from '../user/transaction.service';
import { DashboardData, computeDashboard, computeTransactionStats } from './dashboard-data';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
  standalone: true,
  imports: [CommonModule, MatTooltipModule, MatIconModule, MatSnackBarModule],
})
export class DashboardComponent {
  data = signal<DashboardData | null>(null);
  loading = signal(false);

  /** Reactive pipeline (same pattern as user-list): switchMap cancels stale requests. */
  private reload$ = new Subject<void>();

  constructor(
    private userService: UserService,
    private transactionService: TransactionService,
    private snackBar: MatSnackBar
  ) {
    this.reload$
      .pipe(
        switchMap(() => {
          this.loading.set(true);
          return combineLatest([
            this.userService.list(0, 100).pipe(catchError((err) => this.fail(err))),
            this.transactionService.getStats().pipe(catchError((err) => this.fail(err))),
          ]);
        })
      )
      .subscribe(([usersPage, stats]) => {
        if (usersPage) {
          // A stats failure renders an all-zero bars card instead of blanking
          // the dashboard (ponytail: stricter alternative is fail-whole-view).
          const dashboard = computeDashboard(usersPage.content);
          this.data.set({ ...dashboard, transactionStats: computeTransactionStats(stats ?? []) });
        }
        this.loading.set(false);
      });
  }

  ngOnInit(): void {
    this.reload$.next();
  }

  private fail(err: Error) {
    this.snackBar.open(err.message, 'Fechar', { duration: 4000 });
    return of(null);
  }
}
