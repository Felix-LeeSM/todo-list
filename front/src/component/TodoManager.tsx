import { DragDropContext } from "@hello-pangea/dnd";
import { useTodos } from "../hooks/useTodos";
import { useTodoDragDrop } from "../hooks/useTodoDragDrop";
import { handleApiError } from "../util/handleApiError";
import { TodoList } from "./TodoList";
import TodoForm from "./TodoForm";
import type { GroupInterface } from "../type/Group.interface";
import { TodoInterface } from "../type/Todo.interface";

interface TodoManagerProps {
  group: GroupInterface;
}

export default function TodoManager({ group }: TodoManagerProps) {
  const { todos, todosByStatus, loading, addTodo, deleteTodo, moveTodo } =
    useTodos(group);
  const { handleDragEnd } = useTodoDragDrop({ todos, todosByStatus, moveTodo });

  const handleAddTodo = async (newTodo: TodoInterface) => {
    try {
      await addTodo(newTodo);
    } catch (error) {
      handleApiError(error);
    }
  };

  const handleDeleteTodo = async (todo: TodoInterface) => {
    try {
      await deleteTodo(todo);
    } catch (error) {
      handleApiError(error);
    }
  };

  return (
    <DragDropContext onDragEnd={handleDragEnd}>
      <div className="bg-white p-6 rounded-lg shadow-lg">
        <h2 className="text-2xl font-bold mb-4">{group.name} - Todos</h2>
        {loading ? (
          <div className="text-center py-8">Loading todos...</div>
        ) : (
          <>
            <div className="grid grid-cols-1 lg:grid-cols-4 md:grid-cols-3 sm:grid-cols-2 gap-4">
              {(["TO_DO", "IN_PROGRESS", "DONE", "ON_HOLD"] as const).map(
                (status) => (
                  <TodoList
                    key={`list-${status}`}
                    todoStatus={status}
                    todos={todosByStatus[status]}
                    onDelete={handleDeleteTodo}
                  />
                )
              )}
            </div>
            <div className="mt-6">
              <h3 className="text-lg font-semibold mb-2">Add New Todo</h3>
              <TodoForm onSubmit={handleAddTodo} />
            </div>
          </>
        )}
      </div>
    </DragDropContext>
  );
}
