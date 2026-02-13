import { useState } from "react";
import { useUploadFile } from "../hooks/useUploadFile";

export default function FileUpload() {
  const [selectedFiles, setSelectedFiles] = useState<FileList | null>(null);
  const { mutate: upload, isPending, isSuccess, data, error } = useUploadFile();

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setSelectedFiles(e.target.files);
    }
  };

  const handleUpload = () => {
    if (selectedFiles) {
      upload(selectedFiles);
    }
  };

  return (
    <div className="p-4 border rounded shadow-sm">
      <h3 className="text-lg font-bold mb-4">File Upload</h3>
      
      <div className="flex flex-col gap-4">
        <input 
          type="file" 
          multiple 
          onChange={handleFileChange} 
          className="block w-full text-sm text-slate-500
            file:mr-4 file:py-2 file:px-4
            file:rounded-full file:border-0
            file:text-sm file:font-semibold
            file:bg-violet-50 file:text-violet-700
            hover:file:bg-violet-100
          "
        />

        <button
          onClick={handleUpload}
          disabled={!selectedFiles || isPending}
          className="bg-blue-500 text-white px-4 py-2 rounded disabled:bg-gray-300 hover:bg-blue-600 transition"
        >
          {isPending ? "Uploading..." : "Upload"}
        </button>
      </div>

      {error && (
        <div className="mt-4 text-red-500">
          Error: {error instanceof Error ? error.message : "Upload failed"}
        </div>
      )}

      {isSuccess && data && (
        <div className="mt-4 p-2 bg-green-50 border border-green-200 rounded">
          <p className="text-green-700 font-semibold">Upload Successful!</p>
          <pre className="text-xs mt-2 overflow-auto">
            {JSON.stringify(data, null, 2)}
          </pre>
        </div>
      )}
    </div>
  );
}
