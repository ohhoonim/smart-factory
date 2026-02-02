import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useRef, useState } from "react";

export const Route = createFileRoute("/practice/basic/useRef")({
  component: RouteComponent,
});

function RouteComponent() {
  // 렌더링 되지 않고 count를 유지
  const [inputValue, setInputValue] = useState("");
  const count = useRef(0);

  // 특정 dom에 대한 조작
  const inputBox = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    console.log(count);
    count.current++;
  });

  // ref를 활용한 직전값 비교 : 예전에는 됐었을 수도 있으나
  // 현재는 그냥 현재값을 출력함(react 19)
  const inputText = useRef<HTMLInputElement | null>(null);
  const [currentValue, setCurrentValue] = useState("");

  useEffect(() => {
    if (inputText.current) {
      inputText.current.value = currentValue;
    }
  }, [currentValue]);

  return (
    <>
      <h2>렌더링 되지 않고 count를 유지</h2>
      <p>값을 입력해 보세요</p>
      <input
        type="text"
        value={inputValue}
        onChange={(e) => setInputValue(e.target.value)}
      />
      <p>렌더링 횟수: {count.current}</p>
      <h2>특정 DOM 에 대한 조작 </h2>
      <p>
        <input type="text" ref={inputBox} />
        <button onClick={() => inputBox.current?.focus()}>
          input box에 focus
        </button>
      </p>
      <h2>ref를 활용한 직전값 비교(19버전 이상에서는 동작안함)</h2>
      <p>
        <input
          type="text"
          ref={inputText}
          onChange={(e) => setCurrentValue(e.target.value)}
        />
        <ul>
          <li>현재값 : {currentValue}</li>
          <li>이전값 : {inputText.current?.value} </li>
        </ul>
      </p>
    </>
  );
}

/**
 * userRef의 두 가지 기능
 * 1.
 * useState()와 같이 어떠한 값을  유지하는 변수로써 사용됨
 * 단, 렌더링이 발생되지 않음
 *
 * 2.
 * vanilla script의 querySelector와 같이 HTML DOM 요소에
 * 접근하고자 할 때 사용
 */
