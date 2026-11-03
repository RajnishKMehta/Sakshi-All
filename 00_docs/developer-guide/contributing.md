---
description: "Thank you for your interest in contributing to the Sākṣī ecosystem! We welcome and appreciate contributions of all forms, including bug reports,..."
icon: terminal
---
# Contributing to Sākṣī

Thank you for your interest in contributing to the Sākṣī ecosystem! We welcome and appreciate contributions of all forms, including bug reports, feature requests, documentation improvements, and code changes.

## Getting Started

Because Sākṣī is made up of multiple independent components (Vault, Camera, Portal, SDK), each repository has its own specific setup and development requirements.

Before proposing or making any code modifications, please ensure you understand the ecosystem's architecture. A good starting point is reading the [Developer Reference](../developer-reference/README.md) to understand data models and constraints, and exploring the detailed [API Reference](../developer-reference/api/README.md) to see how client applications communicate with the Vault.

## Repository-Specific Instructions

**Do not start coding immediately.** Each project has a dedicated `CONTRIBUTING.md` and `CODE_OF_CONDUCT.md` file located at the root of its respective repository.

You must read the specific guidelines for the component you are contributing to. These repository-level files contain vital information regarding environment requirements (such as JDK and Android SDK versions), architectural rules (like the SDK being strictly headless), and required tools (such as using Spotless for formatting).

Please locate and review the `CONTRIBUTING.md` file within the repository you wish to modify:
- [Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault)
- [Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera)
- [Sākṣī Portal](https://github.com/RajnishKMehta/Sakshi-Portal)
- [Sākṣī SDK](https://github.com/RajnishKMehta/Sakshi-SDK)

## Basic Contribution Workflow

1. **Find an Issue**: Look for open issues in the relevant repository or open a new one to discuss your proposed changes before starting work.
2. **Fork and Branch**: Fork the target repository and create a new branch from `main` for your work.
3. **Develop**: Write your code, ensuring you follow the repository-specific guidelines and architecture boundaries.
4. **Test**: Write tests if applicable and run the existing test suite (e.g., executing `./gradlew test` and verifying builds).
5. **Submit a Pull Request**: Push your branch to your fork and open a Pull Request against the original `main` branch. Ensure your PR description is clear and references any related issues.

## Basic Expectations

- **Language Options**: All source code, code comments, and commit messages must strictly be in English. However, for issues, discussions, and pull requests, you may communicate in English or Hindi.
- **Documentation**: Update or add KDoc for any new public APIs or important classes. If your code changes require corresponding updates to the broader documentation, you must update the documentation files and submit a Pull Request to the central [Sakshi-Docs](https://github.com/RajnishKMehta/Sakshi-Docs) repository.
- **Testing**: Maintain high test coverage. Ensure all unit and integration tests pass before requesting a review.
- **Clean Code**: Follow modern Kotlin style guides and ensure your code compiles cleanly without warnings.
