import re

file_path = "04_portal/app/src/debug/kotlin/rajnishkmehta/sakshi/portal/debug/DebugLogsActivity.kt"

with open(file_path, "r") as f:
    content = f.read()

# Remove Unused imports related to 'icons' which were causing build failure
content = content.replace("import androidx.compose.material.icons.Icons\n", "")
content = content.replace("import androidx.compose.material.icons.automirrored.filled.ArrowBack\n", "")
content = content.replace("import androidx.compose.material.icons.filled.Share\n", "")
content = content.replace("import androidx.compose.material.icons.filled.Delete\n", "")

with open(file_path, "w") as f:
    f.write(content)

print("Unused imports fixed.")
