"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api, clearToken, getToken, Horario, Servico } from "../../lib/api";

const DIAS = ["SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA", "SABADO", "DOMINGO"];

export default function PainelPage() {
  const router = useRouter();
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const [botLink, setBotLink] = useState("");
  const [servicos, setServicos] = useState<Servico[]>([]);
  const [horarios, setHorarios] = useState<Record<string, { inicio: string; fim: string }>>({});

  // novo serviço
  const [nome, setNome] = useState("");
  const [duracao, setDuracao] = useState(30);
  const [preco, setPreco] = useState("");

  useEffect(() => {
    if (!getToken()) {
      router.replace("/login");
      return;
    }
    carregarTudo();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function carregarTudo() {
    setErro(null);
    try {
      const [link, servs, hors] = await Promise.all([
        api.botLink(),
        api.listarServicos(),
        api.listarHorarios(),
      ]);
      setBotLink(link.botDeepLink);
      setServicos(servs);
      const mapa: Record<string, { inicio: string; fim: string }> = {};
      hors.forEach((h) => (mapa[h.diaSemana] = { inicio: h.horaInicio, fim: h.horaFim }));
      setHorarios(mapa);
    } catch (err) {
      setErro((err as Error).message);
    } finally {
      setCarregando(false);
    }
  }

  async function adicionarServico(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    try {
      await api.criarServico({
        nome,
        duracaoMin: Number(duracao),
        preco: preco ? Number(preco) : null,
        ativo: true,
      });
      setNome("");
      setDuracao(30);
      setPreco("");
      setServicos(await api.listarServicos());
    } catch (err) {
      setErro((err as Error).message);
    }
  }

  async function removerServico(id: number) {
    setErro(null);
    try {
      await api.removerServico(id);
      setServicos(await api.listarServicos());
    } catch (err) {
      setErro((err as Error).message);
    }
  }

  async function salvarHorarios() {
    setErro(null);
    const lista: Horario[] = DIAS.filter((d) => horarios[d]?.inicio && horarios[d]?.fim).map(
      (d) => ({ diaSemana: d, horaInicio: horarios[d].inicio, horaFim: horarios[d].fim })
    );
    try {
      await api.definirHorarios(lista);
      alert("Horários salvos!");
    } catch (err) {
      setErro((err as Error).message);
    }
  }

  function setDia(dia: string, campo: "inicio" | "fim", valor: string) {
    setHorarios((h) => ({ ...h, [dia]: { ...(h[dia] || { inicio: "", fim: "" }), [campo]: valor } }));
  }

  function sair() {
    clearToken();
    router.replace("/login");
  }

  if (carregando) return <main style={wrap}><p>Carregando...</p></main>;

  return (
    <main style={wrap}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <h1>Painel</h1>
        <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
          <button style={linkBtn} onClick={() => router.push("/painel/agenda")}>📅 Agenda</button>
          <button style={linkBtn} onClick={sair}>Sair</button>
        </div>
      </div>
      {erro && <p style={{ color: "crimson" }}>{erro}</p>}

      <section style={card}>
        <h2>Link do bot</h2>
        <code style={codeBox}>{botLink}</code>
        <button style={botao} onClick={() => navigator.clipboard.writeText(botLink)}>Copiar</button>
      </section>

      <section style={card}>
        <h2>Serviços</h2>
        {servicos.length === 0 && <p>Nenhum serviço ainda.</p>}
        <ul style={{ listStyle: "none", padding: 0 }}>
          {servicos.map((s) => (
            <li key={s.id} style={linha}>
              <span>{s.nome} — {s.duracaoMin} min{s.preco != null ? ` — R$ ${s.preco}` : ""}</span>
              <button style={linkBtn} onClick={() => removerServico(s.id)}>remover</button>
            </li>
          ))}
        </ul>
        <form onSubmit={adicionarServico} style={{ display: "flex", gap: 8, flexWrap: "wrap", marginTop: 10 }}>
          <input style={input} placeholder="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
          <input style={{ ...input, width: 110 }} type="number" min={1} placeholder="Duração (min)"
            value={duracao} onChange={(e) => setDuracao(Number(e.target.value))} required />
          <input style={{ ...input, width: 110 }} type="number" min={0} step="0.01" placeholder="Preço (opc.)"
            value={preco} onChange={(e) => setPreco(e.target.value)} />
          <button style={botao}>Adicionar</button>
        </form>
      </section>

      <section style={card}>
        <h2>Horários de atendimento</h2>
        {DIAS.map((d) => (
          <div key={d} style={linha}>
            <span style={{ width: 90 }}>{d}</span>
            <input style={{ ...input, width: 110 }} type="time" value={horarios[d]?.inicio || ""}
              onChange={(e) => setDia(d, "inicio", e.target.value)} />
            <input style={{ ...input, width: 110 }} type="time" value={horarios[d]?.fim || ""}
              onChange={(e) => setDia(d, "fim", e.target.value)} />
          </div>
        ))}
        <button style={{ ...botao, marginTop: 10 }} onClick={salvarHorarios}>Salvar horários</button>
        <p style={{ fontSize: 13, color: "#666" }}>Deixe em branco os dias sem atendimento.</p>
      </section>
    </main>
  );
}

const wrap: React.CSSProperties = { fontFamily: "system-ui, sans-serif", maxWidth: 720, margin: "2rem auto", padding: "0 1rem" };
const card: React.CSSProperties = { border: "1px solid #eee", borderRadius: 10, padding: 16, marginTop: 16 };
const linha: React.CSSProperties = { display: "flex", gap: 10, alignItems: "center", padding: "6px 0", borderBottom: "1px solid #f4f4f4" };
const input: React.CSSProperties = { padding: 8, fontSize: 15, border: "1px solid #ccc", borderRadius: 6 };
const botao: React.CSSProperties = { padding: "8px 14px", fontSize: 15, background: "#0070f3", color: "#fff", border: 0, borderRadius: 6, cursor: "pointer" };
const linkBtn: React.CSSProperties = { background: "none", border: 0, color: "#0070f3", cursor: "pointer", fontSize: 14 };
const codeBox: React.CSSProperties = { display: "block", padding: 10, background: "#f4f4f4", borderRadius: 6, wordBreak: "break-all", margin: "8px 0" };
