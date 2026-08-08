# Proguard/R8 rules consumed by applications integrating Sakshi SDK

# Preserve public API classes and data models
-keep class rajnishkmehta.sakshi.sdk.api.** { *; }
-keep interface rajnishkmehta.sakshi.sdk.api.** { *; }
-keepclassmembers class rajnishkmehta.sakshi.sdk.api.** { *; }

# Preserve AIDL IPC interfaces
-keep interface rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService { *; }
-keep interface rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback { *; }
-keep class rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService$Stub { *; }
-keep class rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback$Stub { *; }
