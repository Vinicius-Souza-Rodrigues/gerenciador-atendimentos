// Cliente HTTP mínimo para a API. O token JWT fica no localStorage.

const BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("token");
}

export function setToken(token: string) {
  localStorage.setItem("token", token);
}

export function clearToken() {
  localStorage.removeItem("token");
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };
  const token = getToken();
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(`${BASE}${path}`, { ...options, headers });

  if (res.status === 204) return undefined as T;

  const texto = await res.text();
  const corpo = texto ? JSON.parse(texto) : null;

  if (!res.ok) {
    const msg = corpo?.mensagem || corpo?.erro || `Erro ${res.status}`;
    throw new Error(msg);
  }
  return corpo as T;
}

export const api = {
  signup: (nome: string, email: string, senha: string) =>
    request<{ contaId: number; botDeepLink: string }>("/auth/signup", {
      method: "POST",
      body: JSON.stringify({ nome, email, senha }),
    }),
  login: (email: string, senha: string) =>
    request<{ token: string }>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, senha }),
    }),
  botLink: () => request<{ botDeepLink: string }>("/conta/bot-link"),
  listarServicos: () => request<Servico[]>("/servicos"),
  criarServico: (s: ServicoInput) =>
    request<Servico>("/servicos", { method: "POST", body: JSON.stringify(s) }),
  removerServico: (id: number) =>
    request<void>(`/servicos/${id}`, { method: "DELETE" }),
  listarHorarios: () => request<Horario[]>("/horarios"),
  definirHorarios: (h: Horario[]) =>
    request<Horario[]>("/horarios", { method: "PUT", body: JSON.stringify(h) }),

  // Fase 5 — agenda
  resumoAgenda: (de: string, ate: string) =>
    request<ResumoDia[]>(`/agenda?de=${de}&ate=${ate}`),
  listarDoDia: (data: string) =>
    request<ItemDoDia[]>(`/agendamentos?data=${data}`),
  cancelarAgendamento: (id: number) =>
    request<void>(`/agendamentos/${id}`, { method: "DELETE" }),
  cadastrarManual: (body: CadastroManualInput) =>
    request<AgendamentoSimples>("/clientes/manual", {
      method: "POST",
      body: JSON.stringify(body),
    }),
};

export type Servico = {
  id: number;
  nome: string;
  duracaoMin: number;
  descricao?: string | null;
  preco?: number | null;
  ativo: boolean;
};

export type ServicoInput = {
  nome: string;
  duracaoMin: number;
  descricao?: string;
  preco?: number | null;
  ativo?: boolean;
};

export type Horario = {
  id?: number;
  diaSemana: string;
  horaInicio: string;
  horaFim: string;
};

export type ResumoDia = {
  data: string;
  quantidade: number;
};

export type ItemDoDia = {
  id: number;
  inicio: string;
  fim: string;
  nomeCliente: string;
  nomeServico: string;
  status: string;
  origem: string;
};

export type CadastroManualInput = {
  nomeCliente: string;
  telefone: string;
  servicoId: number;
  inicio: string;
};

export type AgendamentoSimples = {
  id: number;
  inicio: string;
  fim: string;
  status: string;
};
