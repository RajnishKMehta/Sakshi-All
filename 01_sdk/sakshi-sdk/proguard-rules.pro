# ProGuard rules for Sakshi SDK library internal build

# Preserve public API package classes and methods
-keep class rajnishkmehta.sakshi.sdk.api.** { *; }
-keep interface rajnishkmehta.sakshi.sdk.api.** { *; }
-keepclassmembers class rajnishkmehta.sakshi.sdk.api.** { *; }

# Preserve AIDL IPC interfaces and stub implementations
-keep interface rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService { *; }
-keep interface rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback { *; }
-keep class rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService$Stub { *; }
-keep class rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback$Stub { *; }

# Suppress warning for missing StringConcatFactory (Java 9+)
-dontwarn java.lang.invoke.StringConcatFactory
