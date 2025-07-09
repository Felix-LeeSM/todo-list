import { DropResult } from "@hello-pangea/dnd";
import { generateOrderedString } from "../util/order";
import { handleApiError } from "../util/handleApiError";
import type { TodoInterface } from "../type/Todo.interface";
import type { TodoStatus } from "../type/TodoStatus";

interface UseTodoDragDropProps {
  todos: TodoInterface[];
  todosByStatus: Record<TodoStatus, TodoInterface[]>;
  moveTodo: (
    todoId: number,
    newStatus: TodoStatus,
    newOrder: string
  ) => Promise<void>;
}

export function useTodoDragDrop({
  todos,
  todosByStatus,
  moveTodo,
}: UseTodoDragDropProps) {
  const handleDragEnd = async (result: DropResult) => {
    if (!result.destination) return;

    const { destination, draggableId } = result;
    const movedTodo = todos.find((todo) => todo.id.toString() === draggableId);

    if (!movedTodo) return;

    const newStatus = destination.droppableId as TodoStatus;
    const statusTodos = todosByStatus[newStatus];

    const tempStatusTodos = statusTodos.filter(
      (todo) => todo.id.toString() !== draggableId
    );

    let prevTodo: TodoInterface | undefined;
    let nextTodo: TodoInterface | undefined;

    if (destination.index > 0) {
      prevTodo = tempStatusTodos[destination.index - 1];
    }
    if (destination.index < tempStatusTodos.length) {
      nextTodo = tempStatusTodos[destination.index];
    }

    const newOrder = generateOrderedString(prevTodo?.order, nextTodo?.order);

    try {
      await moveTodo(movedTodo.id, newStatus, newOrder);
    } catch (error) {
      handleApiError(error);
    }
  };

  return { handleDragEnd };
}
