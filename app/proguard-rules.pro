# Keep kotlinx.serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.artmondo.boxters.**$$serializer { *; }
-keepclassmembers class com.artmondo.boxters.** { *** Companion; }
-keepclasseswithmembers class com.artmondo.boxters.** { kotlinx.serialization.KSerializer serializer(...); }
