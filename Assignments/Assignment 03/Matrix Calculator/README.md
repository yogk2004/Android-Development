# Matrix Calculator Android App - Yogesh Kumar 2022593

This Android application enables users to perform fundamental matrix operations—addition, subtraction, multiplication, and division. It combines **Kotlin** for the user interface and **C++** with the **Eigen** library for efficient matrix computations, leveraging the **Java Native Interface (JNI)** for seamless integration.

---

## Project Overview

The Matrix Calculator app provides a simple interface where users can:

- Input dimensions and elements of two matrices.
- Select an operation (addition, subtraction, multiplication, or division).
- View the computed result or error messages for invalid inputs.

The app uses native C++ code for performance-critical matrix operations, interfaced with a Kotlin-based frontend.

---

## File Structure

### `MainActivity.kt`
- **Role**: The main activity of the app, handling the user interface and interaction logic.
- **Details**:
  - Manages UI components like input fields for matrix dimensions and elements, a floating action button (FAB) to toggle the operation menu, and buttons for each operation.
  - Validates user inputs (e.g., matching dimensions for addition/subtraction, compatibility for multiplication/division).
  - Calls native functions via `MatrixOperations` and displays results or errors.

### `MatrixOperations.kt`
- **Role**: A Kotlin object that interfaces with the native C++ library.
- **Details**:
  - Loads the native library `mc_assignment_3_yogesh_kumar`.
  - Declares external functions for matrix operations: `combineMatrices` (addition), `differenceMatrices` (subtraction), `productMatrices` (multiplication), and `quotientMatrices` (division).
  - Acts as a bridge between Kotlin and C++ via JNI.

### `CMakeLists.txt`
- **Role**: Configures the build process for the native C++ code.
- **Details**:
  - Specifies the project name (`mc_assignment_3_yogesh_kumar`).
  - Includes the Eigen library directory for linear algebra support.
  - Links the native library with Android and log libraries for functionality and debugging.

### `native-lib.cpp`
- **Role**: Implements matrix operations in C++ using the Eigen library.
- **Details**:
  - Contains JNI functions corresponding to the operations declared in `MatrixOperations.kt`.
  - Performs addition and subtraction via element-wise operations, multiplication via nested loops, and division using Eigen’s matrix inverse.
  - Includes input validation and error handling (e.g., non-invertible matrices for division).

---

## Installation Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
```

### 2. Open in Android Studio

- Launch Android Studio.
- Select **File > Open**, then navigate to and select the project folder.

### 3. Install Dependencies

- Ensure the **Android NDK** (Native Development Kit) is installed:
  - Go to **File > Project Structure > SDK Location** and install the NDK if not present.
- The **Eigen library (version 3.4.0)** is included in the project under the `eigen-3.4.0` directory—no additional download is needed.

### 4. Build the Project

- Select **Build > Make Project** to compile both Kotlin and C++ code.
- Android Studio uses `CMakeLists.txt` to build the native library automatically.

### 5. Run the App

- Connect an Android device via USB or start an emulator (e.g., via AVD Manager).
- Click **Run > Run 'app'** to deploy and launch the app.

---

## Usage

### Input Matrix Dimensions

- Enter the number of rows and columns for Matrix 1 and Matrix 2 in the respective fields (e.g., "2" for rows, "3" for columns).

### Input Matrix Elements

- Enter the elements for each matrix in the provided text fields.
- **Format**: Space-separated numbers in row-major order  
  _Example: For a 2×2 matrix → `1 2 3 4`_

### Select an Operation

- Tap the **FAB (floating action button)** to show the operation menu.
- Choose from:
  - **Addition**: Adds Matrix 1 and Matrix 2 (requires identical dimensions).
  - **Subtraction**: Subtracts Matrix 2 from Matrix 1 (requires identical dimensions).
  - **Multiplication**: Multiplies Matrix 1 by Matrix 2 (columns of Matrix 1 must equal rows of Matrix 2).
  - **Division**: Computes `Matrix 1 × (Matrix 2)⁻¹` (Matrix 2 must be square and invertible).

### View the Result

- The result appears in the result text view, formatted as a matrix with two-decimal precision.
- Errors (e.g., mismatched dimensions, non-invertible matrix) are shown as **Toast** messages.

---

## Example

**Matrix 1**: 2x2, Elements: `1 2 3 4`  
**Matrix 2**: 2x2, Elements: `5 6 7 8`  
**Operation**: Addition

**Result:**

```
6.00    8.00
10.00   12.00
```

---

## Dependencies

- **Android NDK**: Required for compiling and running the C++ code. Install via Android Studio’s SDK Manager.
- **Eigen Library**: A C++ template library for linear algebra (version 3.4.0), bundled in the project under `eigen-3.4.0`.

---

## Troubleshooting

### Build Errors

- **Issue**: NDK not found or CMake errors.  
  **Fix**: Verify NDK installation in **File > Project Structure > SDK Location**. Ensure **CMake** is installed (via **SDK Tools** tab).

### Runtime Errors

- **Issue**: "Matrix dimensions don’t match" or similar.  
  **Fix**: Check that dimensions align with the operation (e.g., same size for addition/subtraction, compatible for multiplication).

- **Issue**: "Division not possible" Toast.  
  **Fix**: Ensure Matrix 2 is square and invertible (determinant ≠ 0).

### Performance Issues

- **Issue**: Slow computation for large matrices.  
  **Fix**: Test with smaller matrices; the native code is optimized, but very large inputs may still lag on some devices.