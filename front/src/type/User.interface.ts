export interface UserInterface {
  id: number;
  username: string;
  nickname: string;
}

export type SignUpRequestDTO = Pick<UserInterface, "username" | "nickname"> & {
  password: string;
  confirmPassword: string;
};

export type SignInRequestDTO = Pick<UserInterface, "username"> & {
  password: string;
};
