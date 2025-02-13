import GroupList from "../component/GroupList";

export function Group() {
  return (
    <div className="min-h-screen bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <GroupList />
      </div>
    </div>
  );
}
