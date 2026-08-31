import { useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../layouts/AppLayout";
import { Badge, EmptyState, LoadingGrid, QueryErrorState } from "../components/ui/Card";
import { useWorkflowsQuery } from "../hooks/useDashboardQueries";
import { formatRelativeTime } from "../lib/dashboardUtils";

const STATUS_FILTERS = ["ALL", "RUNNING", "COMPLETED", "FAILED"] as const;

type StatusFilter = (typeof STATUS_FILTERS)[number];

function statusTone(status: string): "success" | "warning" | "danger" | "default" {
    if (status === "COMPLETED") {
        return "success";
    }
    if (status === "RUNNING") {
        return "warning";
    }
    if (status === "FAILED") {
        return "danger";
    }
    return "default";
}

export default function WorkflowsPage() {
    const [statusFilter, setStatusFilter] = useState<StatusFilter>("ALL");
    const workflowsQuery = useWorkflowsQuery(
        statusFilter === "ALL" ? undefined : statusFilter
    );

    const workflows = workflowsQuery.data ?? [];

    return (
        <AppLayout>
            <div className="mb-8">
                <h1 className="text-4xl font-bold">Workflow history</h1>
                <p className="text-slate-500 mt-2">
                    Document generation pipelines with live status updates.
                </p>
            </div>

            <div className="flex flex-wrap gap-2 mb-6">
                {STATUS_FILTERS.map((status) => (
                    <button
                        key={status}
                        type="button"
                        onClick={() => setStatusFilter(status)}
                        className={`px-4 py-2 rounded-xl text-sm ${
                            statusFilter === status
                                ? "bg-slate-900 text-white"
                                : "bg-white border border-slate-300 text-slate-700 hover:bg-slate-50"
                        }`}
                    >
                        {status === "ALL" ? "All" : status}
                    </button>
                ))}
            </div>

            {workflowsQuery.isLoading ? (
                <LoadingGrid count={3} />
            ) : workflowsQuery.isError ? (
                <QueryErrorState onRetry={() => void workflowsQuery.refetch()} />
            ) : workflows.length === 0 ? (
                <EmptyState
                    title="No workflows found"
                    description={
                        statusFilter === "ALL"
                            ? "Start document generation from Jobs to see workflow history here."
                            : `No ${statusFilter.toLowerCase()} workflows right now.`
                    }
                    actionLabel="Go to jobs"
                    actionHref="/jobs"
                />
            ) : (
                <div className="grid gap-4">
                    {workflows.map((workflow) => (
                        <div key={workflow.processInstanceKey} className="bg-white rounded-2xl shadow p-6">
                            <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4">
                                <div>
                                    <div className="flex items-center gap-3 flex-wrap">
                                        <h2 className="text-lg font-semibold">
                                            Workflow #{workflow.processInstanceKey}
                                        </h2>
                                        <Badge tone={statusTone(workflow.status)}>
                                            {workflow.status}
                                        </Badge>
                                    </div>
                                    <p className="text-sm text-slate-500 mt-1">{workflow.processId}</p>
                                    <p className="text-slate-700 mt-3">{workflow.message}</p>
                                    <p className="text-xs text-slate-400 mt-2">
                                        Updated {formatRelativeTime(workflow.updatedAt)}
                                    </p>
                                </div>
                                <Link
                                    to="/jobs"
                                    className="text-center bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700 h-fit"
                                >
                                    Open jobs
                                </Link>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </AppLayout>
    );
}
