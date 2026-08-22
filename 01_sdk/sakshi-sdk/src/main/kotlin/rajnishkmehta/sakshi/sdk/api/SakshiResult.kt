package rajnishkmehta.sakshi.sdk.api

/**
 * A sealed hierarchy representing either a successful execution result [Success]
 * or an error outcome [Failure] for Sakshi SDK operations.
 *
 * @param T The type of successful payload returned by the SDK.
 */
public sealed interface SakshiResult<out T> {

    /**
     * Represents a successful SDK execution.
     *
     * @property data The success result payload.
     */
    public data class Success<out T>(public val data: T) : SakshiResult<T>

    /**
     * Represents a failed SDK execution.
     *
     * @property error The typed [SakshiError] detailing the cause of failure.
     */
    public data class Failure(public val error: SakshiError) : SakshiResult<Nothing>

    /**
     * Convenience check returning true if this result is a [Success].
     */
    public val isSuccess: Boolean
        get() = this is Success

    /**
     * Convenience check returning true if this result is a [Failure].
     */
    public val isFailure: Boolean
        get() = this is Failure

    /**
     * Returns the encapsulated data if [Success], or `null` if [Failure].
     */
    public fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    /**
     * Returns the encapsulated error if [Failure], or `null` if [Success].
     */
    public fun errorOrNull(): SakshiError? = when (this) {
        is Success -> null
        is Failure -> error
    }

}

/**
 * Transforms the success value using [transform] if this is a [Success],
 * or returns the original [Failure].
 */
public inline fun <T, R> SakshiResult<T>.map(transform: (T) -> R): SakshiResult<R> = when (this) {
    is SakshiResult.Success -> SakshiResult.Success(transform(data))
    is SakshiResult.Failure -> this
}
