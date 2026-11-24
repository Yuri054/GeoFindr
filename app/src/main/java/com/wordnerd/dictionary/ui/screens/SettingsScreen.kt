package com.wordnerd.dictionary.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.wordnerd.dictionary.WordViewModel
import com.wordnerd.dictionary.notifications.DailyWordNotifier
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: WordViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val daily by vm.daily.collectAsState()
    var canPost by remember { mutableStateOf(DailyWordNotifier.canPost(context)) }
    var showPicker by remember { mutableStateOf(false) }

    // Re-check when returning from the system notification settings.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { canPost = DailyWordNotifier.canPost(context) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        canPost = DailyWordNotifier.canPost(context)
        if (granted) vm.setDailyEnabled(true)
    }

    fun enable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !canPost) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            vm.setDailyEnabled(true)
        }
    }

    val timeText = LocalTime.of(daily.hour, daily.minute)
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Text(
                "Notifications",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
            )
            ListItem(
                modifier = Modifier.clickable { if (daily.enabled) vm.setDailyEnabled(false) else enable() },
                leadingContent = { Icon(Icons.Outlined.NotificationsActive, contentDescription = null) },
                headlineContent = { Text("Daily word") },
                supportingContent = { Text("Get the word of the day as a notification") },
                trailingContent = {
                    Switch(
                        checked = daily.enabled && canPost,
                        onCheckedChange = { on -> if (on) enable() else vm.setDailyEnabled(false) },
                    )
                },
            )
            ListItem(
                modifier = Modifier.clickable(enabled = daily.enabled) { showPicker = true },
                leadingContent = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                headlineContent = { Text("Time") },
                supportingContent = { Text(if (daily.enabled) "Every day at $timeText" else timeText) },
            )
            if (daily.enabled && canPost) {
                ListItem(
                    modifier = Modifier.clickable { vm.previewNotification() },
                    leadingContent = { Icon(Icons.Outlined.Send, contentDescription = null) },
                    headlineContent = { Text("Send a test notification") },
                    supportingContent = { Text("See what today's notification looks like") },
                )
            }
            if (daily.enabled && !canPost) {
                ListItem(
                    modifier = Modifier.clickable {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Outlined.NotificationsOff, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error)
                    },
                    headlineContent = { Text("Notifications are turned off") },
                    supportingContent = { Text("Tap to allow them in system settings") },
                )
            }
        }
    }

    if (showPicker) {
        val state = rememberTimePickerState(
            initialHour = daily.hour,
            initialMinute = daily.minute,
            is24Hour = DateFormat.is24HourFormat(context),
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Notification time") },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    vm.setDailyTime(state.hour, state.minute)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        )
    }
}
