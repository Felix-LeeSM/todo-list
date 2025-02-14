import { Folder, Trash2 } from "lucide-react";
import type { GroupInterface } from "../type/Group.interface";

export interface GroupCardProps {
  group: GroupInterface;
  onSelect: (group: GroupInterface) => void;
  onDelete: (group: GroupInterface) => void;
}

export function GroupCard({ group, onSelect, onDelete }: GroupCardProps) {
  const onDeleteGroup = (e: React.MouseEvent) => {
    e.stopPropagation();
    onDelete(group);
  };
  return (
    <div
      key={group.id}
      className="bg-gray-50 rounded-lg shadow-md hover:shadow-lg transition-all duration-200 overflow-hidden"
    >
      <div
        onClick={() => onSelect(group)}
        className="cursor-pointer p-6"
        tabIndex={0}
        role="button"
        aria-label={`Select ${group.name} group`}
      >
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-3">
            <div className="bg-indigo-100 p-2 rounded-full">
              <Folder className="w-6 h-6 text-indigo-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-800">
              {group.name}
            </h3>
          </div>
          <button
            type="button"
            onClick={onDeleteGroup}
            className="p-2 text-gray-400 hover:text-red-500 transition-colors duration-200"
            aria-label={`Delete ${group.name} group`}
          >
            <Trash2 className="w-5 h-5" />
          </button>
        </div>
        <p className="text-gray-600 mb-4">{group.description}</p>
        <div className="flex justify-end">
          <span className="text-sm text-indigo-600 hover:text-indigo-800 transition-colors duration-200">
            View Tasks →
          </span>
        </div>
      </div>
    </div>
  );
}
