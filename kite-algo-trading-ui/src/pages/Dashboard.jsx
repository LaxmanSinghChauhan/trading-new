import StatusCard from "../components/StatusCard";
import PageHeader from "../components/PageHeader";
import Badge from "../components/Badge";
import Pnl from "../components/Pnl";
import { ErrorState, Skeleton, Button } from "../components/States";
import { useApi } from "../hooks/useApi";
import { fetchHealth, fetchRiskStatus, fetchSignalStats } from "../api/client";
import { formatINR, formatNumber, formatPct, formatTimestamp, humanize } from "../lib/format";

const loadOverview = () =>
  Promise.all([fetchHealth(), fetchRiskStatus(), fetchSignalStats()]).then(([health, risk, signals]) => ({
    health,
    risk,
    signals
  }));

function TodayCard({ risk, health, loading }) {
  return (
    <div className="card animate-rise flex flex-col gap-5 p-6 md:col-span-2 lg:flex-row lg:items-end lg:justify-between xl:col-span-3">
      <div>
        <div className="text-footnote font-medium text-secondary">Realized P&amp;L today</div>
        {loading ? (
          <Skeleton className="mt-3 h-10 w-48" />
        ) : (
          <Pnl value={risk?.realizedPnl} className="mt-2 block text-[2.5rem] font-semibold leading-tight tracking-[-0.025em]" />
        )}
      </div>
      <div className="flex flex-wrap gap-2 lg:justify-end">
        {loading ? (
          <Skeleton className="h-7 w-64 rounded-full" />
        ) : (
          <>
            <Badge tone={risk?.tradingEnabled ? "positive" : "caution"} dot>
              {risk?.tradingEnabled ? "Trading enabled" : "Trading paused"}
            </Badge>
            <Badge tone={risk?.dailyLossBreakerActive ? "negative" : "neutral"} dot>
              {risk?.dailyLossBreakerActive ? "Daily loss limit hit" : "Daily loss limit clear"}
            </Badge>
            <Badge tone={risk?.marketBreakerActive ? "negative" : "neutral"} dot>
              {risk?.marketBreakerActive
                ? `Market breaker on (${formatPct(risk?.marketDropPct)})`
                : "Market breaker clear"}
            </Badge>
            {health?.brokerMode && <Badge tone="accent">{humanize(health.brokerMode)} mode</Badge>}
          </>
        )}
      </div>
    </div>
  );
}

export default function Dashboard() {
  const { data, error, loading, reload, updatedAt } = useApi(loadOverview, { refreshMs: 10000 });
  const { health, risk, signals } = data ?? {};

  return (
    <section>
      <PageHeader title="Overview" subtitle="Live state of the momentum engine, refreshed every 10 seconds.">
        {updatedAt && (
          <span className="text-footnote tabular-nums text-secondary">
            {error ? "Reconnecting…" : `Updated ${updatedAt.toLocaleTimeString("en-IN", { hour12: false, timeZone: "Asia/Kolkata" })}`}
          </span>
        )}
        <Button variant="secondary" icon="refresh" onClick={reload} aria-label="Refresh overview">
          Refresh
        </Button>
      </PageHeader>

      {error && !data ? (
        <ErrorState onRetry={reload} />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <TodayCard risk={risk} health={health} loading={loading} />
          <StatusCard
            index={1}
            loading={loading}
            label="Available capital"
            value={formatINR(risk?.availableCapital)}
            detail={`${formatINR(risk?.reservedCapital)} reserved in open trades`}
          />
          <StatusCard
            index={2}
            loading={loading}
            label="Open positions"
            value={formatNumber(risk?.openPositions)}
            detail={`${formatNumber(risk?.approvedSignals)} approved · ${formatNumber(risk?.blockedSignals)} blocked`}
          />
          <StatusCard
            index={3}
            loading={loading}
            label="Signals today"
            value={formatNumber(signals?.signalsToday)}
            detail={signals?.lastSignalAt ? `Latest at ${formatTimestamp(signals.lastSignalAt)}` : "None yet today"}
          />
          <StatusCard
            index={4}
            loading={loading}
            label="Engine"
            value={health?.status === "UP" ? "Running" : humanize(health?.status)}
            tone={health?.status === "UP" ? "positive" : "caution"}
            detail={health?.brokerConnected ? "Broker connected" : "Broker not connected"}
          />
          <StatusCard
            index={5}
            loading={loading}
            label="Watchlist"
            value={formatNumber(health?.activeUniverseSize)}
            detail="Liquid stocks being scanned"
          />
          <StatusCard
            index={6}
            loading={loading}
            label="Queues"
            value={`${formatNumber(health?.tickQueueDepth)} / ${formatNumber(health?.executionQueueDepth)}`}
            detail="Ticks waiting / orders waiting"
          />
        </div>
      )}
    </section>
  );
}
