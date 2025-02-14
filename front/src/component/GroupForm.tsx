import { useState } from "react";
import type { GroupInterface } from "../type/Group.interface";
import { LoaderCircle, X } from "lucide-react";
import axios from "axios";
import { LoadingButton } from "./LoadingButton";
import { handleApiError } from "../util/handleApiError";

interface GroupFormProps {
  onSubmit: (group: GroupInterface) => void;
  onClose: () => void;
}

export default function GroupForm({ onSubmit, onClose }: GroupFormProps) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    setIsLoading(true);

    axios
      .post<GroupInterface>("/api/v1/group", { name, description })
      .then((response) => onSubmit(response.data))
      .then(() => onClose())
      .then(() => setName(""))
      .then(() => setDescription(""))
      .catch(handleApiError)
      .finally(() => setIsLoading(false));
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center">
      <div className="bg-white p-6 rounded-lg w-full max-w-md">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-xl font-bold">Add New Group</h3>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700"
          >
            <X className="w-6 h-6" />
          </button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label
              htmlFor="name"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Name
            </label>
            <input
              type="text"
              id="name"
              minLength={5}
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>
          <div className="mb-4">
            <label
              htmlFor="description"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Description
            </label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              rows={3}
              required
            />
          </div>
          <div className="flex justify-end space-x-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors duration-200"
            >
              Cancel
            </button>

            <LoadingButton
              isLoading={isLoading}
              className="py-2 px-4"
              type="submit"
              childrenWhileLoading={
                <>
                  <span className="absolute left-0 inset-y-0 flex items-center pl-3">
                    <LoaderCircle className="animate-spin h-5 w-5" />
                  </span>
                  Creating Group...
                </>
              }
            >
              Create Group
            </LoadingButton>
          </div>
        </form>
      </div>
    </div>
  );
}
