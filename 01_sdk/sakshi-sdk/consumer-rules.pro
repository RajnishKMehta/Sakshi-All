# R8 rules for apps using the Sakshi SDK.

# Don't keep the whole API package.
# Unused models and request/response classes can still be removed by R8.

# Keep the AIDL interfaces and generated Stub classes.
# These are used for Binder IPC between the app and the Vault process.
-keep interface rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService { *; }
-keep interface rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback { *; }
-keep class rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService$Stub { *; }
-keep class rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback$Stub { *; }
