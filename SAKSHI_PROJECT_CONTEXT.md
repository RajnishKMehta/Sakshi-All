# Sākṣī — Project Context, Problem and Solution

## 1. The Problem Sākṣī Addresses

In India, people may record evidence of corruption, abuse of authority, intimidation, misconduct, or other wrongdoing. The person responsible may be a powerful individual, a government official, a police officer, or someone else who believes they can act without consequences.

In such situations, the person recording the incident may be threatened or pressured to delete the photos or videos from their phone. Physical violence is another possible risk, but the central problem Sākṣī addresses is the forced deletion of evidence.

Once the only copy of a recording is permanently deleted from the phone's gallery or file manager, the affected person may lose the evidence they need to report the incident or support a complaint. The person responsible may then believe that the evidence no longer exists.

Sākṣī is designed to address this specific problem: **preserving a separate copy of important media so that deleting the visible original does not necessarily destroy the evidence.**

## 2. How Sākṣī Solves This Problem

Sākṣī consists of four independent Android projects that work together:

- **Sākṣī Camera:** Records photos and videos. It is designed to feel like a normal camera application and can also be used independently of the rest of the ecosystem.
- **Sākṣī Vault:** A background-only application without a normal user interface. It stores and manages a separate copy of media received from compatible applications.
- **Sākṣī Portal:** Provides the user interface for browsing, viewing, exporting, and managing media stored in the Vault.
- **Sākṣī SDK:** Provides the APIs that allow the Camera, Portal, and other compatible applications to communicate with the Vault.

### Preserving recordings

When a recording is completed, the Camera can synchronize a copy to the Vault. For fragmented MP4 (fMP4) recordings, synchronization can begin and continue while recording is still in progress, rather than waiting until the entire recording finishes.

This distinction matters when a recording is interrupted by an unexpected shutdown or another failure. Fragmented MP4 is designed to make recorded segments recoverable in suitable circumstances, and incremental synchronization helps preserve data already copied to the Vault. Actual recovery depends on the recording state, successfully written fragments, and synchronization progress; it is not an unconditional guarantee.

For normal MP4 recordings, the synchronization behavior differs: the copy is requested after recording finishes. The README must accurately explain this limitation.

Once a media file has been successfully copied into the Vault, permanently deleting the original from the ordinary gallery or external storage does not, by itself, delete the Vault's separate copy. The user can access and export the preserved copy through the Portal, subject to the actual application behavior and availability.

**The central design principle is that the Vault's copy is separate from the visible original.** Sākṣī is intended to make forced deletion of the ordinary copy insufficient to erase the only available copy of the evidence.

## 3. A Separate Use Case: Keeping Private and Important Files

Sākṣī is not exclusively an evidence-recording system. It can also help people retain personal, private, or otherwise important files that they do not want to lose.

For example, a user might keep personal photos or documents in the Vault instead of leaving their only copy in the ordinary gallery. The Portal can be used to manage those files, and the user may uninstall the Portal when they no longer want its interface available on the device.

Because the Vault is a separate background application, it does not need to present a conventional app-drawer interface. This separation can make it less immediately visible to someone casually browsing installed applications.

However, **the Vault is not an invisibility or anti-detection guarantee**. Its presence may still be discoverable through system settings, installed-app lists, device-management tools, or other means. Uninstalling the Portal does not itself guarantee that its data or the Vault will remain inaccessible or undiscovered.

The user can reinstall the Portal later to access the Vault again, provided the Vault and its stored data remain available and compatible.

This use case is about retaining personal files separately from the normal gallery and reducing casual visibility. It must not be described as guaranteed concealment from parents, authorities, device owners, or anyone with sufficient access to the device.

## 4. What Sākṣī Is — and Is Not

Sākṣī is intended to provide an additional layer of media preservation, not an absolute guarantee of security.

Its design aims to:
- Preserve a separate copy of important media in the Vault.
- Reduce the risk of losing the only copy when the ordinary original is deleted.
- Support incremental synchronization for fMP4 recordings.
- Let users browse, export, and manage preserved media through the Portal.
- Support independent applications connected through the SDK.
- Provide an additional storage option for personal and important files.

Sākṣī does not guarantee protection against every threat, including device seizure, factory reset, physical destruction, privileged system access, root-level attacks, compromise of trusted applications, or loss of the Vault's data. Actual protections depend on the implementation, device state, permissions, and attack scenario.

Do not claim that the Vault is impossible to discover, that data can never be deleted, or that every recording will always be recoverable.

## 5. Guidance for README and Documentation Authors

When writing READMEs or other project documentation:

1. Explain the problem and intended solution accurately and concisely.
2. Describe features only when they are implemented in the relevant source code.
3. Distinguish between fMP4's incremental, in-progress synchronization and normal MP4's post-recording copy behavior.
4. Explain that the Vault is a separate background application, not a magical hidden or undetectable storage system.
5. Do not imply that every action available through the SDK is restricted from a compromised or authorized client unless the implementation actually enforces such restrictions.
6. Do not introduce alarming security caveats unrelated to the feature being documented. Security limitations should be accurate, proportionate, and relevant to the section.
7. Do not promise absolute evidence preservation, guaranteed secrecy, or immunity from device-level attacks.
8. Keep repository READMEs concise. Detailed explanations belong in the central documentation website.

The purpose of Sākṣī should remain clear throughout all documentation:

**Help people retain important media and evidence by keeping a separate copy beyond the ordinary gallery or file manager, while accurately communicating the system's capabilities and limitations.**
