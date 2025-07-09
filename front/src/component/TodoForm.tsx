import { useState } from "react";
import { LoaderCircle } from "lucide-react";
import { LoadingButton } from "./LoadingButton";

export type TodoFormProps = {
  onSubmit: (title: string, description: string) => void;
};

export default function TodoForm({ onSubmit }: TodoFormProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    setIsLoading(true);
    onSubmit(title, description);
    setTitle("");
    setDescription("");
    setIsLoading(false);
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
