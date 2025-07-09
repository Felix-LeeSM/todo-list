export interface GroupInterface {
  id: number;
  name: string;
  description: string;
}

export type CreateGroupRequestDTO = Omit<GroupInterface, "id">;
