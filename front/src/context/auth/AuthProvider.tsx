import React, { useEffect, useState } from "react";
import { AuthContext } from "./AuthContext";
import axios from "axios";
import { UserInterface } from "../../type/User.interface";

export function AuthProvider({
  children,
}: {
  children: React.ReactNode;
}): React.ReactElement {
  const [user, setUser] = useState<UserInterface>();

  const handleSignIn = (user: UserInterface) => {
    setUser(user);
  };

  const handleLogOut = () => {
    axios
      .delete("/api/v1/user/token")
      .then((res) => res.status === 204 && setUser(undefined));
  };

  useEffect(() => {
    axios
      .get<UserInterface>("/api/v1/user/me")
      .then((res) => setUser(res.data))
      .catch(() => {});
  }, []);

  const contextValue = { user, handleSignIn, handleLogOut };

  return (
    <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>
  );
}
