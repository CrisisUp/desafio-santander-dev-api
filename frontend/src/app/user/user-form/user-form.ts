import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from '../user.service';
import { User } from '../user';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.html',
  styleUrls: ['./user-form.css'],
  standalone: false,
})
export class UserFormComponent implements OnInit {
  form: FormGroup;
  isEdit = false;
  userId: number | null = null;
  protected readonly PROTECTED_USER_ID = 1;
  saving = false;

  constructor(
    protected fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      account: this.fb.group({
        number: ['', Validators.required],
        agency: ['', Validators.required],
        balance: [0, Validators.required],
        limit: [0, Validators.required],
      }),
      card: this.fb.group({
        number: ['', Validators.required],
        limit: [0, Validators.required],
      }),
      features: this.fb.array([]),
      news: this.fb.array([]),
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.userId = Number(id);
      this.userService.get(this.userId).subscribe({
        next: (user) => this.fillForm(user),
        error: (err: Error) => {
          this.snackBar.open(err.message, 'Fechar', { duration: 4000 });
          this.router.navigate(['/users']);
        },
      });
    }
  }

  get features(): FormArray {
    return this.form.get('features') as FormArray;
  }

  get news(): FormArray {
    return this.form.get('news') as FormArray;
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
    this.form.patchValue({
      name: user.name,
      account: {
        number: user.account.number,
        agency: user.account.agency,
        balance: user.account.balance,
        limit: user.account.limit,
      },
      card: {
        number: user.card.number,
        limit: user.card.limit,
      },
    });
    (user.features ?? []).forEach((f) => this.addFeature().patchValue(f));
    (user.news ?? []).forEach((n) => this.addNews().patchValue(n));
  }

  onSubmit(): void {
    if (this.form.invalid || this.saving) {
      return;
    }
    this.saving = true;
    const user = this.buildPayload();

    const request =
      this.isEdit && this.userId !== null
        ? this.userService.update(this.userId, user)
        : this.userService.create(user);

    request.subscribe({
      next: () => {
        this.snackBar.open(
          this.isEdit ? 'Usuário atualizado.' : 'Usuário criado.',
          'OK',
          { duration: 3000 }
        );
        this.router.navigate(['/users']);
      },
      error: (err: Error) => {
        this.saving = false;
        this.snackBar.open(err.message, 'Fechar', { duration: 5000 });
      },
    });
  }

  buildPayload(): User {
    const value = this.form.value;

    // On create the children must NOT carry ids (the API rejects pre-persisted ids
    // with 422). On edit they keep their ids so the API can validate ownership.
    const keepIds = this.isEdit;
    const mapItem = (item: { id?: number; description: string; icon?: string }) => ({
      ...(keepIds && item.id != null ? { id: item.id } : {}),
      description: item.description,
      ...(item.icon ? { icon: item.icon } : {}),
    });

    return {
      ...(this.isEdit && this.userId !== null ? { id: this.userId } : {}),
      name: value.name,
      account: { number: value.account.number, agency: value.account.agency, balance: value.account.balance, limit: value.account.limit },
      card: { number: value.card.number, limit: value.card.limit },
      features: value.features.map(mapItem),
      news: value.news.map(mapItem),
    };
  }
}
