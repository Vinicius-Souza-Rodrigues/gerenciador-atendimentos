"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  api,
  getToken,
  ItemDoDia,
  ResumoDia,
  Servico,
} from "../../../lib/api";

function hojeMaisN(n: number): string {
  const d = new Date();
  d.setDate(d.getDate() + n);
  return d.toISOString().slice(0, 10);
}

function formatarHora(iso: string) {
  return iso.slice(11, 16);
}

function formatarData(iso: string) {
  const [ano, mes, dia] = iso.split("-");
  return `${dia}/${mes}/${ano}`;
}

export default function AgendaPage() {
  const router = useRouter();
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const [resumo, setResumo] = useState<ResumoDia[]>([]);
  const [diaSelecionado, setDiaSelecionado] = useState<string | null>(null);
  const [itensDia, setItensDia] = useState<ItemDoDia[]>([]);
  const [carregandoDia, setCarregandoDia] = useState(false);

  const [servicos, setServicos] = useState<Servico[]>([]);

  // formulário cadastro manual
  const [nomeCliente, setNomeCliente] = useState("");
  const [telefone, setTelefone] = useState("");
  const [servicoId, setServicoId] = useState<number | "">("");
  const [inicioManual, setInicioManual] = useState("");
  const [erroManual, setErroManual] = useState<string | null>(null);
  const [sucessoManual, setSucessoManual] = useState<string | null>(null);

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
      const [res, servs] = await Promise.all([
        api.resumoAgenda(hojeMaisN(0), hojeMaisN(30)),
        api.listarServicos(),
      ]);
      setResumo(res);
      setServicos(servs.filter((s) => s.ativo));
    } catch (err) {
      setErro((err as Error).message);
    } finally {
      setCarregando(false);
    }
  }

  async function selecionarDia(data: string) {
    setDiaSelecionado(data);
    setCarregandoDia(true);
    setItensDia([]);
    try {
      const itens = await api.listarDoDia(data);
      setItensDia(itens);
    } catch (err) {
      setErro((err as Error).message);
    } finally {
      setCarregandoDia(false);
    }
  }

  async function cancelar(id: number) {
    if (!confirm("Cancelar este agendamento?")) return;
    try {
      await api.cancelarAgendamento(id);
      setItensDia((prev) => prev.filter((i) => i.id !== id));
      setResumo((prev) =>
        prev
          .map((r) =>
            r.data === diaSelecionado
              ? { ...r, quantidade: r.quantidade - 1 }
              : r
          )
          .filter((r) => r.quantidade > 0)
      );
    } catch (err) {
      setErro((err as Error).message);
    }
  }

  async function cadastrarManual(e: React.FormEvent) {
    e.preventDefault();
    setErroManual(null);
    setSucessoManual(null);
    try {
      const ag = await api.cadastrarManual({
        nomeCliente,
        telefone,
        servicoId: Number(servicoId),
        inicio: inicioManual,
      });
      setSucessoManual(
        `Agendado! ID ${ag.id} — ${formatarHora(ag.inicio)}`
      );
      setNomeCliente("");
      setTelefone("");
      setServicoId("");
      setInicioManual("");
      await carregarTudo();
      if (diaSelecionado && ag.inicio.startsWith(diaSelecionado)) {
        await selecionarDia(diaSelecionado);
      }
    } catch (err) {
      setErroManual((err as Error).message);
    }
  }

  if (carregando) return <main style={wrap}><p>Carregando...</p></main>;

  return (
    <main style={wrap}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <h1>Agenda</h1>
        <button style={linkBtn} onClick={() => router.push("/painel")}>← Painel</button>
      </div>
      {erro && <p style={{ color: "crimson" }}>{erro}</p>}

      {/* Calendário — próximos 30 dias */}
      <section style={card}>
        <h2>Próximos 30 dias</h2>
        {resumo.length === 0 && <p style={{ color: "#888" }}>Nenhum agendamento no período.</p>}
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, marginTop: 8 }}>
          {resumo.map((r) => (
            <button
              key={r.data}
              style={{
                ...diaBtn,
                background: diaSelecionado === r.data ? "#0070f3" : "#f0f4ff",
                color: diaSelecionado === r.data ? "#fff" : "#0070f3",
              }}
              onClick={() => selecionarDia(r.data)}
            >
              <strong>{formatarData(r.data)}</strong>
              <span style={{ fontSize: 12 }}>{r.quantidade} ag.</span>
            </button>
          ))}
        </div>
      </section>

      {/* Detalhe do dia */}
      {diaSelecionado && (
        <section style={card}>
          <h2>Agendamentos — {formatarData(diaSelecionado)}</h2>
          {carregandoDia && <p>Carregando...</p>}
          {!carregandoDia && itensDia.length === 0 && (
            <p style={{ color: "#888" }}>Nenhum agendamento neste dia.</p>
          )}
          <ul style={{ listStyle: "none", padding: 0 }}>
            {itensDia.map((item) => (
              <li key={item.id} style={linhaItem}>
                <div>
                  <strong>{formatarHora(item.inicio)} – {formatarHora(item.fim)}</strong>
                  {" · "}
                  {item.nomeCliente}
                  {" · "}
                  <em>{item.nomeServico}</em>
                  {item.origem === "MANUAL" && (
                    <span style={badge}>manual</span>
                  )}
                </div>
                <button style={cancelBtn} onClick={() => cancelar(item.id)}>Cancelar</button>
              </li>
            ))}
          </ul>
        </section>
      )}

      {/* Cadastro manual */}
      <section style={card}>
        <h2>Cadastrar cliente manualmente</h2>
        {erroManual && <p style={{ color: "crimson" }}>{erroManual}</p>}
        {sucessoManual && <p style={{ color: "green" }}>{sucessoManual}</p>}
        <form onSubmit={cadastrarManual} style={{ display: "flex", flexDirection: "column", gap: 10, maxWidth: 420 }}>
          <input style={input} placeholder="Nome do cliente" value={nomeCliente}
            onChange={(e) => setNomeCliente(e.target.value)} required />
          <input style={input} placeholder="Telefone (ex: 11999999999)" value={telefone}
            onChange={(e) => setTelefone(e.target.value)} required />
          <select style={input} value={servicoId}
            onChange={(e) => setServicoId(e.target.value ? Number(e.target.value) : "")} required>
            <option value="">Selecione o serviço</option>
            {servicos.map((s) => (
              <option key={s.id} value={s.id}>{s.nome} ({s.duracaoMin} min)</option>
            ))}
          </select>
          <input style={input} type="datetime-local" value={inicioManual}
            onChange={(e) => setInicioManual(e.target.value)} required />
          <button style={botao} type="submit">Agendar</button>
        </form>
      </section>
    </main>
  );
}

const wrap: React.CSSProperties = {
  fontFamily: "system-ui, sans-serif", maxWidth: 760, margin: "2rem auto", padding: "0 1rem",
};
const card: React.CSSProperties = {
  border: "1px solid #eee", borderRadius: 10, padding: 16, marginTop: 16,
};
const diaBtn: React.CSSProperties = {
  display: "flex", flexDirection: "column", alignItems: "center",
  padding: "10px 14px", borderRadius: 8, border: "none", cursor: "pointer", gap: 2, minWidth: 90,
};
const linhaItem: React.CSSProperties = {
  display: "flex", justifyContent: "space-between", alignItems: "center",
  padding: "8px 0", borderBottom: "1px solid #f4f4f4", gap: 10,
};
const badge: React.CSSProperties = {
  marginLeft: 6, padding: "2px 6px", fontSize: 11, background: "#e8f4fd",
  color: "#0070f3", borderRadius: 4,
};
const input: React.CSSProperties = {
  padding: 9, fontSize: 15, border: "1px solid #ccc", borderRadius: 6, width: "100%", boxSizing: "border-box",
};
const botao: React.CSSProperties = {
  padding: "9px 16px", fontSize: 15, background: "#0070f3", color: "#fff",
  border: 0, borderRadius: 6, cursor: "pointer",
};
const cancelBtn: React.CSSProperties = {
  padding: "5px 10px", fontSize: 13, background: "none", border: "1px solid #e00",
  color: "#e00", borderRadius: 6, cursor: "pointer", whiteSpace: "nowrap",
};
const linkBtn: React.CSSProperties = {
  background: "none", border: 0, color: "#0070f3", cursor: "pointer", fontSize: 14,
};
