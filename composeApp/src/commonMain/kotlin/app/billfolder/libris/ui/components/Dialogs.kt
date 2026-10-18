package app.billfolder.libris.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import app.billfolder.libris.ui.format.isoFromPickerMillis
import app.billfolder.libris.ui.format.pickerMillisFromIso
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType

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

/** Picks a calendar date as "YYYY-MM-DD"; future days can be blocked (reading dates can't be in the future). */
@Composable
fun IsoDatePicker(initial: String?, maxIso: String?, onPicked: (String) -> Unit, onDismiss: () -> Unit) {
    val maxMillis = maxIso?.let(::pickerMillisFromIso)
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.let(::pickerMillisFromIso),
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = maxMillis == null || utcTimeMillis <= maxMillis
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { state.selectedDateMillis?.let { onPicked(isoFromPickerMillis(it)) }; onDismiss() }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    ) { DatePicker(state) }
}
