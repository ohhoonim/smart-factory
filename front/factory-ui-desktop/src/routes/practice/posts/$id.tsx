import { createFileRoute, useLoaderData } from "@tanstack/react-router";

export const Route = createFileRoute("/practice/posts/$id")({
  component: RouteComponent,
  loader: async ({ params }) => {
    const id = params.id;
    const response = await fetch(
      `https:jsonplaceholder.typicode.com/posts/${id}`
    );
    if (!response.ok) throw Error();
    const data: Post = await response.json();
    return data;
  },
  pendingComponent: () => <div> Lodding...</div>,
  errorComponent: () => <div> There was an error</div>,
});

interface Post {
  id: string;
  userId: number;
  title: string;
  body: string;
}

function RouteComponent() {
  const data: Post = useLoaderData({ from: "/practice/posts/$id" });
  return (
    <>
      <h3>{data.title} </h3>
      <p>{data.body}</p>
    </>
  );
}
