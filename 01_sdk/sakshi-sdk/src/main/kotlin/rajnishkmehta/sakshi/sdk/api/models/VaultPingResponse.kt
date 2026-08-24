package rajnishkmehta.sakshi.sdk.api.models

/**
 * Health check response returned by Vault when pinged.
 *
 * @property isAvailable True if Vault service is active and responsive.
 * @property vaultVersion Version string reported by Vault service.
 * @property responseTimeMs Round-trip ping duration in milliseconds.
 * @property serverTimestampEpochMs Current server timestamp reported by Vault.
 */
public data class VaultPingResponse(
    public val isAvailable: Boolean,
    public val vaultVersion: String = "unknown",
    public val responseTimeMs: Long = 0L,
    public val serverTimestampEpochMs: Long = System.currentTimeMillis()
)
