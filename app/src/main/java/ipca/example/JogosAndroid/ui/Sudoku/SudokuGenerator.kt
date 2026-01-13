package ipca.example.JogosAndroid.ui.Sudoku

import kotlin.random.Random

object SudokuGenerator {
    private const val BOARD_SIZE = 9
    private const val SUBGRID_SIZE = 3

    enum class Difficulty(val cellsToRemove: Int) {
        EASY(40), MEDIUM(50), HARD(55)
    }

    fun generate(difficulty: Difficulty): Pair<Array<IntArray>, Array<IntArray>> {
        val board = Array(BOARD_SIZE) { IntArray(BOARD_SIZE) }
        fill(board)
        val solvedBoard = board.map { it.clone() }.toTypedArray()
        val puzzleBoard = createPuzzle(board, difficulty)
        return Pair(puzzleBoard, solvedBoard)
    }

    private fun fill(board: Array<IntArray>): Boolean {
        for (row in 0 until BOARD_SIZE) {
            for (col in 0 until BOARD_SIZE) {
                if (board[row][col] == 0) {
                    val numbers = (1..BOARD_SIZE).shuffled()
                    for (num in numbers) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num
                            if (fill(board)) return true
                            board[row][col] = 0 // Backtrack
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun createPuzzle(board: Array<IntArray>, difficulty: Difficulty): Array<IntArray> {
        var cellsToRemove = difficulty.cellsToRemove
        val puzzle = board.map { it.clone() }.toTypedArray()
        while (cellsToRemove > 0) {
            val row = Random.nextInt(BOARD_SIZE)
            val col = Random.nextInt(BOARD_SIZE)
            if (puzzle[row][col] != 0) {
                val backup = puzzle[row][col]
                puzzle[row][col] = 0
                val puzzleCopy = puzzle.map { it.clone() }.toTypedArray()
                if (countSolutions(puzzleCopy) != 1) {
                    puzzle[row][col] = backup
                } else {
                    cellsToRemove--
                }
            }
        }
        return puzzle
    }

    private fun countSolutions(board: Array<IntArray>): Int {
        var count = 0
        fun solve() {
            for (row in 0 until BOARD_SIZE) {
                for (col in 0 until BOARD_SIZE) {
                    if (board[row][col] == 0) {
                        for (num in 1..BOARD_SIZE) {
                            if (isSafe(board, row, col, num)) {
                                board[row][col] = num
                                solve()
                            }
                        }
                        board[row][col] = 0
                        return
                    }
                }
            }
            count++
        }
        solve()
        return count
    }

    private fun isSafe(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        for (c in 0 until BOARD_SIZE) if (board[row][c] == num) return false
        for (r in 0 until BOARD_SIZE) if (board[r][col] == num) return false
        val startRow = row - row % SUBGRID_SIZE
        val startCol = col - col % SUBGRID_SIZE
        for (r in 0 until SUBGRID_SIZE) {
            for (c in 0 until SUBGRID_SIZE) {
                if (board[r + startRow][c + startCol] == num) return false
            }
        }
        return true
    }
}
