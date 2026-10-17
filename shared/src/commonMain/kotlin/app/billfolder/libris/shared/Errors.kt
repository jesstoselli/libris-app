package app.billfolder.libris.shared

/** The API answered outside 2xx; [message] is its pt-BR text when it sent one. */
open class ApiException(val status: Int, val code: String?, val apiMessage: String?) :
    Exception("HTTP $status ${code.orEmpty()}")

/** 401: credentials or tokens are not, or no longer, good. */
class UnauthorizedException(code: String?, apiMessage: String?) : ApiException(401, code, apiMessage)

/** The request never got an HTTP answer (offline, DNS, timeout). */
class NoConnectionException(cause: Throwable) : Exception("no connection", cause)

class SignedOutException : Exception("no session")

class SessionExpiredException : Exception("session expired")

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
