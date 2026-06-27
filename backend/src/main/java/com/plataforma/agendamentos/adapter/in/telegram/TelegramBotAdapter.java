package com.plataforma.agendamentos.adapter.in.telegram;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.plataforma.agendamentos.application.exception.RecursoNaoEncontradoException;
import com.plataforma.agendamentos.application.port.in.AgendarUseCase;
import com.plataforma.agendamentos.application.port.in.AgendarUseCase.AgendarCommand;
import com.plataforma.agendamentos.application.port.in.CancelarAgendamentoUseCase;
import com.plataforma.agendamentos.application.port.in.ConsultarDisponibilidadeUseCase;
import com.plataforma.agendamentos.application.port.in.ConsultarDisponibilidadeUseCase.ConsultarDisponibilidadeCommand;
import com.plataforma.agendamentos.application.port.in.GerenciarServicoUseCase;
import com.plataforma.agendamentos.application.port.in.ListarAgendamentosClienteUseCase;
import com.plataforma.agendamentos.application.port.in.RemarcarAgendamentoUseCase;
import com.plataforma.agendamentos.application.port.in.ResolverContaPorTokenUseCase;
import com.plataforma.agendamentos.domain.agendamento.Agendamento;
import com.plataforma.agendamentos.domain.agendamento.ConflitoDeAgendamentoException;
import com.plataforma.agendamentos.domain.agendamento.HorarioIndisponivelException;
import com.plataforma.agendamentos.domain.agendamento.OrigemAgendamento;
import com.plataforma.agendamentos.domain.agendamento.Slot;
import com.plataforma.agendamentos.domain.servico.Servico;

@Component
public class TelegramBotAdapter implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotAdapter.class);

    private static final DateTimeFormatter SLOT_FMT     = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_FMT     = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter DATA_HORA_FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final DateTimeFormatter CB_DATA_FMT  = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter CB_SLOT_FMT  = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final TelegramClient telegramClient;
    private final BotSessionStore sessionStore;
    private final ResolverContaPorTokenUseCase resolverConta;
    private final GerenciarServicoUseCase gerenciarServico;
    private final ConsultarDisponibilidadeUseCase consultarDisponibilidade;
    private final AgendarUseCase agendar;
    private final ListarAgendamentosClienteUseCase listarAgendamentos;
    private final CancelarAgendamentoUseCase cancelar;
    private final RemarcarAgendamentoUseCase remarcar;

    public TelegramBotAdapter(TelegramClient telegramClient,
                               BotSessionStore sessionStore,
                               ResolverContaPorTokenUseCase resolverConta,
                               GerenciarServicoUseCase gerenciarServico,
                               ConsultarDisponibilidadeUseCase consultarDisponibilidade,
                               AgendarUseCase agendar,
                               ListarAgendamentosClienteUseCase listarAgendamentos,
                               CancelarAgendamentoUseCase cancelar,
                               RemarcarAgendamentoUseCase remarcar) {
        this.telegramClient = telegramClient;
        this.sessionStore = sessionStore;
        this.resolverConta = resolverConta;
        this.gerenciarServico = gerenciarServico;
        this.consultarDisponibilidade = consultarDisponibilidade;
        this.agendar = agendar;
        this.listarAgendamentos = listarAgendamentos;
        this.cancelar = cancelar;
        this.remarcar = remarcar;
    }

    @Override
    public void consume(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update);
            } else if (update.hasCallbackQuery()) {
                handleCallback(update);
            }
        } catch (Exception e) {
            log.error("Erro inesperado ao processar update {}", update.getUpdateId(), e);
        }
    }

    // ── mensagens de texto ───────────────────────────────────────────────────

    private void handleMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String texto = update.getMessage().getText().trim();
        String nome  = update.getMessage().getFrom().getFirstName();

        if (texto.startsWith("/start")) {
            handleStart(chatId, userId, nome, texto);
        } else if (texto.equals("/cancelar")) {
            handleCancelar(chatId, userId);
        } else if (texto.equals("/remarcar")) {
            handleRemarcar(chatId, userId);
        } else if (texto.equals("/menu") || texto.equals("/servicos")) {
            handleMenu(chatId, userId);
        } else {
            enviar(chatId, "Use /menu para ver os serviços, /cancelar ou /remarcar.");
        }
    }

    private void handleStart(long chatId, long userId, String nomeUsuario, String texto) {
        String[] partes = texto.split(" ", 2);
        if (partes.length < 2 || partes[1].isBlank()) {
            enviar(chatId, "Link inválido. Peça um novo link ao estabelecimento.");
            return;
        }
        try {
            var conta = resolverConta.resolver(partes[1].trim());
            sessionStore.salvar(userId, BotSession.inicial(conta.id()));
            enviar(chatId, "Olá, " + nomeUsuario + "! Bem-vindo ao " + conta.nome() + ".");
            mostrarServicos(chatId, conta.id());
        } catch (RecursoNaoEncontradoException e) {
            enviar(chatId, "Link inválido ou expirado. Peça um novo link ao estabelecimento.");
        }
    }

    private void handleCancelar(long chatId, long userId) {
        Optional<BotSession> sessao = sessionStore.buscar(userId);
        if (sessao.isEmpty()) {
            enviar(chatId, "Primeiro acesse pelo link do estabelecimento.");
            return;
        }
        BotSession s = sessao.get();
        List<Agendamento> ags = listarAgendamentos.listar(s.contaId(), userId);
        if (ags.isEmpty()) {
            enviar(chatId, "Você não tem agendamentos confirmados.");
            return;
        }
        sessionStore.salvar(userId, s.comStep(BotStep.AGUARDANDO_CANCELAMENTO_SELECAO));
        enviar(chatId, "Qual agendamento deseja cancelar?",
                teclado(ags, nomesServico(s.contaId()), "ca"));
    }

    private void handleRemarcar(long chatId, long userId) {
        Optional<BotSession> sessao = sessionStore.buscar(userId);
        if (sessao.isEmpty()) {
            enviar(chatId, "Primeiro acesse pelo link do estabelecimento.");
            return;
        }
        BotSession s = sessao.get();
        List<Agendamento> ags = listarAgendamentos.listar(s.contaId(), userId);
        if (ags.isEmpty()) {
            enviar(chatId, "Você não tem agendamentos confirmados.");
            return;
        }
        sessionStore.salvar(userId, s.comStep(BotStep.AGUARDANDO_REMARCAR_SELECAO));
        enviar(chatId, "Qual agendamento deseja remarcar?",
                teclado(ags, nomesServico(s.contaId()), "ra"));
    }

    private void handleMenu(long chatId, long userId) {
        sessionStore.buscar(userId).ifPresentOrElse(
                s -> {
                    sessionStore.salvar(userId, s.comStep(BotStep.AGUARDANDO_SERVICO));
                    mostrarServicos(chatId, s.contaId());
                },
                () -> enviar(chatId, "Primeiro acesse pelo link do estabelecimento."));
    }

    // ── callbacks de teclado inline ──────────────────────────────────────────

    private void handleCallback(Update update) {
        var cq   = update.getCallbackQuery();
        long chatId = cq.getMessage().getChatId();
        long userId = cq.getFrom().getId();
        String nome = cq.getFrom().getFirstName();
        String data = cq.getData();

        responderCallback(cq.getId());

        Optional<BotSession> sessao = sessionStore.buscar(userId);
        if (sessao.isEmpty()) {
            enviar(chatId, "Sessão expirada. Acesse pelo link do estabelecimento.");
            return;
        }
        BotSession s = sessao.get();

        try {
            if (data.startsWith("s:")) {
                // Serviço selecionado → mostrar datas
                Long servicoId = Long.parseLong(data.substring(2));
                sessionStore.salvar(userId, s.comServico(servicoId));
                mostrarDatas(chatId, s.contaId(), servicoId);

            } else if (data.startsWith("d:")) {
                // d:<yyyyMMdd>:<servicoId> — data selecionada para agendar
                String[] p = data.substring(2).split(":");
                LocalDate dia = LocalDate.parse(p[0], CB_DATA_FMT);
                Long servicoId = Long.parseLong(p[1]);
                sessionStore.salvar(userId, s.comData(dia));
                mostrarSlots(chatId, s.contaId(), servicoId, dia);

            } else if (data.startsWith("sl:")) {
                // sl:<yyyyMMddHHmm>:<servicoId> — slot selecionado → agendar
                String[] p = data.substring(3).split(":");
                LocalDateTime inicio = LocalDateTime.parse(p[0], CB_SLOT_FMT);
                Long servicoId = Long.parseLong(p[1]);
                var ag = agendar.agendar(new AgendarCommand(
                        s.contaId(), userId, nome, servicoId, inicio, OrigemAgendamento.BOT));
                sessionStore.salvar(userId, s.comStep(BotStep.AGUARDANDO_SERVICO));
                enviar(chatId, "Agendado para " + ag.inicio().format(DATA_HORA_FMT) + "! ✓"
                        + "\n\nUse /cancelar ou /remarcar se precisar.");

            } else if (data.startsWith("ca:")) {
                // Cancelar agendamento selecionado
                Long agId = Long.parseLong(data.substring(3));
                cancelar.cancelar(s.contaId(), agId);
                sessionStore.salvar(userId, s.comStep(BotStep.AGUARDANDO_SERVICO));
                enviar(chatId, "Agendamento cancelado.");

            } else if (data.startsWith("ra:")) {
                // ra:<agendamentoId> — selecionar agendamento para remarcar
                Long agId = Long.parseLong(data.substring(3));
                Long servicoId = servicoIdDoAgendamento(s.contaId(), userId, agId);
                // Armazena tanto agendamentoId quanto servicoId na sessão
                sessionStore.salvar(userId, new BotSession(
                        s.contaId(), BotStep.AGUARDANDO_REMARCAR_NOVA_DATA, servicoId, agId, null));
                mostrarDatasRemarcar(chatId, s.contaId(), servicoId, agId);

            } else if (data.startsWith("rd:")) {
                // rd:<yyyyMMdd>:<agendamentoId> — data selecionada para remarcar
                String[] p = data.substring(3).split(":");
                LocalDate dia = LocalDate.parse(p[0], CB_DATA_FMT);
                Long agId = Long.parseLong(p[1]);
                Long servicoId = s.servicoId(); // gravado na etapa ra:
                sessionStore.salvar(userId, s.comNovaData(servicoId, dia));
                mostrarSlotsRemarcar(chatId, s.contaId(), servicoId, dia, agId);

            } else if (data.startsWith("rs:")) {
                // rs:<yyyyMMddHHmm>:<agendamentoId> — novo slot selecionado → remarcar
                String[] p = data.substring(3).split(":");
                LocalDateTime novoInicio = LocalDateTime.parse(p[0], CB_SLOT_FMT);
                Long agId = Long.parseLong(p[1]);
                remarcar.remarcar(s.contaId(), agId, novoInicio);
                sessionStore.salvar(userId, s.comStep(BotStep.AGUARDANDO_SERVICO));
                enviar(chatId, "Remarcado para " + novoInicio.format(DATA_HORA_FMT) + "! ✓");
            }

        } catch (HorarioIndisponivelException | ConflitoDeAgendamentoException e) {
            enviar(chatId, "Esse horário não está mais disponível. Escolha outro.");
            sessionStore.salvar(userId, s.comStep(BotStep.AGUARDANDO_SERVICO));
            mostrarServicos(chatId, s.contaId());
        } catch (RecursoNaoEncontradoException e) {
            enviar(chatId, "Não encontrado. Tente novamente com /menu.");
        }
    }

    // ── helpers de UI ────────────────────────────────────────────────────────

    private void mostrarServicos(long chatId, Long contaId) {
        List<Servico> ativos = gerenciarServico.listar(contaId)
                .stream().filter(Servico::ativo).toList();
        if (ativos.isEmpty()) {
            enviar(chatId, "O estabelecimento ainda não tem serviços cadastrados.");
            return;
        }
        List<InlineKeyboardRow> linhas = ativos.stream()
                .map(s -> new InlineKeyboardRow(InlineKeyboardButton.builder()
                        .text(s.nome() + " (" + s.duracaoMin() + "min"
                                + (s.preco() != null ? " · R$" + s.preco() : "") + ")")
                        .callbackData("s:" + s.id())
                        .build()))
                .collect(Collectors.toList());
        enviar(chatId, "Escolha o serviço:", InlineKeyboardMarkup.builder().keyboard(linhas).build());
    }

    private void mostrarDatas(long chatId, Long contaId, Long servicoId) {
        LocalDate hoje = LocalDate.now();
        List<Slot> slots = consultarDisponibilidade.consultar(
                new ConsultarDisponibilidadeCommand(contaId, servicoId, hoje.plusDays(1), hoje.plusDays(30)));
        exibirDatas(chatId, slots, "d", servicoId.toString());
    }

    private void mostrarDatasRemarcar(long chatId, Long contaId, Long servicoId, Long agId) {
        LocalDate hoje = LocalDate.now();
        List<Slot> slots = consultarDisponibilidade.consultar(
                new ConsultarDisponibilidadeCommand(contaId, servicoId, hoje.plusDays(1), hoje.plusDays(30)));
        exibirDatas(chatId, slots, "rd", agId.toString());
    }

    private void exibirDatas(long chatId, List<Slot> slots, String prefixo, String sufixo) {
        List<LocalDate> datas = slots.stream()
                .map(s -> s.inicio().toLocalDate()).distinct().sorted().toList();
        if (datas.isEmpty()) {
            enviar(chatId, "Sem horários disponíveis nos próximos 30 dias.");
            return;
        }
        List<InlineKeyboardRow> linhas = new ArrayList<>();
        InlineKeyboardRow linha = new InlineKeyboardRow();
        for (LocalDate d : datas) {
            linha.add(InlineKeyboardButton.builder()
                    .text(d.format(DATA_FMT))
                    .callbackData(prefixo + ":" + d.format(CB_DATA_FMT) + ":" + sufixo)
                    .build());
            if (linha.size() == 4) { linhas.add(linha); linha = new InlineKeyboardRow(); }
        }
        if (!linha.isEmpty()) linhas.add(linha);
        enviar(chatId, "Escolha a data:", InlineKeyboardMarkup.builder().keyboard(linhas).build());
    }

    private void mostrarSlots(long chatId, Long contaId, Long servicoId, LocalDate dia) {
        List<Slot> slots = consultarDisponibilidade.consultar(
                new ConsultarDisponibilidadeCommand(contaId, servicoId, dia, dia));
        exibirSlots(chatId, slots, "sl", servicoId.toString());
    }

    private void mostrarSlotsRemarcar(long chatId, Long contaId, Long servicoId, LocalDate dia, Long agId) {
        List<Slot> slots = consultarDisponibilidade.consultar(
                new ConsultarDisponibilidadeCommand(contaId, servicoId, dia, dia));
        exibirSlots(chatId, slots, "rs", agId.toString());
    }

    private void exibirSlots(long chatId, List<Slot> slots, String prefixo, String sufixo) {
        if (slots.isEmpty()) {
            enviar(chatId, "Sem horários disponíveis nessa data. Escolha outra.");
            return;
        }
        List<InlineKeyboardRow> linhas = new ArrayList<>();
        InlineKeyboardRow linha = new InlineKeyboardRow();
        for (Slot slot : slots) {
            linha.add(InlineKeyboardButton.builder()
                    .text(slot.inicio().format(SLOT_FMT))
                    .callbackData(prefixo + ":" + slot.inicio().format(CB_SLOT_FMT) + ":" + sufixo)
                    .build());
            if (linha.size() == 3) { linhas.add(linha); linha = new InlineKeyboardRow(); }
        }
        if (!linha.isEmpty()) linhas.add(linha);
        enviar(chatId, "Escolha o horário:", InlineKeyboardMarkup.builder().keyboard(linhas).build());
    }

    private InlineKeyboardMarkup teclado(List<Agendamento> ags, Map<Long, String> nomes, String prefixo) {
        List<InlineKeyboardRow> linhas = ags.stream()
                .map(a -> new InlineKeyboardRow(InlineKeyboardButton.builder()
                        .text(nomes.getOrDefault(a.servicoId(), "Serviço")
                                + " — " + a.inicio().format(DATA_HORA_FMT))
                        .callbackData(prefixo + ":" + a.id())
                        .build()))
                .collect(Collectors.toList());
        return InlineKeyboardMarkup.builder().keyboard(linhas).build();
    }

    private Map<Long, String> nomesServico(Long contaId) {
        return gerenciarServico.listar(contaId).stream()
                .collect(Collectors.toMap(Servico::id, Servico::nome));
    }

    private Long servicoIdDoAgendamento(Long contaId, long telegramUserId, Long agId) {
        return listarAgendamentos.listar(contaId, telegramUserId).stream()
                .filter(a -> a.id().equals(agId))
                .findFirst()
                .map(Agendamento::servicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado."));
    }

    private void enviar(long chatId, String texto) {
        try {
            telegramClient.execute(SendMessage.builder().chatId(chatId).text(texto).build());
        } catch (Exception e) {
            log.error("Falha ao enviar mensagem para chatId {}", chatId, e);
        }
    }

    private void enviar(long chatId, String texto, InlineKeyboardMarkup teclado) {
        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId).text(texto).replyMarkup(teclado).build());
        } catch (Exception e) {
            log.error("Falha ao enviar mensagem com teclado para chatId {}", chatId, e);
        }
    }

    private void responderCallback(String callbackQueryId) {
        try {
            telegramClient.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId).build());
        } catch (Exception e) {
            log.warn("Falha ao responder callback {}", callbackQueryId, e);
        }
    }
}
