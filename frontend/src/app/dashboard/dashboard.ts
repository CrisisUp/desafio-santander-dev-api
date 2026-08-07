import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subject, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { UserService } from '../user/user.service';
import { DashboardData, computeDashboard } from './dashboard-data';

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

  constructor(private userService: UserService, private snackBar: MatSnackBar) {
    this.reload$
      .pipe(
        switchMap(() => {
          this.loading.set(true);
          return this.userService.list(0, 100).pipe(
            catchError((err: Error) => {
              this.snackBar.open(err.message, 'Fechar', { duration: 4000 });
              return of(null);
            })
          );
        })
      )
      .subscribe((page) => {
        if (page) {
          this.data.set(computeDashboard(page.content));
        }
        this.loading.set(false);
      });
  }

  ngOnInit(): void {
    this.reload$.next();
  }
}
