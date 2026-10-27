# Sākṣī Ecosystem

Sākṣī ("Witness" in Sanskrit) is an open-source Android ecosystem designed to protect important media—photos, videos, and other files—from accidental deletion, deliberate destruction, or unauthorized tampering.

When recording evidence of wrongdoing, intimidation, corruption, or abuse of power, individuals often face pressure to delete their recordings. Sākṣī addresses this by transparently copying captured media into an independent, secure storage layer over Android IPC. Even if someone forces the user to delete the recording from the camera app, a preserved copy may remain safely hidden in the background.

## About

The Sākṣī ecosystem provides a modular, local-first solution for media preservation. It operates entirely on-device, requiring no internet connection, which prioritizes user privacy and security. Sākṣī consists of four independent projects that work together seamlessly over local inter-process communication (IPC) without sharing databases or storage directories.

## Architecture / How It Works

Sākṣī is built upon a dual-side architecture connecting capture and storage:

- **Sākṣī SDK**: The core communication bridge. This headless Android client provides the Kotlin/coroutines APIs needed for client apps and the Vault to talk to each other securely over AIDL.
- **Sākṣī Vault**: The independent, background-only secure storage application. It receives media incrementally, manages persistence, and schedules delayed deletions. Because it has no user interface, its surface area for compromise is minimal.
- **Sākṣī Camera**: The capture application. It records photos and videos (including crash-safe fragmented MP4 formats) and uses the SDK to instantly mirror that media to the Vault while recording is ongoing.
- **Sākṣī Portal**: The user-facing management application. Built entirely with Jetpack Compose, it securely connects to the Vault to allow users to browse, view, and safely export their preserved media when the threat has passed.

## Documentation

Technical documentation and integration guides are available in the respective repositories:

- [Sākṣī SDK Repository](https://github.com/RajnishKMehta/Sakshi-SDK)
- [Sākṣī Vault Repository](https://github.com/RajnishKMehta/Sakshi-Vault)
- [Sākṣī Camera Repository](https://github.com/RajnishKMehta/Sakshi-Camera)
- [Sākṣī Portal Repository](https://github.com/RajnishKMehta/Sakshi-Portal)

*Note: A centralized GitBook documentation website is currently in development and will be linked here once finalized.*

## Security and Privacy

Sākṣī operates completely offline to maximize privacy. While it is designed to mitigate forced deletion scenarios by separating capture from storage, Sākṣī does not guarantee perfect protection against all possible attacks. It cannot guarantee evidence preservation if a device is completely destroyed, if an attacker has highly privileged (root) access, or if the user's primary device security (like their lock screen pin) is compromised.
