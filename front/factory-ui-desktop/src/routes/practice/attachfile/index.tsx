import { createFileRoute } from "@tanstack/react-router";
import FileUpload from "../../../features/attachFile/components/FileUpload";

export const Route = createFileRoute("/practice/attachfile/")({
  component: AttachFilePage,
});

function AttachFilePage() {
  return (
    <div className="p-8">
      <h2 className="text-2xl font-bold mb-6">Attach File Practice</h2>
      <FileUpload />
    </div>
  );
}
