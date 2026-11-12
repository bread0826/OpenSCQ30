package com.oppzippy.openscq30

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
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
        composeRule.onNodeWithText(translateCategoryId("deviceInformation")).assertIsDisplayed()
        back()

        composeRule.activityRule.scenario.recreate()
        selectMode(R.string.studio_anc)
        composeRule.onNodeWithContentDescription(getString(R.string.settings)).performClick()
        composeRule.onNodeWithText(getString(R.string.dark)).assertIsDisplayed()
        back()

        chooseTheme(R.string.studio_system)
        shell("cmd uimode night yes")
        awaitSurface(0x141414)
        shell("cmd uimode night no")
        awaitSurface(0xF7F2ED)

        val deviceName = translateDeviceModel("SoundcoreA3947")
        shell("input keyevent KEYCODE_BACK")
        composeRule.waitUntil(5000) {
            composeRule.onAllNodes(hasText(deviceName)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(deviceName).performClick()
        selectMode(R.string.studio_anc)

        shell("wm size 720x1280")
        shell("settings put system font_scale 1.5")
        composeRule.waitUntil(5000) { composeRule.activity.resources.configuration.fontScale > 1.4f }
        selectMode(R.string.studio_transparency)
        openShortcut(R.string.studio_controls, "buttonConfiguration")
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
        val screenshot = screenshot()
        assertEquals(surface, screenshot.surfaceColor())
        composeRule.activity.openFileOutput(name, 0).use {
            assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }

    private fun awaitSurface(surface: Int) {
        composeRule.waitUntil(5000) { screenshot().surfaceColor() == surface }
    }

    private fun screenshot(): Bitmap =
        checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())

    private fun Bitmap.surfaceColor(): Int = getPixel(8, height / 3) and 0xFFFFFF

    private fun shell(command: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() }
    }
}
