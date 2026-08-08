import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
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
  accountOptions = signal<AccountOption[]>([]);

  accountId: number;
  pageSize = 10;
  pageIndex = 0;
  displayedColumns = ['date', 'type', 'detail', 'amount'];
  /** Idempotency-Key for the current operation form (regenerated on open). */
  private currentIdempotencyKey: string = crypto.randomUUID();

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
      amount: [null, [Validators.required, Validators.min(0.01), this.balanceValidator.bind(this)]],
      // The destination is required only for TRANSFER (validated conditionally).
      destinationAccountId: [null, this.transferDestinationValidator.bind(this)],
    });
    // Re-run the conditional validations when the type or the balance changes.
    this.form.get('type')?.valueChanges.subscribe(() => {
      this.form.get('destinationAccountId')?.updateValueAndValidity();
      this.form.get('amount')?.updateValueAndValidity();
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
    this.loadAccountOptions();
  }

  /**
   * Loads the accounts for the transfer dropdown (the account number is what the
   * user sees, not the internal id).
   *
   * ponytail: capped at the first 100 users — fine today (seed has 41), but the
   * dropdown goes incomplete past 100. Upgrade path: paginate until exhausted or
   * add a dedicated GET /accounts/options endpoint.
   */
  private loadAccountOptions(): void {
    this.userService.list(0, 100).subscribe({
      next: (page) => this.accountOptions.set(buildAccountOptions(page.content, this.accountId)),
      error: (err: Error) => this.snackBar.open(err.message, 'Fechar', { duration: 4000 }),
    });
  }

  private loadUser(): void {
    this.userService.get(this.accountId).subscribe({
      next: (user) => {
        this.user.set(user);
        // The balance validator reads the just-loaded balance.
        this.form.get('amount')?.updateValueAndValidity();
      },
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
    if (this.showForm()) {
      // New intent = new idempotency key. Retries of the SAME submit reuse it,
      // so a duplicate click can't double-debit.
      this.currentIdempotencyKey = crypto.randomUUID();
    }
  }

  /**
   * A destination is mandatory for TRANSFER and ignored otherwise. Reading the
   * current type from the form keeps the rule in sync with the backend.
   */
  transferDestinationValidator(control: AbstractControl): ValidationErrors | null {
    const type = this.form?.get('type')?.value;
    if (type === 'TRANSFER' && (control.value == null || control.value === '')) {
      return { required: true };
    }
    return null;
  }

  /**
   * Warns before submit when a debit (WITHDRAWAL/PAYMENT/TRANSFER) exceeds the
   * account balance. DEPOSIT never fails this. The backend remains the source
   * of truth — this is UX polish, not a safety control (the lock does that).
   */
  balanceValidator(control: AbstractControl): ValidationErrors | null {
    const type = this.form?.get('type')?.value;
    const balance = this.user()?.account?.balance;
    if (type == null || type === 'DEPOSIT' || balance == null || control.value == null) {
      return null;
    }
    const amount = Number(control.value);
    if (Number.isFinite(amount) && amount > balance) {
      return { insufficientFunds: true };
    }
    return null;
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
    this.transactionService.create(this.accountId, this.buildPayload(), this.currentIdempotencyKey).subscribe({
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

  /** Positive/negative display sign based on the movement direction. */
  amountClass(tx: Transaction): string {
    return tx.credit ? 'balance-positive' : 'balance-negative';
  }

  amountValue(tx: Transaction): number {
    return tx.credit ? tx.amount : -tx.amount;
  }
}

/** A selectable destination for a transfer: visible account number + holder. */
export interface AccountOption {
  id: number;
  label: string;
}

/**
 * Builds the transfer dropdown options from the user list, excluding the
 * current account. Label shows the visible account number, not the internal id.
 */
export function buildAccountOptions(users: User[], currentAccountId: number): AccountOption[] {
  return users
    .filter((u) => u.account?.id != null && u.account.id !== currentAccountId)
    .map((u) => ({ id: u.account.id as number, label: `${u.account.number} — ${u.name}` }))
    .sort((a, b) => a.label.localeCompare(b.label, 'pt-BR'));
}
