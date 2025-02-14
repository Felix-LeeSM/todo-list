import { TodoInterface } from "../type/Todo.interface";
import { TodoStatus } from "../type/TodoStatus";
import TodoItem from "./TodoItem";

export type TodoItemProps = {
  todos: TodoInterface[];
  todoStatus: TodoStatus;
  onDelete: (todo: TodoInterface) => void;
};

export function TodoList({ todos, todoStatus, onDelete }: TodoItemProps) {
  return (
    <div key={todoStatus} className="bg-gray-100 p-4 rounded-lg">
      <h3 className="text-lg font-semibold mb-2">{todoStatus}</h3>
      <div className="space-y-2">
        {todos
          .filter((todo) => todo.status === todoStatus)
          .map((todo) => (
            <TodoItem key={`item-${todo.id}`} todo={todo} onDelete={onDelete} />
          ))}
      </div>
    </div>
  );
}
