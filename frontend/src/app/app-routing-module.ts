import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UnsavedChangesGuard } from './user/unsaved-changes.guard';
import { AuthGuard } from './auth/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: '/users', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login').then((m) => m.LoginComponent),
  },
  {
    path: 'users',
    canActivate: [AuthGuard],
    loadComponent: () => import('./user/user-list/user-list').then((m) => m.UserListComponent),
  },
  {
    path: 'users/new',
    canActivate: [AuthGuard],
    canDeactivate: [UnsavedChangesGuard],
    loadComponent: () => import('./user/user-form/user-form').then((m) => m.UserFormComponent),
  },
  {
    path: 'users/:id/edit',
    canActivate: [AuthGuard],
    canDeactivate: [UnsavedChangesGuard],
    loadComponent: () => import('./user/user-form/user-form').then((m) => m.UserFormComponent),
  },
  {
    path: 'users/:id/transactions',
    canActivate: [AuthGuard],
    loadComponent: () => import('./user/transaction-list/transaction-list').then((m) => m.TransactionListComponent),
  },
  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    loadComponent: () => import('./dashboard/dashboard').then((m) => m.DashboardComponent),
  },
  { path: '**', redirectTo: '/users' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
