package com.zakia.idn.ojolapp.utils

import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

object DirectionMapsV2 {

    //menggabungkan titik koordinat jadi sebuah garis
    internal fun gambarRoute(map: GoogleMap, dataPoly: String) {
        var polyz = decodePoly(dataPoly)
        for (i in 0 until polyz?.size!! - 1) {
            val src = polyz!![i]
            val dest = polyz!![i + 1]
            val line = map.addPolyline(
                PolylineOptions().add(
                    LatLng(src.latitude, src.longitude),
                    LatLng(dest.latitude, dest.longitude)
                ).width(5f)
                    .color(Color.BLUE).geodesic(true)
            )
        }

    }

    internal fun decodePoly(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        var len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0

            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0)
                (result shr 1).inv() else
                result shr 1

            lat += dlat
            shift = 0
            result = 0

            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0)
                (result shr 1).inv() else
                result shr 1

            lng += dlng

            val p = LatLng(
                lat.toDouble(),
                lng.toDouble()
            )
            poly.add(p)
        }

        return poly
    }
}