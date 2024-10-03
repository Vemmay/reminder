package com.example.reminderapp

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReminderScreen()
        }
    }
}

data class Reminder(val text: String, val date: String, val time: String)

@Composable
fun ReminderScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val reminders = remember { mutableStateListOf<Reminder>() }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedTime by remember { mutableStateOf<Calendar?>(null) }
    var reminderText by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()  // Create a CoroutineScope & gemini

    val formattedDate = selectedDate?.let {
        "${it.get(Calendar.DAY_OF_MONTH)}/${it.get(Calendar.MONTH) + 1}/${it.get(Calendar.YEAR)}"
    } ?: ""
    val formattedTime = selectedTime?.let {
        "${it.get(Calendar.HOUR_OF_DAY)}:${it.get(Calendar.MINUTE)}${if (it.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"}"
    } ?: ""

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // TextField for reminder text
                    OutlinedTextField(
                        value = reminderText,
                        onValueChange = { reminderText = it },
                        label = { Text("Reminder Text") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Text(text = "Selected Date: $formattedDate")
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Selected Time: $formattedTime")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        // Button to show Date Picker Dialog
                        OutlinedButton(onClick = {
                            showDatePickerDialog(context) { selected ->
                                selectedDate = selected
                            }
                        }) {
                            Text("Pick Date")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Button to show Time Picker Dialog
                        OutlinedButton(onClick = {
                            showTimePickerDialog(context) { selected ->
                                selectedTime = selected
                            }
                        }) {
                            Text("Pick Time")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Create Reminder Button
                    OutlinedButton(onClick = {
                        if (reminderText.text.isNotEmpty() && selectedDate != null && selectedTime != null) {
                            val reminder = Reminder(reminderText.text, formattedDate, formattedTime)
                            reminders.add(reminder)
                            reminderText = TextFieldValue("")
                            selectedDate = null
                            selectedTime = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Reminder set for $formattedDate at $formattedTime")
                            }
                        }
                    }) {
                        Text("Set Reminder")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Clear Reminder Button
                    OutlinedButton(onClick = {
                        reminders.clear()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Reminders were cleared")
                        }
                    }) {
                        Text("Clear Reminder")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Reminder List
                    LazyColumn {
                        items(reminders.size) { index ->
                            val reminder = reminders[index]
                            Text(text = "${reminder.text}\n" +"- ${reminder.date} @ ${reminder.time}")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        })
}


//check documentation for date and time picker
fun showDatePickerDialog(context: Context, onDateSelected: (Calendar) -> Unit) {
    val now = Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            onDateSelected(selectedDate)
        },
        now.get(Calendar.YEAR),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH)
    )

    datePickerDialog.show()
}

fun showTimePickerDialog(context: Context, onTimeSelected: (Calendar) -> Unit) {
    val now = Calendar.getInstance()
    val hour = now.get(Calendar.HOUR_OF_DAY)
    val minute = now.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            val selectedTime = Calendar.getInstance()
            selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
            selectedTime.set(Calendar.MINUTE, selectedMinute)
            onTimeSelected(selectedTime)
        },
        hour,
        minute,
        false // Use 24-hour format
    )
    timePickerDialog.show()
}