import { createFileRoute } from "@tanstack/react-router";

import Modal from "../../../components/Modal";
import { useState } from "react";

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
