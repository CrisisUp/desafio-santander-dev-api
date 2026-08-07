import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UnsavedChangesGuard } from './user/unsaved-changes.guard';

const routes: Routes = [
  { path: '', redirectTo: '/users', pathMatch: 'full' },
  {
    path: 'users',
    loadComponent: () => import('./user/user-list/user-list').then((m) => m.UserListComponent),
  },
  {
    path: 'users/new',
    canDeactivate: [UnsavedChangesGuard],
    loadComponent: () => import('./user/user-form/user-form').then((m) => m.UserFormComponent),
  },
  {
    path: 'users/:id/edit',
    canDeactivate: [UnsavedChangesGuard],
    loadComponent: () => import('./user/user-form/user-form').then((m) => m.UserFormComponent),
  },
  { path: '**', redirectTo: '/users' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
