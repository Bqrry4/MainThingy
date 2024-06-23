zephyr_library_include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/battery_monitor
    ${CMAKE_CURRENT_SOURCE_DIR}/buzzer
    ${CMAKE_CURRENT_SOURCE_DIR}/led
  )


add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/battery_monitor)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/buzzer)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/led)
