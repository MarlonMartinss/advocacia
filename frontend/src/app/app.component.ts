import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, RouterLink, RouterLinkActive],
  template: `
    <!-- Layout sem autenticação -->
    <ng-container *ngIf="!authService.isAuthenticated()">
      <router-outlet></router-outlet>
    </ng-container>

    <!-- Layout autenticado: Sidebar + Header + Main -->
    <div class="layout" *ngIf="authService.isAuthenticated()">
      <!-- Sidebar -->
      <aside class="sidebar" [class.collapsed]="sidebarCollapsed">
        <div class="sidebar-logo">
          <svg class="logo-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7v2h20V7L12 2z" fill="currentColor"/>
            <path d="M4 11v9a1 1 0 001 1h14a1 1 0 001-1v-9H4zm3 7v-5h2v5H7zm4 0v-5h2v5h-2zm4 0v-5h2v5h-2z" fill="currentColor"/>
          </svg>
        </div>
        <nav class="sidebar-nav">
          <a routerLink="/dashboard" routerLinkActive="active" class="nav-item" (click)="closeSidebarIfMobile()" *ngIf="authService.hasScreenAccess('dashboard')">
            <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7" rx="1"/>
              <rect x="14" y="3" width="7" height="7" rx="1"/>
              <rect x="3" y="14" width="7" height="7" rx="1"/>
              <rect x="14" y="14" width="7" height="7" rx="1"/>
            </svg>
            <span class="nav-label">Dashboard</span>
          </a>
          <a routerLink="/contratos" routerLinkActive="active" class="nav-item" (click)="closeSidebarIfMobile()" *ngIf="authService.hasScreenAccess('contratos')">
            <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
              <polyline points="14,2 14,8 20,8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <line x1="10" y1="9" x2="8" y2="9"/>
            </svg>
            <span class="nav-label">Contratos</span>
          </a>
          <a routerLink="/permissoes-usuarios" routerLinkActive="active" class="nav-item" (click)="closeSidebarIfMobile()" *ngIf="authService.hasScreenAccess('permissoes')">
            <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2a5 5 0 015 5v3H7V7a5 5 0 015-5z"/>
              <rect x="3" y="10" width="18" height="12" rx="2"/>
              <circle cx="12" cy="16" r="1"/>
            </svg>
            <span class="nav-label">Permissões usuários</span>
          </a>
          <a routerLink="/usuarios" routerLinkActive="active" class="nav-item" (click)="closeSidebarIfMobile()" *ngIf="authService.hasScreenAccess('usuarios')">
            <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 00-3-3.87"/>
              <path d="M16 3.13a4 4 0 010 7.75"/>
            </svg>
            <span class="nav-label">Usuários</span>
          </a>
        </nav>
      </aside>

      <!-- Overlay no mobile: fecha sidebar ao clicar fora -->
      <div class="sidebar-overlay" *ngIf="isMobile && !sidebarCollapsed" (click)="closeSidebar()" role="button" tabindex="0" aria-label="Fechar menu"></div>

      <!-- Main content area -->
      <div class="main-wrapper">
        <!-- Header -->
        <header class="header">
          <div class="header-left">
            <button class="hamburger" (click)="toggleSidebar()">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="3" y1="6" x2="21" y2="6"/>
                <line x1="3" y1="12" x2="21" y2="12"/>
                <line x1="3" y1="18" x2="21" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="header-right">
            <div class="user-dropdown">
              <button class="user-btn" (click)="toggleUserMenu()">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                <svg class="chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="6,9 12,15 18,9"/>
                </svg>
              </button>
              <div class="dropdown-menu" *ngIf="userMenuOpen">
                <div class="dropdown-header">
                  <span>{{ (authService.currentUser$ | async)?.name }}</span>
                </div>
                <button class="dropdown-item" (click)="logout()">Sair</button>
              </div>
            </div>
          </div>
        </header>

        <!-- Main content -->
        <main class="main-content">
          <router-outlet></router-outlet>
        </main>
      </div>

      <!-- Bottom Navigation (mobile only) -->
      <nav class="bottom-nav" *ngIf="isMobile">
        <a routerLink="/dashboard" routerLinkActive="active" class="bottom-nav-item" *ngIf="authService.hasScreenAccess('dashboard')">
          <svg class="bottom-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="7" height="7" rx="1"/>
            <rect x="14" y="3" width="7" height="7" rx="1"/>
            <rect x="3" y="14" width="7" height="7" rx="1"/>
            <rect x="14" y="14" width="7" height="7" rx="1"/>
          </svg>
          <span>Dashboard</span>
        </a>
        <a routerLink="/contratos" routerLinkActive="active" class="bottom-nav-item" *ngIf="authService.hasScreenAccess('contratos')">
          <svg class="bottom-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
            <polyline points="14,2 14,8 20,8"/>
          </svg>
          <span>Contratos</span>
        </a>
        <a routerLink="/permissoes-usuarios" routerLinkActive="active" class="bottom-nav-item" *ngIf="authService.hasScreenAccess('permissoes')">
          <svg class="bottom-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2a5 5 0 015 5v3H7V7a5 5 0 015-5z"/>
            <rect x="3" y="10" width="18" height="12" rx="2"/>
          </svg>
          <span>Permissões</span>
        </a>
        <a routerLink="/usuarios" routerLinkActive="active" class="bottom-nav-item" *ngIf="authService.hasScreenAccess('usuarios')">
          <svg class="bottom-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
          </svg>
          <span>Usuários</span>
        </a>
      </nav>
    </div>
  `,
  styles: [`
    /* Layout container */
    .layout {
      display: flex;
      min-height: 100vh;
      background: var(--bg-secondary);
    }

    /* Sidebar */
    .sidebar {
      width: 240px;
      background: #1a1a1a;
      border-right: 1px solid var(--border);
      display: flex;
      flex-direction: column;
      flex-shrink: 0;
      transition: width 0.2s ease;
    }

    .sidebar.collapsed {
      width: 64px;
    }

    .sidebar.collapsed .nav-label {
      display: none;
    }

    .sidebar-logo {
      padding: 1.5rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .logo-icon {
      width: 40px;
      height: 40px;
      color: var(--gold);
    }

    .sidebar-nav {
      padding: 1rem 0;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .nav-item {
      position: relative;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      min-height: 48px;
      height: 48px;
      padding: 0 1.25rem;
      color: var(--text-secondary);
      text-decoration: none;
      font-size: 0.875rem;
      transition: all 0.15s ease;
      box-sizing: border-box;
    }

    .nav-item:hover {
      color: var(--text-primary);
      background: rgba(255,255,255,0.05);
    }

    .nav-item.active {
      color: var(--gold);
      background: rgba(201, 169, 98, 0.1);
    }

    .nav-item.active::before {
      content: '';
      position: absolute;
      top: 0;
      bottom: 0;
      left: 0;
      width: 3px;
      background: var(--gold);
    }

    .nav-icon {
      width: 20px;
      height: 20px;
      flex-shrink: 0;
    }

    .nav-label {
      white-space: nowrap;
    }

    /* Main wrapper */
    .main-wrapper {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-width: 0;
    }

    /* Header */
    .header {
      height: 60px;
      background: #1a1a1a;
      border-bottom: 1px solid var(--border);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 1.5rem;
      flex-shrink: 0;
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .hamburger {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: transparent;
      border: none;
      color: var(--text-secondary);
      cursor: pointer;
      border-radius: var(--radius-md);
      transition: all 0.15s ease;
    }

    .hamburger:hover {
      color: var(--text-primary);
      background: rgba(255,255,255,0.05);
    }

    .hamburger svg {
      width: 20px;
      height: 20px;
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    /* User dropdown */
    .user-dropdown {
      position: relative;
    }

    .user-btn {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem;
      background: transparent;
      border: 1px solid var(--border);
      color: var(--text-secondary);
      cursor: pointer;
      border-radius: var(--radius-md);
      transition: all 0.15s ease;
    }

    .user-btn:hover {
      color: var(--text-primary);
      border-color: var(--gold);
    }

    .user-btn svg {
      width: 20px;
      height: 20px;
    }

    .user-btn .chevron {
      width: 16px;
      height: 16px;
    }

    .dropdown-menu {
      position: absolute;
      top: calc(100% + 0.5rem);
      right: 0;
      min-width: 180px;
      max-width: calc(100vw - 2rem);
      background: #1a1a1a;
      border: 1px solid var(--border);
      border-radius: var(--radius-md);
      box-shadow: var(--shadow-lg);
      z-index: 100;
      overflow: hidden;
    }

    .dropdown-header {
      padding: 0.75rem 1rem;
      border-bottom: 1px solid var(--border);
      color: var(--text-primary);
      font-size: 0.875rem;
      font-weight: 500;
    }

    .dropdown-item {
      display: block;
      width: 100%;
      padding: 0.75rem 1rem;
      background: transparent;
      border: none;
      color: var(--text-secondary);
      font-size: 0.875rem;
      text-align: left;
      cursor: pointer;
      transition: all 0.15s ease;
    }

    .dropdown-item:hover {
      color: var(--text-primary);
      background: rgba(255,255,255,0.05);
    }

    /* Main content */
    .main-content {
      flex: 1;
      padding: 1.25rem 1.5rem 1.5rem 1.5rem;
      overflow-y: auto;
      background: linear-gradient(180deg, #141414 0%, #0f0f0f 100%);
    }

    /* Overlay no mobile (atrás da sidebar, fecha ao clicar) - só é renderizado quando isMobile && !sidebarCollapsed */
    .sidebar-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.5);
      z-index: 199;
      cursor: pointer;
    }

    /* Bottom Navigation (mobile only) */
    .bottom-nav {
      display: none;
    }

    /* Responsive */
    @media (max-width: 768px) {
      /* Esconder sidebar completamente no mobile */
      .sidebar {
        display: none;
      }

      .sidebar-overlay {
        display: none;
      }

      /* Esconder hamburger no mobile (não há sidebar para abrir) */
      .hamburger {
        display: none;
      }

      .header {
        padding: 0 1rem;
      }

      .main-content {
        padding: 1rem;
        padding-bottom: 80px; /* Espaço para a bottom nav */
      }

      .dropdown-menu {
        left: 1rem;
        right: 1rem;
        max-width: none;
      }

      /* Mostrar Bottom Navigation no mobile */
      .bottom-nav {
        display: flex;
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        height: 64px;
        background: #1a1a1a;
        border-top: 1px solid var(--border);
        z-index: 100;
        padding: 0;
        padding-bottom: env(safe-area-inset-bottom, 0);
      }

      .bottom-nav-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 0.25rem;
        color: var(--text-secondary);
        text-decoration: none;
        font-size: 0.625rem;
        font-weight: 500;
        transition: color 0.15s ease;
        padding: 0.5rem 0.25rem;
        min-width: 0;
      }

      .bottom-nav-item:hover {
        color: var(--text-primary);
      }

      .bottom-nav-item.active {
        color: var(--gold);
      }

      .bottom-nav-icon {
        width: 22px;
        height: 22px;
        flex-shrink: 0;
      }

      .bottom-nav-item span {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 100%;
      }
    }
  `]
})
export class AppComponent implements OnInit {
  title = 'advocacia-frontend';
  sidebarCollapsed = false;
  userMenuOpen = false;
  isMobile = false;

  constructor(public authService: AuthService) {}

  ngOnInit(): void {
    this.onResize();
    if (this.isMobile) {
      this.sidebarCollapsed = true;
    }
  }

  @HostListener('window:resize')
  onResize(): void {
    this.isMobile = typeof window !== 'undefined' && window.innerWidth <= 768;
    if (this.isMobile) {
      this.sidebarCollapsed = true;
    }
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  closeSidebar(): void {
    this.sidebarCollapsed = true;
  }

  closeSidebarIfMobile(): void {
    if (this.isMobile) {
      this.sidebarCollapsed = true;
    }
  }

  toggleUserMenu(): void {
    this.userMenuOpen = !this.userMenuOpen;
  }

  logout(): void {
    this.userMenuOpen = false;
    this.authService.logout();
  }
}
