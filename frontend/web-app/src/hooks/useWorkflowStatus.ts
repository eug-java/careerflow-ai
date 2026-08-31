import { useEffect, useRef, useState } from "react";
import { connectWorkflowStatusSocket } from "../api/workflowSocket";

interface WorkflowStatusMessage {
    status: string;
    message?: string;
}

interface Options {
    onCompleted?: () => void;
    onFailed?: (message?: string) => void;
}

export function useWorkflowStatus(processInstanceKey: number | null, options: Options = {}) {
    const [status, setStatus] = useState("");
    const onCompletedRef = useRef(options.onCompleted);
    const onFailedRef = useRef(options.onFailed);

    useEffect(() => {
        onCompletedRef.current = options.onCompleted;
        onFailedRef.current = options.onFailed;
    }, [options.onCompleted, options.onFailed]);

    useEffect(() => {
        if (!processInstanceKey) {
            return;
        }

        const socket = connectWorkflowStatusSocket(processInstanceKey, (statusMessage: WorkflowStatusMessage) => {
            setStatus(statusMessage.status);
            if (statusMessage.status === "COMPLETED") {
                onCompletedRef.current?.();
            }
            if (statusMessage.status === "FAILED") {
                onFailedRef.current?.(statusMessage.message);
            }
        });

        return () => {
            socket.close();
        };
    }, [processInstanceKey]);

    if (!processInstanceKey) {
        return "";
    }

    return status || "RUNNING";
}
