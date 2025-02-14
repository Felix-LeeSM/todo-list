import { useContext, useState } from "react";
import { LoaderCircle } from "lucide-react";
import type { TodoInterface } from "../type/Todo.interface";
import axios from "axios";
import { GroupContext } from "../context/group/GroupContext";
import { LoadingButton } from "./LoadingButton";
import { handleApiError } from "../util/handleApiError";

export type TodoFormProps = {
  onSubmit: (todo: TodoInterface) => void;
};

export default function TodoForm({ onSubmit }: TodoFormProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const { group } = useContext(GroupContext);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    setIsLoading(true);

    axios
      .post<TodoInterface>(`/api/v1/group/${group!.id}/todo`, {
        title,
        description,
      })
      .then((res) => onSubmit(res.data))
      .then(() => setTitle(""))
      .then(() => setDescription(""))
      .catch(handleApiError)
      .finally(() => setIsLoading(false));
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
          maxLength={30}
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
      <LoadingButton
        isLoading={isLoading}
        type="submit"
        className="py-2 px-4"
        childrenWhileLoading={
          <>
            <span className="absolute left-0 inset-y-0 flex items-center pl-3">
              <LoaderCircle className="animate-spin h-5 w-5" />
            </span>
            Adding Todo...
          </>
        }
      >
        Add Todo
      </LoadingButton>
    </form>
  );
}
