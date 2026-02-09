# Project Context for Gemini CLI

This document provides essential information about the `front/factory-ui-desktop` project to help the Gemini CLI agent understand and work with the codebase more effectively.

## Project Overview

This is a modern frontend application built with Vite and React 19, leveraging the TanStack ecosystem for routing, data fetching, and form management. It uses Zustand for global state management and Zod for schema validation.

## Key Technologies and Libraries:

*   **UI Library:** React 19 (`react`, `react-dom`) - Utilizing the latest version of React.
*   **Build Tool:** Vite (`vite`) - A fast and lightweight frontend build tool.
*   **Language:** TypeScript (`typescript`) - Superset of JavaScript providing static typing.

## Core Functionalities and State Management:

*   **Routing:** TanStack Router (`@tanstack/react-router`) - A powerful, type-safe routing library.
*   **Data Fetching/Caching:** TanStack Query (React Query) (`@tanstack/react-query`) - Manages server state and data synchronization.
*   **Global State Management:** Zustand (`zustand`) - A small, fast, and scalable state management library.
*   **Form Management:** TanStack Form (`@tanstack/react-form`) - Handles form state and validation.
*   **Schema Validation:** Zod (`zod`) - A TypeScript-first schema declaration and validation library.
*   **HTTP Client:** Axios (`axios`) - Used for handling API requests.

## Development Tools and Linting:

*   **Linter:** ESLint (`eslint`) - Used for maintaining code quality and consistency (including related plugins).
*   **Testing:** Vitest (`vitest`) - A Vite-native unit testing framework, includes React Testing Library.
*   **Developer Tools:**
    *   `@tanstack/react-query-devtools`: For React Query debugging.
    *   `@tanstack/react-router-devtools`: For Router debugging.
    *   `@tanstack/router-plugin`: Vite plugin for Router (expected to support file-based routing, etc.).

## Build and Test Commands:

*   **Development Server:** `npm run dev`
*   **Build:** `npm run build`
*   **Lint:** `npm run lint`
*   **Preview:** `npm run preview`
*   **Test:** (Not explicitly defined in `scripts`, typically `vitest` or `npm test` if configured)
    *   *Self-correction*: Based on `devDependencies`, `vitest` is installed. The command would likely be `vitest` or `npm test` if a script alias is added.

---

## Recommended Folder Structure

To enhance scalability and maintainability, especially for a growing project, adopting a more feature-based or domain-driven structure is recommended. This approach clearly separates concerns and co-locates related files.

```
src/
├── api/                # (New) Centralized Axios instances and API call functions.
│   ├── axios.ts        # Axios setup, interceptors.
│   └── posts.ts        # Functions for post-related API calls.
├── assets/             # Images, fonts, and other static assets.
├── components/         # Pure UI components without domain-specific logic (e.g., Design System components).
│   ├── common/         # Generic components like Button, Input, Modal.
│   └── layout/         # Header, Footer, Sidebar 등 레이아웃 컴포넌트
├── features/           # (New/Recommended) Grouping by feature/domain.
│   ├── posts/          # Example: Post-related functionalities.
│   │   ├── components/ # Domain-specific components like PostList, PostItem.
│   │   ├── hooks/      # Custom hooks for the feature (e.g., usePosts for React Query).
│   │   └── types.ts    # Type definitions specific to posts.
│   └── auth/           # Example: Authentication-related functionalities.
├── hooks/              # (New) Globally reusable custom Hooks (e.g., useDebounce).
├── lib/                # (New) Utility functions, external library configurations (e.g., Zod schemas).
│   └── utils.ts
├── routes/             # TanStack Router pages (Keep current structure).
│   # Routes should primarily act as entry points for pages.
│   # Complex logic or UI should be imported from 'features' or 'components'.
├── store/              # Zustand global stores (Maintain if needed, can be moved inside features).
├── types/              # (New) Global type definitions (.d.ts, etc.).
├── main.tsx
├── App.tsx
└── routeTree.gen.ts
```

### Action Items for Gradual Refactoring:

1.  **Create `api` and `lib` folders**: Extract API call logic and common utility functions from components or routes into `src/api/` and `src/lib/`.
2.  **Organize `components`**: Move pure, reusable UI components (like `Modal`) into `src/components/common/` (or `ui`). Domain-specific components (like `PostList`) can eventually move into `src/features/[featureName]/components/`.
3.  **Introduce `features` (Optional)**: For larger projects, consider grouping related components, hooks, and types by feature within `src/features/`.