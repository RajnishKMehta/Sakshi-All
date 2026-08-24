# Sakshi SDK - Agent Instructions & Project Rules

This document serves as the single source of truth for guidelines, architecture, standards, and conventions for AI agents and human contributors working on the **Sakshi SDK** repository.

---

## 1. Project Overview & Scope

**Sakshi SDK** (`rajnishkmehta.sakshi.sdk`) is a lightweight, headless Android client library designed strictly for inter-process communication (IPC) between client applications (e.g., Camera applications) and a Vault application.

### Strict Scope Constraints
* **SDK Only**: This repository contains ONLY the Sakshi SDK library.
* **No UI / Views**: Absolutely NO Activities, Fragments, Jetpack Compose, or XML layouts.
* **No Demo / Sample Apps**: Do NOT create demo applications, test apps, or sample modules inside this repository.
* **No Unrelated Modules**: Do NOT create Camera, Vault, Viewer, Audio Recorder, or any external app modules in this repository.

---

## 2. Core Responsibilities & Boundaries

### 2.1 SDK Responsibilities
* Act as the lightweight communication bridge between client Android applications and the Vault application.
* Expose clean, idiomatic, asynchronous Kotlin public APIs.
* Encapsulate and hide all underlying Android Binder / AIDL / IPC complexity.
* Deliver thread-safe, non-blocking execution using Kotlin Coroutines and Structured Concurrency.
* Report status, acknowledgements, and typed errors back to the caller.

### 2.2 Explicit SDK Non-Responsibilities (SDK DOES NOT...)
* **Does NOT store files** or manage local persistence.
* **Does NOT copy files** or perform byte streaming / incremental reads.
* **Does NOT maintain timers**, schedulers, or background polling logic.
* **Does NOT implement synchronization or retry logic**.
* **Does NOT own or maintain a database**.

### 2.3 Vault Responsibilities (Informational Only - External Application)
The Vault application is an external application that handles:
* Receiving photos and video sync requests.
* Incremental file copying (reading new bytes from source, maintaining offsets, preventing overlapping copies).
* Scheduling (timers, immediate triggers, queueing).
* Retries, file persistence, and media database management (e.g., tracking File ID, Original URI, Vault URI, Status, Last Offset, Completed).

---

## 3. General Development Standards

1. **Language**: Kotlin ONLY. English documentation, comments, variable names, and commit messages only.
2. **Min Android Version**: Android 10 (API Level 29).
3. **Target & Compile Versions**: Latest stable Android SDK versions.
4. **Dependencies**:
   * Use strictly necessary dependencies (e.g., Kotlin Standard Library, Kotlinx Coroutines Core, AndroidX Core / Annotations).
   * Verify the latest stable versions from official Google/Android documentation before adding any dependency or selecting an Android API.
   * Never use deprecated Android or Kotlin APIs.
5. **Asynchrony**:
   * Use Kotlin Coroutines (`suspend` functions, `Flow`).
   * Adhere strictly to **Structured Concurrency** (`CoroutineScope`, proper cancellation propagation, `withContext`, SupervisorJob where necessary).

---

## 4. Repository & Package Structure

### 4.1 Package Name
Root package: `rajnishkmehta.sakshi.sdk`

### 4.2 Recommended Package Hierarchy
```
rajnishkmehta.sakshi.sdk/
├── api/                   # Public API interfaces, models, and entry points
│   ├── SakshiClient.kt    # Main client entry point interface/class
│   ├── models/            # Public domain data classes and enums
│   └── result/            # Result wrappers, exceptions, sealed error classes
├── internal/              # Internal implementation hidden from consumers
│   ├── ipc/               # IPC connection management, AIDL/Binder wrappers, Service Connection
│   ├── mapper/            # Data mappers between internal IPC data & public models
│   └── util/              # Internal utilities and helpers
```

---

## 5. Public API Design Principles

* **Clean & Modern**: Expose intuitive Kotlin APIs (`suspend` functions and `Flow` for streams/events).
* **Future-Proof & Extensible**: Use sealed interfaces/classes for results and options to allow backward-compatible expansion.
* **Information Hiding**: Mark internal classes and implementation details with the `internal` visibility modifier.
* **Comprehensive Documentation**: Every public class, interface, method, parameter, and return value MUST be documented with **KDoc** (`/** ... */`).

---

## 6. Supported Operations (SDK Capabilities)

### 6.1 Photo Operations
* **Send Photo**: Submit a photo payload/URI reference to the Vault for ingestion.

### 6.2 Video Operations
* **Start Video Synchronization**: Notify Vault of a new recording with a unique File ID and source URI/fd to begin incremental background sync.
* **Stop Video Synchronization**: Signal Vault to stop syncing a specific recording session.
* **Ping Vault**: Health-check / availability check to verify Vault service connection status.
* **Recording Query**: Ask Vault whether a specific recording / File ID exists or is actively syncing.
* **Acknowledgements & Errors**: Asynchronous feedback indicating success, progress events, or typed error codes.

---

## 7. Video Synchronization Protocol Overview

1. **Client (e.g., Camera App)**:
   - Creates a recording.
   - Generates a unique File ID.
   - Invokes `SakshiClient.startVideoSync(recordingInfo)`.
2. **Sakshi SDK**:
   - Validates input.
   - Marshals parameters across IPC bridge to the Vault Service.
3. **Vault (External Service)**:
   - Takes ownership of synchronization.
   - Periodically reads newly written bytes using its own scheduler.
   - Guarantees non-overlapping copy operations.

---

## 8. Coding Conventions & Quality Guidelines

* Follow the official Kotlin Coding Conventions.
* Immutability first: use `val` instead of `var` wherever possible, immutable data classes, and read-only collections.
* Handle service disconnections gracefully: support auto-rebind, clean resource release, and explicit exception signaling when IPC fails.
* Zero warning policy: fix all compiler warnings, lint issues, and KDoc formatting errors.

---

## 9. Instructions for AI Agents

When implementing or modifying code in this repository:
1. Always consult this `AGENT.md` file first.
2. Maintain strict separation of public API (`api/`) and internal details (`internal/`).
3. Ensure no UI code, activities, fragments, or demo apps are introduced into the project.
4. Verify Kotlin syntax, KDoc comments, and modern Android standards before finishing tasks.

**STRICT RULE:**
Do not downgrade any dependency, plugin, library, SDK, Gradle version, Kotlin version, Android version, GitHub Action version, or other project/tooling version without explicit permission from the user. Never downgrade a version merely to solve a compatibility issue, build error, warning, or test failure. Ask the user first if a downgrade is genuinely required.
