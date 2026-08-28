package com.example.voltguard

import android.content.Context
import android.os.BatteryManager

object BatteryCapacity {

    const val DEFAULT_DESIGN_CAPACITY = 4000
    private const val PROPERTY_DESIGN_CAPACITY = 5

    @Volatile
    private var cachedDesignCapacity: Int = -1

    fun getDesignCapacity(context: Context): Int {
        if (cachedDesignCapacity > 0) return cachedDesignCapacity

        val batteryManager =
            context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val reported = batteryManager?.getIntProperty(PROPERTY_DESIGN_CAPACITY) ?: 0

        cachedDesignCapacity = if (reported > 0) reported else DEFAULT_DESIGN_CAPACITY
        return cachedDesignCapacity
    }
}
