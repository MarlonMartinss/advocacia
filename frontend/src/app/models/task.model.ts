export interface Task {
  id: number;
  title: string;
  done: boolean;
  createdAt: string;
}

export interface TaskRequest {
  title: string;
  done?: boolean;
}
