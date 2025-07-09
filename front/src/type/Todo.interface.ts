import { TodoStatus } from "./TodoStatus";

export interface TodoInterface {
  id: number;
  title: string;
  description: string;
  status: TodoStatus;
  authorId: number;
  groupId: number;
  order: string;
}

export type CreateTodoRequestDTO = Pick<TodoInterface, "title" | "description">;
