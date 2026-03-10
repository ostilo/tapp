if(NOT TARGET hermes-engine::libhermes)
add_library(hermes-engine::libhermes SHARED IMPORTED)
set_target_properties(hermes-engine::libhermes PROPERTIES
    IMPORTED_LOCATION "/Users/ayodejiolalekan/.gradle/caches/8.10.2/transforms/f8b697a63386a5dabc8a397b3986fe0d/transformed/hermes-android-0.76.0-release/prefab/modules/libhermes/libs/android.arm64-v8a/libhermes.so"
    INTERFACE_INCLUDE_DIRECTORIES "/Users/ayodejiolalekan/.gradle/caches/8.10.2/transforms/f8b697a63386a5dabc8a397b3986fe0d/transformed/hermes-android-0.76.0-release/prefab/modules/libhermes/include"
    INTERFACE_LINK_LIBRARIES ""
)
endif()

