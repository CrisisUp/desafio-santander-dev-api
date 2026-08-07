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
import { TransactionListComponent } from './transaction-list';

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

  it('amountValue signs debits negative and credits positive', () => {
    const fixture = TestBed.createComponent(TransactionListComponent);
    const comp = fixture.componentInstance;

    expect(
      comp.amountValue({ id: 1, type: 'DEPOSIT', amount: 50, accountId: 1, createdAt: '' })
    ).toBe(50);
    expect(
      comp.amountValue({ id: 2, type: 'WITHDRAWAL', amount: 20, accountId: 1, createdAt: '' })
    ).toBe(-20);
    expect(
      comp.amountValue({ id: 3, type: 'PAYMENT', amount: 10, accountId: 1, createdAt: '' })
    ).toBe(-10);
    expect(
      comp.amountValue({
        id: 4,
        type: 'TRANSFER',
        amount: 30,
        accountId: 1,
        destinationAccountId: 2,
        createdAt: '',
      })
    ).toBe(-30);
  });
});
