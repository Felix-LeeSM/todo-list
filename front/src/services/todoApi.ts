import axios from "axios";
import type {
  CreateTodoRequestDTO,
  TodoInterface,
} from "../type/Todo.interface";

export const todoApi = {
  getTodos: (groupId: number): Promise<TodoInterface[]> => {
    return axios
      .get<TodoInterface[]>(`/api/v1/group/${groupId}/todo`)
      .then((res) => res.data);
  },

  createTodo: (
    todo: CreateTodoRequestDTO,
    groupId: number
  ): Promise<TodoInterface> => {
    return axios
      .post<TodoInterface>(`/api/v1/group/${groupId}/todo`, todo)
      .then((res) => res.data);
  },

  updateTodo: (
    groupId: number,
    todoId: number,
    todo: TodoInterface
  ): Promise<TodoInterface> => {
    return axios
      .put<TodoInterface>(`/api/v1/group/${groupId}/todo/${todoId}`, todo)
      .then((res) => res.data);
  },

  deleteTodo: (groupId: number, todoId: number): Promise<void> => {
    return axios.delete(`/api/v1/group/${groupId}/todo/${todoId}`);
  },
};
