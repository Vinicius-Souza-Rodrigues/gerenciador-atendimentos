"use client";

import { useState } from "react";
import Link from "next/link";
import { api } from "../../lib/api";

export default function SignupPage() {
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [botLink, setBotLink] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  async function criar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    setCarregando(true);
    try {
      const { botDeepLink } = await api.signup(nome, email, senha);
      setBotLink(botDeepLink);
    } catch (err) {
      setErro((err as Error).message);
    } finally {
      setCarregando(false);
    }
  }

  if (botLink) {
    return (
      <main style={wrap}>
        <h1>Conta criada! 🎉</h1>
        <p>Seu link do bot para compartilhar com clientes:</p>
        <code style={{ display: "block", padding: 10, background: "#f4f4f4", borderRadius: 6, wordBreak: "break-all" }}>
          {botLink}
        </code>
        <p style={{ marginTop: 16 }}><Link href="/login">Ir para o login</Link></p>
      </main>
    );
  }

  return (
    <main style={wrap}>
      <h1>Criar conta</h1>
      <form onSubmit={criar} style={form}>
        <input style={input} placeholder="Nome ou estabelecimento" value={nome}
          onChange={(e) => setNome(e.target.value)} required />
        <input style={input} type="email" placeholder="Email" value={email}
          onChange={(e) => setEmail(e.target.value)} required />
        <input style={input} type="password" placeholder="Senha (mín. 6)" value={senha}
          onChange={(e) => setSenha(e.target.value)} required minLength={6} />
        {erro && <p style={{ color: "crimson" }}>{erro}</p>}
        <button style={botao} disabled={carregando}>
          {carregando ? "Criando..." : "Criar conta"}
        </button>
      </form>
      <p>Já tem conta? <Link href="/login">Entrar</Link></p>
    </main>
  );
}

const wrap: React.CSSProperties = { fontFamily: "system-ui, sans-serif", maxWidth: 420, margin: "4rem auto", padding: "0 1rem" };
const form: React.CSSProperties = { display: "flex", flexDirection: "column", gap: 10 };
const input: React.CSSProperties = { padding: 10, fontSize: 16, border: "1px solid #ccc", borderRadius: 6 };
const botao: React.CSSProperties = { padding: 10, fontSize: 16, background: "#0070f3", color: "#fff", border: 0, borderRadius: 6, cursor: "pointer" };
