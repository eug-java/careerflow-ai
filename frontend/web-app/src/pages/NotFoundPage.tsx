import { Link } from "react-router-dom";

export default function NotFoundPage() {
    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-100 px-4">
            <div className="bg-white w-full max-w-lg rounded-2xl shadow p-8 text-center">
                <p className="text-sm uppercase tracking-wide text-slate-400">404</p>
                <h1 className="text-3xl font-bold mt-2">Page not found</h1>
                <p className="text-slate-500 mt-3">
                    The page you requested does not exist or may have moved.
                </p>
                <div className="mt-8 flex flex-col sm:flex-row gap-3 justify-center">
                    <Link
                        to="/"
                        className="bg-slate-900 text-white px-5 py-2 rounded-xl hover:bg-slate-700"
                    >
                        Go to dashboard
                    </Link>
                    <Link
                        to="/jobs"
                        className="border border-slate-300 px-5 py-2 rounded-xl hover:bg-slate-50"
                    >
                        Browse jobs
                    </Link>
                </div>
            </div>
        </div>
    );
}
