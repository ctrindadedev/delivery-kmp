package br.caio.delivery.app

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SmokeTest {
    @Test
    fun `kotlin test executa no app`() {
        assertEquals(2, 1 + 1)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `compose ui test renderiza no desktop`() =
        runComposeUiTest {
            setContent { Text("olá") }
            onNodeWithText("olá").assertIsDisplayed()
        }
}
