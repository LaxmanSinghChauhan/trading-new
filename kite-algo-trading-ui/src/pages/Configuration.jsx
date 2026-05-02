const configExamples = [
  "trading_enabled",
  "signal_price_change_pct",
  "signal_volume_multiplier",
  "max_open_positions",
  "market_fall_threshold_pct"
];

export default function Configuration() {
  return (
    <section className="grid gap-5">
      <div>
        <h2 className="text-3xl font-black tracking-[-0.05em] text-ink">Configuration</h2>
        <p className="mt-2 max-w-2xl text-sm leading-7 text-slate-600">
          This screen is wired for the runtime-editable `system_config` dataset exposed from `/api/admin/config`.
          Add form actions and admin-key handling here once the frontend is connected to a live operator session.
        </p>
      </div>
      <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
        {configExamples.map((item) => (
          <div key={item} className="rounded-3xl border border-black/5 bg-white/80 p-5">
            <div className="text-xs uppercase tracking-[0.16em] text-slate-500">{item}</div>
            <div className="mt-3 text-xl font-bold text-ink">Pending live binding</div>
          </div>
        ))}
      </div>
    </section>
  );
}
