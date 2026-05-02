import axios from "axios";

export const api = axios.create({
  baseURL: "/api"
});

export async function fetchHealth() {
  const { data } = await api.get("/health");
  return data;
}

export async function fetchRiskStatus() {
  const { data } = await api.get("/risk/status");
  return data;
}

export async function fetchPositions() {
  const { data } = await api.get("/positions");
  return data;
}

export async function fetchTradeHistory() {
  const { data } = await api.get("/positions/history");
  return data;
}

export async function fetchSignals() {
  const { data } = await api.get("/signals/recent");
  return data;
}

export async function fetchSignalStats() {
  const { data } = await api.get("/signals/stats");
  return data;
}

export async function fetchUniverse() {
  const { data } = await api.get("/universe");
  return data;
}
