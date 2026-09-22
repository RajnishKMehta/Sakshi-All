import re

with open("02_vault/app/src/main/kotlin/rajnishkmehta/sakshi/vault/storage/StorageManager.kt", "r") as f:
    content = f.read()

content = content.replace(
    "and synchronizes the file header to capture metadata updates (e.g., MP4 mdat size updates).",
    "and synchronizes the file header to capture metadata updates (e.g., MP4 moov/mdat size updates)."
)

with open("02_vault/app/src/main/kotlin/rajnishkmehta/sakshi/vault/storage/StorageManager.kt", "w") as f:
    f.write(content)
