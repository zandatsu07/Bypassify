# Shizuku.newProcess is called via reflection, so R8 must not rename or remove it.
-keep class rikka.shizuku.** { *; }
-dontwarn rikka.shizuku.**

# Keep our own entry points and shared logic intact
-keep class com.example.bypasscharging.** { *; }
