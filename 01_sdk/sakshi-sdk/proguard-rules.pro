# ProGuard rules for Sakshi SDK library internal build

# Do not globally keep all classes in rajnishkmehta.sakshi.sdk.api.**
# By default, building the release AAR without stripping is handled by library defaults,
# but if the user shrinks their app, we want the shrinking to apply.
# We only strictly preserve the AIDL IPC layer as the IPC bridge.

# Preserve AIDL IPC interfaces and stub implementations
-keep interface rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService { *; }
-keep interface rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback { *; }
-keep class rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService$Stub { *; }
-keep class rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback$Stub { *; }

# Suppress warning for missing StringConcatFactory (Java 9+)
-dontwarn java.lang.invoke.StringConcatFactory
