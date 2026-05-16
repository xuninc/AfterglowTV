package com.afterglowtv.app.ui.components

import android.content.ClipboardManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.afterglowtv.app.ui.theme.AfterglowTVTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClipboardCopyButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun clickCopiesCurrentTextToClipboard() {
        lateinit var clipboard: ClipboardManager

        composeRule.setContent {
            clipboard = LocalClipboard.current.nativeClipboard
            AfterglowTVTheme {
                ClipboardCopyButton(
                    text = "https://example.com/playlist.m3u",
                    label = "Copy"
                )
            }
        }

        composeRule.onNodeWithText("Copy").performClick()

        composeRule.runOnIdle {
            assertEquals(
                "https://example.com/playlist.m3u",
                clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            )
        }
    }

    @Test
    fun clickClearEmptiesCurrentText() {
        var text by mutableStateOf("http://bad-input")

        composeRule.setContent {
            AfterglowTVTheme {
                ClipboardClearButton(
                    onClear = { text = "" },
                    label = "Clear",
                    enabled = text.isNotEmpty()
                )
            }
        }

        composeRule.onNodeWithText("Clear").performClick()

        composeRule.runOnIdle {
            assertEquals("", text)
        }
    }
}
