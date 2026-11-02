# disconnect

## Purpose

Explicitly disconnects from the Vault IPC service and releases internal resources.

## Declaration

```kotlin
public fun disconnect()
```

## Behaviour

Unbinds the underlying Android `ServiceConnection`.

## Client Application Integration

Call this when your component (e.g., Activity, ViewModel) is being destroyed to prevent memory or connection leaks.

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import androidx.lifecycle.ViewModel

class MyViewModel(private val client: SakshiClient) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        client.disconnect()
    }
}
```

## Vault Integration
The Android system handles unbinding the client from the `SakshiVaultRemoteService`.
