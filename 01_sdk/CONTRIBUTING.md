# Contributing to Sakshi SDK

Thank you for your interest in contributing to **Sakshi SDK**!

## Code of Conduct

All contributors are expected to adhere to our [Code of Conduct](CODE_OF_CONDUCT.md).

## Development Setup & Guidelines

1. **Environment Requirements**:
   - JDK 21
   - Android SDK 37 (API level 37)
   - Gradle 9.6.1
2. **Architecture Rules**:
   - Please read [AGENT.md](AGENT.md) and [context.txt](docs/context.txt) before making any code modifications.
   - **Sakshi SDK is headless**: No UI, Activities, Fragments, Compose, or XML layouts.
   - **Responsibility Boundaries**: Sakshi SDK performs IPC communication only. It does not perform file copying, timer management, or database storage.
3. **Coding Standards**:
   - Kotlin language only.
   - Public APIs must be fully documented with KDoc.
   - Maintain strict explicit API mode (`-Xexplicit-api=strict`).

## Pull Request Process

1. Fork the repository and create your branch from `main`.
2. Ensure your code passes all lint checks and compiles cleanly without warnings.
3. Submit your Pull Request using our [PR Template](.github/PULL_REQUEST_TEMPLATE.md).
