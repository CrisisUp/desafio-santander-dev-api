import { Component, OnInit, ViewChild, signal } from '@angular/core';
import { animate, query, state, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PageEvent } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Subject, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { UserService } from '../user.service';
import { User } from '../user';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';

/** Entry animation for table rows and the empty state. */
export const fadeSlideIn = trigger('fadeSlideIn', [
  transition(':enter', [
    style({ opacity: 0, transform: 'translateY(8px)' }),
    animate('200ms ease-out', style({ opacity: 1, transform: 'translateY(0)' })),
  ]),
]);

/** Row-level animations for the users table. */
export const rowAnimation = trigger('rowAnimation', [
  transition('* => *', [query(':enter', style({ opacity: 0, transform: 'translateY(8px)' }), { optional: true })]),
]);

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.html',
  styleUrls: ['./user-list.css'],
  standalone: true,
  animations: [
    rowAnimation,
    trigger('expandIcon', [
      state('collapsed', style({ transform: 'rotate(0deg)' })),
      state('expanded', style({ transform: 'rotate(180deg)' })),
      transition('expanded <=> collapsed', animate('200ms ease-out')),
    ]),
    trigger('detailExpand', [
      transition(':enter', [style({ opacity: 0 }), animate('200ms ease-out', style({ opacity: 1 }))]),
      transition(':leave', [animate('150ms ease-in', style({ opacity: 0 }))]),
    ]),
  ],
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    MatFormFieldModule,
    MatInputModule,
  ],
})
export class UserListComponent implements OnInit {
  /** The API protects this user from writes — disable its edit/delete actions. */
  protected readonly PROTECTED_USER_ID = 1;

  // Signals: the framework tracks template reads and re-renders the changed
  // bindings without the dev-mode double-check, so fast HTTP responses can't
  // trip NG0100 (ExpressionChangedAfterItHasBeenChecked).
  users = signal<User[]>([]);
  totalElements = signal(0);
  loading = signal(false);
  searchTerm = signal('');
  expandedRow = signal<number | null>(null);

  pageSize = 10;
  pageIndex = 0;
  private searchSubject = new Subject<string>();

  /** Sortable columns map to the property path used by the Spring Pageable sort. */
  readonly sortMap: Record<string, string> = {
    name: 'name',
    agency: 'account.agency',
    account: 'account.number',
    balance: 'account.balance',
  };

  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns = ['expand', 'name', 'agency', 'account', 'balance', 'card', 'extra', 'actions'];

  ngOnInit(): void {
    this.loadUsers();
  }

  onSearch(value: string): void {
    this.searchTerm.set(value);
    this.searchSubject.next(value);
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.expandedRow.set(null);
    this.loadUsers();
  }

  onSortChange(sort: Sort): void {
    // Reset to the first page when the sort changes.
    this.pageIndex = 0;
    this.expandedRow.set(null);
    this.loadUsers();
  }

  private sortParam(): string {
    const active = this.sort?.active;
    if (!active || !this.sort.direction) {
      return '';
    }
    const path = this.sortMap[active] ?? active;
    return `${path},${this.sort.direction}`;
  }

  /**
   * Loads the current page, cancelling any in-flight request.
   *
   * Updates flow through an observable so that:
   *  - a fast local response that lands mid change-detection does not trip
   *    NG0100 (ExpressionChangedAfterItHasBeenChecked) — the change runs in a
   *    reactive zone, not inside the HTTP callback,
   *  - a slow previous request can't overwrite a newer one (the race that
   *    flipped `loading` twice within one CD cycle and also produced NG0100).
   */
  private users$ = new Subject<void>();

  private loadUsers(): void {
    this.users$.next();
  }

  constructor(
    private userService: UserService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    // Debounced search: fires 300ms after the user stops typing.
    this.searchSubject.pipe(debounceTime(300), distinctUntilChanged()).subscribe(() => {
      this.pageIndex = 0;
      this.loadUsers();
    });

    // Single reactive pipeline: emit -> set loading -> fetch -> apply model.
    this.users$
      .pipe(
        switchMap(() => {
          this.loading.set(true);
          return this.userService.list(this.pageIndex, this.pageSize, this.searchTerm(), this.sortParam()).pipe(
            catchError((err: Error) => {
              this.snackBar.open(err.message, 'Fechar', { duration: 4000 });
              return of(null);
            })
          );
        })
      )
      .subscribe((page) => {
        if (page) {
          this.users.set(page.content);
          this.totalElements.set(page.totalElements);
        }
        this.loading.set(false);
      });
  }

  isProtected(user: User): boolean {
    return user.id === this.PROTECTED_USER_ID;
  }

  /** Toggle the expanded detail row for a user. */
  toggleRow(user: User): void {
    const current = this.expandedRow();
    this.expandedRow.set(current === user.id ? null : (user.id ?? null));
  }

  isExpanded(user: User): boolean {
    return user.id != null && this.expandedRow() === user.id;
  }

  confirmDelete(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Excluir usuário',
        message: `Excluir o usuário "${user.name}"?`,
        confirmLabel: 'Excluir',
      },
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed && user.id !== undefined) {
        this.userService.delete(user.id).subscribe({
          next: () => {
            // Defer the snackbar to the next macrotask: opening it synchronously
            // inside the dialog-close flow runs a change detection pass while
            // `loading` is still true, tripping NG0100.
            setTimeout(() => this.snackBar.open(`Usuário "${user.name}" excluído.`, 'OK', { duration: 3000 }), 0);
            this.loadUsers();
          },
          error: (err: Error) => this.snackBar.open(err.message, 'Fechar', { duration: 4000 }),
        });
      }
    });
  }
}
