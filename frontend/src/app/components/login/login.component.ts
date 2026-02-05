import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  credentials: LoginRequest = {
    username: '',
    password: ''
  };

  isLoading = false;
  error: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    // Se já estiver autenticado, redireciona para home
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  onSubmit(): void {
    if (!this.credentials.username || !this.credentials.password) {
      this.error = 'Preencha todos os campos';
      return;
    }

    this.isLoading = true;
    this.error = null;

    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 401 || err.status === 403) {
          this.error = 'Usuário ou senha inválidos';
        } else {
          this.error = 'Erro ao fazer login. Tente novamente.';
        }
        console.error('Erro no login:', err);
      }
    });
  }
}
