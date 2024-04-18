
target_sources(app PRIVATE
	       ${CMAKE_CURRENT_SOURCE_DIR}/gnss.c
)

#Add assistance subdir when enabled
add_subdirectory_ifdef(CONFIG_USE_ASSISTANCE ${CMAKE_CURRENT_SOURCE_DIR}/assistance)
