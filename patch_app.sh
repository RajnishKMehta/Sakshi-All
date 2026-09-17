#!/bin/bash
sed -i '/super.onCreate()/a \
        rajnishkmehta.sakshi.camera.logging.Logger.init(this)' 03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/App.kt
