import { useMutation } from "@tanstack/react-query";
import { uploadFile } from "../../../api/attachFile";

export const useUploadFile = () => {
  return useMutation({
    mutationFn: uploadFile,
    onError: (error) => {
      console.error("File upload failed:", error);
    },
  });
};
