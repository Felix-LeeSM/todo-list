import axios from "axios";
import { useContext, useState } from "react";
import { UserInterface } from "../type/User.interface";
import { AuthContext } from "../context/auth/AuthContext";
import { User, Lock, LoaderCircle } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { LoadingButton } from "./LoadingButton";
import { handleApiError } from "../util/handleApiError";

export function SignInForm() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const { handleSignIn } = useContext(AuthContext);

  const navigate = useNavigate();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    setIsLoading(true);

    const body = { username, password };

    axios
      .post<UserInterface>("/api/v1/user/token/access-token", body)
      .then((res) => handleSignIn(res.data))
      .then(() => setUsername(""))
      .then(() => setPassword(""))
      .then(() => navigate("/group"))
      .catch(handleApiError)
      .finally(() => setIsLoading(false));
  };

  return (
    <form onSubmit={handleSubmit} className="mt-8 space-y-6">
      <div className="rounded-md shadow-sm">
        <div>
          <label htmlFor="username" className="sr-only">
            Username
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <User className="h-5 w-5 text-gray-400" />
            </div>
            <input
              id="username"
              name="username"
              type="text"
              required
              className="appearance-none rounded-none relative block w-full px-3 py-2 pl-10 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </div>
        </div>
        <div>
          <label htmlFor="password" className="sr-only">
            Password
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Lock className="h-5 w-5 text-gray-400" />
            </div>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              required
              className="appearance-none rounded-none relative block w-full px-3 py-2 pl-10 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
        </div>
      </div>

      <div>
        <LoadingButton
          isLoading={isLoading}
          type="submit"
          className="py-2 px-4 w-full"
          childrenWhileLoading={
            <>
              <span className="absolute left-0 inset-y-0 flex items-center pl-3">
                <LoaderCircle className="animate-spin h-5 w-5" />
              </span>
              Signing In...
            </>
          }
        >
          Sign In
        </LoadingButton>
      </div>
    </form>
  );
}
