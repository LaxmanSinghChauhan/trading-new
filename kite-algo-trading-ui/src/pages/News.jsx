export default function News() {
  return (
    <section className="grid gap-5">
      <div>
        <h2 className="text-3xl font-black tracking-[-0.05em] text-ink">News</h2>
        <p className="mt-2 max-w-2xl text-sm leading-7 text-slate-600">
          The backend `NewsDaemon` and `NewsSentimentClassifier` already persist advisory items to `news_log`.
          This page is the place to add polling views, bearish-position alerts, and source filters.
        </p>
      </div>
      <div className="rounded-3xl border border-black/5 bg-white/80 p-6 text-sm leading-7 text-slate-600">
        Hook this page to `/api/admin/news/recent` with the operator's admin key to show the latest stored
        headlines, source labels, sentiment, and timestamps.
      </div>
    </section>
  );
}
