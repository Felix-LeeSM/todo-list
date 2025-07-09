import axios from "axios";
import type { ErrorInterface } from "../type/Error.interface";
import { toast } from "react-toastify";

export const handleApiError = (err: unknown) => {
  if (axios.isAxiosError<ErrorInterface | null>(err) && err.response)
    toast.error(
      err.response.data?.message ||
        "Something Went Wrong, Please Try Again Later."
    );
  else toast.error("Something Went Wrong, Please Try Again Later.");
};
