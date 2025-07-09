import axios from "axios";
import type { TodoInterface } from "../type/Todo.interface";

export const todoApi = {
  getTodos: async (groupId: number): Promise<TodoInterface[]> => {
    const response = await axios.get<TodoInterface[]>(
      `/api/v1/group/${groupId}/todo`
    );
    return response.data;
  },

  updateTodo: async (
    groupId: number,
    todoId: number,
    todo: TodoInterface
  ): Promise<TodoInterface> => {
    const response = await axios.put<TodoInterface>(
      `/api/v1/group/${groupId}/todo/${todoId}`,
      todo
    );
    return response.data;
  },

  deleteTodo: async (groupId: number, todoId: number): Promise<void> => {
    await axios.delete(`/api/v1/group/${groupId}/todo/${todoId}`);
  },
};
