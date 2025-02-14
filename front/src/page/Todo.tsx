import { Link } from "react-router-dom";
import TodoManager from "../component/TodoManager";
import { useContext } from "react";
import { GroupContext } from "../context/group/GroupContext";

export function Todo() {
  const { group } = useContext(GroupContext);
  return (
    <div className="min-h-screen bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <button className="mb-4 bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-700 transition-colors duration-200">
          <Link to="/group" replace={true}>
            Back to Groups
          </Link>
        </button>
        <TodoManager group={group!} />
      </div>
    </div>
  );
}
