# Reglas de ofuscación para MateLab.
# La app no usa reflexión salvo la que generan Room y kotlinx.serialization.

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.matelab.islas.**$$serializer { *; }
-keepclassmembers class com.matelab.islas.** {
    *** Companion;
}
-keepclasseswithmembers class com.matelab.islas.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Room ---
-keep class androidx.room.RoomDatabase { *; }
-keep class com.matelab.islas.data.local.** { *; }

# --- Modelos de dominio (se serializan a JSON en la base de datos) ---
-keep class com.matelab.islas.domain.model.** { *; }
