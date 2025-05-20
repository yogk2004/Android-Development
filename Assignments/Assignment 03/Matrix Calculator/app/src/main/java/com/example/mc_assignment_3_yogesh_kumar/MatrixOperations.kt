package com.example.mc_assignment_3_yogesh_kumar

import android.util.Log

object MatrixOperations {

    private const val NATIVE_LIB = "mc_assignment_3_yogesh_kumar"
    private const val TAG = "MatrixOperations"

    init {
        loadNativeCode()
    }

    private fun loadNativeCode() = try {
        System.loadLibrary(NATIVE_LIB).also {
            Log.d(TAG, "Native library loaded successfully")
        }
    } catch (e: UnsatisfiedLinkError) {
        Log.e(TAG, "Native library loading failed: ${e.message}").also {
            throw e
        }
    }

    external fun combineMatrices(
        rowCount: Int,
        colCount: Int,
        matrixA: DoubleArray,
        matrixB: DoubleArray
    ): DoubleArray

    external fun differenceMatrices(
        rowCount: Int,
        colCount: Int,
        matrixA: DoubleArray,
        matrixB: DoubleArray
    ): DoubleArray

    external fun productMatrices(
        rowsA: Int,
        colsA: Int,
        rowsB: Int,
        colsB: Int,
        matrixA: DoubleArray,
        matrixB: DoubleArray
    ): DoubleArray

    external fun quotientMatrices(
        rowsA: Int,
        colsA: Int,
        rowsB: Int,
        colsB: Int,
        matrixA: DoubleArray,
        matrixB: DoubleArray
    ): DoubleArray
}