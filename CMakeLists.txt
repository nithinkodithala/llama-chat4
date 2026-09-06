cmake_minimum_required(VERSION 3.21)
project(llama)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# JNI interface
find_package(JNI REQUIRED)

# Add llama.cpp sources
set(LLAMA_CPP_DIR "${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp")

add_library(llama SHARED
    "${LLAMA_CPP_DIR}/ggml.c"
    "${LLAMA_CPP_DIR}/llama.cpp"
    "app/src/main/cpp/llama_jni.cpp"
)

target_include_directories(llama PRIVATE
    ${LLAMA_CPP_DIR}
    ${JNI_INCLUDE_DIRS}
)

target_compile_options(llama PRIVATE
    -O3
    -ffast-math
    -march=native
)

# Link against Android libraries
target_link_libraries(llama
    android
    log
    m
)
