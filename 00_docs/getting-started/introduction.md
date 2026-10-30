# Introduction to Sākṣī

Sākṣī (meaning "Witness" in Sanskrit) is an open-source Android ecosystem designed to help you securely retain personal, private, or important media.

## The Problem

When capturing evidence of wrongdoing, individuals are often pressured or forced to delete recordings from their phones. In a typical scenario, if you are forced to delete a photo or video from your phone's main gallery or file manager, the only copy of that evidence is destroyed.

## The Solution

Sākṣī addresses this risk by ensuring that a separate, independent copy of your media is safely stored away from your phone's default gallery.

By utilizing a dedicated, background-only storage application, Sākṣī allows you to capture media that is automatically and locally backed up on your device. After this independent copy is securely stored, deleting the visible original from your phone's standard gallery does not delete the copy preserved by Sākṣī. This provides a crucial additional layer of media preservation.

## The Ecosystem

To provide this functionality securely and modularly, Sākṣī is built as an ecosystem consisting of four independent but tightly integrated components:

* **Sākṣī Vault**: The secure storage engine. It is a background-only application without a user interface, dedicated to receiving, protecting, and independently storing media locally on your device.
* **Sākṣī Camera**: The capture application. It allows you to take photos and record videos, safely transferring them to the Vault. Fragmented MP4 (fMP4) videos synchronize incrementally while recording, providing crash safety, while standard MP4 videos are copied entirely to the Vault after recording finishes.
* **Sākṣī Portal**: The user interface gateway. It provides a secure way to browse, view, and manage the media safely stored in your Vault.
* **Sākṣī SDK**: The developer library. A headless integration tool that enables other Android applications to communicate securely with the Vault.
