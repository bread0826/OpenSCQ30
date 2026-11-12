package com.oppzippy.openscq30

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.oppzippy.openscq30.actions.addAndConnectToDemoDevice
import com.oppzippy.openscq30.lib.bindings.translateCategoryId
import com.oppzippy.openscq30.lib.bindings.translateDeviceModel
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@HiltAndroidTest
class StudioWhiteDemoTest : OpenSCQ30RootTestBase() {
    @Test
    fun libertyDashboard() {
        addAndConnectToDemoDevice(composeRule, translateDeviceModel("SoundcoreA3947"))
        selectMode(R.string.studio_anc)
        checkBattery(R.string.studio_battery_left, "0%")
        checkBattery(R.string.studio_battery_right, "0%")
        checkBattery(R.string.studio_battery_case, "0%")
        selectMode(R.string.studio_normal)
        selectMode(R.string.studio_transparency)
        selectMode(R.string.studio_anc)

        chooseTheme(R.string.light)
        capture("studio-white-light.png", 0xF7F2ED)
        chooseTheme(R.string.dark)
        capture("studio-white-dark.png", 0x141414)

        openShortcut(R.string.studio_equaliser, "equalizer")
        openShortcut(R.string.studio_controls, "buttonConfiguration")
        openShortcut(R.string.studio_sound_mode_settings, "soundModes")
        openMoreDeviceSettings()
        composeRule.onNodeWithText(translateCategoryId("general")).assertIsDisplayed()
        back()

        composeRule.activityRule.scenario.recreate()
        selectMode(R.string.studio_anc)
        composeRule.onNodeWithContentDescription(getString(R.string.settings)).performClick()
        composeRule.onNodeWithText(getString(R.string.dark)).assertIsDisplayed()
        back()
    }

    private fun checkBattery(label: Int, value: String) {
        composeRule.onNodeWithContentDescription("${getString(label)}, $value").performScrollTo().assertIsDisplayed()
    }

    private fun selectMode(label: Int) {
        val name = getString(label)
        composeRule.onNodeWithText(name).performScrollTo().performClick()
        composeRule.waitUntil(5000) {
            composeRule.onAllNodes(hasText(name) and isSelected()).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun chooseTheme(theme: Int) {
        composeRule.onNodeWithContentDescription(getString(R.string.settings)).performScrollTo().performClick()
        composeRule.onNodeWithTag("${getString(R.string.theme)} select").performClick()
        composeRule.onNodeWithText(getString(theme)).performClick()
        back()
    }

    private fun openShortcut(shortcut: Int, category: String) {
        composeRule.onNodeWithText(getString(shortcut)).performScrollTo().performClick()
        composeRule.onNodeWithText(translateCategoryId(category)).assertIsDisplayed()
        back()
    }

    private fun back() {
        composeRule.onNodeWithContentDescription(getString(R.string.back)).performClick()
    }

    private fun capture(name: String, surface: Int) {
        composeRule.onNodeWithContentDescription(getString(R.string.settings)).performScrollTo()
        composeRule.waitForIdle()
        val screenshot = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        assertEquals(surface, screenshot.getPixel(8, screenshot.height / 3) and 0xFFFFFF)
        composeRule.activity.openFileOutput(name, 0).use {
            assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }
}
