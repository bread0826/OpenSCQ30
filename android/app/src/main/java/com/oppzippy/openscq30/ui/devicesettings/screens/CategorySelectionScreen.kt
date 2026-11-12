package com.oppzippy.openscq30.ui.devicesettings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oppzippy.openscq30.ui.devicesettings.Screen
import com.oppzippy.openscq30.ui.devicesettings.ScreenInfo
import com.oppzippy.openscq30.ui.theme.OpenSCQ30Theme
import com.oppzippy.openscq30.ui.utils.NavItem

@Composable
fun CategorySelectionScreen(screens: List<ScreenInfo>, onNavigation: (Screen) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(screens) { screenInfo ->
            NavItem(
                onClick = { onNavigation(screenInfo.baseRoute) },
                icon = { Icon(painter = painterResource(screenInfo.icon), contentDescription = null) },
                text = screenInfo.name.translated(),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewCategorySelectionScreen() {
    OpenSCQ30Theme(dynamicColor = false) {
        CategorySelectionScreen(
            screens = listOf(
                Screen.SettingsCategory("general").screenInfo(),
                Screen.SettingsCategory("equalizer").screenInfo(),
                Screen.SettingsCategory("buttonConfiguration").screenInfo(),
                Screen.QuickPresets.screenInfo,
            ),
            onNavigation = {},
        )
    }
}
