import PageHeader from "../components/PageHeader";
import { EmptyState } from "../components/States";

export default function News() {
  return (
    <section>
      <PageHeader title="News" subtitle="Headlines and sentiment that can affect stocks you hold or watch." />
      <EmptyState
        icon="news"
        title="News feed coming soon"
        message="The news daemon is already saving headlines. They'll appear here with sentiment, source, and an alert when a held stock turns bearish."
      />
    </section>
  );
}
