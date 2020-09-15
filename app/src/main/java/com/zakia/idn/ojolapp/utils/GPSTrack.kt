package com.zakia.idn.ojolapp.utils

import android.Manifest
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat

class GPSTrack (c: Context) : Service(), LocationListener {

    private var context: Context? = null
    internal var isGPSEnabled = false
    internal var isNetworkEnable = false
    internal var canGetLocation = false
    internal var location: Location? = null
    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()
    protected var locationManager: LocationManager? = null

    val locations: Location?
        get() = if (location != null) {
            location
        } else null


    init {
        this.context = c
        getLocation()
    }

    private fun getLocation(): Location? {
        try {
            locationManager = context!!
                .getSystemService(Context.LOCATION_SERVICE)
                    as LocationManager

            //getting gps status
            isGPSEnabled = locationManager!!
                .isProviderEnabled(LocationManager.GPS_PROVIDER)

            //getting network status
            isNetworkEnable = locationManager!!
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGPSEnabled && !isNetworkEnable) {
                showSettingGPS()

            } else {
                canGetLocation = true

                //get lat/lng by network
                if (isNetworkEnable) {

                    if (checkPermission(context)) {
                        locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, MIN_TIME,
                            MIN_DISTANCE.toFloat(), this
                        )

                        Log.d("network", "network enabled")

                        if (locationManager != null) {
                            location = locationManager!!
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                            if (location != null) {
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    } else {

                    }
                }

                //get lat/lng by gps
                if (isGPSEnabled) {
                    if (location == null) {
                        if (checkPermission(context)) {
                            locationManager!!.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, MIN_TIME,
                                MIN_DISTANCE.toFloat(), this
                            )

                            Log.d("GPS", "GPS enabled")

                            if (locationManager != null) {
                                location = locationManager!!
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                if (location != null) {
                                    latitude = location!!.latitude
                                    longitude = location!!.longitude
                                }
                            }
                        } else {

                        }
                    }
                }


            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return location
    }

    fun getLatitude(): Double {
        if (location != null) {
            latitude = location!!.latitude
        }
        return latitude
    }

    fun getlongitude(): Double {
        if (location != null) {
            longitude = location!!.longitude
        }
        return longitude
    }

    //function to check GPS/Wifi enabled
    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    companion object {
        private val MIN_DISTANCE = 1.toLong() //10 meter
        private val MIN_TIME = (1000 * 1 * 1).toLong() //1 minute
        val NEW_POSITION = "newPosition"

        fun checkPermission(context: Context?): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                ActivityCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
    }

    fun showSettingGPS() {
        val alertBuilder = AlertDialog.Builder(context)

        alertBuilder.setTitle("GPS Setting")
        alertBuilder
            .setMessage("GPS is not enabled. do you want to go to settings menu?")

        alertBuilder.setPositiveButton("Setting") { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        alertBuilder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        alertBuilder.show()
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
        return null
    }

    override fun onLocationChanged(p0: Location) {
        if (location != null) {
            if (this.location !== location) {
                this.location = location
            }
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onProviderEnabled(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onProviderDisabled(p0: String?) {
        TODO("Not yet implemented")
    }


}