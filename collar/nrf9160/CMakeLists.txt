cmake_minimum_required(VERSION 3.20.0)
find_package(Zephyr REQUIRED HINTS $ENV{ZEPHYR_BASE})

project(NRF9160)

# Entry point
target_sources(app PRIVATE src/main.c)

zephyr_include_directories(src)

# App headers
zephyr_library_include_directories(
  src/events
  src/modem
  src/uart_channel
  src/server_exchange
  src/modules
  src/watchdog
  src/uart_resolver
  )

# App sources
add_subdirectory(src/events)
add_subdirectory(src/modem)
add_subdirectory(src/uart_channel)
add_subdirectory(src/server_exchange)
add_subdirectory(src/modules)
add_subdirectory(src/uart_resolver)
add_subdirectory_ifdef(CONFIG_ENABLE_WATCHDOG src/watchdog)