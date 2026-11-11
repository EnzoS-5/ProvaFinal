import React from "react";
import { beforeEach, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useAuth0 } from "@auth0/auth0-react";
import App from "./App.jsx";
vi.mock("@auth0/auth0-react", () => ({ useAuth0: vi.fn() }));
let auth;
beforeEach(() => {
  auth = {
    isAuthenticated: false,
    isLoading: false,
    user: { email: "qa@example.com" },
    loginWithRedirect: vi.fn(),
    logout: vi.fn(),
    getAccessTokenSilently: vi.fn().mockResolvedValue("access-token"),
  };
  useAuth0.mockReturnValue(auth);
  vi.stubGlobal(
    "fetch",
    vi
      .fn()
      .mockResolvedValue(
        new Response(JSON.stringify({ status: "ok", banco: 1 }), {
          status: 200,
        }),
      ),
  );
});
it("pede login sem chamar API", async () => {
  render(<App />);
  await userEvent.click(
    screen.getByRole("button", { name: "Entrar com Auth0" }),
  );
  expect(auth.loginWithRedirect).toHaveBeenCalled();
  expect(fetch).not.toHaveBeenCalled();
});
it("consulta infraestrutura com access token", async () => {
  auth.isAuthenticated = true;
  render(<App />);
  await userEvent.click(screen.getByRole("button", { name: "Verificar API" }));
  expect(
    await screen.findByText("API protegida e banco conectados."),
  ).toBeInTheDocument();
  expect(fetch).toHaveBeenCalledWith("/api/registros/_status", {
    headers: { Authorization: "Bearer access-token" },
  });
  expect(
    screen.queryByRole("button", { name: "Cadastrar" }),
  ).not.toBeInTheDocument();
});
it.each([
  [401, "Token ausente, inválido ou expirado."],
  [403, "Seu perfil não tem acesso à API."],
  [502, "Falha na API (HTTP 502)."],
])("trata HTTP %s", async (status, message) => {
  auth.isAuthenticated = true;
  fetch.mockResolvedValue(new Response("", { status }));
  render(<App />);
  await userEvent.click(screen.getByRole("button", { name: "Verificar API" }));
  expect(await screen.findByRole("alert")).toHaveTextContent(message);
});
it("trata falha de sessão sem enviar token falso", async () => {
  auth.isAuthenticated = true;
  auth.getAccessTokenSilently.mockRejectedValue(new Error("Entre novamente."));
  render(<App />);
  await userEvent.click(screen.getByRole("button", { name: "Verificar API" }));
  expect(await screen.findByRole("alert")).toHaveTextContent(
    "Entre novamente.",
  );
  expect(fetch).not.toHaveBeenCalled();
});
it("faz logout", async () => {
  auth.isAuthenticated = true;
  render(<App />);
  await userEvent.click(screen.getByRole("button", { name: "Sair" }));
  expect(auth.logout).toHaveBeenCalledWith({
    logoutParams: { returnTo: window.location.origin },
  });
});
