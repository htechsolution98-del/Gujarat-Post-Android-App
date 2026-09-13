# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit & OkHttp
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson models
-keepclassmembers class com.gujaratpost.app.data.models.** { <fields>; }
-keep class com.gujaratpost.app.data.models.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.** { *; }
