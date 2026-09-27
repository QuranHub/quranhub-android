# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers & file names so Crashlytics can symbolicate stack traces
# (mapping.txt is uploaded automatically by the Firebase Crashlytics Gradle plugin).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Needed for reflection over generics (Gson TypeToken, Retrofit call adapters).
-keepattributes Signature,InnerClasses,EnclosingMethod

# Gson deserializes these by reflection. Fields without @SerializedName must
# keep their names, and Firestore's toObjects() resolves Kotlin data-class
# getters/setters reflectively - so keep members of all POJOs.
-keep class app.quranhub.core.data.model.** { *; }

# EventBus, Retrofit, Gson, RxJava2, Room, Glide, Firebase, and MaterialDrawer
# 9.x bundle their own consumer rules.

# circular-progress-button: field accessed reflectively by the library
-keepclassmembers class com.dd.StrokeGradientDrawable {
    public void setStrokeColor(int);
}
