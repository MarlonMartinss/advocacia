import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="app-container">
      <header class="app-header">
        <div class="header-content">
          <h1 class="app-title">📋 Advocacia</h1>
          <p class="app-subtitle">Sistema de Gestão de Tarefas</p>
        </div>
      </header>
      <main class="app-main">
        <router-outlet></router-outlet>
      </main>
      <footer class="app-footer">
        <p>&copy; 2024 Advocacia - Todos os direitos reservados</p>
      </footer>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    .app-header {
      background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
      color: white;
      padding: 2rem;
      text-align: center;
    }

    .header-content {
      max-width: 1200px;
      margin: 0 auto;
    }

    .app-title {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 0.25rem;
    }

    .app-subtitle {
      font-size: 1rem;
      opacity: 0.9;
    }

    .app-main {
      flex: 1;
      padding: 2rem;
      max-width: 1200px;
      margin: 0 auto;
      width: 100%;
    }

    .app-footer {
      background-color: var(--text-primary);
      color: var(--text-light);
      padding: 1rem;
      text-align: center;
      font-size: 0.875rem;
    }
  `]
})
export class AppComponent {
  title = 'advocacia-frontend';
}
