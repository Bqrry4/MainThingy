
target_sources(app PRIVATE
	       ${CMAKE_CURRENT_SOURCE_DIR}/lte.c
)
target_sources(app PRIVATE
	       ${CMAKE_CURRENT_SOURCE_DIR}/modem_config.c
)

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/gnss)
