package app.billfolder.libris.ui.common

import app.billfolder.libris.domain.error.ApiException
import app.billfolder.libris.domain.error.NoConnectionException
import app.billfolder.libris.domain.error.SessionExpiredException
import app.billfolder.libris.domain.rules.DateProblem
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorMessagesTest {
    @Test
    fun apiError_showsTheApiMessage() =
        assertEquals("A página precisa estar entre 1 e 512.", ApiException(400, "page_out_of_range", "A página precisa estar entre 1 e 512.").userMessage())

    @Test
    fun transportFailures_haveTheirOwnWording() {
        assertEquals("Sem conexão com o servidor.", NoConnectionException(IllegalStateException()).userMessage())
        assertEquals("Sua sessão expirou. Entre de novo.", SessionExpiredException().userMessage())
        assertEquals("Algo deu errado. Tente de novo.", ApiException(500, null, null).userMessage())
    }

    @Test
    fun dateProblems_explainThemselves() {
        assertEquals("O término não pode ser antes do início.", DateProblem.FinishBeforeStart.message())
        assertEquals("As datas não podem estar no futuro.", DateProblem.InFuture.message())
    }
}
