import { apiClient } from "./client";

export interface GeneratedDocument {
    id: string;
    profileId: string;
    jobId: string;
    documentType: string;
    fileName: string;
    contentType: string;
    storageBucket: string;
    storageKey: string;
    createdAt: string;
}

export async function fetchDocuments(): Promise<GeneratedDocument[]> {
    const response = await apiClient.get("/api/v1/documents");
    return response.data;
}

export async function fetchDocumentContent(id: string): Promise<string> {
    const response = await apiClient.get(
        `/api/v1/documents/${id}/content`,
        {
            responseType: "text",
        }
    );

    return response.data;
}

export async function deleteDocument(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/documents/${id}`);
}

export async function downloadDocumentPdf(id: string, fileName: string): Promise<void> {
    const response = await apiClient.get(`/api/v1/documents/${id}/pdf`, {
        responseType: "blob",
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");

    link.href = url;
    link.download = fileName.endsWith(".pdf")
        ? fileName
        : fileName.replace(".md", ".pdf");

    document.body.appendChild(link);
    link.click();

    link.remove();
    window.URL.revokeObjectURL(url);
}

export async function downloadDocumentDocx(id: string, fileName: string): Promise<void> {
    const response = await apiClient.get(`/api/v1/documents/${id}/docx`, {
        responseType: "blob",
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");

    link.href = url;
    link.download = fileName.endsWith(".docx")
        ? fileName
        : fileName.replace(".md", ".docx");

    document.body.appendChild(link);
    link.click();

    link.remove();
    window.URL.revokeObjectURL(url);
}