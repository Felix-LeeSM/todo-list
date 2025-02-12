import { Link } from "react-router-dom";

export function Task() {
  return (
    <div className="min-h-screen bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <button className="mb-4 bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 transition-colors duration-200">
          <Link to="/group">Back to Groups</Link>
        </button>
        {/* <TaskManager group={selectedGroup} /> */}
      </div>
    </div>
  );
}
