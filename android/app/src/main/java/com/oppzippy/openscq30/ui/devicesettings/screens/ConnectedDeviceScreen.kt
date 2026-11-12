package com.oppzippy.openscq30.ui.devicesettings.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.wrapper.Setting
import java.text.NumberFormat
import kotlinx.coroutines.Job

private const val LIBERTY_4_NC = "SoundcoreA3947"
internal const val AMBIENT_SOUND_MODE = "ambientSoundMode"
private const val SOUND_MODES = "soundModes"
private val headingFont = FontFamily(Font(R.font.doto))

private enum class NoiseMode(val deviceValue: String, @param:StringRes val label: Int) {
    Anc("NoiseCanceling", R.string.studio_anc),
    Normal("Normal", R.string.studio_normal),
    Transparency("Transparency", R.string.studio_transparency),
}

private enum class BatteryReading(val id: String, @param:StringRes val label: Int, @param:DrawableRes val icon: Int) {
    Left("batteryLevelLeft", R.string.studio_battery_left, R.drawable.earbuds_2_24px),
    Right("batteryLevelRight", R.string.studio_battery_right, R.drawable.earbuds_2_24px),
    Case("caseBatteryLevel", R.string.studio_battery_case, R.drawable.earbud_case_24px),
    Device("batteryLevel", R.string.studio_battery, R.drawable.devices_other_24px),
}

@Composable
internal fun ConnectedDeviceScreen(
    deviceName: String,
    deviceModel: String,
    settings: Map<String, Setting>,
    categories: List<String>,
    darkTheme: Boolean,
    onSettings: () -> Unit,
    onCategory: (String) -> Unit,
    onMoreSettings: () -> Unit,
    onSoundMode: (String) -> Job,
) {
    val isLiberty = deviceModel == LIBERTY_4_NC
    val colorScheme = MaterialTheme.colorScheme
    val accent = Color(0xFFF33D1D)
    val mode = settings[AMBIENT_SOUND_MODE] as? Setting.SelectSetting
    val modes = NoiseMode.entries.filter { it.deviceValue in mode?.setting?.options.orEmpty() }
    var pendingWrite by remember(deviceModel) { mutableStateOf<Job?>(null) }
    LaunchedEffect(pendingWrite) {
        val write = pendingWrite ?: return@LaunchedEffect
        write.join()
        pendingWrite = null
    }

    Surface(Modifier.fillMaxSize(), color = colorScheme.surface) {
        BoxWithConstraints(contentAlignment = Alignment.TopCenter) {
            val stackControls = maxWidth < 320.dp || LocalDensity.current.fontScale > 1.3f
            Column(
                Modifier
                    .widthIn(max = 510.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onSettings) {
                        Icon(painterResource(R.drawable.settings_24px), stringResource(R.string.settings))
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(Modifier.size(9.dp).background(Color(0xFF53C970), CircleShape))
                    Text(stringResource(R.string.connected), style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = deviceName.uppercase(java.util.Locale.ROOT),
                    modifier = Modifier.clearAndSetSemantics {
                        heading()
                        contentDescription = deviceName
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = headingFont,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.4.sp,
                )

                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (isLiberty) {
                        Image(
                            painterResource(R.drawable.liberty_4_nc_studio),
                            contentDescription = null,
                            modifier = Modifier
                                .widthIn(max = 390.dp)
                                .fillMaxWidth()
                                .aspectRatio(1.082f),
                        )
                    } else {
                        Icon(
                            painterResource(R.drawable.devices_other_24px),
                            contentDescription = null,
                            modifier = Modifier.padding(vertical = 32.dp).size(112.dp),
                            tint = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                val batteries = when {
                    isLiberty -> listOf(BatteryReading.Left, BatteryReading.Right, BatteryReading.Case)

                    BatteryReading.Left.id in settings || BatteryReading.Right.id in settings ->
                        listOf(BatteryReading.Left, BatteryReading.Right)

                    else -> listOf(BatteryReading.Device, BatteryReading.Case).filter { it.id in settings }
                }
                if (batteries.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        batteries.forEach { battery ->
                            BatteryGauge(
                                battery,
                                (settings[battery.id] as? Setting.InformationSetting)?.value,
                                Modifier.weight(1f),
                            )
                        }
                    }
                }

                if (modes.isNotEmpty()) {
                    val group = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                        .clip(RoundedCornerShape(32.dp))
                        .background(if (darkTheme) Color(0xFF252525) else Color(0xFFEAE4DE))
                        .padding(4.dp)
                    val content: @Composable (NoiseMode, Modifier) -> Unit = { option, modifier ->
                        val selected = option.deviceValue == mode?.value
                        Row(
                            modifier
                                .heightIn(min = 48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected && darkTheme) {
                                        Color(0xFF383838)
                                    } else if (selected) {
                                        Color(0xFFFCF8F3)
                                    } else {
                                        Color.Transparent
                                    },
                                )
                                .selectable(
                                    selected = selected,
                                    enabled = pendingWrite == null,
                                    role = Role.RadioButton,
                                    onClick = {
                                        if (!selected && pendingWrite == null) {
                                            pendingWrite = onSoundMode(option.deviceValue)
                                        }
                                    },
                                )
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (selected) Box(Modifier.size(9.dp).background(accent, CircleShape))
                            Text(
                                stringResource(option.label),
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    if (stackControls) {
                        Column(group) { modes.forEach { content(it, Modifier.fillMaxWidth()) } }
                    } else {
                        Row(group, verticalAlignment = Alignment.CenterVertically) {
                            modes.forEach {
                                content(it, Modifier.weight(if (it == NoiseMode.Transparency) 1.2f else 1f))
                            }
                        }
                    }
                    if (pendingWrite != null) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = accent,
                        )
                    }
                }

                val shortcuts = listOf(
                    Triple("equalizer", R.string.studio_equaliser, R.drawable.equalizer_24px),
                    Triple("buttonConfiguration", R.string.studio_controls, R.drawable.radio_button_checked_24px),
                ).filter { it.first in categories }
                if (shortcuts.isNotEmpty()) {
                    if (stackControls) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            shortcuts.forEach { (id, label, icon) ->
                                DeviceShortcut(label, icon, Modifier.fillMaxWidth()) { onCategory(id) }
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            shortcuts.forEach { (id, label, icon) ->
                                DeviceShortcut(label, icon, Modifier.weight(1f)) { onCategory(id) }
                            }
                        }
                    }
                }
                if (SOUND_MODES in categories && modes.isNotEmpty()) {
                    TextButton(onClick = { onCategory(SOUND_MODES) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.studio_sound_mode_settings))
                    }
                }
                TextButton(onClick = onMoreSettings, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.studio_more_settings))
                }
            }
        }
    }
}

@Composable
private fun BatteryGauge(battery: BatteryReading, rawValue: String?, modifier: Modifier = Modifier) {
    val parts = rawValue?.split('/', limit = 2).orEmpty()
    val level = parts.getOrNull(0)?.toIntOrNull()
    val maximum = parts.getOrNull(1)?.toIntOrNull()
    val fraction = if (level != null && maximum != null && maximum > 0 && level in 0..maximum) {
        level.toFloat() / maximum
    } else {
        null
    }
    val locale = LocalConfiguration.current.locales[0]
    val percent = fraction?.let { NumberFormat.getPercentInstance(locale).format(it.toDouble()) }
    val label = stringResource(battery.label)
    val reading = percent ?: stringResource(R.string.studio_battery_unavailable)
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.clearAndSetSemantics { contentDescription = "$label, $reading" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(battery.icon), contentDescription = null, Modifier.size(18.dp))
            Text(percent ?: "—", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
        Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        Box(
            Modifier
                .widthIn(max = 78.dp)
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape)
                .background(colors.outlineVariant),
        ) {
            if (fraction != null) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .height(5.dp)
                        .background(colors.onSurface, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun DeviceShortcut(@StringRes label: Int, @DrawableRes icon: Int, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 54.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(icon), contentDescription = null, Modifier.size(20.dp))
            Text(stringResource(label), textAlign = TextAlign.Center)
        }
    }
}
