
target_sources(app PRIVATE
	       ${CMAKE_CURRENT_SOURCE_DIR}/lte.c
)

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/gnss)
