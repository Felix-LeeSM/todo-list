import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { groupApi } from "../../services/groupApi";
import { LoaderCircle } from "lucide-react";
import { GroupContext } from "./GroupContext";
import type { GroupInterface } from "../../type/Group.interface";

export function GroupProvider({
  children,
}: {
  children: React.ReactNode;
}): React.ReactElement {
  const [group, setGroup] = useState<GroupInterface>();
  const [isLoading, setIsLoading] = useState(true);

  const { groupId } = useParams<{ groupId: string }>();

  const handleSetGroup = (group?: GroupInterface) => {
    setGroup(group);
  };

  useEffect(() => {
    if (!groupId) return;
    const parsedId = parseInt(groupId, 10);
    if (isNaN(parsedId)) return;

    groupApi
      .getGroupById(parsedId)
      .then((res) => setGroup(res))
      .catch(() => setGroup(undefined))
      .finally(() => setIsLoading(false));
  }, []);

  const contextValue = { group, handleSetGroup };

  return (
    <>
      {isLoading ? (
        <LoaderCircle className="w-10 h-10 mx-auto mt-20 animate-spin" />
      ) : (
        <GroupContext.Provider value={contextValue}>
          {children}
        </GroupContext.Provider>
      )}
    </>
  );
}
