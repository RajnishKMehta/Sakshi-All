# Validation Constraints

The SDK applies strict validation constraints to prevent path traversal and ensure secure file system operations.

## Media Type
Must be one of the strictly allowed values:
*   `PHOTO`
*   `VIDEO`
*   `AUDIO`
*   `OTHER`

## File ID
Must be a safe filename component.
*   Allowed characters: lowercase `a-z`, digits `0-9`, underscore `_`, hyphen `-`
*   Length: Between `8` and `128` characters
*   Must not contain path separators or spaces.

## File Extension
Must be a valid extension format.
*   Length: Between `1` and `12` characters
*   Must not start or end with a dot (`.`)
*   Must not contain path separators (`/`, `\`) or traversal sequences (`..`)
*   Must not contain spaces.
