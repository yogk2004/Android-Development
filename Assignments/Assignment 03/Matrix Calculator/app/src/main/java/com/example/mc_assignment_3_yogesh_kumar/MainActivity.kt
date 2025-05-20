package com.example.mc_assignment_3_yogesh_kumar

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mc_assignment_3_yogesh_kumar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var uiElements: ActivityMainBinding
    private var isMenuVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        setContentView(uiElements.root)
        prepareViews()
    }

    private fun setupBinding() {
        uiElements = ActivityMainBinding.inflate(layoutInflater)
    }

    private fun prepareViews() {
        setupMenuButton()
        setupActionButtons()
    }

    private fun setupMenuButton() {
        uiElements.fabMenu.setOnClickListener {
            isMenuVisible = !isMenuVisible
            toggleMenuVisibility()
        }
    }

    private fun toggleMenuVisibility() {
        uiElements.apply {
            buttonAddition.visibility = if (isMenuVisible) View.VISIBLE else View.GONE
            buttonSubtraction.visibility = if (isMenuVisible) View.VISIBLE else View.GONE
            buttonMultiplication.visibility = if (isMenuVisible) View.VISIBLE else View.GONE
            buttonDivision.visibility = if (isMenuVisible) View.VISIBLE else View.GONE
        }
    }

    private fun setupActionButtons() {
        val buttonHandler = createButtonHandler()
        uiElements.apply {
            buttonAddition.setOnClickListener(buttonHandler)
            buttonSubtraction.setOnClickListener(buttonHandler)
            buttonMultiplication.setOnClickListener(buttonHandler)
            buttonDivision.setOnClickListener(buttonHandler)
        }
    }

    private fun createButtonHandler(): View.OnClickListener {
        return View.OnClickListener { view ->
            when (view.id) {
                R.id.buttonAddition -> performCalculation("add")
                R.id.buttonSubtraction -> performCalculation("subtract")
                R.id.buttonMultiplication -> performCalculation("multiply")
                R.id.buttonDivision -> performCalculation("divide")
            }
            isMenuVisible = false
            toggleMenuVisibility()
        }
    }

    private fun performCalculation(action: String) {
        try {
            checkInputs(action)
            calculateResult(action)
        } catch (e: Exception) {
            showErrorMessage(e)
        }
    }

    private fun checkInputs(action: String) {
        val (r1, c1) = getDimensions(uiElements.inputMatrix1Rows, uiElements.inputMatrix1Cols, "Matrix 1")
        val (r2, c2) = getDimensions(uiElements.inputMatrix2Rows, uiElements.inputMatrix2Cols, "Matrix 2")

        when (action) {
            "multiply" -> verifyMultiplication(c1, r2)
            "divide" -> verifyDivision(r2, c2, c1, r2)
            else -> verifyDimensions(r1, c1, r2, c2)
        }
    }

    private fun getDimensions(rowInput: EditText, colInput: EditText, name: String): Pair<Int, Int> {
        return Pair(
            rowInput.text.toString().toIntOrNull() ?: throw IllegalArgumentException("Invalid rows for $name"),
            colInput.text.toString().toIntOrNull() ?: throw IllegalArgumentException("Invalid columns for $name")
        )
    }

    private fun verifyMultiplication(colsA: Int, rowsB: Int) {
        if (colsA != rowsB) {
            throw IllegalArgumentException("For multiplication, columns of first matrix must equal rows of second matrix")
        }
    }

    private fun verifyDivision(rowsB: Int, colsB: Int, colsA: Int, rowsBDiv: Int) {
        if (rowsB != colsB) throw IllegalArgumentException("For division, second matrix must be square")
        if (colsA != rowsBDiv) throw IllegalArgumentException("For division, columns of first matrix must equal rows of second matrix")
    }

    private fun verifyDimensions(r1: Int, c1: Int, r2: Int, c2: Int) {
        if (r1 != r2 || c1 != c2) {
            throw IllegalArgumentException("Matrix dimensions must match for this operation")
        }
    }

    private fun calculateResult(operation: String) {
        val r1 = uiElements.inputMatrix1Rows.text.toString().toInt()
        val c1 = uiElements.inputMatrix1Cols.text.toString().toInt()
        val r2 = uiElements.inputMatrix2Rows.text.toString().toInt()
        val c2 = uiElements.inputMatrix2Cols.text.toString().toInt()

        val m1 = parseInput(uiElements.inputMatrix1Elements.text.toString(), r1, c1)
        val m2 = parseInput(uiElements.inputMatrix2Elements.text.toString(), r2, c2)

        val output = when (operation) {
            "add" -> MatrixOperations.combineMatrices(r1, c1, m1, m2)
            "subtract" -> MatrixOperations.differenceMatrices(r1, c1, m1, m2)
            "multiply" -> MatrixOperations.productMatrices(r1, c1, r2, c2, m1, m2)
            "divide" -> MatrixOperations.quotientMatrices(r1, c1, r2, c2, m1, m2)
            else -> doubleArrayOf()
        }

        processOutput(operation, output, r1, c1, r2, c2)
    }

    private fun parseInput(data: String, rows: Int, cols: Int): DoubleArray {
        val values = data.trim().split("\\s+".toRegex()).map { it.toDouble() }
        if (values.size != rows * cols) throw IllegalArgumentException("Matrix dimensions don't match input elements")
        return values.toDoubleArray()
    }

    private fun processOutput(op: String, data: DoubleArray, r1: Int, c1: Int, r2: Int, c2: Int) {
        if (op == "divide" && data.isEmpty()) {
            Toast.makeText(this, "Division not possible (second matrix is not invertible)", Toast.LENGTH_SHORT).show()
            return
        }

        val outRows = if (op in listOf("multiply", "divide")) r1 else r1
        val outCols = if (op in listOf("multiply", "divide")) c2 else c1

        uiElements.textViewResult.text = formatOutput(data, outRows, outCols)
    }

    private fun formatOutput(matrix: DoubleArray, rows: Int, cols: Int): String {
        return (0 until rows).joinToString("\n") { row ->
            (0 until cols).joinToString("\t") { col ->
                "%.2f".format(matrix[row * cols + col])
            }
        }
    }

    private fun showErrorMessage(e: Exception) {
        Log.e("MainActivity", "Error: ${e.message}")
        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}