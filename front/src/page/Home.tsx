import { ArrowRight, Calendar, CheckCircle, Clock } from "lucide-react";
import { useContext } from "react";
import { Link } from "react-router-dom";
import { AuthContext } from "../context/auth/AuthContext";

export function Home() {
  const { user } = useContext(AuthContext);
  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-500 to-purple-600 text-white">
      <div className="container mx-auto px-4 py-16">
        <header className="text-center mb-16">
          <h1 className="text-5xl font-bold mb-4">Welcome to TodoMaster</h1>
          <p className="text-xl mb-8">
            Manage your todos and schedule with ease and efficiency
          </p>
          <div className="space-x-4">
            {user ? null : (
              <Link
                to="/signin"
                className="bg-white text-indigo-600 hover:bg-indigo-100 font-bold py-3 px-6 rounded-full inline-flex items-center transition duration-300"
              >
                SignIn
                <ArrowRight className="ml-2" />
              </Link>
            )}

            <Link
              to="/group"
              className="bg-transparent hover:bg-white hover:text-indigo-600 font-bold py-3 px-6 rounded-full inline-flex items-center border-2 border-white transition duration-300"
            >
              Go to Dashboard
              <ArrowRight className="ml-2" />
            </Link>
          </div>
        </header>
        <div className="grid md:grid-cols-3 gap-8 text-center">
          <div className="bg-white/10 p-8 rounded-xl backdrop-blur-lg">
            <CheckCircle className="w-12 h-12 mx-auto mb-4 text-green-400" />
            <h2 className="text-2xl font-bold mb-4">
              Efficient Todo Management
            </h2>
            <p>
              Organize and prioritize your todos with our intuitive interface.
            </p>
          </div>
          <div className="bg-white/10 p-8 rounded-xl backdrop-blur-lg">
            <Calendar className="w-12 h-12 mx-auto mb-4 text-yellow-400" />
            <h2 className="text-2xl font-bold mb-4">
              Visual Calendar Integration
            </h2>
            <p>See your todos and events in a clear, visual calendar format.</p>
          </div>
          <div className="bg-white/10 p-8 rounded-xl backdrop-blur-lg">
            <Clock className="w-12 h-12 mx-auto mb-4 text-blue-400" />
            <h2 className="text-2xl font-bold mb-4">Time-Saving Features</h2>
            <p>
              Boost your productivity with our time-saving tools and features.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
