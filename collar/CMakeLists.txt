cmake_minimum_required(VERSION 3.20.0)
find_package(Zephyr REQUIRED HINTS $ENV{ZEPHYR_BASE})

project(MainThingy)

target_sources(app PRIVATE src/main.c)

# zephyr_include_directories(src)
zephyr_library_sources(src/lte.c)
zephyr_library_sources(src/gnss.c)
zephyr_library_sources(src/assistance.c)
zephyr_library_sources(src/mainThingy.c)
