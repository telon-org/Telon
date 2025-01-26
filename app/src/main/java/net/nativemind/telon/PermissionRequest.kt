package net.nativemind.telon

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionRequest {
    companion object {
        // TODO: Make app accessible while permission is not granted
        fun request(context: Context, activity: Activity, permissions: Array<String>): Int {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Return PERMISSION_GRANTED if accept else return PERMISSION_DENIED
                ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    1
                )
                return PackageManager.PERMISSION_GRANTED
            }
            return PackageManager.PERMISSION_DENIED
        }
    }
}