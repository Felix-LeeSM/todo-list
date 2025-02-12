import React, { useEffect, useState } from "react";
import { AuthContext } from "./AuthContext";
import axios from "axios";
import { UserInterface } from "../../type/User.interface";
import { LoaderCircle } from "lucide-react";

export function AuthProvider({
  children,
}: {
  children: React.ReactNode;
}): React.ReactElement {
  const [user, setUser] = useState<UserInterface>();
  const [isLoading, setIsLoading] = useState(true);

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
      .catch(() => {})
      .finally(() => setIsLoading(false));
  }, []);

  const contextValue = { user, handleSignIn, handleLogOut };

  return (
    <>
      {isLoading ? (
        <LoaderCircle className="w-10 h-10 mx-auto mt-20 animate-spin" />
      ) : (
        <AuthContext.Provider value={contextValue}>
          {children}
        </AuthContext.Provider>
      )}
    </>
  );
}
