import GroupList from "../component/GroupList";

export function Group() {
  // const { handleLogOut } = useContext(AuthContext);
  // return (
  // <div className="min-h-screen flex flex-col items-center justify-center bg-gray-100">
  //   <div className="flex justify-end">
  //     <button
  //       type="button"
  //       className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
  //       onClick={() => handleLogOut()}
  //     >
  //       LogOut
  //     </button>
  //   </div>
  //   <h1>Dashboard</h1>
  // </div>

  return (
    <div className="min-h-screen bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <GroupList />
      </div>
    </div>
  );
}
