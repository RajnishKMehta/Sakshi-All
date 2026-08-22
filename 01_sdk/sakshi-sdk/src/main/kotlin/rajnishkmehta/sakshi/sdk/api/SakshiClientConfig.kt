package rajnishkmehta.sakshi.sdk.api

/**
 * Configuration settings used when instantiating [SakshiClient].
 *
 * @property vaultPackageName Android package name of the Vault application.
 * @property vaultServiceAction Intent action used to bind to Vault's remote IPC service.
 * @property connectionTimeoutMs Connection timeout in milliseconds when binding to Vault.
 */
public data class SakshiClientConfig(
    public val vaultPackageName: String = DEFAULT_VAULT_PACKAGE_NAME,
    public val vaultServiceAction: String = DEFAULT_VAULT_SERVICE_ACTION,
    public val connectionTimeoutMs: Long = DEFAULT_CONNECTION_TIMEOUT_MS
) {
    public companion object {
        /** Default package name for the Vault application. */
        public const val DEFAULT_VAULT_PACKAGE_NAME: String = "rajnishkmehta.sakshi.vault"

        /** Default intent action used for binding to the Vault service. */
        public const val DEFAULT_VAULT_SERVICE_ACTION: String = "rajnishkmehta.sakshi.vault.BIND_VAULT_SERVICE"

        /** Default timeout duration in milliseconds for IPC service binding. */
        public const val DEFAULT_CONNECTION_TIMEOUT_MS: Long = 5000L
    }
}
