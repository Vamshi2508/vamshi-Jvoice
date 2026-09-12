# ---------------------------------------------------------------- Firestore/RTDB
# Firestore and Realtime Database map documents onto these classes by REFLECTION
# (toObject<T>() reads property names at runtime). R8 renames fields by default,
# so without these rules every model would deserialise to nulls in release only -
# a failure that never shows up in a debug build or in testing on a debug APK.
-keepclassmembers class com.jvoice.**.data.model.** {
    *;
}
-keep class com.jvoice.**.data.model.** { *; }

# Firebase keeps its own annotations and the no-arg constructors it needs.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.PropertyName <fields>;
}
-keepclasseswithmembers class * {
    public <init>();
}

# ------------------------------------------------------------------- Kotlin meta
# Reflection over Kotlin classes needs the metadata annotation intact.
-keep class kotlin.Metadata { *; }

# ----------------------------------------------------------------------- Compose
# Compose ships its own consumer rules; this only silences the warnings R8 emits
# for optional desugaring classes it cannot resolve.
-dontwarn org.jetbrains.annotations.**
-dontwarn javax.annotation.**
