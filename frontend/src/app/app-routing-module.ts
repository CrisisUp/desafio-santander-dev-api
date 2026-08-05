import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserListComponent } from './user/user-list/user-list';
import { UserFormComponent } from './user/user-form/user-form';
import { UnsavedChangesGuard } from './user/unsaved-changes.guard';

const routes: Routes = [
  { path: '', redirectTo: '/users', pathMatch: 'full' },
  { path: 'users', component: UserListComponent },
  { path: 'users/new', component: UserFormComponent, canDeactivate: [UnsavedChangesGuard] },
  { path: 'users/:id/edit', component: UserFormComponent, canDeactivate: [UnsavedChangesGuard] },
  { path: '**', redirectTo: '/users' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
