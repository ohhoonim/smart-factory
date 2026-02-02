import { createFileRoute, Link } from "@tanstack/react-router";

export const Route = createFileRoute("/")({
  component: RouteComponent,
});

function RouteComponent() {
  return (
    <>
      <div>main</div>
      <Link to="/practice"> 연습하기 ➡️ </Link>
    </>
  );
}
