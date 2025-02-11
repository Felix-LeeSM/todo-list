import { createContext } from "react";
import { UserInterface } from "../../type/User.interface";

export interface AuthContextType {
  user?: UserInterface;
  handleSignIn: (user: UserInterface) => void;
  handleLogOut: () => void;
}

export const AuthContext = createContext<AuthContextType>({
  handleSignIn: () => {},
  handleLogOut: () => {},
});
