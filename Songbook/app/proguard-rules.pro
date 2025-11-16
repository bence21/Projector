# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Mario\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Keep ALL classes in com.bence.songbook package with their original names
# This prevents obfuscation issues with reflection-based frameworks (ORMLite, Gson, etc.)
-keep class com.bence.songbook.** { *; }
-keepnames class com.bence.songbook.** { *; }

# Retrofit and Gson
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Gson specific classes
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Gson model classes - keep classes with SerializedName annotations
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ORMLite - keep ORMLite classes
-keep class com.j256.ormlite.** { *; }

# Keep ORMLite annotated fields and classes
-keepclassmembers class * {
    @com.j256.ormlite.field.DatabaseField <fields>;
}
-keepclassmembers class * {
    @com.j256.ormlite.field.ForeignCollectionField <fields>;
}
-keepclassmembers class * {
    @com.j256.ormlite.table.DatabaseTable <methods>;
}
# ORMLite optional dependencies (not needed for Android)
-dontwarn javax.sql.rowset.serial.SerialBlob
-dontwarn org.apache.log4j.Level
-dontwarn org.apache.log4j.Logger
-dontwarn org.apache.log4j.Priority
-dontwarn org.apache.logging.log4j.LogManager
-dontwarn org.apache.logging.log4j.Logger
-dontwarn org.slf4j.Logger
-dontwarn org.slf4j.LoggerFactory

# AndroidX
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Parcelables
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
