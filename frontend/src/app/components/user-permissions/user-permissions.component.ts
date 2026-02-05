import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService, UserResponse, ScreenResponse } from '../../services/user.service';

@Component({
  selector: 'app-user-permissions',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-permissions.component.html',
  styleUrl: './user-permissions.component.scss'
})
export class UserPermissionsComponent implements OnInit {
  users: UserResponse[] = [];
  screens: ScreenResponse[] = [];
  selectedUser: UserResponse | null = null;
  selectedScreens: Set<string> = new Set();
  isLoading = false;
  saving = false;
  error: string | null = null;
  successMessage: string | null = null;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.error = null;

    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Erro ao carregar usuários.';
        this.isLoading = false;
      }
    });

    this.userService.getScreens().subscribe({
      next: (screens) => this.screens = screens,
      error: () => {}
    });
  }

  selectUser(user: UserResponse): void {
    this.selectedUser = user;
    this.selectedScreens = new Set();
    this.userService.getUserScreens(user.id).subscribe({
      next: (codes) => this.selectedScreens = new Set(codes),
      error: () => {}
    });
  }

  toggleScreen(code: string): void {
    if (this.selectedScreens.has(code)) {
      this.selectedScreens.delete(code);
    } else {
      this.selectedScreens.add(code);
    }
    this.selectedScreens = new Set(this.selectedScreens);
  }

  hasScreen(code: string): boolean {
    return this.selectedScreens.has(code);
  }

  save(): void {
    if (!this.selectedUser) {
      this.error = 'Selecione um usuário primeiro.';
      return;
    }

    this.saving = true;
    this.error = null;
    this.successMessage = null;

    const screenCodes = Array.from(this.selectedScreens);
    console.log('Salvando permissões para usuário:', this.selectedUser.id, 'Telas:', screenCodes);

    this.userService.updateUserScreens(this.selectedUser.id, screenCodes).subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = 'Permissões salvas com sucesso!';
        console.log('Permissões salvas com sucesso');
        // Limpa a mensagem após 3 segundos
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        console.error('Erro ao salvar permissões:', err);
        if (err?.status === 0) {
          this.error = 'Erro de conexão. Verifique se o servidor está rodando.';
        } else {
          this.error = err?.error?.message || 'Erro ao salvar permissões.';
        }
        this.saving = false;
      }
    });
  }
}
