import { createBrowserRouter } from "react-router-dom";

import LoginPage from "../pages/LoginPage";
import ProfilesPage from "../pages/ProfilesPage";
import CreateProfilePage from "../pages/CreateProfilePage";
import JobsPage from "../pages/JobsPage";
import DocumentsPage from "../pages/DocumentsPage";
import ProtectedRoute from "./ProtectedRoute";
import CreateJobPage from "../pages/CreateJobPage";
import EditProfilePage from "../pages/EditProfilePage";
import EditJobPage from "../pages/EditJobPage";

export const router = createBrowserRouter([
    {
        path: "/login",
        element: <LoginPage />,
    },
    {
        path: "/",
        element: (
            <ProtectedRoute>
                <ProfilesPage />
            </ProtectedRoute>
        ),
    },
    {
        path: "/profiles",
        element: (
            <ProtectedRoute>
                <ProfilesPage />
            </ProtectedRoute>
        ),
    },
    {
        path: "/profiles/new",
        element: (
            <ProtectedRoute>
                <CreateProfilePage />
            </ProtectedRoute>
        ),
    },
    {
        path: "/jobs",
        element: (
            <ProtectedRoute>
                <JobsPage />
            </ProtectedRoute>
        ),
    },
    {
        path: "/documents",
        element: (
            <ProtectedRoute>
                <DocumentsPage />
            </ProtectedRoute>
        ),
    },
    {
        path: "/jobs/new",
        element: (
            <ProtectedRoute>
                <CreateJobPage />
            </ProtectedRoute>
        ),
    },
    {
        path: "/profiles/:id/edit",
        element: (
            <ProtectedRoute>
                <EditProfilePage />
            </ProtectedRoute>
        ),
    },
    {
        path: "/jobs/:id/edit",
        element: (
            <ProtectedRoute>
                <EditJobPage />
            </ProtectedRoute>
        ),
    },
]);