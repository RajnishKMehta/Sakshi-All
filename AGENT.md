# Sakshi Monorepo - Agent Instructions & Project Rules

This document summarizes the core guidelines from the individual projects (SDK, Vault, Camera) in this monorepo.

1. **English Only**: The entire project must use English only for all source code, comments, commit messages, documentation, and pull requests.
2. **Clean & Structured Code**: Keep the project clean, structured, modern, and production-ready.
3. **Latest Stable APIs**: Always prefer the latest stable Android APIs or Release Candidates if a stable version is unavailable.
4. **Modern Architecture**: Follow the official Android architecture guidelines. Never place unrelated logic into a single file. Keep the project modular.
5. **Documentation**: Every public API and important class should be documented with clear KDoc.
6. **No UI in SDK**: The SDK must not contain any UI elements (Activities, Fragments, Compose, XML). It is strictly for headless IPC.

**STRICT RULE:**
Do not downgrade any dependency, plugin, library, SDK, Gradle version, Kotlin version, Android version, GitHub Action version, or other project/tooling version without explicit permission from the user. Never downgrade a version merely to solve a compatibility issue, build error, warning, or test failure. Ask the user first if a downgrade is genuinely required.
