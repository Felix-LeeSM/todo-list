import type { TodoInterface } from "../type/Todo.interface";
import {
  CheckCircle,
  Circle,
  Clock,
  PlayCircle,
  GripVertical,
  X,
} from "lucide-react";

interface TodoItemProps {
  todo: TodoInterface;
  onDelete: (todo: TodoInterface) => void;
}

export default function TodoItem({ todo, onDelete }: TodoItemProps) {
  const statusColors = {
    TO_DO: "bg-yellow-100 border-yellow-300",
    IN_PROGRESS: "bg-orange-100 border-orange-300",
    DONE: "bg-blue-100 border-blue-300",
    ON_HOLD: "bg-green-100 border-green-300",
  };

  const statusIcons = {
    TO_DO: <Circle className="w-5 h-5 text-yellow-500" />,
    IN_PROGRESS: <Clock className="w-5 h-5 text-orange-500" />,
    DONE: <PlayCircle className="w-5 h-5 text-blue-500" />,
    ON_HOLD: <CheckCircle className="w-5 h-5 text-green-500" />,
  };

  return (
    <div
      className={`p-4 rounded-lg border ${
        statusColors[todo.status]
      } flex items-center`}
    >
      <div className="mr-2 cursor-move">
        <GripVertical className="w-5 h-5 text-gray-400" />
      </div>
      <div className="flex-1 min-w-0">
        <h3 className="font-medium truncate">{todo.title}</h3>
        <p className="text-sm text-gray-600 mt-1">{todo.description}</p>
      </div>
      <div className="flex flex-col self-stretch">
        <button
          type="button"
          className="self-start"
          onClick={() => onDelete(todo)}
        >
          <X className="w-5 h-5" />
        </button>
        <div className="flex-1 flex items-center justify-center">
          {statusIcons[todo.status]}
        </div>
      </div>
    </div>
  );
}
