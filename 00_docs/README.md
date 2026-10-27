# Sākṣī

**Preserve what matters.**

Sākṣī is an open-source Android ecosystem designed to help people keep important photos, videos, and other files safe from accidental loss or deletion. It is especially relevant when someone needs to preserve a recording of possible wrongdoing, intimidation, corruption, or abuse of power and fears that the evidence may be deleted.

Sākṣī keeps media storage separate from the app that captures or views it. This can help retain a separate copy, but it cannot guarantee protection against every attack, device failure, or compromise.

## The Sākṣī Components

### [Sākṣī SDK](https://github.com/RajnishKMehta/Sakshi-SDK)
The Kotlin API layer that lets compatible apps communicate with the Vault through Android IPC.

### [Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault)
A background-only app that stores and manages media independently of the capture and viewing apps.

### [Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera)
A camera app that can be used on its own or with the ecosystem. It supports fragmented MP4 (fMP4) recording for improved resilience to interrupted recordings and can synchronize supported media with the Vault.

### [Sākṣī Portal](https://github.com/RajnishKMehta/Sakshi-Portal)
The user-facing app for browsing, viewing, exporting, and managing media stored in the Vault.

## How They Work Together

Camera captures media, the SDK provides the communication interface, Vault stores the separate copy, and Portal lets the user access and manage stored media. Each component has a distinct responsibility.

## Documentation

Detailed guides will be published here as they become available. For now, visit the individual project repositories linked above for their current README and project information.
