cmake_minimum_required(VERSION 3.20.0)
find_package(Zephyr REQUIRED HINTS $ENV{ZEPHYR_BASE})

project(NRF52840)

# Entry point
target_sources(app PRIVATE src/main.c)

# App headers
zephyr_library_include_directories(
  src/events
  src/uart_channel
  src/ble_gatt
  src/imu_classifier
  src/watchdog
  src/uart_resolver
  )

# App directories
add_subdirectory(src/events)
add_subdirectory(src/uart_channel)
add_subdirectory(src/ble_gatt)
add_subdirectory(src/imu_classifier)
add_subdirectory(src/uart_resolver)
add_subdirectory_ifdef(CONFIG_ENABLE_WATCHDOG src/watchdog)