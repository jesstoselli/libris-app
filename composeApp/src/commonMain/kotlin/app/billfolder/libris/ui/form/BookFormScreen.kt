package app.billfolder.libris.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.billfolder.libris.shared.Book
import app.billfolder.libris.shared.BookOwnership
import app.billfolder.libris.shared.BookStatus
import app.billfolder.libris.shared.formatIsoDate
import app.billfolder.libris.shared.todayIso
import app.billfolder.libris.ui.components.BookCover
import app.billfolder.libris.ui.components.Ficha
import app.billfolder.libris.ui.components.IsoDatePicker
import app.billfolder.libris.ui.components.LocalSnackbar
import app.billfolder.libris.ui.components.PageHeading
import app.billfolder.libris.ui.components.PrimaryButton
import app.billfolder.libris.ui.components.RuledField
import app.billfolder.libris.ui.components.SessionAction
import app.billfolder.libris.ui.components.STATUS_LABELS
import app.billfolder.libris.ui.components.Stamp
import app.billfolder.libris.ui.platform.IsbnScanner
import app.billfolder.libris.ui.platform.rememberCoverPicker
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BookFormScreen(
    bookId: String?,
    onSaved: (Book) -> Unit,
    onBack: (() -> Unit)?,
    onLogout: (() -> Unit)? = null,
    viewModel: BookFormViewModel = koinViewModel(key = "form-${bookId ?: "new"}") {
        parametersOf(
            bookId
        )
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val existing by viewModel.existingGenres.collectAsStateWithLifecycle()
    val type = LocalLibrisType.current
    val snackbar = LocalSnackbar.current
    var scanning by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf<String?>(null) }
    val picker = rememberCoverPicker(
        onPicked = { viewModel.uploadCover(it.bytes, it.mimeType) },
        onError = { viewModel.touch("cover") })

    LaunchedEffect(viewModel) { viewModel.messages.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(viewModel) { viewModel.results.collect { if (it is FormResult.Saved) onSaved(it.book) } }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())
                .padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            onBack?.let {
                Text(
                    "‹ Voltar",
                    fontFamily = type.body,
                    color = Libris.deskText,
                    modifier = Modifier.clickable(onClick = it)
                )
            }
            PageHeading(
                if (state.isEdit) "Editar livro" else "Registrar livro",
                action = onLogout?.let { logout -> { SessionAction(logout) } },
            )
            state.loadError?.let { Text(it, fontFamily = type.body, color = Libris.deskText) }

            Ficha(Modifier.fillMaxWidth(), padding = 20.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Cover
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val src = viewModel.coverSrc()
                        if (src != null) {
                            BookCover(
                                src,
                                state.title.ifBlank { "Capa" },
                                width = 96.dp,
                                height = 140.dp
                            )
                        } else {
                            Box(
                                Modifier.size(96.dp, 140.dp).border(2.dp, Libris.inkSoft)
                                    .clickable { picker.fromGallery() },
                                contentAlignment = Alignment.Center,
                            ) { Text("Capa", fontFamily = type.title, color = Libris.inkSoft) }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LinkText("Escolher da galeria") { picker.fromGallery() }
                            if (picker.cameraAvailable) LinkText("Fotografar a capa") { picker.fromCamera() }
                            if (src != null) LinkText(
                                "Remover capa",
                                Libris.inkSoft
                            ) { viewModel.removeCover() }
                            if (state.uploading) Text(
                                "Enviando capa…",
                                fontFamily = type.body,
                                fontSize = 13.sp,
                                color = Libris.inkSoft
                            )
                            state.coverError?.let {
                                Text(
                                    it,
                                    fontFamily = type.body,
                                    fontSize = 13.sp,
                                    color = Libris.oxblood
                                )
                            }
                        }
                    }

                    // ISBN
                    Column {
                        RuledField(
                            "ISBN", state.isbn, viewModel::onIsbn,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(onSearch = { viewModel.lookUp() }),
                            trailing = {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    SmallButton("Escanear") { scanning = true }
                                    SmallButton(
                                        if (state.lookingUp) "Buscando…" else "Buscar",
                                        enabled = state.canLookUp
                                    ) { viewModel.lookUp() }
                                }
                            },
                        )
                        state.lookupNote?.let {
                            Text(
                                it,
                                fontFamily = type.body,
                                fontSize = 13.sp,
                                color = Libris.inkSoft
                            )
                        }
                        state.lookupError?.let {
                            Text(
                                it,
                                fontFamily = type.body,
                                fontSize = 13.sp,
                                color = Libris.oxblood
                            )
                        }
                    }

                    RuledField(
                        "Título",
                        state.title,
                        viewModel::onTitle,
                        error = state.shows("title", state.titleError)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Autor(es)",
                            fontFamily = type.body,
                            fontSize = 13.sp,
                            color = Libris.inkSoft
                        )
                        state.authors.forEachIndexed { i, author ->
                            RuledField(
                                if (state.authors.size > 1) "Autor ${i + 1}" else "",
                                author,
                                { viewModel.onAuthor(i, it) },
                                trailing = if (state.authors.size > 1) ({
                                    Text(
                                        "×",
                                        color = Libris.inkSoft,
                                        modifier = Modifier.clickable { viewModel.removeAuthor(i) }
                                            .padding(8.dp)
                                    )
                                }) else null,
                            )
                        }
                        state.shows("authors", state.authorsError)?.let {
                            Text(
                                it,
                                fontFamily = type.body,
                                fontSize = 13.sp,
                                color = Libris.oxblood
                            )
                        }
                        if (state.authors.size < 10) LinkText("+ autor") { viewModel.addAuthor() }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        RuledField(
                            "Páginas totais",
                            state.totalPages,
                            viewModel::onPages,
                            Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            error = state.shows("pages", state.pagesError)
                        )
                        RuledField(
                            "Ano",
                            state.publishedYear,
                            viewModel::onYear,
                            Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            error = state.shows("year", state.yearError)
                        )
                    }
                    RuledField("Editora", state.publisher, viewModel::onPublisher)
                    RuledField("Sinopse", state.synopsis, viewModel::onSynopsis, singleLine = false)

                    GenreChips(state, existing.map { it.name }, viewModel)

                    if (!state.isEdit) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Destino", fontFamily = type.title, fontSize = 16.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Toggle(
                                    "Acervo",
                                    state.ownership == BookOwnership.Owned
                                ) { viewModel.onOwnership(BookOwnership.Owned) }
                                Toggle(
                                    "Wishlist",
                                    state.ownership == BookOwnership.Wishlist
                                ) { viewModel.onOwnership(BookOwnership.Wishlist) }
                            }
                        }
                        if (state.ownership == BookOwnership.Owned) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Situação", fontFamily = type.title, fontSize = 16.sp)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    BookStatus.entries.forEach { s ->
                                        Stamp(
                                            STATUS_LABELS.getValue(s),
                                            state.status == s,
                                            { viewModel.onStatus(s) })
                                    }
                                }
                            }
                        }
                    }

                    if (state.showStart || state.showFinish) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            if (state.showStart) DateField(
                                "Início",
                                state.startedAt,
                                { picking = "start" },
                                { viewModel.onStartedAt(null) })
                            if (state.showFinish) DateField(
                                "Término",
                                state.finishedAt,
                                { picking = "end" },
                                { viewModel.onFinishedAt(null) })
                        }
                    }
                    state.datesError?.let {
                        Text(
                            it,
                            fontFamily = type.body,
                            fontSize = 13.sp,
                            color = Libris.oxblood
                        )
                    }
                    state.saveError?.let {
                        Text(
                            it,
                            fontFamily = type.body,
                            fontSize = 14.sp,
                            color = Libris.oxblood
                        )
                    }

                    PrimaryButton(
                        if (state.saving) "Salvando…" else if (state.isEdit) "Salvar" else "Cadastrar obra",
                        enabled = state.canSubmit,
                        onClick = viewModel::save
                    )
                }
            }
        }
        if (scanning) {
            IsbnScanner(
                onScanned = { scanning = false; viewModel.onScanned(it) },
                onClose = { scanning = false })
        }
    }
    picking?.let { which ->
        IsoDatePicker(
            if (which == "start") state.startedAt else state.finishedAt, todayIso(),
            onPicked = {
                if (which == "start") viewModel.onStartedAt(it) else viewModel.onFinishedAt(
                    it
                )
            },
            onDismiss = { picking = null },
        )
    }
}

@Composable
private fun GenreChips(state: BookFormState, existing: List<String>, viewModel: BookFormViewModel) {
    val type = LocalLibrisType.current
    var draft by remember { mutableStateOf("") }
    val matches = existing.filter { e ->
        draft.isNotBlank() && e.startsWith(
            draft.trim(),
            ignoreCase = true
        ) && state.genres.none { it.equals(e, true) }
    }.take(5)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Gêneros", fontFamily = type.body, fontSize = 13.sp, color = Libris.inkSoft)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            state.genres.forEach { g ->
                Text(
                    "$g  ×",
                    fontFamily = type.body,
                    fontSize = 13.sp,
                    color = Libris.inkSoft,
                    modifier = Modifier
                        .drawBehind {
                            drawLine(
                                color = Libris.brass,
                                start = androidx.compose.ui.geometry.Offset(0f, size.height),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }
                        .clickable { viewModel.removeGenre(g) }
                        .padding(horizontal = 4.dp, vertical = 5.dp),
                )
            }
        }
        RuledField(
            "", draft, { v ->
                if (v.endsWith(",")) {
                    viewModel.addGenre(v); draft = ""
                } else draft = v
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.addGenre(draft); draft = "" }),
        )
        if (matches.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                matches.forEach { m ->
                    Text(
                        m,
                        fontFamily = type.body,
                        fontSize = 13.sp,
                        color = Libris.oxblood,
                        modifier = Modifier
                            .drawBehind {
                                drawLine(
                                    color = Libris.brass,
                                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                                    end = androidx.compose.ui.geometry.Offset(
                                        size.width,
                                        size.height
                                    ),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)),
                                )
                            }
                            .clickable { viewModel.addGenre(m); draft = "" }
                            .padding(horizontal = 4.dp, vertical = 5.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DateField(label: String, value: String?, onPick: () -> Unit, onClear: () -> Unit) {
    val type = LocalLibrisType.current
    Column {
        Text(label, fontFamily = type.body, fontSize = 13.sp, color = Libris.inkSoft)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value?.let(::formatIsoDate) ?: "escolher",
                fontFamily = type.body,
                fontSize = 17.sp,
                modifier = Modifier.clickable(onClick = onPick).padding(vertical = 6.dp)
            )
            if (value != null) Text(
                "  ×",
                color = Libris.inkSoft,
                modifier = Modifier.clickable(onClick = onClear)
            )
        }
    }
}

@Composable
private fun LinkText(text: String, color: Color = Libris.oxblood, onClick: () -> Unit) {
    Text(
        text,
        fontFamily = LocalLibrisType.current.body,
        fontSize = 14.sp,
        color = color,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 2.dp)
    )
}

@Composable
private fun SmallButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Text(
        text,
        fontFamily = LocalLibrisType.current.title,
        fontSize = 13.sp,
        color = if (enabled) Libris.oxblood else Libris.inkSoft.copy(alpha = 0.55f),
        modifier = Modifier
            .border(1.dp, if (enabled) Libris.brass else Libris.paperShade)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun Toggle(text: String, on: Boolean, onClick: () -> Unit) {
    Text(
        text,
        fontFamily = LocalLibrisType.current.title,
        fontSize = 14.sp,
        color = if (on) Libris.paper else Libris.inkSoft,
        modifier = Modifier.background(if (on) Libris.inkSoft else Color.Transparent)
            .border(2.dp, Libris.inkSoft).clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 5.dp),
    )
}
