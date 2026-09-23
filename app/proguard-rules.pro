# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve annotations and line numbers for stack traces
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# Moshi rules
-keepclasseswithmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }

# Kotlin reflection (used by Moshi's KotlinJsonAdapterFactory)
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-dontwarn kotlin.reflect.jvm.internal.**

# Retrofit & OkHttp
-keepattributes Signature
-keepattributes Exceptions
-dontwarn javax.annotation.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Data Models
-keep class com.example.data.model.** { *; }
-keep class com.example.data.api.** { *; }
-keep class com.example.data.auth.** { *; }

# Firebase & Credential Manager
-keep class com.google.firebase.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
