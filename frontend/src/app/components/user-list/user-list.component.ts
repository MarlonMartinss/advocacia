import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, UserResponse, UserRequest } from '../../services/user.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.scss'
})
export class UserListComponent implements OnInit {
  users: UserResponse[] = [];
  isLoading = false;
  isSaving = false;
  error: string | null = null;
  showModal = false;
  showDeleteModal = false;
  userToDelete: UserResponse | null = null;
  editingUser: UserResponse | null = null;
  form: UserRequest = this.getEmptyForm();

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.error = null;
    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err, 'Erro ao carregar usuários.');
        this.isLoading = false;
      }
    });
  }

  openCreate(): void {
    this.editingUser = null;
    this.form = {
      username: '',
      password: '',
      name: '',
      email: '',
      role: 'USER',
      active: true
    };
    this.showModal = true;
  }

  openEdit(user: UserResponse): void {
    this.editingUser = user;
    this.form = {
      username: user.username,
      name: user.name,
      email: user.email ?? '',
      role: user.role,
      active: user.active
    };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.editingUser = null;
    this.form = this.getEmptyForm();
  }

  save(): void {
    if (!this.form.username?.trim()) {
      this.error = 'Nome de usuário é obrigatório.';
      return;
    }
    if (!this.editingUser && (!this.form.password || this.form.password.length < 4)) {
      this.error = 'Senha deve ter pelo menos 4 caracteres.';
      return;
    }

    this.error = null;
    this.isSaving = true;

    const username = this.form.username?.trim() || '';
    const password = this.form.password?.trim();
    const payload: UserRequest = {
      username,
      name: username,
      role: this.form.role || 'USER',
      active: this.form.active ?? true
    };
    if (this.editingUser) {
      if (password) payload.password = password;
    } else {
      payload.password = password || undefined;
    }

    if (this.editingUser) {
      this.userService.updateUser(this.editingUser.id, payload).subscribe({
        next: () => {
          this.loadUsers();
          this.closeModal();
          this.isSaving = false;
        },
        error: (err) => {
          this.error = this.extractErrorMessage(err, 'Erro ao atualizar usuário.');
          this.isSaving = false;
        }
      });
    } else {
      this.userService.createUser(payload).subscribe({
        next: () => {
          this.loadUsers();
          this.closeModal();
          this.isSaving = false;
        },
        error: (err) => {
          this.error = this.extractErrorMessage(err, 'Erro ao criar usuário.');
          this.isSaving = false;
        }
      });
    }
  }

  confirmDelete(user: UserResponse): void {
    this.userToDelete = user;
    this.showDeleteModal = true;
  }

  cancelDelete(): void {
    this.showDeleteModal = false;
    this.userToDelete = null;
  }

  executeDelete(): void {
    if (!this.userToDelete) return;
    const id = this.userToDelete.id;
    this.userService.deleteUser(id).subscribe({
      next: () => {
        this.cancelDelete();
        this.loadUsers();
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err, 'Erro ao excluir usuário.');
        this.cancelDelete();
      }
    });
  }

  private getEmptyForm(): UserRequest {
    return {
      username: '',
      password: '',
      name: '',
      email: '',
      role: 'USER',
      active: true
    };
  }

  private extractErrorMessage(err: any, fallback: string): string {
    // Erro de rede (sem conexão com backend)
    if (err?.status === 0) {
      return 'Erro de conexão. Verifique se o servidor está rodando.';
    }
    // Erro de validação (400 com fieldErrors)
    const fieldErrors = err?.error?.fieldErrors;
    if (fieldErrors && typeof fieldErrors === 'object') {
      const messages = Object.entries(fieldErrors).map(([campo, msg]) => `${campo}: ${msg}`);
      return messages.length > 0 ? messages.join(' ') : (err?.error?.message || fallback);
    }
    // Erro do backend com mensagem
    if (err?.error?.message) {
      return err.error.message;
    }
    // Erro HTTP padrão
    if (err?.message) {
      return err.message;
    }
    // Fallback
    return fallback;
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }
}
