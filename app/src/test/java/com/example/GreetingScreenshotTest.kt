package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.model.GameEntity
import com.example.model.GameStatus
import com.example.ui.screens.GameCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleGame = GameEntity(
      id = 1L,
      title = "The Legend of Zelda: Breath of the Wild",
      platform = "Nintendo Switch",
      coverUrl = "",
      description = "Open world adventure in Hyrule",
      releaseYear = 2017,
      genres = "Action RPG",
      developer = "Nintendo",
      status = GameStatus.PLAYING.id,
      rating = 10,
      totalPlayTimeSeconds = 7200L
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        GameCard(
          game = sampleGame,
          isTracking = false,
          onClick = {},
          onQuickPlay = {},
          isDark = true,
          modifier = Modifier.padding(16.dp)
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
