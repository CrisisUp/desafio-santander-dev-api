import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subject, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import { TransactionService } from '../transaction.service';
import { UserService } from '../user.service';
import { User } from '../user';
import { Transaction, TransactionType } from '../transaction';

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.html',
  styleUrls: ['./transaction-list.css'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
})
export class TransactionListComponent implements OnInit {
  // Signal state (same pattern as UserListComponent — avoids NG0100).
  transactions = signal<Transaction[]>([]);
  totalElements = signal(0);
  loading = signal(false);
  saving = signal(false);
  user = signal<User | null>(null);
  showForm = signal(false);

  accountId: number;
  pageSize = 10;
  pageIndex = 0;
  displayedColumns = ['date', 'type', 'detail', 'amount'];

  form: FormGroup;

  /** Reactive pipeline: emits trigger a load, switchMap cancels stale requests. */
  private reload$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private transactionService: TransactionService,
    private userService: UserService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.accountId = Number(this.route.snapshot.paramMap.get('id'));

    this.form = this.fb.group({
      type: ['DEPOSIT', Validators.required],
      amount: [null, Validators.required],
      destinationAccountId: [null],
    });

    this.reload$
      .pipe(
        switchMap(() => {
          this.loading.set(true);
          return this.transactionService.list(this.accountId, this.pageIndex, this.pageSize).pipe(
            catchError((err: Error) => {
              this.snackBar.open(err.message, 'Fechar', { duration: 4000 });
              return of(null);
            })
          );
        })
      )
      .subscribe((page) => {
        if (page) {
          this.transactions.set(page.content);
          this.totalElements.set(page.totalElements);
        }
        this.loading.set(false);
      });
  }

  ngOnInit(): void {
    this.loadUser();
    this.loadTransactions();
  }

  private loadUser(): void {
    this.userService.get(this.accountId).subscribe({
      next: (user) => this.user.set(user),
      error: (err: Error) => {
        this.snackBar.open(err.message, 'Fechar', { duration: 4000 });
        this.router.navigate(['/users']);
      },
    });
  }

  private loadTransactions(): void {
    this.reload$.next();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadTransactions();
  }

  toggleForm(): void {
    this.showForm.set(!this.showForm());
  }

  /** Builds the request payload from the form (mirrors UserFormComponent.buildPayload). */
  buildPayload(): { type: TransactionType; amount: number; destinationAccountId?: number } {
    const value = this.form.value;
    const payload: { type: TransactionType; amount: number; destinationAccountId?: number } = {
      type: value.type,
      amount: Number(value.amount),
    };
    if (value.type === 'TRANSFER' && value.destinationAccountId != null) {
      payload.destinationAccountId = Number(value.destinationAccountId);
    }
    return payload;
  }

  onSubmit(): void {
    if (this.form.invalid || this.saving()) {
      return;
    }
    this.saving.set(true);
    this.transactionService.create(this.accountId, this.buildPayload()).subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.form.reset({ type: 'DEPOSIT', amount: null, destinationAccountId: null });
        this.snackBar.open('✓ Operação registrada.', 'OK', { duration: 3000, panelClass: 'snack-success' });
        // Reload the statement AND the parent user (its balance changed).
        this.loadTransactions();
        this.loadUser();
      },
      error: (err: Error) => {
        this.saving.set(false);
        this.snackBar.open(err.message, 'Fechar', { duration: 5000 });
      },
    });
  }

  /** Human-readable label for a transaction type. */
  typeLabel(type: TransactionType): string {
    return (
      {
        DEPOSIT: 'Depósito',
        WITHDRAWAL: 'Saque',
        TRANSFER: 'Transferência',
        PAYMENT: 'Pagamento',
      }[type] ?? type
    );
  }

  /** Positive/negative display sign based on the type (credits add, debits subtract). */
  amountClass(tx: Transaction): string {
    return tx.type === 'DEPOSIT' ? 'balance-positive' : 'balance-negative';
  }

  amountValue(tx: Transaction): number {
    return tx.type === 'DEPOSIT' ? tx.amount : -tx.amount;
  }
}
