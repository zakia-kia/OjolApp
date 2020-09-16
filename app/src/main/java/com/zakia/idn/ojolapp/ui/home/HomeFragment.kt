package com.zakia.idn.ojolapp.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zakia.idn.ojolapp.R
import com.zakia.idn.ojolapp.activity.WaitingDriverActivity
import com.zakia.idn.ojolapp.data.ResultRoute
import com.zakia.idn.ojolapp.data.RoutesItem
import com.zakia.idn.ojolapp.model.Booking
import com.zakia.idn.ojolapp.network.NetworkModule
import com.zakia.idn.ojolapp.network.RequestNotification
import com.zakia.idn.ojolapp.utils.ChangeFormat
import com.zakia.idn.ojolapp.utils.Constan
import com.zakia.idn.ojolapp.utils.DirectionMapsV2
import com.zakia.idn.ojolapp.utils.GPSTrack
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home.*
import okhttp3.ResponseBody
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*

class HomeFragment : Fragment() , OnMapReadyCallback {

    var map: GoogleMap? = null

    var tanggal: String? = null
    var latAwal: Double? = null
    var lonAwal: Double? = null
    var latAkhir: Double? = null
    var lonAkhir: Double? = null

    var jarak: String? = null
    var dialog: Dialog? = null

    var keyy: String? = null
    private var auth: FirebaseAuth? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_home, container, false)
        auth = FirebaseAuth.getInstance()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //menginisialisasi dari mapsView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync (this)

        showPermission()
        visibleView(false)

        keyy?.let { bookingHistoryUser(it) }

        tv_home_awal.onClick {
            takeLocation(1)
        }

        tv_home_tujuan.onClick {
            takeLocation(2)
        }

        btn_home_next.onClick {
            if (tv_home_awal.text.isNotEmpty()!!
                && tv_home_tujuan.text.isNotEmpty()
            ) {
                insertServer()
            } else {
                toast("tidak boleh kosong").show()
                view.let {
                    Snackbar.make(
                        it, "tidak boleh kosong",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun bookingHistoryUser(key: String) {
        showDialog(true)
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constan.tb_booking)

        myRef.child(key).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val booking = snapshot.getValue(Booking::class.java)
                if (booking?.driver != ""){
                    startActivity<WaitingDriverActivity>(Constan.key to key)
                    showDialog(false)
                }
            }

        })
    }

    //insert data booking ke database
    private fun insertServer() {
        val currentTime = Calendar.getInstance().time
        tanggal = currentTime.toString()
        insertRequest(
            currentTime.toString(), auth?.uid.toString(),
            tv_home_awal.text.toString(), latAwal, lonAwal,
            tv_home_tujuan.text.toString(), latAkhir, lonAkhir,
            tv_home_price.text.toString(), jarak.toString()
        )
    }

    private fun insertRequest(
        tanggal: String,
        uid: String,
        lokasiAwal: String,
        latAwal: Double?,
        lonAwal: Double?,
        lokasiTujuan: String,
        latTujuan: Double?,
        lonTujuan: Double?,
        harga: String,
        jarak: String
    ): Boolean {

        val booking = Booking()
        booking.tanggal = tanggal
        booking.uid = uid
        booking.lokasiAwal = lokasiAwal
        booking.latAwal = latAwal
        booking.lonAwal = lonAwal
        booking.lokasiTujuan = lokasiTujuan
        booking.latTujuan = latTujuan
        booking.lonTujuan = lonTujuan
        booking.jarak = jarak
        booking.harga = harga
        booking.status = 1
        booking.driver = ""

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constan.tb_booking)
        keyy = database.reference.push().key
        val k = keyy

        pushNotif(booking)
        k?.let { bookingHistoryUser(it) }

        myRef.child(keyy ?: "").setValue(booking)

        return true
    }

    private fun pushNotif(booking: Booking) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Driver")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (issue in snapshot.children) {
                    val token = issue.child("token").getValue(String::class.java)

                    println(token.toString())
                    val request = RequestNotification()
                    request.token = token
                    request.sendNotificationModel = booking

                    NetworkModule.getServiceFcm().sendChatNotification(request)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                                Log.d("network failed :", t.message.toString())
                            }

                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                response.body()
                                Log.d("response server", response.message())
                            }

                        })
                }
            }

        })
    }

    private fun showDialog(status: Boolean) {
        dialog = this!!.activity?.let { Dialog(it) }
        dialog?.setContentView(R.layout.dialog_waiting_driver)

        if (status){
            dialog?.show()
        } else dialog?.dismiss()
    }

    private fun showPermission() {
        showGPS()

        if (activity?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {

            if (activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        it,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                }!!) {

                showGPS()

            } else {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 1
                )
            }
        }
    }

    //utk menampilkan lokasi user
    private fun showGPS() {
        val gps = context?.let { GPSTrack(it) }

        if (gps?.canGetLocation()!!) {
            latAwal = gps.latitude
            lonAwal = gps.longitude

            showMainMarker(latAwal ?: 0.0, lonAwal ?: 0.0, "My Location")
            val name = showName(latAwal ?: 0.0, lonAwal ?: 0.0)

            tv_home_awal.text = name

        } else gps.showSettingGPS()
    }

    private fun visibleView(status: Boolean) {
        if (status) {
            cv_home_bottom?.visibility = View.VISIBLE
            btn_home_next?.visibility = View.VISIBLE
        } else {
            cv_home_bottom?.visibility = View.GONE
            btn_home_next?.visibility = View.GONE
        }
    }

    //proses mengarahkan autocomplet google place
    fun takeLocation(status: Int) {
        try {
            context?.applicationContext?.let {
                Places.initialize(
                    it, Constan.API_KEY
                )
            }

            val fields = arrayListOf(
                Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.ADDRESS
            )

            val intent = context?.applicationContext?.let {
                Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN,
                    fields
                ).build(it)
            }

            startActivityForResult(intent, status)
        } catch (e: GooglePlayServicesRepairableException) {

        } catch (e: GooglePlayServicesNotAvailableException) {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val place = data?.let { Autocomplete.getPlaceFromIntent(it) }

                latAwal = place?.latLng?.latitude
                lonAwal = place?.latLng?.longitude

                tv_home_awal.text = place?.address.toString()
                showMainMarker(
                    latAwal ?: 0.0, lonAwal ?: 0.0,
                    place?.address.toString()
                )

                Log.i("location", "place : " + place?.name)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = data?.let { Autocomplete.getStatusFromIntent(it) }

                Log.i("location", status?.statusMessage.toString())
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        } else {
            if (resultCode == Activity.RESULT_OK) {
                val place = data?.let { Autocomplete.getPlaceFromIntent(it) }

                latAkhir = place?.latLng?.latitude
                lonAkhir = place?.latLng?.longitude

                tv_home_tujuan.text = place?.address.toString()
                showMarker(latAkhir ?: 0.0, lonAwal ?: 0.0, place?.address.toString())

                route()
                Log.i("location", "place" + place?.name)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = data?.let { Autocomplete.getStatusFromIntent(it) }

                Log.i("location", status?.statusMessage.toString())
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    @SuppressLint("CheckResult")
    private fun route() {
        val origin = latAwal.toString() + "," + lonAwal.toString()
        val dest = latAkhir.toString() + "," + lonAkhir.toString()

        NetworkModule.getService().actionRoute(origin, dest, Constan.API_KEY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ t: ResultRoute? ->
                showData(t?.routes)
            }, {})
    }

    private fun showData(routes: List<RoutesItem?>?) {
        visibleView(true)

        if (routes != null) {
            val point = routes[0]?.overviewPolyline?.points
            jarak = routes[0]?.legs?.get(0)?.distance?.text
            val jarakValue = routes[0]?.legs?.get(0)?.distance?.value
            val waktu = routes[0]?.legs?.get(0)?.duration?.text

            tv_home_waktu_distance.text = waktu + "(" + jarak + ")"
            val pricex = jarakValue?.toDouble()?.let { Math.round(it) }
            val price = pricex?.div(1000.0)?.times(2000.0)
            val price2 = ChangeFormat.toRupiahFormat2(price.toString())

            tv_home_price.text = "Rp, " + price2
            DirectionMapsV2.gambarRoute(map!!, point!!)

        } else {
            alert {
                message = "data route null"
            }.show()
        }
    }

    //geocorder
    private fun showName(lat: Double, lon: Double): String? {
        var name = ""
        var geocoder = Geocoder(context, Locale.getDefault())

        try {
            val address = geocoder.getFromLocation(lat, lon, 1)

            if (address.size > 0) {
                val fetchedAddress = address.get(0)
                val strAddress = StringBuilder()

                for (i in 0..fetchedAddress.maxAddressLineIndex) {
                    name = strAddress.append(fetchedAddress.getAddressLine(i))
                        .append("").toString()
                }
            }
        } catch (e: Exception) {
        }
        return name
    }

    //marker origin
    private fun showMainMarker(lat: Double, lon: Double, msg: String) {

        val res = context?.resources
        val marker1 = BitmapFactory.decodeResource(res, R.drawable.placeholder)
        val smallmarker = Bitmap.createScaledBitmap(marker1, 80, 120, false)
        val coordinate = LatLng(lat, lon)

        map?.addMarker(
            MarkerOptions().position(coordinate)
                .title(msg).icon(BitmapDescriptorFactory.fromBitmap(smallmarker))
        )
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 16f))
        map?.moveCamera(CameraUpdateFactory.newLatLng(coordinate))
    }

    //marker destination
    private fun showMarker(lat: Double, lon: Double, msg: String) {
        val coordinat = LatLng(lat, lon)

        map?.addMarker(MarkerOptions().position(coordinat).title(msg))
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinat, 16f))
        map?.moveCamera(CameraUpdateFactory.newLatLng(coordinat))
    }

    //utk menamplkan map ke fragment
    override fun onMapReady(p0: GoogleMap?) {
        map = p0
        map?.uiSettings?.isMyLocationButtonEnabled = false
        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(-6.3088652, 106.682188), 12f
            )
        )
    }

    override fun onResume() {
        keyy?.let { bookingHistoryUser(it) }
        mapView?.onResume()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1){
            showGPS()
        }
    }
}