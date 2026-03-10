if(NOT TARGET fbjni::fbjni)
add_library(fbjni::fbjni SHARED IMPORTED)
set_target_properties(fbjni::fbjni PROPERTIES
    IMPORTED_LOCATION "/Users/ayodejiolalekan/.gradle/caches/8.10.2/transforms/45b9ce6cd480923371067ff4bc5f1a72/transformed/fbjni-0.6.0/prefab/modules/fbjni/libs/android.x86/libfbjni.so"
    INTERFACE_INCLUDE_DIRECTORIES "/Users/ayodejiolalekan/.gradle/caches/8.10.2/transforms/45b9ce6cd480923371067ff4bc5f1a72/transformed/fbjni-0.6.0/prefab/modules/fbjni/include"
    INTERFACE_LINK_LIBRARIES ""
)
endif()

