import { useEffect, useState } from "react";
import axios from "axios";
import type { GroupInterface } from "../type/Group.interface";
import type { TodoInterface } from "../type/Todo.interface";
import TodoForm from "./TodoForm";
import { TodoList } from "./TodoList";

interface TodoManagerProps {
  group: GroupInterface;
}

export default function TodoManager({ group }: TodoManagerProps) {
  const [todos, setTodos] = useState<TodoInterface[]>([]);

  useEffect(() => {
    axios.get<TodoInterface[]>(`/api/v1/group/${group.id}/todo`).then((res) => {
      setTodos(res.data);
    });
  }, [group.id]);

  const addTodo = (todo: TodoInterface) =>
    setTodos((todos) => [...todos, todo]);
  return (
    <div className="bg-white p-6 rounded-lg shadow-lg">
      <h2 className="text-2xl font-bold mb-4">{group.name} - Tasks</h2>
      <div className="grid grid-cols-4 gap-4">
        {(["TO_DO", "IN_PROGRESS", "DONE", "ON_HOLD"] as const).map(
          (status) => (
            <TodoList
              todoStatus={status}
              todos={todos.filter((todo) => todo.status === status)}
            />
          )
        )}
      </div>
      <div className="mt-6">
        <h3 className="text-lg font-semibold mb-2">Add New Task</h3>
        <TodoForm onSubmit={addTodo} />
      </div>
    </div>
  );
}
