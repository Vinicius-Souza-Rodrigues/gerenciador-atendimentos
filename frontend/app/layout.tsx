import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'Gerenciador de Atendimentos',
  description: 'Plataforma de agendamento por bot de Telegram',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  )
}
