import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { UserListComponent } from './components/user-list/user-list.component';
import { UserPermissionsComponent } from './components/user-permissions/user-permissions.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ContratosComponent } from './components/contratos/contratos.component';
import { AuthGuard } from './guards/auth.guard';
import { PermissionGuard } from './guards/permission.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard, PermissionGuard] },
  { path: 'contratos', component: ContratosComponent, canActivate: [AuthGuard, PermissionGuard] },
  { path: 'usuarios', component: UserListComponent, canActivate: [AuthGuard, PermissionGuard] },
  { path: 'permissoes-usuarios', component: UserPermissionsComponent, canActivate: [AuthGuard, PermissionGuard] },
  { path: '**', redirectTo: '/dashboard' }
];
