import { useEffect, useState } from "react";
import axios from "axios";
import type { GroupInterface } from "../type/Group.interface";
import type { TodoInterface } from "../type/Todo.interface";
import TodoForm from "./TodoForm";
import { TodoList } from "./TodoList";
import { DragDropContext, DropResult } from "@hello-pangea/dnd";
import { TodoStatus } from "../type/TodoStatus";
import { ErrorInterface } from "../type/Error.interface";
import { toast } from "react-toastify";

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

  // 변경: onDragEnd 함수 추가 (드래그 완료 시 todo 상태 업데이트)
  const onDragEnd = (result: DropResult) => {
    if (!result.destination) return;

    const { source, destination, draggableId } = result;
    if (source.droppableId === destination.droppableId) return;

    const todo = todos.find((todo) => todo.id.toString() === draggableId);

    if (!todo) return;

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
      .catch(
        (err) =>
          axios.isAxiosError<ErrorInterface>(err) &&
          err.response &&
          toast.error(err.response.data.message)
      );
  };

  return (
    <DragDropContext onDragEnd={onDragEnd}>
      <div className="bg-white p-6 rounded-lg shadow-lg">
        <h2 className="text-2xl font-bold mb-4">{group.name} - Todos</h2>
        <div className="grid grid-cols-4 gap-4">
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
