import PageHeader from "../components/PageHeader";
import Badge from "../components/Badge";
import { EmptyState, ErrorState, Skeleton } from "../components/States";
import { useApi } from "../hooks/useApi";
import { fetchSignals } from "../api/client";
import { formatINR, formatNumber, formatPct, formatTimestamp, humanize } from "../lib/format";

const strengthTone = { STRONG: "accent", MODERATE: "neutral", WEAK: "neutral" };

export default function Signals() {
  const { data: signals, error, loading, reload } = useApi(fetchSignals, { refreshMs: 5000 });

  return (
    <section>
      <PageHeader
        title="Signals"
        subtitle="Momentum spikes the detector has flagged. Risk checks decide which ones become trades."
      />
      {error && !signals ? (
        <ErrorState onRetry={reload} />
      ) : !loading && !signals?.length ? (
        <EmptyState
          icon="signals"
          title="No signals yet"
          message="Spikes that pass price and volume checks will appear here as they happen."
        />
      ) : (
        <ul className="card divide-y divide-separator overflow-hidden">
          {loading
            ? [0, 1, 2, 3].map((row) => (
                <li key={row} className="flex items-center justify-between px-5 py-4">
                  <Skeleton className="h-4 w-32" />
                  <Skeleton className="h-6 w-20 rounded-full" />
                </li>
              ))
            : signals.map((signal, index) => (
                <li
                  key={`${signal.symbol}-${signal.detectedAt}`}
                  className="animate-rise flex flex-wrap items-center gap-x-6 gap-y-2 px-5 py-4"
                  style={{ "--i": index }}
                >
                  <div className="min-w-[8rem] flex-1">
                    <div className="text-headline font-semibold text-label">{signal.symbol}</div>
                    <div className="text-footnote tabular-nums text-secondary">
                      {formatTimestamp(signal.detectedAt)} · {formatINR(signal.lastPrice)}
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="text-body font-medium tabular-nums text-label">
                      {formatPct(signal.priceChangePct)}
                    </div>
                    <div className="text-footnote text-secondary">price move</div>
                  </div>
                  <div className="text-right">
                    <div className="text-body font-medium tabular-nums text-label">
                      {formatNumber(signal.volumeMultiplier)}×
                    </div>
                    <div className="text-footnote text-secondary">usual volume</div>
                  </div>
                  <div className="w-24 text-right">
                    <Badge tone={strengthTone[signal.strength] ?? "neutral"}>{humanize(signal.strength)}</Badge>
                  </div>
                </li>
              ))}
        </ul>
      )}
    </section>
  );
}
