import { useEffect } from "react";

export default function PageHeader({ title, subtitle, children }) {
  useEffect(() => {
    document.title = `${title} · Momentum`;
  }, [title]);

  return (
    <header className="mb-8 flex flex-wrap items-end justify-between gap-4">
      <div className="min-w-0">
        <h1 className="text-large-title font-bold text-label">{title}</h1>
        {subtitle && <p className="mt-1 max-w-[70ch] text-body text-secondary">{subtitle}</p>}
      </div>
      {children && <div className="flex items-center gap-3">{children}</div>}
    </header>
  );
}
