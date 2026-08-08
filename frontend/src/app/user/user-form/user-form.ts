import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { AbstractControl, AsyncValidatorFn, FormArray, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Observable, debounceTime, distinctUntilChanged, map, of, switchMap } from 'rxjs';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UserService } from '../user.service';
import { User } from '../user';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.html',
  styleUrls: ['./user-form.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
})
export class UserFormComponent implements OnInit {
  form: FormGroup;
  isEdit = signal(false);
  userId = signal<number | null>(null);
  protected readonly PROTECTED_USER_ID = 1;
  saving = signal(false);

  constructor(
    protected fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      // required rejects only empty; whitespace-only is valid to it but the
      // backend's @NotBlank rejects it — so reject whitespace here too.
      name: ['', [Validators.required, this.noWhitespaceValidator]],
      account: this.fb.group({
        id: [null],
        // Async check: the number must be unique across users (debounced).
        number: ['', [Validators.required], [this.uniqueAccountValidator()]],
        agency: ['', Validators.required],
        balance: [0, Validators.required],
        limit: [0, Validators.required],
      }),
      card: this.fb.group({
        id: [null],
        number: ['', [Validators.required], [this.uniqueCardValidator()]],
        limit: [0, Validators.required],
      }),
      features: this.fb.array([]),
      news: this.fb.array([]),
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const userId = Number(id);
      this.isEdit.set(true);
      this.userId.set(userId);
      this.userService.get(userId).subscribe({
        next: (user) => this.fillForm(user),
        error: (err: Error) => {
          this.snackBar.open(err.message, 'Fechar', { duration: 4000 });
          this.router.navigate(['/users']);
        },
      });
    }
  }

  /** Rejects whitespace-only strings (mirrors the backend's @NotBlank). */
  noWhitespaceValidator(control: AbstractControl): ValidationErrors | null {
    if (typeof control.value === 'string' && control.value.trim() === '') {
      return { required: true };
    }
    return null;
  }

  /**
   * Debounced async uniqueness check for the account number. Reports
   * { accountTaken: true } when the number is already used by another user.
   * The backend remains the source of truth (422 backstop on save).
   */
  private uniqueAccountValidator(): AsyncValidatorFn {
    return (control: AbstractControl) => this.checkUnique(control, 'accountNumber');
  }

  /** Same, for the card number. */
  private uniqueCardValidator(): AsyncValidatorFn {
    return (control: AbstractControl) => this.checkUnique(control, 'cardNumber');
  }

  private checkUnique(control: AbstractControl, field: 'accountNumber' | 'cardNumber'): Observable<ValidationErrors | null> {
    const value = (control.value ?? '').trim();
    if (!value) {
      return of(null); // required handles emptiness; don't query the API
    }
    const errorKey = field === 'accountNumber' ? 'accountTaken' : 'cardTaken';
    return of(value).pipe(
      debounceTime(400),
      distinctUntilChanged(),
      switchMap(() =>
        this.userService.checkUniqueness(
          field === 'accountNumber' ? value : undefined,
          field === 'cardNumber' ? value : undefined,
          this.userId() ?? undefined
        )
      ),
      map((res) => {
        const available = field === 'accountNumber' ? res.accountNumberAvailable : res.cardNumberAvailable;
        return available ? null : { [errorKey]: true };
      })
    );
  }

  get features(): FormArray {
    return this.form.get('features') as FormArray;
  }

  get news(): FormArray {
    return this.form.get('news') as FormArray;
  }

  /** Shows a per-field validation message when the control is invalid and touched. */
  showError(control: AbstractControl | null): boolean {
    return !!control && control.invalid && control.touched;
  }

  /** Maps a feature/news description to a registered brand SVG, or null. */
  brandIcon(description: string | null | undefined): string | null {
    const key = (description ?? '').trim().toLowerCase();
    const map: Record<string, string> = {
      pix: 'pix',
      pagar: 'pay',
      pagamentos: 'pay',
      transferir: 'transfer',
      transferência: 'transfer',
      'conta corrente': 'account',
      cartões: 'cards',
      crédito: 'credit',
      investimentos: 'others',
      seguros: 'insurance',
      'seguro casa': 'insurance',
    };
    return map[key] ?? null;
  }

  addFeature(): FormGroup {
    const group = this.fb.group({ id: null, description: '', icon: '' });
    this.features.push(group);
    return group;
  }

  addNews(): FormGroup {
    const group = this.fb.group({ id: null, description: '', icon: '' });
    this.news.push(group);
    return group;
  }

  removeFeature(index: number): void {
    this.features.removeAt(index);
  }

  removeNews(index: number): void {
    this.news.removeAt(index);
  }

  private fillForm(user: User): void {
    // ponytail: patchValue fires the async uniqueness validators, so opening the
    // edit form issues two GET /users/check (account + card) even when nothing
    // changed. Harmless (they return available), but could be skipped by setting
    // the values without triggering validators (e.g. emitEvent: false).
    this.form.patchValue({
      name: user.name,
      account: {
        id: user.account.id ?? null,
        number: user.account.number,
        agency: user.account.agency,
        balance: user.account.balance,
        limit: user.account.limit,
      },
      card: {
        id: user.card.id ?? null,
        number: user.card.number,
        limit: user.card.limit,
      },
    });
    (user.features ?? []).forEach((f) => this.addFeature().patchValue(f));
    (user.news ?? []).forEach((n) => this.addNews().patchValue(n));
  }

  onSubmit(): void {
    // PENDING means an async uniqueness check is still in flight — don't submit
    // until it resolves (else a slow network lets a taken number through to the
    // backend's 422). invalid covers required/whitespace/scale errors.
    if (this.form.invalid || this.form.status === 'PENDING' || this.saving()) {
      return;
    }
    this.saving.set(true);
    const user = this.buildPayload();

    const request =
      this.isEdit() && this.userId() !== null
        ? this.userService.update(this.userId() as number, user)
        : this.userService.create(user);

    request.subscribe({
      next: () => {
        const msg = this.isEdit() ? '✓ Usuário atualizado.' : '✓ Usuário criado.';
        this.snackBar.open(msg, 'OK', { duration: 3000, panelClass: 'snack-success' });
        this.router.navigate(['/users']).then(() => window.scrollTo(0, 0));
      },
      error: (err: Error) => {
        this.saving.set(false);
        this.snackBar.open(err.message, 'Fechar', { duration: 5000 });
      },
    });
  }

  buildPayload(): User {
    const value = this.form.value;
    const isEdit = this.isEdit();

    // On create the children must NOT carry ids (the API rejects pre-persisted ids
    // with 422). On edit they keep their ids so the API can validate ownership.
    const keepIds = isEdit;
    const mapItem = (item: { id?: number; description: string; icon?: string }) => ({
      ...(keepIds && item.id != null ? { id: item.id } : {}),
      description: item.description,
      ...(item.icon ? { icon: item.icon } : {}),
    });

    // On create the children must NOT carry ids (the API rejects pre-persisted ids
    // with 422); on edit they carry the persisted ids so the API validates ownership.
    const account = { number: value.account.number, agency: value.account.agency, balance: value.account.balance, limit: value.account.limit };
    const card = { number: value.card.number, limit: value.card.limit };
    if (isEdit) {
      Object.assign(account, { id: value.account.id });
      Object.assign(card, { id: value.card.id });
    }

    return {
      ...(isEdit && this.userId() !== null ? { id: this.userId() as number } : {}),
      name: value.name,
      account,
      card,
      features: value.features.map(mapItem),
      news: value.news.map(mapItem),
    };
  }
}
