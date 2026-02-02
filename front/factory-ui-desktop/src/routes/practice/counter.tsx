import { createFileRoute } from "@tanstack/react-router";
import { z } from "zod";
import useCounterStore from "../../store/useCounterStore";

async function userLoader({ params }: { params: { userId: string } }) {
  // some data fetch ...
  return `${params.userId}`;
}

export const Route = createFileRoute("/practice/counter")({
  beforeLoad: async () => {
    // // Check if the user is authenticated
    // const authed = await isAuthenticated();
    // if (!authed) {
    //   // Redirect the user to the login page
    //   return "/login";
    // }
  },
  validateSearch: z.object({
    tab: z.enum(["posts", "profile"]).optional().default("profile"),
  }),
  loader: userLoader,
  component: CounterPage,
});

/*
creatorFileRoute properties
- component : 필수
- loader : 컴포넌트 렌더링 이전에 데이터를 비동기적으로 가져오는 함수. 데이터가 준비될때까지 컴포넌트 렌더링 차단하여 깜박임을 방지
- errorComponent : 에러발생시 사용할 컴포넌트 
- pendingComponent : loader가 데이터를 가져오는 동안 렌더링될 컴포넌트
- validateSearch: query 매개변수의 유효성을 검사. Zod 사용 권장
- beforeLoad : loader가 실행되기 전에 라우팅을 리다이렉트하거나 취소할 수 있는 함수. 주로 인증 검사에 사용
*/

/* 
Route.useSearch()
Route.useLoaderData()

훅,용도,설명
Route.useParams(),경로 매개변수 접근,URL의 동적 세그먼트(예: /posts/:postId에서 :postId) 값을 타입 안전하게 가져옵니다.
Route.useSearch(),쿼리 매개변수 접근,URL의 쿼리 문자열(예: ?page=2&sort=date) 값을 타입 안전하게 가져옵니다.
Route.useLoaderData(),로더 데이터 접근,loader 옵션에서 반환된 데이터를 컴포넌트에서 타입 안전하게 사용합니다.
Route.useRouteContext(),라우트 컨텍스트 접근,부모 라우트 또는 자기 자신에서 정의한 context 데이터를 가져옵니다. 주로 부모 라우트에서 자식 라우트로 데이터를 전달할 때 사용됩니다.
Route.useParentRouteContext(),부모 컨텍스트 접근,가장 가까운 부모 라우트에서 정의한 context 데이터를 가져옵니다. 계층적 데이터 공유에 유용합니다.
Route.useMatch(),현재 라우트 정보 접근,"현재 렌더링되고 있는 라우트의 모든 정보(ID, 경로, 매개변수, 컨텍스트 등)를 가져옵니다."
Route.useMatches(),상위 라우트 목록 접근,루트부터 현재 라우트까지 모든 일치하는 라우트 객체의 배열을 가져옵니다. 현재 렌더링 체인을 이해하거나 상위 라우트들의 데이터를 한 번에 볼 때 유용합니다.
Route.useParams({ select }),선택적 값 접근,메모이제이션(memoization)을 활용하여 params 객체 중 특정 필드만 구독하여 불필요한 리렌더링을 방지합니다. (useSearch나 useLoaderData 등 다른 훅에도 적용 가능)

*/

function CounterPage() {
  const { count, increment, decrement } = useCounterStore();

  return (
    <div>
      <h1>Count: {count}</h1>
      <button onClick={increment}>증가 (+)</button>
      <button onClick={decrement}>감소 (-)</button>
    </div>
  );
}
