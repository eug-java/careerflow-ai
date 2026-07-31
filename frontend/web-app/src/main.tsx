import React from "react";
import ReactDOM from "react-dom/client";
import { QueryClientProvider } from "@tanstack/react-query";
import { RouterProvider } from "react-router-dom";

import "./index.css";
import { router } from "./router";
import { queryClient } from "./lib/queryClient";
import { ToastProvider } from "./contexts/ToastContext";

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <QueryClientProvider client={queryClient}>
            <ToastProvider>
                <RouterProvider router={router} />
            </ToastProvider>
        </QueryClientProvider>
    </React.StrictMode>
);
