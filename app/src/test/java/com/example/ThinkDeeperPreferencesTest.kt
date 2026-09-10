package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ThinkDeeperPreferencesTest {

  @Test
  fun testDefaultPreferences() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = PreferencesManager(context)

    assertEquals("openai", prefs.apiProvider.value)
    assertEquals("gpt-4o-mini", prefs.selectedModel.value)
    assertEquals(0.7f, prefs.temperature.value, 0.01f)
    assertTrue(prefs.customModels.value.contains("gpt-4o-mini"))
    assertTrue(prefs.customModels.value.contains("claude-3-5-sonnet"))
  }

  @Test
  fun testProviderSwitching() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = PreferencesManager(context)

    prefs.setApiProvider("groq")
    assertEquals("groq", prefs.apiProvider.value)
    assertEquals(PreferencesManager.DEFAULT_GROQ_BASE, prefs.apiBaseUrl.value)
    assertEquals("llama-3.3-70b-versatile", prefs.selectedModel.value)
  }

  @Test
  fun testCustomModelLibrary() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = PreferencesManager(context)

    prefs.setSelectedModel("my-fine-tuned-model")
    assertEquals("my-fine-tuned-model", prefs.selectedModel.value)
    assertTrue(prefs.customModels.value.contains("my-fine-tuned-model"))
  }
}
