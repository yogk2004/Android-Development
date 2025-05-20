cmake_minimum_required(VERSION 3.22.1)

project("mc_assignment_3_yogesh_kumar")

# Add Eigen include directory
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/eigen-3.4.0)
add_library(${CMAKE_PROJECT_NAME} SHARED
        native-lib.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        log)