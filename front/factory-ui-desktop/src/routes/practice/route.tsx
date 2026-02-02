import { createFileRoute, Link, Outlet } from "@tanstack/react-router";

export const Route = createFileRoute("/practice")({
  component: PractiseLayoutComponent,
});

function PractiseLayoutComponent() {
  const layout = {
    display: "flex",
    border: "1px soldi blue",
    with: "100%",
    height: "100vh",
  };
  const nav = {
    width: "120px",
    fontSize: "10px",
    border: "1px solid black",
  };
  const contents = {
    width: "500px",
    border: "1px solid red",
  };
  return (
    <div style={layout}>
      <nav style={nav}>
        <h1>React 연습</h1>
        <h2>리액트 기초</h2>
        <ul>
          <li>
            <Link to="/practice/basic/useRef">useRef : 기본 사용법</Link>
          </li>
          <li>
            <Link to="/practice/basic/form">form 다루기</Link>
          </li>
          <li>
            <Link to="/practice/basic/modal">Potal을 사용한 modal</Link>
          </li>
          <li>
            <Link to="/practice/basic/suspense">Suspense - lazy 로딩</Link>
          </li>
        </ul>
        <h2>Zustand</h2>
        <ul>
          <li>
            <Link to="/practice/counter" params={{ userId: "ABC-U001" }}>
              Counter : Tanstack Router의 hooks 사용법과 Zustand 기본 예제
            </Link>
          </li>
        </ul>
        <h2> Tanstack </h2>
        <ul>
          <li>
            <Link to="/practice/dashboard">
              Dashboard : 외부 컴포넌트를 페이지에서 사용하기
            </Link>
          </li>
          <li>
            <Link to="/practice/posts">
              Posts : 목록 조회 후 상세 조회 기본 예제, params 사용법
            </Link>
          </li>
          <li>
            <Link to="/practice/form-simple">Form - Simple</Link>
          </li>
        </ul>
      </nav>
      <div style={contents}>
        <Outlet />
      </div>
    </div>
  );
}
