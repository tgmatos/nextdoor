# Add project specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.nextdoor.app.**$$serializer { *; }
-keepclassmembers class com.nextdoor.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.nextdoor.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
