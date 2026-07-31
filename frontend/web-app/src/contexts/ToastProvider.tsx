import { useCallback, useMemo, useState, type ReactNode } from "react";
import {
    nextToastId,
    ToastContext,
    type ToastMessage,
} from "./toast-context";

export function ToastProvider({ children }: { children: ReactNode }) {
    const [toasts, setToasts] = useState<ToastMessage[]>([]);

    const dismissToast = useCallback((id: number) => {
        setToasts((current) => current.filter((toast) => toast.id !== id));
    }, []);

    const pushToast = useCallback(
        (type: ToastMessage["type"], text: string) => {
            const id = nextToastId();
            setToasts((current) => [...current, { id, type, text }]);
            window.setTimeout(() => dismissToast(id), 4000);
        },
        [dismissToast]
    );

    const value = useMemo(
        () => ({ toasts, pushToast, dismissToast }),
        [toasts, pushToast, dismissToast]
    );

    return (
        <ToastContext.Provider value={value}>
            {children}
            <div className="fixed bottom-6 right-6 z-50 flex flex-col gap-3 max-w-sm">
                {toasts.map((toast) => (
                    <div
                        key={toast.id}
                        className={`rounded-xl px-4 py-3 shadow-lg text-white text-sm ${
                            toast.type === "success"
                                ? "bg-emerald-600"
                                : toast.type === "error"
                                  ? "bg-rose-600"
                                  : "bg-slate-800"
                        }`}
                    >
                        {toast.text}
                    </div>
                ))}
            </div>
        </ToastContext.Provider>
    );
}
