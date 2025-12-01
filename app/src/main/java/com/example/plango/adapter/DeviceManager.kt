package com.example.plango

import android.content.Context
import java.util.UUID

object DeviceIdManager {

    private const val PREF_NAME = "plango_prefs"
    private const val KEY_DEVICE_ID = "device_id"

    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var id = prefs.getString(KEY_DEVICE_ID, null)

        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, id).apply()
        }

        return id
    }
}
