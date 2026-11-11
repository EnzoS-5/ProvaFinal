import { useState } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { checkApi } from "./api.js";
export default function App() {
  const {
    isAuthenticated,
    isLoading,
    user,
    error: loginError,
    loginWithRedirect,
    logout,
    getAccessTokenSilently,
  } = useAuth0();
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  async function verifyApi() {
    setBusy(true);
    setError("");
    setMessage("");
    try {
      await checkApi(await getAccessTokenSilently());
      setMessage("API protegida e banco conectados.");
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <header className="topbar">
        <a className="brand" href="/">
          prova<span>·</span>final
        </a>
        <span className="top-label">BASE DA APLICAÇÃO</span>
        {isAuthenticated && (
          <div className="account">
            <span>{user?.email || user?.name}</span>
            <button
              className="button-quiet"
              onClick={() =>
                logout({ logoutParams: { returnTo: window.location.origin } })
              }
            >
              Sair
            </button>
          </div>
        )}
      </header>
      <main className="dashboard">
        <div className="page-title">
          <div>
            <span className="eyebrow">PROVA FINAL</span>
            <h1>
              {isAuthenticated
                ? "Sua sessão está pronta."
                : "Acesse sua aplicação."}
            </h1>
            <p>
              {isAuthenticated
                ? "Implemente aqui as telas e os campos definidos no enunciado."
                : "Entre para acessar a aplicação."}
            </p>
          </div>
        </div>
        {loginError && <p role="alert">{loginError.message}</p>}
        {error && (
          <div className="notice error" role="alert">
            {error}
          </div>
        )}
        {message && (
          <div className="notice success" role="status">
            {message}
          </div>
        )}
        {isLoading ? (
          <p role="status">Carregando sessão…</p>
        ) : isAuthenticated ? (
          <button
            className="button-primary"
            disabled={busy}
            onClick={verifyApi}
          >
            {busy ? "Verificando…" : "Verificar API"}
          </button>
        ) : (
          <button
            className="button-primary"
            onClick={() => loginWithRedirect()}
          >
            Entrar com Auth0
          </button>
        )}
      </main>
      <footer className="footer">
        <span>prova·final</span>
        <span>Aplicação autenticada</span>
      </footer>
    </>
  );
}
