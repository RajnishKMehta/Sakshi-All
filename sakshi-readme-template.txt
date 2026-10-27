SAKSHI README TEMPLATE — STRUCTURE AND RULES
================================================

PURPOSE
-------
Use this template to create or update a repository's README.md.
Inspect the actual source code, project configuration, existing documentation,
and repository links before writing. Preserve accurate existing information,
but reorganize and rewrite the README to follow this template.

Do not invent features, version numbers, badges, links, test results, support
channels, security guarantees, or release status. Keep the README concise.
This template defines a common structure; not every section belongs in every
repository.

SECTION STATUS
--------------
[REQUIRED]    Include in every relevant README.
[OPTIONAL]    Include only if useful and supported by the repository.
[CONDITIONAL] Include only when the stated condition applies.
[OMIT]        Do not include when it is irrelevant or unsupported.

1. PROJECT HEADER [REQUIRED]
----------------------------
Include:
- Project name as the main heading.
- One short tagline or description explaining its purpose.
- A concise paragraph describing what it does.

Logo: [CONDITIONAL]
- Include only if an actual logo asset exists and is intended for the README.
- No logo exists currently; do not invent one or add a placeholder.

2. BADGES [OPTIONAL]
-------------------
Include only useful, verified badges, such as:
- License
- Latest release/version
- Build or CI status
- Android compatibility, if represented accurately

Rules:
- Verify each badge's target URL and current status.
- Do not add decorative, broken, guessed, or misleading badges.
- Omit this section if no meaningful verified badges are available.

3. ABOUT [REQUIRED]
-------------------
Briefly explain:
- What this project is.
- What problem it solves.
- Who may benefit from it.
- Its role in the Sākṣī ecosystem, where relevant.

Keep this concise. Do not put the full project story here; detailed explanations
belong in the separate documentation website.

4. FEATURES [REQUIRED]
----------------------
List the main implemented features in short bullets.

Rules:
- Confirm features from the source code and configuration.
- Do not describe planned, incomplete, or hypothetical features as available.
- Keep the list focused on the most important capabilities.

5. ARCHITECTURE / HOW IT WORKS [CONDITIONAL]
--------------------------------------------
Include when understanding components, data flow, or integration is important.

Explain only the parts relevant to this repository:
- Ecosystem overview: briefly explain how SDK, Camera, Vault, and Portal relate.
- SDK: explain how applications communicate with the Vault through the SDK.
- Vault: explain its background-only storage and media-management role.
- Camera: explain recording and the supported synchronization flow.
- Portal: explain how users browse and manage media provided by the Vault.

Keep diagrams optional. Add one only if it makes the architecture significantly
easier to understand and can be kept accurate.

6. REQUIREMENTS / COMPATIBILITY [CONDITIONAL]
---------------------------------------------
Include when users or developers need to know prerequisites.

Examples:
- Minimum supported Android version.
- Required runtime, SDK, or build environment.
- Important compatibility limitations.

Use verified values from the repository. Do not list routine dependencies unless
they matter to setup or compatibility.

7. INSTALLATION / USAGE [CONDITIONAL]
-------------------------------------
Include the instructions relevant to the project.

For installable Android applications:
- Link to a verified release/download page if one exists.
- Give minimal installation or first-use instructions where useful.
- Do not invent download URLs or imply that a public release exists.

For the SDK:
- Include a short dependency installation snippet.
- Use the latest version verified from the repository's version configuration
  or a verified published release.
- Use the correct repository/package coordinates and setup instructions.
- Do not guess the latest version.
- Keep the example minimal; detailed integration belongs in the documentation.

For an ecosystem/overview repository:
- Explain the repository's purpose and link to the individual project
  repositories using verified URLs.
- Do not describe a submodule workflow unless submodules actually exist.

8. DOCUMENTATION [OPTIONAL]
---------------------------
Link to the relevant detailed documentation, if it exists.

Rules:
- Use verified URLs or valid relative paths to existing files.
- The central documentation repository's README is the GitBook homepage:
  introduce Sākṣī, briefly explain the problem and the four components, and
  provide clear navigation to available documentation.
- Do not add links to documentation pages that do not exist yet.
- Do not invent a published GitBook URL. Add it only after it is verified.
- Individual application repositories may link to their own docs and the
  central documentation website when those links are available.
- Do not duplicate long guides in the README.

9. BUILD FROM SOURCE [CONDITIONAL]
----------------------------------
Include only when building from source is a meaningful user/developer task.

If included, provide:
- Verified prerequisites.
- The minimum commands needed to build.
- Relevant output/artifact information, if known.

Do not include lengthy build troubleshooting or unverified commands. If the
repository is primarily a published SDK/application for ordinary users, this
section may be omitted or linked to detailed developer documentation.

10. CONTRIBUTING [OPTIONAL]
---------------------------
Include if the repository accepts outside contributions or has contribution
guidelines.

Link to existing contribution instructions, issue tracker, or pull-request
guidance. Do not invent a process or link.

11. SECURITY AND PRIVACY [CONDITIONAL]
--------------------------------------
Include when the project handles sensitive data, media, permissions, or security
claims, or when a verified vulnerability-reporting process exists.

Describe actual behavior and limitations accurately. Include a security
reporting link only if one is configured or verified. Never promise absolute
protection, guaranteed evidence preservation, or immunity from device failure,
uninstall, privileged access, or other attacks.

12. LICENSE AND ACKNOWLEDGEMENTS [CONDITIONAL]
----------------------------------------------
Include:
- License: only when a valid license file or verified license declaration exists.
- Acknowledgements/third-party credits: only when relevant and verified.

Do not guess the license or add generic credits without evidence.

13. LINKS AND SUPPORT [OPTIONAL]
-------------------------------
Include useful, verified links such as:
- Main project or related repositories.
- Releases.
- Issue tracker.
- Documentation website.
- Relevant upstream projects.

Avoid repeating links already clearly presented elsewhere. Do not invent contact
addresses, support channels, or repository URLs.

REPOSITORY-SPECIFIC GUIDANCE
============================

A. CENTRAL DOCUMENTATION REPOSITORY — Sakshi-All/00_docs/README.md
------------------------------------------------------------------
This README is the GitBook homepage, so prioritize the reader's first
impression and navigation.

[REQUIRED]
- Clear Sākṣī heading and concise tagline.
- Short introduction: Sākṣī helps preserve important media and may help retain
  evidence when someone fears a recording could be deleted or destroyed.
- Explain the intended problem without making absolute security guarantees.
- Briefly introduce the four components: SDK, Vault, Camera, Portal.
- Provide navigation to documentation pages that actually exist.

[OPTIONAL]
- A short "How the components work together" section.
- Links to verified project repositories and issue trackers.
- A small "Getting started" section if the relevant guide already exists.

[DO NOT]
- Turn the homepage into the complete project history or a long essay.
- Add Git submodules or claim that the repositories are submodules.
- Invent future documentation pages or an unpublished GitBook URL.
- Add a fake logo or decorative badges without verified targets.

B. SDK REPOSITORY
-----------------
[REQUIRED]
- Explain that the SDK connects client applications to the Vault.
- List verified public APIs and key capabilities at a high level.
- Include a short installation/dependency snippet with the latest verified
  version and correct coordinates.
- Link to the SDK's actual API/integration documentation.

[CONDITIONAL]
- Include a minimal integration example if the public API makes it useful.
- Include build-from-source instructions if developers need them.

C. VAULT REPOSITORY
-------------------
[REQUIRED]
- Explain that Vault is a background-only app without a normal user interface.
- Describe its actual media storage, synchronization, and lifecycle features.
- Explain its role in the ecosystem without promising perfect protection.

[CONDITIONAL]
- Document permissions, retention/deletion behavior, recovery, or storage
  limitations if implemented and important for users or integrators.

D. CAMERA REPOSITORY
--------------------
[REQUIRED]
- Explain that the camera can be used independently and can integrate with
  the Sākṣī ecosystem.
- Describe only verified recording and Vault synchronization features.
- Explain fMP4 and normal MP4 behavior and limitations accurately.

[CONDITIONAL]
- Include installation/download instructions if a verified distribution exists.

E. PORTAL REPOSITORY
--------------------
[REQUIRED]
- Explain that Portal is the user-facing app for browsing and managing media
  stored in the Vault.
- List verified features such as viewing, exporting, recovery, or app lock only
  if they are actually implemented.

[CONDITIONAL]
- Include concise first-use instructions if they help users get started.

FINAL QUALITY CHECK
===================
Before finishing:
- Update only README.md files explicitly in scope.
- Respect AGENTS.md instructions.
- Keep headings and overall presentation consistent across repositories, while
  adapting section content to each project's purpose.
- Remove empty headings and irrelevant sections.
- Prefer concise explanations and bullets over long paragraphs.
- Verify every version, feature, command, badge, and link against the repository.
- Do not run builds, tests, or lint for a README-only change unless explicitly
  requested.
- Submit the README changes directly without asking for approval.
