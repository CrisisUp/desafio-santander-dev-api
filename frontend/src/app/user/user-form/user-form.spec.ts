import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { firstValueFrom } from 'rxjs';
import { UserFormComponent } from './user-form';
import { UserService } from '../user.service';

describe('UserFormComponent', () => {
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
        BrowserAnimationsModule,
        UserFormComponent,
      ],
    }).compileComponents();
  });

  it('buildPayload on create strips child ids (API rejects pre-persisted ids with 422)', () => {
    const fixture = TestBed.createComponent(UserFormComponent);
    const comp = fixture.componentInstance;
    comp.isEdit.set(false);
    comp.form.patchValue({
      name: 'Novo',
      account: { number: '0001', agency: '0001', balance: 10, limit: 100 },
      card: { number: 'x', limit: 500 },
    });
    comp.addFeature();
    comp.addNews();
    comp.features.controls[0].patchValue({ id: 99, description: 'f', icon: '' });
    comp.news.controls[0].patchValue({ id: 77, description: 'n', icon: '' });

    const payload = comp.buildPayload();
    expect(payload.id).toBeUndefined();
    expect(payload.account.id).toBeUndefined();
    expect(payload.card.id).toBeUndefined();
    expect(payload.features[0].id).toBeUndefined();
    expect(payload.news[0].id).toBeUndefined();
  });

  it('buildPayload on edit keeps child ids (API validates ownership)', () => {
    const fixture = TestBed.createComponent(UserFormComponent);
    const comp = fixture.componentInstance;
    comp.isEdit.set(true);
    comp.userId.set(5);
    comp.form.patchValue({
      name: 'Edit',
      account: { id: 7, number: '0001', agency: '0001', balance: 10, limit: 100 },
      card: { id: 8, number: 'x', limit: 500 },
    });
    comp.addFeature();
    comp.features.controls[0].patchValue({ id: 3, description: 'f', icon: '' });

    const payload = comp.buildPayload();
    expect(payload.id).toBe(5);
    expect(payload.account.id).toBe(7);
    expect(payload.card.id).toBe(8);
    expect(payload.features[0].id).toBe(3);
  });

  it('marks account number as taken when the API says so', async () => {
    const fixture = TestBed.createComponent(UserFormComponent);
    const comp = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    const accountNumber = comp.form.get('account.number')!;

    // The async validator runs on change; subscribe and flush the debounced GET.
    const done = firstValueFrom(accountNumber.statusChanges);
    accountNumber.setValue('0002'); // exists in the seed
    http.expectOne((req) => req.url === '/users/check' && req.params.get('accountNumber') === '0002')
      .flush({ accountNumberAvailable: false, cardNumberAvailable: true });
    await done;

    expect(accountNumber.hasError('accountTaken')).toBe(true);
    http.verify();
  });

  it('does not mark account number when the API says it is free', async () => {
    const fixture = TestBed.createComponent(UserFormComponent);
    const comp = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    const accountNumber = comp.form.get('account.number')!;

    const done = firstValueFrom(accountNumber.statusChanges);
    accountNumber.setValue('900111');
    http.expectOne((req) => req.url === '/users/check' && req.params.get('accountNumber') === '900111')
      .flush({ accountNumberAvailable: true, cardNumberAvailable: true });
    await done;

    expect(accountNumber.hasError('accountTaken')).toBe(false);
    http.verify();
  });

  it('marks card number as taken when the API says so', async () => {
    const fixture = TestBed.createComponent(UserFormComponent);
    const comp = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    const cardNumber = comp.form.get('card.number')!;

    const done = firstValueFrom(cardNumber.statusChanges);
    cardNumber.setValue('**** **** **** 2201'); // exists in the seed
    http.expectOne((req) => req.url === '/users/check' && req.params.get('cardNumber') === '**** **** **** 2201')
      .flush({ accountNumberAvailable: true, cardNumberAvailable: false });
    await done;

    expect(cardNumber.hasError('cardTaken')).toBe(true);
    http.verify();
  });

  it('does not submit while the form is PENDING (async check in flight)', () => {
    const fixture = TestBed.createComponent(UserFormComponent);
    const comp = fixture.componentInstance;
    const userService = TestBed.inject(UserService);
    comp.isEdit.set(false);
    comp.form.patchValue({
      name: 'Novo',
      account: { number: '0001', agency: '0001', balance: 10, limit: 100 },
      card: { number: 'x', limit: 500 },
    });

    const spy = vi.spyOn(userService, 'create');
    // Force the form into PENDING (as if an async validator were in flight).
    Object.defineProperty(comp.form, 'status', { value: 'PENDING', configurable: true });

    comp.onSubmit();
    expect(spy).not.toHaveBeenCalled();
  });
});
