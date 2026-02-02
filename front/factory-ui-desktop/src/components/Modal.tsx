import type { ReactNode } from "react";
import { createPortal } from "react-dom";

export default function Modal({ children }: { children: ReactNode }) {
  const overlayStyle: React.CSSProperties = {
    position: "fixed",
    top: 0,
    left: 0,
    width: "100%",
    height: "100%",
    backgroundColor: "rgba(0, 0, 0, 0.7)", // 반투명 검은 배경
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    zIndex: 1000, // 다른 요소 위에 나타나도록 Z-Index 설정
  };

  // 모달 내용물 스타일
  const contentStyle: React.CSSProperties = {
    backgroundColor: "white",
    padding: "30px",
    borderRadius: "8px",
    boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
    minWidth: "300px",
    zIndex: 1001, // 오버레이보다 더 위에
  };
  return createPortal(
    <div style={overlayStyle}>
      <div style={contentStyle}>{children}</div>
    </div>,
    document.body
  );
}
