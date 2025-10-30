cmake_minimum_required(VERSION 3.22.1)
project(edgeproc)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(edgeproc SHARED
    native-lib.cpp)

find_library(log-lib log)

# Optional OpenCV (if provided by user via OpenCV_DIR)
option(WITH_OPENCV "Build with OpenCV" ON)
if (WITH_OPENCV)
    find_package(OpenCV QUIET COMPONENTS core imgproc)
    if (OpenCV_FOUND)
        target_compile_definitions(edgeproc PRIVATE HAVE_OPENCV=1)
        target_include_directories(edgeproc PRIVATE ${OpenCV_INCLUDE_DIRS})
        target_link_libraries(edgeproc ${OpenCV_LIBS})
    endif()
endif()

target_link_libraries(edgeproc ${log-lib})


