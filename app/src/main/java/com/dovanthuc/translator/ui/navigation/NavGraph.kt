package com.dovanthuc.translator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dovanthuc.translator.di.AppContainer
import com.dovanthuc.translator.ui.flashcard.FlashcardScreen
import com.dovanthuc.translator.ui.home.HomeScreen
import com.dovanthuc.translator.ui.settings.SettingsScreen
import com.dovanthuc.translator.ui.voice.VoiceTranslateScreen

@Composable
fun TranslatorNavGraph(
    container: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Destinations.HOME) {
        composable(Destinations.HOME) {
            HomeScreen(
                settingsDataStore = container.settingsDataStore,
                onOpenFlashcard = { navController.navigate(Destinations.FLASHCARD) },
                onOpenVoiceTranslate = { navController.navigate(Destinations.VOICE_TRANSLATE) },
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) }
            )
        }
        composable(Destinations.FLASHCARD) {
            FlashcardScreen(
                repository = container.translationCardRepository,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Destinations.VOICE_TRANSLATE) {
            VoiceTranslateScreen(
                openAiRepository = container.openAiRepository,
                onBack = { navController.popBackStack() },
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) }
            )
        }
        composable(Destinations.SETTINGS) {
            SettingsScreen(
                settingsDataStore = container.settingsDataStore,
                dataSyncManager = container.dataSyncManager,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
