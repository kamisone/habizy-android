# Retrofit + OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson / JSON models
-keepattributes EnclosingMethod
-keep class com.google.gson.** { *; }
-keep class com.habizy.app.data.model.** { *; }
-keep class com.habizy.app.data.remote.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Jetpack Compose — keep runtime internals
-dontwarn androidx.compose.**

# DataStore
-keep class androidx.datastore.** { *; }

# Coil
-dontwarn coil.**
