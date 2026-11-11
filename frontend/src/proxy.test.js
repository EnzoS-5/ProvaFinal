import { beforeEach, afterEach, expect, it, vi } from "vitest";
import handler from "../api/proxy.js";
let req, res;
beforeEach(() => {
  vi.stubEnv("BACKEND_URL", "http://api.example:8083");
  req = {
    method: "GET",
    query: { upstreamPath: "exemplo", valor: "Ja" },
    headers: { authorization: "Bearer token" },
  };
  res = {
    setHeader: vi.fn(),
    status: vi.fn().mockReturnThis(),
    json: vi.fn(),
    send: vi.fn(),
    end: vi.fn(),
  };
  vi.stubGlobal(
    "fetch",
    vi
      .fn()
      .mockResolvedValue(
        new Response("[]", { headers: { "Content-Type": "application/json" } }),
      ),
  );
});
afterEach(() => vi.unstubAllEnvs());
it("encaminha caminho, filtro e Bearer ao mesmo backend", async () => {
  await handler(req, res);
  expect(fetch.mock.calls[0][0].toString()).toBe(
    "http://api.example:8083/exemplo?valor=Ja",
  );
  expect(fetch.mock.calls[0][1].headers.Authorization).toBe("Bearer token");
  expect(fetch.mock.calls[0][1].redirect).toBe("manual");
  expect(res.status).toHaveBeenCalledWith(200);
});
it("encaminha POST e reescreve Location relativo", async () => {
  req.method = "POST";
  req.body = { valor: "Teste" };
  fetch.mockResolvedValue(
    new Response('{"id":1}', {
      status: 201,
      headers: { Location: "/exemplo/1" },
    }),
  );
  await handler(req, res);
  expect(JSON.parse(fetch.mock.calls[0][1].body)).toEqual({ valor: "Teste" });
  expect(res.setHeader).toHaveBeenCalledWith("Location", "/api/exemplo/1");
});
it("responde 204 sem tentar ler JSON", async () => {
  req.method = "DELETE";
  req.query = { upstreamPath: "exemplo/1" };
  fetch.mockResolvedValue(new Response(null, { status: 204 }));
  await handler(req, res);
  expect(res.status).toHaveBeenCalledWith(204);
  expect(res.end).toHaveBeenCalled();
});
it.each([
  "../segredo",
  "//outro.example",
  "https://outro.example",
  "exemplo/../outro",
])("rejeita caminho inválido %s", async (route) => {
  req.query.upstreamPath = route;
  await handler(req, res);
  expect(res.status).toHaveBeenCalledWith(400);
  expect(fetch).not.toHaveBeenCalled();
});
it("orienta configurar backend ausente", async () => {
  vi.stubEnv("BACKEND_URL", "");
  await handler(req, res);
  expect(res.status).toHaveBeenCalledWith(503);
  expect(fetch).not.toHaveBeenCalled();
});
it("transforma falha de rede em 502", async () => {
  fetch.mockRejectedValue(new Error("rede"));
  await handler(req, res);
  expect(res.status).toHaveBeenCalledWith(502);
});
it("não permite métodos fora da API", async () => {
  req.method = "TRACE";
  await handler(req, res);
  expect(res.status).toHaveBeenCalledWith(405);
  expect(fetch).not.toHaveBeenCalled();
});
