import re

file_path = "02_vault/app/src/main/kotlin/rajnishkmehta/sakshi/vault/db/MediaRecord.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    'val state: String, // "INITIALIZING", "SYNCING", "COMPLETED", "STOPPED", "FAILED"',
    'val state: String, // "INITIALIZING", "SYNCING", "COMPLETED", "STOPPED", "FAILED", "DELETE"\n    val deletionScheduledAt: Long?'
)

with open(file_path, "w") as f:
    f.write(content)
