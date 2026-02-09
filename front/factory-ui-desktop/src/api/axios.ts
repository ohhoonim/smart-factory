import axios from "axios";

// Create a configured axios instance
// You can set base URL here if needed, e.g., import.meta.env.VITE_API_URL
export const api = axios.create({
  baseURL: "/api", // Proxy is usually set up in vite.config.ts or this points to backend
  headers: {
    "Content-Type": "application/json",
  },
});
