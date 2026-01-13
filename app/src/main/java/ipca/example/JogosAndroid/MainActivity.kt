package ipca.example.JogosAndroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import ipca.example.JogosAndroid.ui.Nonograma.MainMenuScreen
import ipca.example.JogosAndroid.nonogram.SizeSelectionScreen
import ipca.example.JogosAndroid.ui.Nonograma.CompletedNonogramsScreen
import ipca.example.JogosAndroid.ui.Nonograma.NonogramContainer
import ipca.example.JogosAndroid.ui.Nonograma.NonogramViewModel
import ipca.example.JogosAndroid.ui.PokeCliker.GameManager
import ipca.example.JogosAndroid.ui.PokeCliker.GameScreenView
import ipca.example.JogosAndroid.ui.PokeCliker.HighScoreScreen
import ipca.example.JogosAndroid.ui.Sudoku.*
import ipca.example.JogosAndroid.ui.login.AuthViewModel
import ipca.example.JogosAndroid.ui.login.LoginView
import ipca.example.JogosAndroid.ui.login.RegisterView
import ipca.example.JogosAndroid.ui.theme.JogosAndroidTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JogosAndroidTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val gameManager: GameManager = viewModel()
                val nonogramViewModel: NonogramViewModel = viewModel()
                val sudokuViewModel: GameViewModel = viewModel()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginView(
                            navController = navController,
                            authViewModel = authViewModel,
                            onLoginSuccess = {
                                gameManager.resetForNewUser()
                                nonogramViewModel.resetForNewUser()
                                navController.navigate("home") { popUpTo("login") { inclusive = true } }
                            }
                        )
                    }
                    composable("register") {
                        RegisterView(
                            navController = navController,
                            authViewModel = authViewModel,
                            onRegisterSuccess = {
                                gameManager.resetForNewUser()
                                nonogramViewModel.resetForNewUser()
                                navController.navigate("home") { popUpTo("login") { inclusive = true } }
                            }
                        )
                    }

                    composable("home") {
                        HomeView(navController) {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("login") { popUpTo("home") { inclusive = true } }
                        }
                    }

                    // PokeClicker
                    composable("poke_clicker_hub") { PokeClickerView(navController, gameManager) }
                    composable("poke_clicker_game") { GameScreenView(navController = navController, gameManager = gameManager) }
                    composable("poke_clicker_highscore") { HighScoreScreen(navController, gameManager) }

                    // Nonogram
                    composable("nonogram_hub") { MainMenuScreen(navController) }
                    composable("nonogram_size_selection") { SizeSelectionScreen(navController) }

                    composable(
                        "nonogram_game/{size}",
                        arguments = listOf(navArgument("size") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val puzzleSize = backStackEntry.arguments?.getInt("size") ?: 0
                        NonogramContainer(
                            puzzleSize = puzzleSize,
                            onNavigateBack = { navController.popBackStack() },
                            nonogramViewModel = nonogramViewModel
                        )
                    }
                    composable("nonogram_completos") { CompletedNonogramsScreen(navController, nonogramViewModel) }

                    // Sudoku
                    composable("sudoku_hub") { InitialScreen(navController) }
                    composable("sudoku_difficulty_selection") { DificultySelectionScreen(navController, sudokuViewModel) }
                    composable("sudoku_board") { SudokuBoardScreen(navController, sudokuViewModel) }
                    composable("sudoku_best_times") { BestTimesScreen(navController, sudokuViewModel) }
                }
            }
        }
    }
}
