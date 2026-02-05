import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

const ROUTE_TO_SCREEN: Record<string, string> = {
  'dashboard': 'dashboard',
  'contratos': 'contratos',
  'usuarios': 'usuarios',
  'permissoes-usuarios': 'permissoes'
};

@Injectable({
  providedIn: 'root'
})
export class PermissionGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return false;
    }

    const path = route.routeConfig?.path ?? '';
    const screenCode = ROUTE_TO_SCREEN[path];

    if (!screenCode) {
      return true;
    }

    if (this.authService.hasScreenAccess(screenCode)) {
      return true;
    }

    const user = this.authService.getCurrentUser();
    const hasAnyScreen = user?.allowedScreens && user.allowedScreens.length > 0;
    if (!hasAnyScreen && screenCode === 'dashboard') {
      return true;
    }

    this.router.navigate(['/dashboard']);
    return false;
  }
}
