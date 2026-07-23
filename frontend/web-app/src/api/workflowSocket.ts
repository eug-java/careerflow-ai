const wsBaseUrl =
    import.meta.env.VITE_WS_BASE_URL ?? "ws://localhost:8080";

export interface WorkflowStatusMessage {
    processInstanceKey: number;
    processId: string;
    status: string;
    message: string;
}

export function connectWorkflowStatusSocket(
    processInstanceKey: number,
    onMessage: (message: WorkflowStatusMessage) => void
): WebSocket {
    const socket = new WebSocket(
        `${wsBaseUrl}/ws/workflows/status?processInstanceKey=${processInstanceKey}`
    );

    socket.onmessage = (event) => {
        const data = JSON.parse(event.data) as WorkflowStatusMessage;
        onMessage(data);
    };

    return socket;
}
