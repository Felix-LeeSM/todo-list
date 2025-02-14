import { useEffect, useState } from "react";
import type { GroupInterface } from "../type/Group.interface";
import { Plus } from "lucide-react";
import GroupForm from "./GroupForm";
import axios from "axios";
import { GroupCard } from "./GroupCard";
import { useNavigate } from "react-router-dom";

export default function GroupList() {
  const [groups, setGroups] = useState<GroupInterface[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    axios.get<GroupInterface[]>("/api/v1/group").then((response) => {
      setGroups(response.data);
    });
  }, []);

  const addGroup = (group: GroupInterface) =>
    setGroups((groups) => [group, ...groups]);

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-2xl font-bold">Groups</h2>
        <button
          onClick={() => setIsModalOpen(true)}
          className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 transition-colors duration-200 flex items-center"
        >
          <Plus className="w-5 h-5 mr-2" />
          Add Group
        </button>
      </div>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {groups.map((group) => (
          <GroupCard
            group={group}
            key={group.id}
            onClick={() => navigate(`/group/${group.id}`)}
          />
        ))}
      </div>
      {isModalOpen && (
        <GroupForm onSubmit={addGroup} onClose={() => setIsModalOpen(false)} />
      )}
    </div>
  );
}
