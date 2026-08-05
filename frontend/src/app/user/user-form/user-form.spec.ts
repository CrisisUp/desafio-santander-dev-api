import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { UserFormComponent } from './user-form';

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
      ],
      declarations: [UserFormComponent],
    }).compileComponents();
  });

  it('buildPayload on create strips child ids (API rejects pre-persisted ids with 422)', () => {
    const fixture = TestBed.createComponent(UserFormComponent);
    const comp = fixture.componentInstance;
    comp.isEdit = false;
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
    comp.isEdit = true;
    comp.userId = 5;
    comp.form.patchValue({
      name: 'Edit',
      account: { number: '0001', agency: '0001', balance: 10, limit: 100 },
      card: { number: 'x', limit: 500 },
    });
    comp.addFeature();
    comp.features.controls[0].patchValue({ id: 3, description: 'f', icon: '' });

    const payload = comp.buildPayload();
    expect(payload.id).toBe(5);
    expect(payload.features[0].id).toBe(3);
  });
});
