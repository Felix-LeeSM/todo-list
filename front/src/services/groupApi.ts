import axios from "axios";
import { CreateGroupRequestDTO, GroupInterface } from "../type/Group.interface";

export const groupApi = {
  getGroups: () => {
    return axios.get<GroupInterface[]>("/api/v1/group").then((res) => res.data);
  },

  createGroup: (data: CreateGroupRequestDTO) => {
    return axios
      .post<GroupInterface>("/api/v1/group", data)
      .then((res) => res.data);
  },

  deleteGroup: (groupId: number) => {
    return axios.delete(`/api/v1/group/${groupId}`);
  },

  getGroupById: (groupId: number) => {
    return axios
      .get<GroupInterface>(`/api/v1/group/${groupId}`)
      .then((res) => res.data);
  },
};
