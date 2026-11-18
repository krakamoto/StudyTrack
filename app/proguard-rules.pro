# StudyTrack ProGuard Rules

# Domain and Data Models
-keep class com.studytrack.data.entities.** { *; }
-keep class com.studytrack.domain.models.** { *; }
-keep class com.studytrack.data.dao.** { *; }

# Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Vico Charts
-keep class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**
