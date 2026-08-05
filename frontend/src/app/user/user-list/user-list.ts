import { Component, OnInit } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
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

  displayedColumns = ['name', 'account', 'balance', 'limit', 'actions'];

  constructor(
    private userService: UserService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.userService.list(this.pageIndex, this.pageSize).subscribe({
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
      data: { message: `Excluir o usuário "${user.name}"?` },
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
