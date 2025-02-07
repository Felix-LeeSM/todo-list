import { createContext } from "react";
import { User } from "../../type/User.interface";

export interface AuthContextType {
  user?: User;
  handleSignIn: (user: User) => void;
  handleLogOut: () => void;
}

export const AuthContext = createContext<AuthContextType>({
  handleSignIn: () => {},
  handleLogOut: () => {},
});
