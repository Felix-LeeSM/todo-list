import axios, { AxiosResponse } from "axios";
import type {
  SignInRequestDTO,
  SignUpRequestDTO,
  UserInterface,
} from "../type/User.interface";

export const authApi = {
  signIn: (data: SignInRequestDTO): Promise<UserInterface> => {
    return axios
      .post<UserInterface>("/api/v1/auth/sign-in", data)
      .then((res) => res.data);
  },

  signUp: (data: SignUpRequestDTO): Promise<UserInterface> => {
    return axios
      .post<UserInterface>("/api/v1/auth/sign-up", data)
      .then((res) => res.data);
  },

  signOut: (): Promise<AxiosResponse<unknown, unknown>> => {
    return axios.delete("/api/v1/user/token");
  },

  getMe: (): Promise<UserInterface> => {
    return axios.get<UserInterface>("/api/v1/user/me").then((res) => res.data);
  },
};
