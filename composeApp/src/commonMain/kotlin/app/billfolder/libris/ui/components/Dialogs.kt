package app.billfolder.libris.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.billfolder.libris.ui.format.dateFromPickerMillis
import app.billfolder.libris.ui.format.pickerMillisFrom
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType
import kotlinx.datetime.LocalDate

@Composable
fun ConfirmDialog(message: String, confirmLabel: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val type = LocalLibrisType.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Libris.paper,
        text = { Text(message, fontFamily = type.body, color = Libris.ink) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel, fontFamily = type.title, color = Libris.oxblood) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", fontFamily = type.body, color = Libris.inkSoft) } },
    )
}

/** Picks a calendar date; days after [max] can be blocked (reading dates can't be in the future). */
@Composable
fun LibrisDatePicker(initial: LocalDate?, max: LocalDate?, onPicked: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val maxMillis = max?.let(::pickerMillisFrom)
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.let(::pickerMillisFrom),
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = maxMillis == null || utcTimeMillis <= maxMillis
        },
    )
    // Material's picker sits on surfaceContainerHigh, which the dark scheme leaves near-black under our dark ink:
    // every color is spelled out so the calendar reads as a sheet of paper.
    val colors = DatePickerDefaults.colors(
        containerColor = Libris.paper,
        titleContentColor = Libris.inkSoft,
        headlineContentColor = Libris.ink,
        weekdayContentColor = Libris.inkSoft,
        subheadContentColor = Libris.inkSoft,
        navigationContentColor = Libris.ink,
        yearContentColor = Libris.ink,
        disabledYearContentColor = Libris.ink.copy(alpha = 0.3f),
        currentYearContentColor = Libris.oxblood,
        selectedYearContentColor = Libris.paper,
        selectedYearContainerColor = Libris.oxblood,
        dayContentColor = Libris.ink,
        disabledDayContentColor = Libris.ink.copy(alpha = 0.3f),
        selectedDayContentColor = Libris.paper,
        selectedDayContainerColor = Libris.oxblood,
        todayContentColor = Libris.oxblood,
        todayDateBorderColor = Libris.oxblood,
        dividerColor = Libris.rule,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = colors,
        confirmButton = {
            TextButton(onClick = { state.selectedDateMillis?.let { onPicked(dateFromPickerMillis(it)) }; onDismiss() }) {
                Text("OK", color = Libris.oxblood)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Libris.inkSoft) } },
    ) {
        DatePicker(
            state,
            colors = colors,
            title = { Text("Escolher data", color = Libris.inkSoft, modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)) },
        )
    }
}
