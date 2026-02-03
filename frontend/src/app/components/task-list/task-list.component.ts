import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Task, TaskRequest } from '../../models/task.model';
import { TaskService } from '../../services/task.service';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './task-list.component.html',
  styleUrl: './task-list.component.css'
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  newTaskTitle = '';
  isLoading = false;
  error: string | null = null;

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.isLoading = true;
    this.error = null;

    this.taskService.findAll().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar tarefas. Verifique se o backend está rodando.';
        this.isLoading = false;
        console.error('Erro ao carregar tarefas:', err);
      }
    });
  }

  createTask(): void {
    if (!this.newTaskTitle.trim() || this.newTaskTitle.length < 3) {
      this.error = 'O título deve ter pelo menos 3 caracteres.';
      return;
    }

    const taskRequest: TaskRequest = {
      title: this.newTaskTitle.trim(),
      done: false
    };

    this.taskService.create(taskRequest).subscribe({
      next: (task) => {
        this.tasks.unshift(task);
        this.newTaskTitle = '';
        this.error = null;
      },
      error: (err) => {
        this.error = 'Erro ao criar tarefa.';
        console.error('Erro ao criar tarefa:', err);
      }
    });
  }

  toggleDone(task: Task): void {
    this.taskService.toggleDone(task.id).subscribe({
      next: (updatedTask) => {
        const index = this.tasks.findIndex(t => t.id === task.id);
        if (index !== -1) {
          this.tasks[index] = updatedTask;
        }
      },
      error: (err) => {
        this.error = 'Erro ao atualizar tarefa.';
        console.error('Erro ao atualizar tarefa:', err);
      }
    });
  }

  deleteTask(task: Task): void {
    if (!confirm(`Deseja realmente excluir a tarefa "${task.title}"?`)) {
      return;
    }

    this.taskService.delete(task.id).subscribe({
      next: () => {
        this.tasks = this.tasks.filter(t => t.id !== task.id);
      },
      error: (err) => {
        this.error = 'Erro ao excluir tarefa.';
        console.error('Erro ao excluir tarefa:', err);
      }
    });
  }

  get pendingTasks(): number {
    return this.tasks.filter(t => !t.done).length;
  }

  get completedTasks(): number {
    return this.tasks.filter(t => t.done).length;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
