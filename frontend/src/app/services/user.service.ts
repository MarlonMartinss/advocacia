import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UserResponse {
  id: number;
  username: string;
  name: string;
  email: string | null;
  role: string;
  active: boolean;
  createdAt: string;
}

export interface UserRequest {
  username: string;
  password?: string;
  name?: string;
  email?: string;
  role: string;
  active: boolean;
}

export interface ScreenResponse {
  id: number;
  code: string;
  label: string;
  route: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.apiUrl}/users`);
  }

  getUser(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/users/${id}`);
  }

  createUser(user: UserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.apiUrl}/users`, user);
  }

  updateUser(id: number, user: UserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.apiUrl}/users/${id}`, user);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${id}`);
  }

  getScreens(): Observable<ScreenResponse[]> {
    return this.http.get<ScreenResponse[]>(`${this.apiUrl}/screens`);
  }

  getUserScreens(userId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/users/${userId}/screens`);
  }

  updateUserScreens(userId: number, screenCodes: string[]): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/users/${userId}/screens`, screenCodes);
  }
}
