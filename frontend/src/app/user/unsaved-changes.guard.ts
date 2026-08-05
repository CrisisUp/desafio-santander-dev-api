import { Injectable } from '@angular/core';
import { CanDeactivate } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { UserFormComponent } from './user-form/user-form';
import { ConfirmDialogComponent } from './confirm-dialog/confirm-dialog';

/**
 * Prompts the user before leaving the form with unsaved changes.
 */
@Injectable({ providedIn: 'root' })
export class UnsavedChangesGuard implements CanDeactivate<UserFormComponent> {
  constructor(private dialog: MatDialog) {}

  canDeactivate(component: UserFormComponent): boolean | Promise<boolean> {
    if (!component.form.dirty) {
      return true;
    }
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Alterações não salvas',
        message: 'Você tem alterações não salvas. Descartar?',
        cancelLabel: 'Voltar',
        confirmLabel: 'Descartar',
      },
    });
    return ref.afterClosed().toPromise().then((confirmed: boolean) => {
      if (confirmed) {
        component.form.markAsPristine();
      }
      return !!confirmed;
    });
  }
}
