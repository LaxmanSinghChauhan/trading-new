import Icon from "./Icon";

export function Skeleton({ className = "h-4 w-24" }) {
  return <div className={`skeleton ${className}`} aria-hidden="true" />;
}

export function EmptyState({ icon = "tray", title, message, children }) {
  return (
    <div className="card flex flex-col items-center px-6 py-14 text-center">
      <div className="flex h-12 w-12 items-center justify-center rounded-full bg-fill text-secondary">
        <Icon name={icon} className="h-6 w-6" />
      </div>
      <h2 className="mt-4 text-headline font-semibold text-label">{title}</h2>
      <p className="mt-1 max-w-[46ch] text-body text-secondary">{message}</p>
      {children && <div className="mt-5">{children}</div>}
    </div>
  );
}

export function ErrorState({ onRetry }) {
  return (
    <EmptyState
      icon="alert"
      title="Can't reach the trading engine"
      message="Make sure the backend is running, then try again."
    >
      <Button onClick={onRetry}>Try Again</Button>
    </EmptyState>
  );
}

export function Button({ children, variant = "primary", icon, ...props }) {
  const styles =
    variant === "primary"
      ? "bg-accent text-white hover:bg-accent/90"
      : "bg-surface text-label shadow-card hover:bg-fill";
  return (
    <button
      type="button"
      className={`pressable inline-flex min-h-[44px] items-center justify-center gap-2 rounded-full px-5 text-body font-semibold md:min-h-[36px] ${styles}`}
      {...props}
    >
      {icon && <Icon name={icon} className="h-4 w-4" strokeWidth={2} />}
      {children}
    </button>
  );
}
