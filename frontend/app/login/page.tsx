"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { api, setToken } from "../../lib/api";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  async function entrar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    setCarregando(true);
    try {
      const { token } = await api.login(email, senha);
      setToken(token);
      router.push("/painel");
    } catch (err) {
      setErro((err as Error).message);
    } finally {
      setCarregando(false);
    }
  }

  return (
    <main style={wrap}>
      <h1>Entrar</h1>
      <form onSubmit={entrar} style={form}>
        <input style={input} type="email" placeholder="Email" value={email}
          onChange={(e) => setEmail(e.target.value)} required />
        <input style={input} type="password" placeholder="Senha" value={senha}
          onChange={(e) => setSenha(e.target.value)} required />
        {erro && <p style={{ color: "crimson" }}>{erro}</p>}
        <button style={botao} disabled={carregando}>
          {carregando ? "Entrando..." : "Entrar"}
        </button>
      </form>
      <p>Não tem conta? <Link href="/signup">Criar conta</Link></p>
    </main>
  );
}

const wrap: React.CSSProperties = { fontFamily: "system-ui, sans-serif", maxWidth: 380, margin: "4rem auto", padding: "0 1rem" };
const form: React.CSSProperties = { display: "flex", flexDirection: "column", gap: 10 };
const input: React.CSSProperties = { padding: 10, fontSize: 16, border: "1px solid #ccc", borderRadius: 6 };
const botao: React.CSSProperties = { padding: 10, fontSize: 16, background: "#0070f3", color: "#fff", border: 0, borderRadius: 6, cursor: "pointer" };
