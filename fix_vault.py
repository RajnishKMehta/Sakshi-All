import os

files = [
    "02_vault/app/src/main/kotlin/rajnishkmehta/sakshi/vault/sync/CopyEngine.kt",
    "02_vault/app/src/main/kotlin/rajnishkmehta/sakshi/vault/sync/SyncScheduler.kt"
]

for file in files:
    with open(file, "r") as f:
        content = f.read()

    content = content.replace('state = "INITIALIZING",', 'state = "INITIALIZING",\n                    deletionScheduledAt = null,')
    content = content.replace('state = state,', 'state = state,\n                    deletionScheduledAt = null,')
    # Actually wait, there is no need, MediaRecord allows named arguments or maybe not?
    # Let's check how it's instantiated
    pass
