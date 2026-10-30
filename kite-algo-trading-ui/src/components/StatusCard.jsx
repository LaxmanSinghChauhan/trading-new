import { Skeleton } from "./States";

const valueTones = {
  neutral: "text-label",
  positive: "text-positive",
  negative: "text-negative",
  caution: "text-caution"
};

export default function StatusCard({ label, value, detail, tone = "neutral", loading = false, index = 0 }) {
  return (
    <div className="card animate-rise p-5" style={{ "--i": index }}>
      <div className="text-footnote font-medium text-secondary">{label}</div>
      {loading ? (
        <>
          <Skeleton className="mt-3 h-7 w-28" />
          <Skeleton className="mt-3 h-3.5 w-40" />
        </>
      ) : (
        <>
          <div className={`mt-2 truncate text-title font-semibold tabular-nums ${valueTones[tone]}`}>{value}</div>
          <div className="mt-1 text-footnote text-secondary">{detail}</div>
        </>
      )}
    </div>
  );
}
