# ProGuard / R8 rules for Julie app

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Room, Kotlinx Serialization, and Hilt typically include their own consumer rules
# but if we run into any specific missing classes in production, add them below.

# Retain generic signatures for type parameters
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Preserve Serialization classes
-keep @kotlinx.serialization.Serializable class * {
    <fields>;
}

# Preserve DataStore preferences
-keep class androidx.datastore.preferences.** { *; }
