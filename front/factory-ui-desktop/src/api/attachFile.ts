import { api } from "./axios";
import type { AttachFileResponse } from "../features/attachFile/types";

export const uploadFile = async (files: FileList | File[]): Promise<AttachFileResponse[]> => {
  const formData = new FormData();
  
  // The API expects 'files' as the field name
  Array.from(files).forEach((file) => {
    formData.append("files", file);
  });

  const response = await api.post<AttachFileResponse[]>("/attachFile/upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });

  return response.data;
};
