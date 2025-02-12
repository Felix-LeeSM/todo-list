import { GroupInterface } from "../type/Group.interface";

export interface GroupCardProps {
  group: GroupInterface;
  onClick: (group: GroupInterface) => void;
}

export function GroupCard({ group, onClick }: GroupCardProps) {
  return (
    <div
      className="border rounded-lg p-4 hover:shadow-md transition-shadow duration-200 cursor-pointer"
      onClick={() => onClick(group)}
    >
      <h3 className="text-xl font-semibold mb-2">{group.name}</h3>
      <p className="text-gray-600">{group.description}</p>
    </div>
  );
}
