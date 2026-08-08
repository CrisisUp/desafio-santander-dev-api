import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { TransactionListComponent, buildAccountOptions } from './transaction-list';
import { User } from '../user';

describe('TransactionListComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        RouterModule.forRoot([]),
        HttpClientTestingModule,
        MatSnackBarModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatSelectModule,
        BrowserAnimationsModule,
        TransactionListComponent,
      ],
    }).compileComponents();
  });

  it('buildPayload for DEPOSIT omits destinationAccountId', () => {
    const fixture = TestBed.createComponent(TransactionListComponent);
    const comp = fixture.componentInstance;
    comp.form.patchValue({ type: 'DEPOSIT', amount: 150.5, destinationAccountId: null });

    const payload = comp.buildPayload();
    expect(payload.type).toBe('DEPOSIT');
    expect(payload.amount).toBe(150.5);
    expect(payload.destinationAccountId).toBeUndefined();
  });

  it('buildPayload for TRANSFER keeps destinationAccountId', () => {
    const fixture = TestBed.createComponent(TransactionListComponent);
    const comp = fixture.componentInstance;
    comp.form.patchValue({ type: 'TRANSFER', amount: 100, destinationAccountId: 3 });

    const payload = comp.buildPayload();
    expect(payload.type).toBe('TRANSFER');
    expect(payload.destinationAccountId).toBe(3);
  });

  it('requires destination only for TRANSFER', () => {
    const fixture = TestBed.createComponent(TransactionListComponent);
    const comp = fixture.componentInstance;
    const dest = comp.form.get('destinationAccountId')!;

    // Non-transfer: destination optional.
    comp.form.patchValue({ type: 'DEPOSIT', amount: 10, destinationAccountId: null });
    expect(dest.hasError('required')).toBe(false);

    // TRANSFER without destination → invalid.
    comp.form.patchValue({ type: 'TRANSFER', amount: 10, destinationAccountId: null });
    expect(dest.hasError('required')).toBe(true);

    // TRANSFER with destination → valid.
    comp.form.patchValue({ type: 'TRANSFER', amount: 10, destinationAccountId: 3 });
    expect(dest.hasError('required')).toBe(false);
  });

  it('rejects a non-positive amount', () => {
    const fixture = TestBed.createComponent(TransactionListComponent);
    const comp = fixture.componentInstance;
    comp.form.patchValue({ type: 'DEPOSIT', amount: 0 });
    expect(comp.form.get('amount')!.hasError('min')).toBe(true);
    comp.form.patchValue({ type: 'DEPOSIT', amount: 0.01 });
    expect(comp.form.get('amount')!.valid).toBe(true);
  });

  it('flags insufficient funds before submit for a debit exceeding balance', () => {
    const fixture = TestBed.createComponent(TransactionListComponent);
    const comp = fixture.componentInstance;
    // The account header supplies the balance to the validator.
    comp.user.set({
      id: 3,
      name: 'Bruno',
      account: { id: 3, number: '0003', agency: '0001', balance: 100, limit: 500 },
      card: { number: 'x', limit: 0 },
      features: [],
      news: [],
    });
    const amount = comp.form.get('amount')!;

    // Debit above balance → insufficient.
    comp.form.patchValue({ type: 'WITHDRAWAL', amount: 150 });
    expect(amount.hasError('insufficientFunds')).toBe(true);

    // Debit within balance → OK.
    comp.form.patchValue({ type: 'WITHDRAWAL', amount: 50 });
    expect(amount.hasError('insufficientFunds')).toBe(false);

    // DEPOSIT is never limited by balance.
    comp.form.patchValue({ type: 'DEPOSIT', amount: 5000 });
    expect(amount.hasError('insufficientFunds')).toBe(false);
  });

  it('amountValue signs debits negative and credits positive', () => {
    const fixture = TestBed.createComponent(TransactionListComponent);
    const comp = fixture.componentInstance;

    // DEPOSIT credits.
    expect(
      comp.amountValue({ id: 1, type: 'DEPOSIT', amount: 50, accountId: 1, createdAt: '', credit: true })
    ).toBe(50);
    expect(comp.amountClass({ id: 1, type: 'DEPOSIT', amount: 50, accountId: 1, createdAt: '', credit: true }))
      .toBe('balance-positive');
    // Debits are negative regardless of type.
    expect(
      comp.amountValue({ id: 2, type: 'WITHDRAWAL', amount: 20, accountId: 1, createdAt: '', credit: false })
    ).toBe(-20);
    expect(
      comp.amountValue({ id: 3, type: 'PAYMENT', amount: 10, accountId: 1, createdAt: '', credit: false })
    ).toBe(-10);
    // Transfer DEBIT leg (source) is negative.
    expect(
      comp.amountValue({
        id: 4,
        type: 'TRANSFER',
        amount: 30,
        accountId: 1,
        destinationAccountId: 2,
        createdAt: '',
        credit: false,
      })
    ).toBe(-30);
    // Transfer CREDIT leg (destination) is positive — the fix for double-entry.
    expect(
      comp.amountValue({
        id: 5,
        type: 'TRANSFER',
        amount: 30,
        accountId: 2,
        destinationAccountId: 1,
        createdAt: '',
        credit: true,
      })
    ).toBe(30);
    expect(
      comp.amountClass({
        id: 5,
        type: 'TRANSFER',
        amount: 30,
        accountId: 2,
        destinationAccountId: 1,
        createdAt: '',
        credit: true,
      })
    ).toBe('balance-positive');
  });
});

describe('buildAccountOptions', () => {
  const users = [
    { id: 1, name: 'Ana', account: { id: 10, number: '0002', agency: '0001', balance: 0, limit: 0 }, card: { number: 'x', limit: 0 }, features: [], news: [] },
    { id: 2, name: 'Bruno', account: { id: 20, number: '0003', agency: '0001', balance: 0, limit: 0 }, card: { number: 'y', limit: 0 }, features: [], news: [] },
  ] as unknown as User[];

  it('excludes the current account and labels by visible number', () => {
    const options = buildAccountOptions(users, 10);
    expect(options).toEqual([{ id: 20, label: '0003 — Bruno' }]);
  });

  it('returns empty when no other accounts exist', () => {
    expect(buildAccountOptions(users, 10)).toHaveLength(1);
    expect(buildAccountOptions(users, 99)).toHaveLength(2);
  });
});
