package app.billfolder.libris.ui.common

import app.billfolder.libris.domain.error.ApiException
import app.billfolder.libris.domain.error.NoConnectionException
import app.billfolder.libris.domain.error.SessionExpiredException
import app.billfolder.libris.domain.rules.DateProblem

private const val NO_CONNECTION = "Sem conexão com o servidor."
private const val GENERIC = "Algo deu errado. Tente de novo."

/** The API already writes its messages in Portuguese; only transport failures need wording here. */
fun Throwable.userMessage(): String = when (this) {
    is NoConnectionException -> NO_CONNECTION
    is SessionExpiredException -> "Sua sessão expirou. Entre de novo."
    is ApiException -> apiMessage ?: GENERIC
    else -> GENERIC
}

val Throwable.apiCode: String? get() = (this as? ApiException)?.code

fun DateProblem.message(): String = when (this) {
    DateProblem.FinishBeforeStart -> "O término não pode ser antes do início."
    DateProblem.InFuture -> "As datas não podem estar no futuro."
}
