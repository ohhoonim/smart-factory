import { createFileRoute } from "@tanstack/react-router";
import PostList from "../../features/posts/components/PostList";

export const Route = createFileRoute("/practice/dashboard")({
  component: PostList,
});
