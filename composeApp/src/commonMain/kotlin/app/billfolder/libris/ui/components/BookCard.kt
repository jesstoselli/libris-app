package app.billfolder.libris.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.billfolder.libris.domain.model.Book
import app.billfolder.libris.domain.model.BookOwnership
import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType

/** The progress line under a book: a finished book (often registered without logs) reads "Lido". */
fun progressLine(book: Book): String? = when {
    book.ownership != BookOwnership.Owned || book.totalPages == null -> null
    book.status == BookStatus.Read -> "Lido · ${book.totalPages} págs."
    else -> "pág. ${book.currentPage} de ${book.totalPages} · ${book.percent}%"
}

@Composable
fun BookCard(book: Book, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val type = LocalLibrisType.current
    PaperCard(modifier.fillMaxWidth().clickable(onClick = onClick), padding = 14.dp, foldedCorner = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BookCover(book.coverUrl, book.title, width = 62.dp, height = 94.dp, fontSize = 9.sp)
            Column(Modifier.weight(1f)) {
                Text(book.title, fontFamily = type.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 20.sp, color = Libris.oxblood, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text(book.authors.joinToString(", "), fontFamily = type.body, fontSize = 14.sp, color = Libris.inkSoft)
                progressLine(book)?.let { line ->
                    Text(line, fontFamily = type.body, fontSize = 14.sp, color = Libris.inkSoft)
                    ProgressBar(if (book.status == BookStatus.Read) 100 else book.percent, Modifier.padding(top = 6.dp, bottom = 4.dp))
                }
                if (book.genres.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 5.dp)) {
                        book.genres.forEach { g ->
                            Text(
                                g.name,
                                fontFamily = type.body,
                                fontSize = 11.sp,
                                color = Libris.inkSoft,
                                modifier = Modifier
                                    .drawBehind {
                                        drawLine(Libris.inkSoft.copy(alpha = 0.55f), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                                    }
                                    .padding(bottom = 1.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
