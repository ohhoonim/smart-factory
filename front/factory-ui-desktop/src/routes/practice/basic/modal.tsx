import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import Modal from "../../../components/common/Modal";

export const Route = createFileRoute("/practice/basic/modal")({
  component: RouteComponent,
});

function RouteComponent() {
  const [open, setOpen] = useState(false);
  return (
    <>
      <button onClick={() => setOpen(true)}> Open Modal open</button>
      {open && (
        <Modal>
          <h2> hi!</h2>
          <button onClick={() => setOpen(false)}>닫기</button>
        </Modal>
      )}
    </>
  );
}
