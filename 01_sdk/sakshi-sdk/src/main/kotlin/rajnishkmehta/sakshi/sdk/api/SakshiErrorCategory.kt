/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.api

/**
 * High-level categorization of Sākṣī errors to simplify error handling logic.
 */
public enum class SakshiErrorCategory {
    /** Failures related to binding, finding, or communicating with the Vault service. */
    CONNECTION,

    /** Failures related to invalid arguments, missing files, or bad state in the request. */
    VALIDATION,

    /** Failures caused by system limitations like permissions or I/O access. */
    ENVIRONMENT,

    /** Unexpected system faults or remote exceptions. */
    SYSTEM_FAULT
}
