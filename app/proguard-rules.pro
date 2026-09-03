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

# Needed for reflection over generics (Gson TypeToken, Retrofit call adapters,
# FastAdapter/MaterialDrawer TypeUtils).
-keepattributes Signature,InnerClasses,EnclosingMethod

# Gson deserializes these by reflection. Fields without @SerializedName must
# keep their names, and Firestore's toObjects() resolves Kotlin data-class
# getters/setters reflectively - so keep members of all POJOs.
-keep class app.quranhub.data.model.** { *; }
-keep class app.quranhub.data.remote.model.** { *; }

# MaterialDrawer v6.1.3 does not ship consumer ProGuard rules.
# (EventBus, Retrofit, Gson, RxJava2, Room, Glide and Firebase all bundle
# their own consumer rules - no manual rules required for those.)
-keep class com.mikepenz.materialdrawer.** { *; }
-keep class com.mikepenz.fastadapter.** { *; }

# circular-progress-button: field accessed reflectively by the library
-keepclassmembers class com.dd.StrokeGradientDrawable {
    public void setStrokeColor(int);
}
