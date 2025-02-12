import { BrowserRouter, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/auth/AuthProvider";
import { SignIn } from "./page/SignIn";
import { SignUp } from "./page/SignUp";
import { UserRoute } from "./route/UserRoute";
import { GuestRoute } from "./route/GuestRoute";
import { Group } from "./page/Group";
import { Home } from "./page/Home";
import { Task } from "./page/Task";

function App() {
  return (
    <>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Home />}></Route>
            <Route
              path="/group"
              element={<UserRoute children={<Group />} />}
            ></Route>
            <Route
              path="/group/:groupId"
              element={<UserRoute children={<Task />} />}
            ></Route>
            <Route
              path="/signin"
              element={<GuestRoute children={<SignIn />} />}
            ></Route>
            <Route
              path="/signup"
              element={<GuestRoute children={<SignUp />} />}
            ></Route>
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </>
  );
}

export default App;
