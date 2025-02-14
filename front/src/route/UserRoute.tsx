import { ReactNode, useContext } from "react";
import { Navigate } from "react-router-dom";
import { AuthContext } from "../context/auth/AuthContext";
import { toast } from "react-toastify";

export function UserRoute({ children }: { children: ReactNode }) {
  const { user } = useContext(AuthContext);

  if (!user) {
    toast.warning("You need to sign in first.");
    return <Navigate to="/signin" replace />;
  }

  return children;
}
