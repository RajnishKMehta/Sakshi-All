#!/bin/bash
sed -i '/if (!showStorageSettings) {/i \
        rajnishkmehta.sakshi.camera.logging.DebugHelper.addLogsOption(this, binding.rootView as android.widget.LinearLayout)\n' 03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/ui/activities/MoreSettings.kt
