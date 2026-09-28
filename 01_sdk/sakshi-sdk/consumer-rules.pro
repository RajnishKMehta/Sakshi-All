# Proguard/R8 rules consumed by applications integrating Sakshi SDK

# Do not globally keep all classes in rajnishkmehta.sakshi.sdk.api.**
# Let R8 strip out unused models, requests, and results if the consumer app does not use them.

# We only need to preserve AIDL IPC interfaces, since those map directly to the Binder
# transactions and might be accessed reflectively or dynamically by the OS across process boundaries.
-keep interface rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService { *; }
-keep interface rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback { *; }
-keep class rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService$Stub { *; }
-keep class rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback$Stub { *; }
