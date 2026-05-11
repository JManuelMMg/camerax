# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ========== Project-specific rules ==========

# Camera X
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ML Kit - Barcode Scanning
-keep class com.google.mlkit.vision.barcode.** { *; }
-dontwarn com.google.mlkit.vision.barcode.**

# ML Kit - Object Detection
-keep class com.google.mlkit.vision.objects.** { *; }
-dontwarn com.google.mlkit.vision.objects.**

# ML Kit Common
-keep class com.google.mlkit.vision.common.** { *; }
-dontwarn com.google.mlkit.vision.common.**

# Coil (Image Loading)
-keep class coil.** { *; }
-dontwarn coil.**

# Accompanist Permissions
-keep class com.google.accompanist.permissions.** { *; }
-dontwarn com.google.accompanist.permissions.**

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.internal.MainDispatcherFactory {
    *;
}

# View Models
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# ========== Debug/Optimization ==========

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Hide original source file name
-renamesourcefileattribute SourceFile

# Keep our main classes
-keep class com.example.camerax.MainActivity { *; }
-keep class com.example.camerax.ui.** { *; }
-keep class com.example.camerax.analyzer.** { *; }
-keep class com.example.camerax.util.** { *; }

# Verbose logging for rule matching
-verbose

