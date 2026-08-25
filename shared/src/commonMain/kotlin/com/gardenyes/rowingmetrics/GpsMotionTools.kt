package com.gardenyes.rowingmetrics

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Great-circle distance between two WGS84 points (haversine).
 * @return segment length in **kilometers**.
 */
fun calcDistanceKm(prevLat: Double, prevLon: Double, curLat: Double, curLon: Double): Double {
    val earthRadiusM = 6_371_000.0
    val lat1 = Math.toRadians(prevLat)
    val lat2 = Math.toRadians(curLat)
    val dLat = Math.toRadians(curLat - prevLat)
    val dLon = Math.toRadians(curLon - prevLon)
    val a =
        sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusM * c / 1000.0
}
