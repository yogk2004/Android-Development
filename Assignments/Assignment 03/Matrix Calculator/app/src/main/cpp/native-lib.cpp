#include <jni.h>
#include <Eigen/Dense>
#include <cmath>
#include <android/log.h>

#define LOG_IDENTIFIER "MatrixCalculations"
#define LOG_OUTPUT(level, format, ...) __android_log_print(level, LOG_IDENTIFIER, "[%s]: " format, __FUNCTION__, ##__VA_ARGS__)

typedef Eigen::Matrix<double, Eigen::Dynamic, Eigen::Dynamic, Eigen::RowMajor> DynamicMatrix;

void raiseJavaError(JNIEnv* env, const char* message) {
    jclass errorClass = env->FindClass("java/lang/RuntimeException");
    if (errorClass) {
        env->ThrowNew(errorClass, message);
        env->DeleteLocalRef(errorClass);
    }
}

bool validateMatrix(JNIEnv* env, jint rowNum, jint colNum, jdoubleArray array, jsize expectedSize, const char* matrixName) {
    if (rowNum <= 0 || colNum <= 0) {
        LOG_OUTPUT(ANDROID_LOG_ERROR, "Invalid %s dimensions: rows=%d, cols=%d", matrixName, rowNum, colNum);
        raiseJavaError(env, "Matrix dimensions must be positive");
        return false;
    }
    jsize actualSize = env->GetArrayLength(array);
    if (actualSize != expectedSize) {
        LOG_OUTPUT(ANDROID_LOG_ERROR, "Invalid %s array size: got %d, expected %d", matrixName, actualSize, expectedSize);
        raiseJavaError(env, "Array size does not match matrix dimensions");
        return false;
    }
    return true;
}

bool getMatrixData(JNIEnv* env, jdoubleArray array, jdouble** elements, const char* matrixName) {
    *elements = env->GetDoubleArrayElements(array, nullptr);
    if (!*elements) {
        LOG_OUTPUT(ANDROID_LOG_ERROR, "Failed to access %s matrix data", matrixName);
        raiseJavaError(env, "Failed to access matrix data");
        return false;
    }
    return true;
}

void releaseMatrixData(JNIEnv* env, jdoubleArray array, jdouble* elements, jboolean saveChanges) {
    if (elements) {
        env->ReleaseDoubleArrayElements(array, elements, saveChanges ? 0 : JNI_ABORT);
    }
}

jdoubleArray createOutputMatrix(JNIEnv* env, jint size, const double* data) {
    jdoubleArray outputArray = env->NewDoubleArray(size);
    if (!outputArray) {
        LOG_OUTPUT(ANDROID_LOG_ERROR, "Failed to create output matrix");
        raiseJavaError(env, "Failed to create output matrix");
        return nullptr;
    }
    env->SetDoubleArrayRegion(outputArray, 0, size, data);
    return outputArray;
}

void addMatrixElements(jint rowNum, jint colNum, const jdouble* matA, const jdouble* matB, double* result) {
    for (jint i = 0; i < rowNum * colNum; ++i) {
        result[i] = matA[i] + matB[i];
    }
}

void subtractMatrixElements(jint rowNum, jint colNum, const jdouble* matA, const jdouble* matB, double* result) {
    for (jint i = 0; i < rowNum * colNum; ++i) {
        result[i] = matA[i] - matB[i];
    }
}

void multiplyMatrices(jint rowsA, jint colsA, jint colsB, const jdouble* matA, const jdouble* matB, double* result) {
    for (jint i = 0; i < rowsA; ++i) {
        for (jint j = 0; j < colsB; ++j) {
            double sum = 0.0;
            for (jint k = 0; k < colsA; ++k) {
                sum += matA[i * colsA + k] * matB[k * colsB + j];
            }
            result[i * colsB + j] = sum;
        }
    }
}

bool divideMatrices(JNIEnv* env, jint rowsA, jint colsA, jint rowsB, jint colsB, const jdouble* matA, const jdouble* matB, double* result) {
    double* matACopy = new double[rowsA * colsA];
    double* matBCopy = new double[rowsB * colsB];
    for (jint i = 0; i < rowsA * colsA; ++i) matACopy[i] = matA[i];
    for (jint i = 0; i < rowsB * colsB; ++i) matBCopy[i] = matB[i];

    Eigen::Map<DynamicMatrix> eigenMatA(matACopy, rowsA, colsA);
    Eigen::Map<DynamicMatrix> eigenMatB(matBCopy, rowsB, colsB);
    Eigen::MatrixXd matBColMajor = eigenMatB;

    double determinant = matBColMajor.determinant();
    if (std::abs(determinant) < 1e-10) {
        LOG_OUTPUT(ANDROID_LOG_ERROR, "Non-invertible matrix: determinant=%f", determinant);
        delete[] matACopy;
        delete[] matBCopy;
        return false;
    }

    Eigen::MatrixXd inverseMatB = matBColMajor.inverse();
    DynamicMatrix divisionResult = eigenMatA * inverseMatB;
    for (jint i = 0; i < rowsA * colsB; ++i) {
        result[i] = divisionResult.data()[i];
    }

    delete[] matACopy;
    delete[] matBCopy;
    return true;
}

extern "C" {

JNIEXPORT jdoubleArray JNICALL
Java_com_example_mc_1assignment_13_1yogesh_1kumar_MatrixOperations_combineMatrices(
        JNIEnv* env,
        jobject,
        jint rowNum,
        jint colNum,
        jdoubleArray matA,
        jdoubleArray matB) {

    if (!validateMatrix(env, rowNum, colNum, matA, rowNum * colNum, "first") ||
        !validateMatrix(env, rowNum, colNum, matB, rowNum * colNum, "second")) {
        return nullptr;
    }

    jdouble* matAData = nullptr;
    jdouble* matBData = nullptr;
    if (!getMatrixData(env, matA, &matAData, "first") ||
        !getMatrixData(env, matB, &matBData, "second")) {
        releaseMatrixData(env, matA, matAData, false);
        releaseMatrixData(env, matB, matBData, false);
        return nullptr;
    }

    double* output = new double[rowNum * colNum];
    addMatrixElements(rowNum, colNum, matAData, matBData, output);

    jdoubleArray resultMatrix = createOutputMatrix(env, rowNum * colNum, output);

    releaseMatrixData(env, matA, matAData, false);
    releaseMatrixData(env, matB, matBData, false);
    delete[] output;

    LOG_OUTPUT(ANDROID_LOG_DEBUG, "Matrix addition completed");
    return resultMatrix;
}

JNIEXPORT jdoubleArray JNICALL
Java_com_example_mc_1assignment_13_1yogesh_1kumar_MatrixOperations_differenceMatrices(
        JNIEnv* env,
        jobject,
        jint rowNum,
        jint colNum,
        jdoubleArray matA,
        jdoubleArray matB) {

    if (!validateMatrix(env, rowNum, colNum, matA, rowNum * colNum, "first") ||
        !validateMatrix(env, rowNum, colNum, matB, rowNum * colNum, "second")) {
        return nullptr;
    }

    jdouble* matAData = nullptr;
    jdouble* matBData = nullptr;
    if (!getMatrixData(env, matA, &matAData, "first") ||
        !getMatrixData(env, matB, &matBData, "second")) {
        releaseMatrixData(env, matA, matAData, false);
        releaseMatrixData(env, matB, matBData, false);
        return nullptr;
    }

    double* output = new double[rowNum * colNum];
    subtractMatrixElements(rowNum, colNum, matAData, matBData, output);

    jdoubleArray resultMatrix = createOutputMatrix(env, rowNum * colNum, output);

    releaseMatrixData(env, matA, matAData, false);
    releaseMatrixData(env, matB, matBData, false);
    delete[] output;

    LOG_OUTPUT(ANDROID_LOG_DEBUG, "Matrix subtraction completed");
    return resultMatrix;
}

JNIEXPORT jdoubleArray JNICALL
Java_com_example_mc_1assignment_13_1yogesh_1kumar_MatrixOperations_productMatrices(
        JNIEnv* env,
        jobject,
        jint rowsA,
        jint colsA,
        jint rowsB,
        jint colsB,
        jdoubleArray matA,
        jdoubleArray matB) {

    if (!validateMatrix(env, rowsA, colsA, matA, rowsA * colsA, "first") ||
        !validateMatrix(env, rowsB, colsB, matB, rowsB * colsB, "second")) {
        return nullptr;
    }

    jdouble* matAData = nullptr;
    jdouble* matBData = nullptr;
    if (!getMatrixData(env, matA, &matAData, "first") ||
        !getMatrixData(env, matB, &matBData, "second")) {
        releaseMatrixData(env, matA, matAData, false);
        releaseMatrixData(env, matB, matBData, false);
        return nullptr;
    }

    double* output = new double[rowsA * colsB];
    multiplyMatrices(rowsA, colsA, colsB, matAData, matBData, output);

    jdoubleArray resultMatrix = createOutputMatrix(env, rowsA * colsB, output);

    releaseMatrixData(env, matA, matAData, false);
    releaseMatrixData(env, matB, matBData, false);
    delete[] output;

    LOG_OUTPUT(ANDROID_LOG_DEBUG, "Matrix multiplication completed");
    return resultMatrix;
}

JNIEXPORT jdoubleArray JNICALL
Java_com_example_mc_1assignment_13_1yogesh_1kumar_MatrixOperations_quotientMatrices(
        JNIEnv* env,
        jobject,
        jint rowsA,
        jint colsA,
        jint rowsB,
        jint colsB,
        jdoubleArray matA,
        jdoubleArray matB) {

    if (!validateMatrix(env, rowsA, colsA, matA, rowsA * colsA, "first") ||
        !validateMatrix(env, rowsB, colsB, matB, rowsB * colsB, "second")) {
        return nullptr;
    }

    jdouble* matAData = nullptr;
    jdouble* matBData = nullptr;
    if (!getMatrixData(env, matA, &matAData, "first") ||
        !getMatrixData(env, matB, &matBData, "second")) {
        releaseMatrixData(env, matA, matAData, false);
        releaseMatrixData(env, matB, matBData, false);
        return nullptr;
    }

    double* output = new double[rowsA * colsB];
    if (!divideMatrices(env, rowsA, colsA, rowsB, colsB, matAData, matBData, output)) {
        releaseMatrixData(env, matA, matAData, false);
        releaseMatrixData(env, matB, matBData, false);
        delete[] output;
        return env->NewDoubleArray(0);
    }

    jdoubleArray resultMatrix = createOutputMatrix(env, rowsA * colsB, output);

    releaseMatrixData(env, matA, matAData, false);
    releaseMatrixData(env, matB, matBData, false);
    delete[] output;

    LOG_OUTPUT(ANDROID_LOG_DEBUG, "Matrix division completed");
    return resultMatrix;
}

}