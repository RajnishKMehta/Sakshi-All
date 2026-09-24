
## Media Access API

A new API `getMedia(mediaType, fileId)` is available in the Sākṣī SDK to retrieve a `String` containing the ContentProvider URI template for media access.
The returned template retains `{mediaType}` and `{fileId}` placeholders as requested. Vault serves the media files using the `MediaProvider` resolving MIME types dynamically.
