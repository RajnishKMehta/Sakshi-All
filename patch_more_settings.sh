#!/bin/bash
sed -i '/override fun onCreate(savedInstanceState: Bundle?)/a \
        rajnishkmehta.sakshi.camera.logging.Logger.i("MoreSettings", "onCreate called")' 03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/ui/activities/MoreSettings.kt
