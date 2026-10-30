import PageHeader from "../components/PageHeader";
import Badge from "../components/Badge";
import Pnl from "../components/Pnl";
import { EmptyState, ErrorState, Skeleton } from "../components/States";
import { useApi } from "../hooks/useApi";
import { fetchTradeHistory } from "../api/client";
import { formatINR, formatTimestamp, humanize } from "../lib/format";

const statusTone = { OPEN: "accent", CLOSING: "caution", CLOSED: "neutral", FAILED_EXIT: "negative" };

export default function TradeHistory() {
  const { data: rows, error, loading, reload } = useApi(fetchTradeHistory);

  return (
    <section>
      <PageHeader title="Trade History" subtitle="The last 50 trades, newest first, with every partial exit recorded." />
      {error && !rows ? (
        <ErrorState onRetry={reload} />
      ) : !loading && !rows?.length ? (
        <EmptyState
          icon="history"
          title="No trades yet"
          message="Completed and open trades will be listed here with entry, exit, and net P&L after charges."
        />
      ) : (
        <ul className="card divide-y divide-separator overflow-hidden">
          {loading
            ? [0, 1, 2].map((row) => (
                <li key={row} className="flex items-center justify-between px-5 py-4">
                  <Skeleton className="h-4 w-40" />
                  <Skeleton className="h-4 w-24" />
                </li>
              ))
            : rows.map(({ trade, legs }, index) => (
                <li
                  key={trade.id}
                  className="animate-rise flex flex-wrap items-center gap-x-6 gap-y-2 px-5 py-4"
                  style={{ "--i": index }}
                >
                  <div className="min-w-[10rem] flex-1">
                    <div className="flex items-center gap-2">
                      <span className="text-headline font-semibold text-label">{trade.symbol}</span>
                      <Badge tone={statusTone[trade.status] ?? "neutral"}>{humanize(trade.status)}</Badge>
                    </div>
                    <div className="mt-0.5 text-footnote tabular-nums text-secondary">
                      {formatTimestamp(trade.entryTime)}
                      {trade.finalExitReason && ` · ${humanize(trade.finalExitReason)}`}
                      {` · ${legs.length} ${legs.length === 1 ? "leg" : "legs"}`}
                    </div>
                  </div>
                  <div className="text-right text-footnote tabular-nums text-secondary">
                    <div>
                      {formatINR(trade.entryPriceVwap)} → {formatINR(trade.exitPriceVwap)}
                    </div>
                    <div>entry → exit</div>
                  </div>
                  <div className="w-36 text-right">
                    <Pnl value={trade.netPnl} className="text-headline font-semibold" />
                    <div className="text-footnote text-secondary">net after charges</div>
                  </div>
                </li>
              ))}
        </ul>
      )}
    </section>
  );
}
