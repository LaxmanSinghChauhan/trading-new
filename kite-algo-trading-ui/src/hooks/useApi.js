import { useCallback, useEffect, useRef, useState } from "react";

// Loads data once, then optionally refreshes every `refreshMs` while the tab is visible.
// Refreshes keep the previous data on screen, so live values update in place without flicker.
export function useApi(fetcher, { refreshMs } = {}) {
  const [state, setState] = useState({ data: null, error: null, loading: true, updatedAt: null });
  const fetcherRef = useRef(fetcher);
  const requestRef = useRef(0);
  fetcherRef.current = fetcher;

  const load = useCallback(async () => {
    const request = ++requestRef.current;
    try {
      const data = await fetcherRef.current();
      if (request !== requestRef.current) return;
      setState({ data, error: null, loading: false, updatedAt: new Date() });
    } catch (error) {
      if (request !== requestRef.current) return;
      setState((previous) => ({ ...previous, error, loading: false }));
    }
  }, []);

  const reload = useCallback(() => {
    setState((previous) => ({ ...previous, loading: previous.data === null, error: null }));
    load();
  }, [load]);

  useEffect(() => {
    load();
    // Bumping the request id makes any in-flight response ignore itself after unmount.
    const cancel = () => {
      requestRef.current++;
    };
    if (!refreshMs) return cancel;

    const timer = setInterval(() => {
      if (!document.hidden) load();
    }, refreshMs);
    const onVisible = () => {
      if (!document.hidden) load();
    };
    document.addEventListener("visibilitychange", onVisible);

    return () => {
      cancel();
      clearInterval(timer);
      document.removeEventListener("visibilitychange", onVisible);
    };
  }, [load, refreshMs]);

  return { ...state, reload };
}
