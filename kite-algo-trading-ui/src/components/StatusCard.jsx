export default function StatusCard({ label, value, detail }) {
  return (
    <div className="rounded-3xl border border-black/5 bg-white/80 p-5 shadow-sm">
      <div className="text-xs uppercase tracking-[0.16em] text-slate-500">{label}</div>
      <div className="mt-3 text-3xl font-black tracking-[-0.05em] text-ink">{value}</div>
      <div className="mt-2 text-sm text-slate-600">{detail}</div>
    </div>
  );
}
