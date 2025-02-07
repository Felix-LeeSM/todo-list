import { SignUpForm } from "../component/SignUpForm";

export function SignUp() {
  return (
    <>
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="bg-white p-8 rounded-lg shadow-md w-96">
          <h1 className="text-2xl font-bold mb-6 text-center">
            SignUp for DayArchive
          </h1>
          <SignUpForm />
        </div>
      </div>
    </>
  );
}
