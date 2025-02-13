import { useContext, useState } from "react";
import { PlusCircle } from "lucide-react";
import { TodoInterface } from "../type/Todo.interface";
import axios from "axios";
import { GroupContext } from "../context/group/GroupContext";

export type TodoFormProps = {
  onSubmit: (todo: TodoInterface) => void;
};

export default function TodoForm({ onSubmit }: TodoFormProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const { group } = useContext(GroupContext);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    axios
      .post<TodoInterface>(`/api/v1/group/${group!.id}/todo`, {
        title,
        description,
      })
      .then((res) => onSubmit(res.data))
      .then(() => setTitle(""))
      .then(() => setDescription(""))
      .catch(() => {});
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
          placeholder="Enter Todo title"
          className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
        />
      </div>
      <div>
        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Enter Todo description"
          required
          className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
          rows={3}
        />
      </div>
      <button
        type="submit"
        className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
      >
        <PlusCircle className="w-5 h-5 mr-2" />
        Add Todo
      </button>
    </form>
  );
}
