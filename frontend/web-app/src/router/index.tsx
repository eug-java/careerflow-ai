import { createBrowserRouter } from "react-router-dom";

import LoginPage from "../pages/LoginPage";
import DashboardPage from "../pages/DashboardPage";
import ProfilesPage from "../pages/ProfilesPage";
import ProfileDetailPage from "../pages/ProfileDetailPage";
import CreateProfilePage from "../pages/CreateProfilePage";
import JobsPage from "../pages/JobsPage";
import JobDetailPage from "../pages/JobDetailPage";
import MatchHistoryPage from "../pages/MatchHistoryPage";
import MatchDetailPage from "../pages/MatchDetailPage";
import DocumentsPage from "../pages/DocumentsPage";
import EmailInboxPage from "../pages/EmailInboxPage";
import EmailSettingsPage from "../pages/EmailSettingsPage";
import AiSettingsPage from "../pages/AiSettingsPage";
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
                <DashboardPage />
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
        path: "/profiles/:id/edit",
        element: (
            <ProtectedRoute>
                <EditProfilePage />
            </ProtectedRoute>
        ),
    },
    {
        path: "/profiles/:id",
        element: (
            <ProtectedRoute>
                <ProfileDetailPage />
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
        path: "/jobs/new",
        element: (
            <ProtectedRoute>
                <CreateJobPage />
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
    {
        path: "/jobs/:id",
        element: (
            <ProtectedRoute>
                <JobDetailPage />
            </ProtectedRoute>
        ),
    },
    {
        path: "/matches",
        element: (
            <ProtectedRoute>
                <MatchHistoryPage />
            </ProtectedRoute>
        ),
    },
    {
        path: "/matches/:id",
        element: (
            <ProtectedRoute>
                <MatchDetailPage />
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
        path: "/email",
        element: (
            <ProtectedRoute>
                <EmailInboxPage />
            </ProtectedRoute>
        ),
    },
    {
        path: "/email/settings",
        element: (
            <ProtectedRoute>
                <EmailSettingsPage />
            </ProtectedRoute>
        ),
    },
    {
        path: "/settings/ai",
        element: (
            <ProtectedRoute>
                <AiSettingsPage />
            </ProtectedRoute>
        ),
    },
]);
