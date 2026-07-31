import { createContext } from "react";

export interface ToastMessage {
    id: number;
    type: "success" | "error" | "info";
    text: string;
}

export interface ToastContextValue {
    toasts: ToastMessage[];
    pushToast: (type: ToastMessage["type"], text: string) => void;
    dismissToast: (id: number) => void;
}

export const ToastContext = createContext<ToastContextValue | null>(null);

export let toastCounter = 0;

export function nextToastId() {
    toastCounter += 1;
    return toastCounter;
}
