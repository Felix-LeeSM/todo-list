import { createContext } from "react";
import type { GroupInterface } from "../../type/Group.interface";

export interface GroupContextType {
  group?: GroupInterface;
  handleSetGroup: (group?: GroupInterface) => void;
}

export const GroupContext = createContext<GroupContextType>({
  handleSetGroup: () => {},
});
