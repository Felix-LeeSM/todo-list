import { ReactNode, useContext } from "react";
import { Navigate } from "react-router-dom";
import { AuthContext } from "../context/auth/AuthContext";

export function GuestRoute({ children }: { children: ReactNode }) {
  const { user } = useContext(AuthContext);

  if (user) {
    return <Navigate to="/group" replace />;
  }

  return children;
}
