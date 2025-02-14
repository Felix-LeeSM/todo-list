import { BrowserRouter, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/auth/AuthProvider";
import { SignIn } from "./page/SignIn";
import { SignUp } from "./page/SignUp";
import { UserRoute } from "./route/UserRoute";
import { GuestRoute } from "./route/GuestRoute";
import { Group } from "./page/Group";
import { Home } from "./page/Home";
import { Todo } from "./page/Todo";
import { GroupProvider } from "./context/group/GroupProvider";
import { GroupRoute } from "./route/GroupRoute";
import { Flip, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

function App() {
  return (
    <>
      <ToastContainer
        position="bottom-right"
        autoClose={3000}
        newestOnTop={true}
        closeOnClick
        transition={Flip}
      />
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route
              path="/group"
              element={
                <UserRoute>
                  <Group />
                </UserRoute>
              }
            />
            <Route
              path="/group/:groupId"
              element={
                <UserRoute>
                  <GroupProvider>
                    <GroupRoute>
                      <Todo />
                    </GroupRoute>
                  </GroupProvider>
                </UserRoute>
              }
            />
            <Route
              path="/signin"
              element={
                <GuestRoute>
                  <SignIn />
                </GuestRoute>
              }
            />
            <Route
              path="/signup"
              element={
                <GuestRoute>
                  <SignUp />
                </GuestRoute>
              }
            />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </>
  );
}

export default App;
