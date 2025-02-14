import React from "react";

export type LoadingButtonProps = {
  isLoading: boolean;
  children?: React.ReactNode;
  childrenWhileLoading?: React.ReactNode;
  className?: string;
  type?: "button" | "reset" | "submit";
};

export function LoadingButton({
  isLoading,
  children,
  childrenWhileLoading,
  className,
  type,
}: LoadingButtonProps) {
  return (
    <button
      type={type}
      className={`
  relative flex justify-center border border-transparent text-sm font-medium rounded-md text-white
  bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500
  transition-colors duration-150 ease-in-out
  ${isLoading ? "bg-indigo-400 cursor-not-allowed" : ""}
  ${className}
`}
      disabled={isLoading}
    >
      {isLoading ? childrenWhileLoading : children}
    </button>
  );
}
