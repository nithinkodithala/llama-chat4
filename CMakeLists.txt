cmake_minimum_required(VERSION 3.21)
project(llama_android)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Disable unnecessary llama.cpp targets
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "")
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "")
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "")

# Use the upstream CMakeLists.txt so it handles all version header generation properly
add_subdirectory(llama.cpp)

add_library(llama_android SHARED
    "app/src/main/cpp/llama_jni.cpp"
)

target_include_directories(llama_android PRIVATE
    "${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/include"
)

# Link our JNI library against the core llama target
target_link_libraries(llama_android
    llama
    android
    log
    m
)
