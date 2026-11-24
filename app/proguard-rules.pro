# Components are referenced from the manifest and kept automatically by AGP.
# DataStore Preferences uses protobuf-lite internally; keep its generated fields.
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
