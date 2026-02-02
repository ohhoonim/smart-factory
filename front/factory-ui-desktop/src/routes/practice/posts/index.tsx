import { createFileRoute, Link } from "@tanstack/react-router";
import PostList from "../../../components/PostList";

export const Route = createFileRoute("/practice/posts/")({
  component: RouteComponent,
});

function RouteComponent() {
  return (
    <>
      <Link to="/practice/posts/$id" params={{ id: "3" }}>
        Post 3
      </Link>
      <PostList></PostList>
    </>
  );
}
