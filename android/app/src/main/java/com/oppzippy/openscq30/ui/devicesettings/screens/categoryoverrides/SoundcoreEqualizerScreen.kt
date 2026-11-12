package com.oppzippy.openscq30.ui.devicesettings.screens.categoryoverrides

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.bindings.translateSettingId
import com.oppzippy.openscq30.lib.wrapper.Equalizer
import com.oppzippy.openscq30.lib.wrapper.ModifiableSelectCommandInner
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.Value
import com.oppzippy.openscq30.lib.wrapper.toValue
import com.oppzippy.openscq30.ui.devicesettings.composables.Equalizer
import com.oppzippy.openscq30.ui.devicesettings.composables.ReadOnlyEqualizer
import com.oppzippy.openscq30.ui.utils.ModifiableSelect

private const val SETTING_ID_PRESET_EQUALIZER_PROFILE = "presetEqualizerProfile"
private const val SETTING_ID_CUSTOM_EQUALIZER_PROFILE = "customEqualizerProfile"
private const val SETTING_ID_VOLUME_ADJUSTMENTS = "volumeAdjustments"

val deviceBlacklist = hashSetOf(
    "SoundcoreA3116", // volume adjustments are unknown, so they would be shown as all 0s
)

object SoundcoreEqualizerScreen : CategoryOverride {
    // Be overly cautious and ensure all settings are as expected. It's better to not use this override when we should
    // rather than the other way around.
    override fun shouldOverride(deviceModel: String, settings: List<Pair<String, Setting>>): Boolean {
        if (!deviceModel.startsWith("Soundcore")) return false
        if (deviceModel in deviceBlacklist) return false
        if (settings.size != 3) return false

        getSettingById<Setting.PresetEqualizerProfileSelect>(
            settings,
            SETTING_ID_PRESET_EQUALIZER_PROFILE,
        ) ?: return false
        getSettingById<Setting.ModifiableSelectSetting>(
            settings,
            SETTING_ID_CUSTOM_EQUALIZER_PROFILE,
        ) ?: return false
        getSettingById<Setting.EqualizerSetting>(settings, SETTING_ID_VOLUME_ADJUSTMENTS) ?: return false

        return true
    }

    @Composable
    override fun Screen(settings: List<Pair<String, Setting>>, setSettings: (List<Pair<String, Value>>) -> Unit) {
        val presetEqualizerProfile =
            settings.find {
                it.first == SETTING_ID_PRESET_EQUALIZER_PROFILE
            }!!.second as Setting.PresetEqualizerProfileSelect
        val selectedPresetIndex = presetEqualizerProfile.select.options.indexOf(presetEqualizerProfile.value)
            .let { if (it == -1) null else it }
        val customEqualizerProfile =
            settings.find {
                it.first == SETTING_ID_CUSTOM_EQUALIZER_PROFILE
            }!!.second as Setting.ModifiableSelectSetting
        val volumeAdjustments =
            settings.find { it.first == SETTING_ID_VOLUME_ADJUSTMENTS }!!.second as Setting.EqualizerSetting

        val isPresetSelected = presetEqualizerProfile.value != null

        var selectedTabIndex by remember { mutableIntStateOf(if (isPresetSelected) 0 else 1) }

        Column {
            PrimaryTabRow(selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(stringResource(R.string.preset))
                            if (isPresetSelected) {
                                Icon(
                                    painter = painterResource(R.drawable.check_24px),
                                    contentDescription = stringResource(R.string.in_effect),
                                )
                            }
                        }
                    },
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(stringResource(R.string.custom))
                            if (!isPresetSelected) {
                                Icon(
                                    painter = painterResource(R.drawable.check_24px),
                                    contentDescription = stringResource(R.string.in_effect),
                                )
                            }
                        }
                    },
                )
            }

            val presetLazyListState = rememberLazyListState(
                initialFirstVisibleItemIndex = selectedPresetIndex ?: 0,
            )
            val customScrollState = rememberScrollState()

            if (selectedTabIndex == 0) {
                Preset(
                    lazyListState = presetLazyListState,
                    localizedOptions = presetEqualizerProfile.select.localizedOptions
                        .take(presetEqualizerProfile.select.options.size),
                    volumeAdjustments = presetEqualizerProfile.presets,
                    selectedIndex = selectedPresetIndex,
                    equalizer = presetEqualizerProfile.equalizer,
                    onSelected = {
                        val selectedOption = presetEqualizerProfile.select.options[it]
                        setSettings(listOf(SETTING_ID_PRESET_EQUALIZER_PROFILE to selectedOption.toValue()))
                    },
                )
            } else {
                Custom(
                    scrollState = customScrollState,
                    customProfileSetting = customEqualizerProfile,
                    equalizerSetting = volumeAdjustments,
                    onValueChange = { index, value ->
                        setSettings(
                            listOf(
                                SETTING_ID_VOLUME_ADJUSTMENTS to
                                    volumeAdjustments.value.mapIndexed { i, v -> if (i == index) value else v }
                                        .toValue(),
                            ),
                        )
                    },
                    onSelectCustomProfile = {
                        val selectedOption = customEqualizerProfile.setting.options[it]
                        setSettings(listOf(SETTING_ID_CUSTOM_EQUALIZER_PROFILE to selectedOption.toValue()))
                    },
                    onAddCustomProfile = {
                        setSettings(
                            listOf(
                                SETTING_ID_CUSTOM_EQUALIZER_PROFILE to ModifiableSelectCommandInner.Add(it).toValue(),
                            ),
                        )
                    },
                    onRemoveCustomProfile = {
                        val selectedOption = customEqualizerProfile.setting.options[it]
                        setSettings(
                            listOf(
                                SETTING_ID_CUSTOM_EQUALIZER_PROFILE to
                                    ModifiableSelectCommandInner.Remove(selectedOption).toValue(),
                            ),
                        )
                    },
                )
            }
        }
    }
}

private inline fun <reified T> getSettingById(settings: List<Pair<String, Setting>>, settingId: String): T? =
    settings.find { (id, setting) -> id == settingId && setting is T }?.second as T

@Composable
private fun Preset(
    localizedOptions: List<String>,
    volumeAdjustments: List<List<Short>>,
    equalizer: Equalizer,
    selectedIndex: Int?,
    onSelected: (Int) -> Unit,
    lazyListState: LazyListState,
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.selectableGroup(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(localizedOptions.zip(volumeAdjustments)) { index, (name, adjustments) ->
            PresetCard(
                name = name,
                isSelected = index == selectedIndex,
                volumeAdjustments = adjustments,
                equalizer = equalizer,
                onClick = { onSelected(index) },
            )
        }
    }
}

@Composable
private fun PresetCard(
    name: String,
    equalizer: Equalizer,
    volumeAdjustments: List<Short>,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                if (isSelected) {
                    Icon(
                        painter = painterResource(R.drawable.check_circle_24px),
                        contentDescription = null,
                        tint = colors.primary,
                    )
                }
            }
            ReadOnlyEqualizer(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                color = if (isSelected) colors.primary else colors.onSurfaceVariant,
                bands = equalizer.bandHz,
                values = volumeAdjustments,
                minValue = equalizer.min,
                maxValue = equalizer.max,
                fractionDigits = equalizer.fractionDigits,
                drawHorizontalGuide = false,
            )
        }
    }
}

@Composable
fun Custom(
    scrollState: ScrollState,
    customProfileSetting: Setting.ModifiableSelectSetting,
    equalizerSetting: Setting.EqualizerSetting,
    onValueChange: (index: Int, value: Short) -> Unit,
    onSelectCustomProfile: (Int) -> Unit,
    onAddCustomProfile: (String) -> Unit,
    onRemoveCustomProfile: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        ModifiableSelect(
            name = translateSettingId(SETTING_ID_CUSTOM_EQUALIZER_PROFILE),
            showLabel = false,
            options = customProfileSetting.setting.options,
            selectedIndex = customProfileSetting.setting.options.indexOf(customProfileSetting.value)
                .let { if (it == -1) null else it },
            onSelect = onSelectCustomProfile,
            onAddOption = onAddCustomProfile,
            onRemoveOption = onRemoveCustomProfile,
        )
        Spacer(Modifier.height(16.dp))
        Equalizer(
            bands = equalizerSetting.setting.bandHz,
            values = equalizerSetting.value,
            minValue = equalizerSetting.setting.min,
            maxValue = equalizerSetting.setting.max,
            fractionDigits = equalizerSetting.setting.fractionDigits,
            onValueChange = onValueChange,
        )
    }
}
