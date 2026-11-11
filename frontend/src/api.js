import { endpoint } from "./config.js";
export async function checkApi(token) {
  const base = (import.meta.env.VITE_API_URL || "/api").replace(/\/$/, "");
  const response = await fetch(base + endpoint + "/_status", {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) {
    const messages = {
      401: "Token ausente, inválido ou expirado.",
      403: "Seu perfil não tem acesso à API.",
    };
    throw new Error(
      messages[response.status] || `Falha na API (HTTP ${response.status}).`,
    );
  }
  const data = await response.json();
  if (data.status !== "ok" || data.banco !== 1)
    throw new Error("Resposta de infraestrutura inesperada.");
  return data;
}
