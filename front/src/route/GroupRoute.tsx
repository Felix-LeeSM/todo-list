import { useContext } from "react";
import { Navigate } from "react-router-dom";
import { GroupContext } from "../context/group/GroupContext";

export function GroupRoute({ children }: { children: JSX.Element }) {
  const { group } = useContext(GroupContext);

  if (!group) {
    return <Navigate to="/group" replace />;
  }

  return children;
}
