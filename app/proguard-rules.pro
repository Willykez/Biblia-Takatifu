# Keep only app entry points
-keep class com.willykez.appdesign.MainActivity { *; }

# Room entities: field names/types are read by the generated DAO via reflection at
# schema-validation time, and shrinking release builds have tripped on this before.
-keep class com.willykez.appdesign.data.*Entity { *; }
-keepclassmembers class com.willykez.appdesign.data.*Entity { *; }

# Enums persisted by name (DataStore theme-mode prefs) and restored via valueOf() - keep
# both the class list and the constant names stable so a release build can still round-trip
# a value a debug build wrote (or vice versa).
-keepclassmembers enum com.willykez.appdesign.data.** { *; }
-keep enum com.willykez.appdesign.data.** { *; }