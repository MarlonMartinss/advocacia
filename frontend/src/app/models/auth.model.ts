export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  name: string;
  role: string;
  allowedScreens?: string[];
}

export interface User {
  id: number;
  username: string;
  name: string;
  role: string;
  allowedScreens?: string[];
}
