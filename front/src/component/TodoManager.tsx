import { useEffect, useState } from "react";
import axios from "axios";
import type { GroupInterface } from "../type/Group.interface";
import type { TodoInterface } from "../type/Todo.interface";
import TodoForm from "./TodoForm";
import { TodoList } from "./TodoList";
import { DragDropContext, DropResult } from "@hello-pangea/dnd";
import { TodoStatus } from "../type/TodoStatus";
import { handleApiError } from "../util/handleApiError";

interface TodoManagerProps {
  group: GroupInterface;
}

export default function TodoManager({ group }: TodoManagerProps) {
  const [todos, setTodos] = useState<TodoInterface[]>([]);

  const deleteTodo = (todo: TodoInterface) => {
    axios
      .delete(`/api/v1/group/${group.id}/todo/${todo.id}`)
      .then(() => setTodos((todos) => todos.filter((t) => t.id !== todo.id)));
  };

  useEffect(() => {
    axios.get<TodoInterface[]>(`/api/v1/group/${group.id}/todo`).then((res) => {
      setTodos(res.data);
    });
  }, [group.id]);

  const addTodo = (todo: TodoInterface) =>
    setTodos((todos) => [...todos, todo]);

  const onDragEnd = (result: DropResult) => {
    if (!result.destination) return;

    const { source, destination, draggableId } = result;
    if (source.droppableId === destination.droppableId) return;

    const todo = todos.find((todo) => todo.id.toString() === draggableId);

    if (!todo) return;

    setTodos((todos) =>
      todos.map((todo) =>
        todo.id.toString() === draggableId
          ? { ...todo, status: destination.droppableId as TodoStatus }
          : todo
      )
    );

    axios
      .put<TodoInterface>(`/api/v1/group/${group.id}/todo/${draggableId}`, {
        ...todo,
        status: destination.droppableId as TodoStatus,
      })
      .then((res) =>
        setTodos((todos) =>
          todos.map((todo) => (todo.id === res.data.id ? res.data : todo))
        )
      )
      .catch((err) => {
        handleApiError(err);
        setTodos(todos);
      });
  };

  return (
    <DragDropContext onDragEnd={onDragEnd}>
      <div className="bg-white p-6 rounded-lg shadow-lg">
        <h2 className="text-2xl font-bold mb-4">{group.name} - Todos</h2>
        <div className="grid grid-cols-1 lg:grid-cols-4 md:grid-cols-3 sm:grid-cols-2 gap-4">
          {(["TO_DO", "IN_PROGRESS", "DONE", "ON_HOLD"] as const).map(
            (status) => (
              <TodoList
                key={`list-${status}`}
                todoStatus={status}
                todos={todos.filter((todo) => todo.status === status)}
                onDelete={deleteTodo}
              />
            )
          )}
        </div>
        <div className="mt-6">
          <h3 className="text-lg font-semibold mb-2">Add New Todo</h3>
          <TodoForm onSubmit={addTodo} />
        </div>
      </div>
    </DragDropContext>
  );
}
