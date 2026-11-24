package dev.hiarni.wifidisabler.ui

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.hiarni.wifidisabler.R
import dev.hiarni.wifidisabler.core.GuardEvent

/** All user actions of the main screen – keeps [MainScreen] stateless and previewable. */
data class MainActions(
    val onEnabledChange: (Boolean) -> Unit = {},
    val onReliableModeChange: (Boolean) -> Unit = {},
    val onNotifyChange: (Boolean) -> Unit = {},
    val onAddNetwork: (String) -> Boolean = { true },
    val onRemoveNetwork: (String) -> Unit = {},
    val onClearHistory: () -> Unit = {},
    val onGrantLocation: () -> Unit = {},
    val onEnableLocationService: () -> Unit = {},
    val onBatteryUnrestricted: () -> Unit = {},
    val onOpenAppSettings: () -> Unit = {},
    val onDismissReliability: () -> Unit = {},
    val onOpenSource: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(state: UiState, actions: MainActions, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "status") { StatusCard(state, actions) }
            if (!state.setup.isReady) item(key = "setup") { SetupCard(state, actions) }
            if (state.showReliabilityCard) item(key = "reliability") { ReliabilityCard(state, actions) }
            item(key = "networks") { NetworksCard(state, actions) }
            item(key = "options") { OptionsCard(state, actions) }
            if (state.settings.events.isNotEmpty()) item(key = "history") { HistoryCard(state.settings.events, actions) }
            item(key = "footer") { Footer(state.versionName, actions.onOpenSource) }
        }
    }
}

// ---------------------------------------------------------------- status

private enum class Status { Empty, Active, Limited, Paused }

@Composable
private fun StatusCard(state: UiState, actions: MainActions) {
    val s = state.settings
    val status = when {
        s.blocked.isEmpty() -> Status.Empty
        !s.enabled -> Status.Paused
        !state.setup.isReady -> Status.Limited
        else -> Status.Active
    }
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (status) {
        Status.Active -> colors.primaryContainer to colors.onPrimaryContainer
        Status.Limited -> colors.tertiaryContainer to colors.onTertiaryContainer
        else -> colors.surfaceContainerHigh to colors.onSurface
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .toggleable(value = s.enabled, role = Role.Switch, onValueChange = actions.onEnabledChange)
                .padding(20.dp),
        ) {
            Surface(shape = CircleShape, color = content.copy(alpha = 0.12f), contentColor = content) {
                Icon(
                    painterResource(R.drawable.ic_tile),
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(32.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        when (status) {
                            Status.Empty -> R.string.status_empty
                            Status.Active -> R.string.status_active
                            Status.Limited -> R.string.status_limited
                            Status.Paused -> R.string.status_paused
                        },
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = when (status) {
                        Status.Empty -> stringResource(R.string.status_empty_desc)
                        Status.Active -> pluralStringResource(
                            R.plurals.status_active_desc, s.blocked.size, s.blocked.size,
                        )
                        Status.Limited -> stringResource(R.string.status_limited_desc)
                        Status.Paused -> stringResource(R.string.status_paused_desc)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = content.copy(alpha = 0.8f),
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(checked = s.enabled, onCheckedChange = null)
        }
    }
}

// ---------------------------------------------------------------- setup

@Composable
private fun SetupCard(state: UiState, actions: MainActions) {
    SectionCard(
        title = stringResource(R.string.setup_title),
        subtitle = stringResource(R.string.setup_desc),
    ) {
        val setup = state.setup
        SetupRow(
            done = setup.backgroundLocationGranted,
            title = stringResource(R.string.setup_location_title),
            description = stringResource(
                if (setup.locationGranted) R.string.setup_location_background_desc else R.string.setup_location_desc,
            ),
            action = stringResource(R.string.action_allow),
            onAction = actions.onGrantLocation,
        )
        SetupRow(
            done = setup.locationServiceOn,
            title = stringResource(R.string.setup_location_service_title),
            description = stringResource(R.string.setup_location_service_desc),
            action = stringResource(R.string.action_turn_on),
            onAction = actions.onEnableLocationService,
        )
    }
}

@Composable
private fun ReliabilityCard(state: UiState, actions: MainActions) {
    SectionCard(
        title = stringResource(R.string.reliability_title),
        subtitle = stringResource(R.string.reliability_desc),
        trailing = {
            IconButton(onClick = actions.onDismissReliability) {
                Icon(painterResource(R.drawable.ic_close), stringResource(R.string.action_dismiss))
            }
        },
    ) {
        if (!state.setup.batteryUnrestricted) {
            SetupRow(
                done = false,
                title = stringResource(R.string.reliability_battery_title),
                description = stringResource(R.string.reliability_battery_desc),
                action = stringResource(R.string.action_allow),
                onAction = actions.onBatteryUnrestricted,
            )
        }
        if (state.aggressiveVendor) {
            SetupRow(
                done = false,
                title = stringResource(R.string.reliability_autostart_title),
                description = stringResource(R.string.reliability_autostart_desc),
                action = stringResource(R.string.action_open),
                onAction = actions.onOpenAppSettings,
            )
        }
    }
}

@Composable
private fun SetupRow(done: Boolean, title: String, description: String, action: String, onAction: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        Icon(
            painterResource(if (done) R.drawable.ic_check_circle else R.drawable.ic_error),
            contentDescription = null,
            tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!done) {
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(onClick = onAction) { Text(action) }
        }
    }
}

// ---------------------------------------------------------------- networks

@Composable
private fun NetworksCard(state: UiState, actions: MainActions) {
    val blocked = state.settings.blocked.sortedWith(String.CASE_INSENSITIVE_ORDER)
    var input by rememberSaveable { mutableStateOf("") }
    var invalid by rememberSaveable { mutableStateOf(false) }
    val submit = {
        if (input.isNotBlank()) {
            if (actions.onAddNetwork(input)) {
                input = ""
                invalid = false
            } else {
                invalid = true
            }
        }
    }

    SectionCard(
        title = stringResource(R.string.networks_title),
        subtitle = stringResource(R.string.networks_desc),
    ) {
        val current = state.currentSsid
        if (current != null && current !in state.settings.blocked) {
            FilledTonalButton(
                onClick = { actions.onAddNetwork(current) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                Icon(painterResource(R.drawable.ic_wifi), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.networks_add_current, current),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it
                invalid = false
            },
            label = { Text(stringResource(R.string.networks_input_label)) },
            singleLine = true,
            isError = invalid,
            supportingText = if (invalid) {
                { Text(stringResource(R.string.networks_input_invalid)) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            trailingIcon = {
                IconButton(onClick = submit, enabled = input.isNotBlank()) {
                    Icon(painterResource(R.drawable.ic_add), stringResource(R.string.action_add))
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        if (blocked.isEmpty()) {
            Text(
                stringResource(R.string.networks_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        } else {
            Spacer(Modifier.size(8.dp))
            blocked.forEachIndexed { index, ssid ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        painterResource(R.drawable.ic_wifi),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        ssid,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { actions.onRemoveNetwork(ssid) }) {
                        Icon(
                            painterResource(R.drawable.ic_delete),
                            stringResource(R.string.action_remove_network, ssid),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- options

@Composable
private fun OptionsCard(state: UiState, actions: MainActions) {
    SectionCard(title = stringResource(R.string.options_title)) {
        SwitchRow(
            title = stringResource(R.string.option_reliable_title),
            description = stringResource(R.string.option_reliable_desc),
            checked = state.settings.reliableMode,
            onChange = actions.onReliableModeChange,
        )
        SwitchRow(
            title = stringResource(R.string.option_notify_title),
            description = stringResource(R.string.option_notify_desc),
            checked = state.settings.notifyOnDisable,
            onChange = actions.onNotifyChange,
        )
    }
}

@Composable
private fun SwitchRow(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 8.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}

// ---------------------------------------------------------------- history

@Composable
private fun HistoryCard(events: List<GuardEvent>, actions: MainActions) {
    val context = LocalContext.current
    SectionCard(
        title = stringResource(R.string.history_title),
        trailing = { TextButton(onClick = actions.onClearHistory) { Text(stringResource(R.string.action_clear)) } },
    ) {
        events.take(VISIBLE_EVENTS).forEach { e ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Surface(
                    shape = CircleShape,
                    color = if (e.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(8.dp),
                ) {}
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(e.ssid, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        stringResource(if (e.success) R.string.history_success else R.string.history_failed),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (e.success) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                    )
                }
                Text(
                    DateUtils.formatDateTime(
                        context, e.timestamp,
                        DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val VISIBLE_EVENTS = 8

// ---------------------------------------------------------------- shared

@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                trailing?.invoke()
            }
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                )
            } else {
                Spacer(Modifier.size(6.dp))
            }
            content()
        }
    }
}

@Composable
private fun Footer(versionName: String, onOpenSource: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
    ) {
        TextButton(onClick = onOpenSource) {
            Text(stringResource(R.string.footer_source, versionName))
        }
        Text(
            stringResource(R.string.footer_privacy),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
