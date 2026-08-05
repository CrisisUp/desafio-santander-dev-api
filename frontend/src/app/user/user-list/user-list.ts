import { Component, OnInit, ViewChild } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { UserService } from '../user.service';
import { User } from '../user';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.html',
  styleUrls: ['./user-list.css'],
  standalone: false,
})
export class UserListComponent implements OnInit {
  /** The API protects this user from writes — disable its edit/delete actions. */
  protected readonly PROTECTED_USER_ID = 1;

  users: User[] = [];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;
  searchTerm = '';
  private searchSubject = new Subject<string>();

  /** Sortable columns map to the property path used by the Spring Pageable sort. */
  readonly sortMap: Record<string, string> = {
    name: 'name',
    agency: 'account.agency',
    account: 'account.number',
    balance: 'account.balance',
  };

  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns = ['name', 'agency', 'account', 'balance', 'card', 'extra', 'actions'];

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
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  onSearch(value: string): void {
    this.searchTerm = value;
    this.searchSubject.next(value);
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  onSortChange(sort: Sort): void {
    // Reset to the first page when the sort changes.
    this.pageIndex = 0;
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

  loadUsers(): void {
    this.loading = true;
    this.userService.list(this.pageIndex, this.pageSize, this.searchTerm, this.sortParam()).subscribe({
      next: (page) => {
        this.users = page.content;
        this.totalElements = page.totalElements;
        this.loading = false;
      },
      error: (err: Error) => {
        this.loading = false;
        this.snackBar.open(err.message, 'Fechar', { duration: 4000 });
      },
    });
  }

  isProtected(user: User): boolean {
    return user.id === this.PROTECTED_USER_ID;
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
            this.snackBar.open(`Usuário "${user.name}" excluído.`, 'OK', { duration: 3000 });
            this.loadUsers();
          },
          error: (err: Error) => this.snackBar.open(err.message, 'Fechar', { duration: 4000 }),
        });
      }
    });
  }
}
