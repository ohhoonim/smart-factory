import { createFileRoute } from "@tanstack/react-router";
import React, { Suspense } from "react";

export const Route = createFileRoute("/practice/basic/suspense")({
  component: RouteComponent,
});

const LazyGreeting = React.lazy(() => {
  return new Promise((resolve) => {
    setTimeout(() => {
      return resolve(import("../../../components/PostList"));
    }, 2000);
  }) as Promise<{ default: React.ComponentType<unknown> }>;
});

function RouteComponent() {
  return (
    <>
      <Suspense fallback={<div>Loding...</div>}>
        <LazyGreeting />
      </Suspense>
    </>
  );
}
