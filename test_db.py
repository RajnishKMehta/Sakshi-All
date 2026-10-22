import sqlite3
import os

print(os.path.exists("02_vault/app/src/main/kotlin/rajnishkmehta/sakshi/vault/db/MediaRecord.kt"))

with open("02_vault/app/src/main/kotlin/rajnishkmehta/sakshi/vault/db/MediaRecord.kt", "r") as f:
    print(f.read())
