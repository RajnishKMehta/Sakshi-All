# Keep Room Database and DAOs
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep interface * extends androidx.room.RoomDatabase { *; }
-keep class * implements androidx.room.RoomDatabase { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep AIDL-generated binder interfaces and stubs
-keep public interface * extends android.os.IInterface { *; }
-keep public class * extends android.os.Binder { *; }
-keep class * implements android.os.IInterface { *; }

# Keep Sakshi Vault classes and methods
-keep class rajnishkmehta.sakshi.vault.** { *; }
-keep class rajnishkmehta.sakshi.sdk.** { *; }

# Keep Service classes (so they are not renamed)
-keep public class * extends android.app.Service

# remove logs
-assumenosideeffects class rajnishkmehta.sakshi.vault.AppLog {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}
