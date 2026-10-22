import re

file_path = "02_vault/app/src/main/kotlin/rajnishkmehta/sakshi/vault/sync/SyncScheduler.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace('state = state,', 'state = state,\n                    deletionScheduledAt = existing?.deletionScheduledAt,')

with open(file_path, "w") as f:
    f.write(content)
