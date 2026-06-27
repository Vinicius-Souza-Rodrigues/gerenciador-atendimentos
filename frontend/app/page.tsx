import Link from "next/link";

export default function Home() {
  return (
    <main
      style={{
        fontFamily: "system-ui, sans-serif",
        maxWidth: 640,
        margin: "4rem auto",
        padding: "0 1rem",
        lineHeight: 1.6,
      }}
    >
      <h1>Agendamentos</h1>
      <p>Plataforma de agendamento por bot de Telegram — área do dono da conta.</p>
      <p style={{ display: "flex", gap: 12 }}>
        <Link href="/login">Entrar</Link>
        <Link href="/signup">Criar conta</Link>
      </p>
    </main>
  );
}
