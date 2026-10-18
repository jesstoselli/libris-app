package app.billfolder.libris.ui.feature.bookdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.billfolder.libris.domain.model.Book
import app.billfolder.libris.domain.model.BookOwnership
import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.domain.model.ReadingLog
import app.billfolder.libris.domain.rules.readingDatesProblem
import app.billfolder.libris.domain.time.today
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.common.message
import app.billfolder.libris.ui.components.BookCover
import app.billfolder.libris.ui.components.ConfirmDialog
import app.billfolder.libris.ui.components.LibrisDatePicker
import app.billfolder.libris.ui.components.LocalSnackbar
import app.billfolder.libris.ui.components.PaperCard
import app.billfolder.libris.ui.components.PrimaryButton
import app.billfolder.libris.ui.components.ProgressBar
import app.billfolder.libris.ui.components.Refreshable
import app.billfolder.libris.ui.components.RuledField
import app.billfolder.libris.ui.components.STATUS_LABELS
import app.billfolder.libris.ui.components.Stamp
import app.billfolder.libris.ui.format.formatDate
import app.billfolder.libris.ui.format.formatName
import app.billfolder.libris.ui.format.formatShortDate
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val STATUS_TONES = mapOf(
    BookStatus.NotStarted to Libris.brass,
    BookStatus.Reading to Libris.oxblood,
    BookStatus.Read to Libris.inkSoft,
    BookStatus.Abandoned to Libris.ink,
)

@Composable
fun BookDetailScreen(
    bookId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: BookDetailViewModel = koinViewModel(key = bookId) { parametersOf(bookId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val type = LocalLibrisType.current
    val snackbar = LocalSnackbar.current
    var firstResume by remember { mutableStateOf(true) }
    // Back from "Editar livro": show what was just saved.
    LifecycleResumeEffect(Unit) {
        if (firstResume) firstResume = false else viewModel.reload()
        onPauseOrDispose { }
    }
    LaunchedEffect(viewModel) { viewModel.messages.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }

    Refreshable(state.refreshing, viewModel::pullToRefresh) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "‹ Voltar",
                        fontFamily = type.body,
                        color = Libris.deskText,
                        modifier = Modifier.clickable(onClick = onBack).minimumInteractiveComponentSize(),
                    )
                    Spacer(Modifier.weight(1f))
                    if (state.book is Load.Loaded) Text(
                        "✎ Editar livro",
                        fontFamily = type.body,
                        color = Libris.deskText,
                        modifier = Modifier.clickable(onClick = onEdit).minimumInteractiveComponentSize(),
                    )
                }
                when {
                    state.notFound -> PaperCard(Modifier.fillMaxWidth()) {
                        Text(
                            "Livro não encontrado.",
                            fontFamily = type.body
                        )
                    }

                    else -> when (val load = state.book) {
                        Load.Loading -> Text(
                            "Carregando…",
                            fontFamily = type.body,
                            color = Libris.deskTextSoft
                        )

                        is Load.Failed -> Text(
                            load.message,
                            fontFamily = type.body,
                            color = Libris.deskText
                        )

                        // One continuous sheet: the book, then reading (or "Comprei"), then the destructive action last.
                        is Load.Loaded -> PaperCard(Modifier.fillMaxWidth(), padding = 20.dp) {
                            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                BookSheet(load.value, viewModel)
                                SheetDivider()
                                if (load.value.ownership == BookOwnership.Owned) ReadingSheet(state, viewModel) else AcquireSheet(load.value, viewModel)
                                SheetDivider()
                                DeleteBook(load.value, viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookSheet(book: Book, viewModel: BookDetailViewModel) {
    val type = LocalLibrisType.current
    var editingDates by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BookCover(
                book.coverUrl,
                book.title,
                width = 104.dp,
                height = 154.dp,
                fontSize = 13.sp
            )
            Column(Modifier.weight(1f)) {
                Text(
                    "FICHA",
                    fontFamily = type.title,
                    fontSize = 13.sp,
                    letterSpacing = 1.5.sp,
                    color = Libris.oxblood
                )
                Text(
                    book.title,
                    fontFamily = type.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    lineHeight = 26.sp,
                    color = Libris.oxblood
                )
                Text(
                    "de ${book.authors.joinToString(", ")}",
                    fontFamily = type.body,
                    fontSize = 14.sp,
                    color = Libris.inkSoft
                )
                Text(
                    "Formato: ${formatName(book.formats).lowercase()}",
                    fontFamily = type.body,
                    fontSize = 14.sp,
                    color = Libris.inkSoft
                )
                publication(book)?.let {
                    Text(
                        it,
                        fontFamily = type.body,
                        fontSize = 14.sp,
                        color = Libris.inkSoft
                    )
                }
            }
        }
        if (book.genres.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                book.genres.forEach {
                    Text(
                        it.name,
                        fontFamily = type.body,
                        fontSize = 12.sp,
                        color = Libris.inkSoft,
                        modifier = Modifier
                            .drawBehind {
                                drawLine(
                                    Libris.brass,
                                    Offset(0f, size.height),
                                    Offset(size.width, size.height),
                                    1.dp.toPx(),
                                )
                            }
                            .padding(horizontal = 3.dp, vertical = 4.dp),
                    )
                }
            }
        }
        book.synopsis?.let {
            Text(
                it,
                fontFamily = type.body,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        if (book.ownership == BookOwnership.Owned) {
            Text(
                if (book.status == BookStatus.Read) "Progresso · Lido · ${book.totalPages} págs." else "Progresso · pág. ${book.currentPage} de ${book.totalPages} (${book.percent}%)",
                fontFamily = type.body,
                fontSize = 14.sp,
                color = Libris.inkSoft
            )
            ProgressBar(if (book.status == BookStatus.Read) 100 else book.percent)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BookStatus.entries.forEach { s ->
                    Stamp(
                        STATUS_LABELS.getValue(s),
                        book.status == s,
                        { viewModel.changeStatus(s) },
                        tone = STATUS_TONES.getValue(s)
                    )
                }
            }
            Text(
                "Início: ${book.startedAt?.let(::formatDate) ?: "—"} · Término: ${
                    book.finishedAt?.let(
                        ::formatDate
                    ) ?: "—"
                }",
                fontFamily = type.body, fontSize = 14.sp, color = Libris.inkSoft,
            )
            if (editingDates) {
                EditDates(
                    book,
                    onSave = { s, f -> viewModel.saveDates(s, f); editingDates = false },
                    onCancel = { editingDates = false })
            } else {
                Text(
                    "✎ editar datas",
                    fontFamily = type.body,
                    fontSize = 14.sp,
                    color = Libris.oxblood,
                    modifier = Modifier.clickable { editingDates = true }.minimumInteractiveComponentSize())
            }
        }
    }
}

/** Kept apart at the end of the sheet so it never competes with "Anotar". */
@Composable
private fun DeleteBook(book: Book, viewModel: BookDetailViewModel) {
    var confirmDelete by remember { mutableStateOf(false) }
    Text(
        "Apagar livro",
        fontFamily = LocalLibrisType.current.body,
        fontSize = 14.sp,
        color = Libris.inkSoft,
        modifier = Modifier.clickable { confirmDelete = true }.minimumInteractiveComponentSize(),
    )
    if (confirmDelete) {
        ConfirmDialog(
            "Apagar \"${book.title}\"? Os registros de leitura vão junto.",
            "Apagar livro",
            onConfirm = { confirmDelete = false; viewModel.deleteBook() },
            onDismiss = { confirmDelete = false },
        )
    }
}

private fun publication(book: Book): String? {
    val edition = listOfNotNull(book.publisher, book.publishedYear?.toString()).joinToString(", ")
    val pages = book.totalPages?.let { "$it págs." }
    return listOf(edition, pages.orEmpty()).filter { it.isNotBlank() }.joinToString(" · ")
        .ifBlank { null }
}

@Composable
private fun EditDates(book: Book, onSave: (LocalDate?, LocalDate?) -> Unit, onCancel: () -> Unit) {
    val type = LocalLibrisType.current
    var start by remember { mutableStateOf(book.startedAt) }
    var end by remember { mutableStateOf(book.finishedAt) }
    var picking by remember { mutableStateOf<String?>(null) }
    val problem = readingDatesProblem(start, end, today())?.message()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DateChip("Início", start, { picking = "start" }, { start = null })
            DateChip("Término", end, { picking = "end" }, { end = null })
        }
        problem?.let { Text(it, fontFamily = type.body, fontSize = 13.sp, color = Libris.oxblood) }
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                "Cancelar",
                fontFamily = type.body,
                color = Libris.inkSoft,
                modifier = Modifier.clickable(onClick = onCancel).minimumInteractiveComponentSize()
            )
            Text(
                "Salvar datas",
                fontFamily = type.title,
                color = if (problem == null) Libris.oxblood else Libris.inkSoft,
                modifier = Modifier.clickable(enabled = problem == null) { onSave(start, end) }.minimumInteractiveComponentSize())
        }
    }
    picking?.let { which ->
        LibrisDatePicker(
            if (which == "start") start else end,
            today(),
            onPicked = { if (which == "start") start = it else end = it },
            onDismiss = { picking = null })
    }
}

@Composable
private fun DateChip(label: String, value: LocalDate?, onPick: () -> Unit, onClear: () -> Unit) {
    val type = LocalLibrisType.current
    Column {
        Text(label, fontFamily = type.body, fontSize = 12.sp, color = Libris.inkSoft)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value?.let(::formatDate) ?: "escolher",
                fontFamily = type.body,
                fontSize = 16.sp,
                color = Libris.ink,
                modifier = Modifier.clickable(onClick = onPick).minimumInteractiveComponentSize()
            )
            if (value != null) Text(
                "×",
                color = Libris.inkSoft,
                modifier = Modifier.clickable(onClick = onClear).minimumInteractiveComponentSize()
            )
        }
    }
}

@Composable
private fun ReadingSheet(state: BookDetailState, viewModel: BookDetailViewModel) {
    val type = LocalLibrisType.current
    var pickingDate by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ReadingLog?>(null) }
    var deleting by remember { mutableStateOf<ReadingLog?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Registrar leitura")
        RuledField(
            "Parei na página",
            state.pageInput,
            viewModel::onPage,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            error = state.pageProblem ?: state.logError,
        )
        Column {
            Text("Data", fontFamily = type.body, fontSize = 13.sp, color = Libris.inkSoft)
            Text(
                if (state.date == today()) "hoje, ${formatDate(state.date)}" else formatDate(
                    state.date
                ),
                fontFamily = type.body,
                fontSize = 17.sp,
                modifier = Modifier.clickable { pickingDate = true }.minimumInteractiveComponentSize()
            )
        }
        PrimaryButton(
            "Anotar",
            enabled = state.canLog && state.pageProblem == null,
            onClick = viewModel::addLog
        )

        SectionTitle("Histórico", Modifier.padding(top = 12.dp))
        if (state.logs.isEmpty()) {
            Text(
                "Nenhuma leitura anotada ainda.",
                fontFamily = type.body,
                fontWeight = FontWeight.Light,
                color = Libris.inkSoft
            )
        }
        state.logs.forEach { log ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${formatShortDate(log.date)} · pág. ${if (log.fromPage == 0) "início" else log.fromPage} → ${log.page} · +${log.pagesRead}",
                    fontFamily = type.body, fontSize = 14.sp, modifier = Modifier.weight(1f),
                )
                Text(
                    "✎",
                    color = Libris.inkSoft,
                    modifier = Modifier.clickable { editing = log }.minimumInteractiveComponentSize()
                )
                Text(
                    "×",
                    color = Libris.inkSoft,
                    modifier = Modifier.clickable { deleting = log }.minimumInteractiveComponentSize()
                )
            }
        }
    }
    if (pickingDate) LibrisDatePicker(state.date, today(), viewModel::onDate) {
        pickingDate = false
    }
    editing?.let { log ->
        EditLogDialog(
            log,
            onSave = { d, p -> viewModel.editLog(log, d, p); editing = null },
            onDismiss = { editing = null })
    }
    deleting?.let { log ->
        ConfirmDialog(
            "Apagar o registro de ${formatShortDate(log.date)} (pág. ${log.page})?",
            "Apagar",
            onConfirm = { viewModel.deleteLog(log); deleting = null },
            onDismiss = { deleting = null })
    }
}

@Composable
private fun EditLogDialog(log: ReadingLog, onSave: (LocalDate, Int) -> Unit, onDismiss: () -> Unit) {
    var page by remember { mutableStateOf(log.page.toString()) }
    var date by remember { mutableStateOf(log.date) }
    var picking by remember { mutableStateOf(false) }
    val type = LocalLibrisType.current
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Libris.paper,
        title = { Text("Editar registro", fontFamily = type.title, color = Libris.oxblood) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RuledField(
                    "Página",
                    page,
                    { page = it.filter(Char::isDigit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(
                    "Data: ${formatDate(date)}",
                    fontFamily = type.body,
                    modifier = Modifier.clickable { picking = true }.minimumInteractiveComponentSize())
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                page.toIntOrNull()?.let { onSave(date, it) }
            }, enabled = page.toIntOrNull() != null) {
                Text("Salvar", fontFamily = type.title, color = Libris.oxblood)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(
                    "Cancelar",
                    color = Libris.inkSoft
                )
            }
        },
    )
    if (picking) LibrisDatePicker(date, today(), { date = it }) { picking = false }
}

@Composable
private fun AcquireSheet(book: Book, viewModel: BookDetailViewModel) {
    val type = LocalLibrisType.current
    var pages by remember { mutableStateOf("") }
    val needsPages = book.totalPages == null
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Wishlist")
        Text("Ainda não está no acervo.", fontFamily = type.body, color = Libris.inkSoft)
        if (needsPages) RuledField(
            "Páginas totais",
            pages,
            { pages = it.filter(Char::isDigit) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        PrimaryButton(
            "Comprei",
            enabled = !needsPages || (pages.toIntOrNull() ?: 0) >= 1,
            onClick = { viewModel.acquire(if (needsPages) pages.toInt() else null) })
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        fontFamily = LocalLibrisType.current.title,
        fontSize = 15.sp,
        color = Libris.oxblood,
        modifier = modifier
    )
}

@Composable
private fun SheetDivider() = Box(Modifier.fillMaxWidth().height(1.dp).background(Libris.rule.copy(alpha = 0.6f)))
