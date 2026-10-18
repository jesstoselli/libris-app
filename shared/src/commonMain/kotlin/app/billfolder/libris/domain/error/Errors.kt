package app.billfolder.libris.domain.error

/** The API answered outside 2xx; [message] is its pt-BR text when it sent one. */
open class ApiException(val status: Int, val code: String?, val apiMessage: String?) :
    Exception("HTTP $status ${code.orEmpty()}")

/** 401: credentials or tokens are not, or no longer, good. */
class UnauthorizedException(code: String?, apiMessage: String?) : ApiException(401, code, apiMessage)

/** The request never got an HTTP answer (offline, DNS, timeout). */
class NoConnectionException(cause: Throwable) : Exception("no connection", cause)

class SignedOutException : Exception("no session")

class SessionExpiredException : Exception("session expired")
