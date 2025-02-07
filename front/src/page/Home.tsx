import { Link } from "react-router-dom";

export function Home() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-100">
      <h1 className="text-4xl font-bold mb-8">Welcome to DayArchive</h1>
      <p className="text-xl mb-8">Manage your tasks and schedule efficiently</p>
      <div className="space-x-4">
        <Link
          to="/signin"
          className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
        >
          SignIn
        </Link>
        <Link
          to="/dashboard"
          className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
        >
          Go to Dashboard
        </Link>
      </div>
    </div>
  );
}
