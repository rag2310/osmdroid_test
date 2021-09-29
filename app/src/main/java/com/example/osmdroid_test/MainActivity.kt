package com.example.osmdroid_test

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var mapView: MapView
    private var permissionDenied = false
    private lateinit var mLocationOverlay: MyLocationNewOverlay
    private lateinit var compassOverlay: CompassOverlay
    private lateinit var mapController: IMapController
    private lateinit var userAgentValue: String
    private lateinit var rotationGestureOverlay: RotationGestureOverlay
    private lateinit var listMarker: List<Marker>
    private var markedMap: Boolean = false
    private var gpsSpeed = 0f
    private var gpsBearing = 0f
    private var lat = 0f
    private var lon = 0f
    private var alt = 0f
    private var timeOfFix: Long = 0
    var deviceOrientation = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        userAgentValue = Configuration.getInstance().userAgentValue

        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)

        mapController = mapView.controller
        mapController.setZoom(ZOOM)
        enableMyLocation()
    }

    override fun onResume() {
        super.onResume()
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        Configuration.getInstance().save(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        mapView.onPause()
    }

    private fun isPermissionGranted(
        grantPermissions: Array<String>, grantResults: IntArray,
        permission: String
    ): Boolean {
        for (i in grantPermissions.indices) {
            if (permission == grantPermissions[i]) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }

    private fun requestPermission(
        activity: AppCompatActivity, requestId: Int,
        permission: String, finishActivity: Boolean
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            RationaleDialog.newInstance(requestId, finishActivity)
                .show(activity.supportFragmentManager, "dialog")
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                requestId
            )
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @DelicateCoroutinesApi
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            //LOCATION GEO POINT
            mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)

            mLocationOverlay.enableMyLocation()
            mLocationOverlay.enableFollowLocation()
            mLocationOverlay.isDrawAccuracyEnabled = true
            mLocationOverlay.runOnFirstFix {
                runOnUiThread {
                    mapController.animateTo(mLocationOverlay.myLocation)
                    mapController.setZoom(ZOOM)
                }
            }

            //COMPASS
            compassOverlay = CompassOverlay(this, InternalCompassOrientationProvider(this), mapView)
            compassOverlay.enableCompass()

            //ROTATION GESTURE
            rotationGestureOverlay = RotationGestureOverlay(mapView);
            rotationGestureOverlay.isEnabled
            mapView.setMultiTouchControls(true);

            //ICONS IN MAP
            /*Marker Casa*/
            val marker1 = GeoPoint(12.143512, -86.221260)
            val marker2 = GeoPoint(12.143433, -86.221247)
            val marker3 = GeoPoint(12.143281, -86.221200)
            val marker4 = GeoPoint(12.143118, -86.221152)

            /*Marker Trabajo*/
            val marker5 = GeoPoint(12.102939, -86.262639)
            val marker6 = GeoPoint(12.102655, -86.262590)
            val marker7 = GeoPoint(12.102674, -86.262101)
            val marker8 = GeoPoint(12.102713, -86.261776)

            listMarker = listOf(
                marker(marker1, "Servicio #1", "Medidor #1"),
                marker(marker2, "Servicio #2", "Medidor #2"),
                marker(marker3, "Servicio #3", "Medidor #3"),
                marker(marker4, "Servicio #4", "Medidor #4"),
                marker(marker5, "Servicio #5", "Medidor #5"),
                marker(marker6, "Servicio #6", "Medidor #6"),
                marker(marker7, "Servicio #7", "Medidor #7"),
                marker(marker8, "Servicio #8", "Medidor #8"),
            )

            //Routing
            /*val wayPoints = ArrayList<GeoPoint>()
            wayPoints.add(marker1)
            wayPoints.add(marker2)
            val wayPoints2 = ArrayList<GeoPoint>()
            wayPoints2.add(marker2)
            wayPoints2.add(marker3)
            val wayPoints3 = ArrayList<GeoPoint>()
            wayPoints3.add(marker3)
            wayPoints3.add(marker4)*/

            val wayPoints4 = ArrayList<GeoPoint>()
            wayPoints4.add(marker5)
            wayPoints4.add(marker6)

            val wayPoints5 = ArrayList<GeoPoint>()
            wayPoints5.add(marker6)
            wayPoints5.add(marker7)

            val wayPoints6 = ArrayList<GeoPoint>()
            wayPoints6.add(marker7)
            wayPoints6.add(marker8)

            GlobalScope.launch {
                /*routing(applicationContext, wayPoints)
                routing(applicationContext, wayPoints2)
                routing(applicationContext, wayPoints3)*/
                routing(applicationContext, wayPoints4)
                routing(applicationContext, wayPoints5)
                routing(applicationContext, wayPoints6)
            }

            //ADD OVERLAYS
            mapView.overlays.add(mLocationOverlay)
            mapView.overlays.add(compassOverlay)
            mapView.overlays.add(rotationGestureOverlay)
            listMarker.forEach {
                if (!markedMap) markedMap = true
                mapView.overlays.add(it)
            }
            mapView.invalidate()

            mapView.addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    event?.let { zoomEvent ->
                        if (zoomEvent.zoomLevel >= 12.0) {
                            if (!markedMap) {
                                listMarker.forEach {
                                    mapView.overlays.add(it)
                                    if (!markedMap) markedMap = true
                                }
                            }
                        }

                        if (zoomEvent.zoomLevel < 12.0) {
                            if (markedMap) {
                                listMarker.forEach {
                                    mapView.overlays.remove(it)
                                    if (markedMap) markedMap = false
                                }
                            }
                        }
                    }
                    return false
                }

            })
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
        // [END maps_check_location_permission]
    }

    suspend fun routing(context: Context, wayPoints: ArrayList<GeoPoint>) {
        withContext(Dispatchers.IO) {
            val roadManager: RoadManager = OSRMRoadManager(context, userAgentValue)
            val road: Road = roadManager.getRoad(wayPoints)
            val roadOverlay: Polyline =
                RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 20.0f)
            mapView.overlays.add(roadOverlay)
            mapView.invalidate()
        }
    }

    private fun marker(geoPoint: GeoPoint, titles: String, description: String) =
        Marker(mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
            title = titles
            subDescription = description
            icon = application.getDrawable(R.drawable.red_pushpin)
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val ZOOM: Double = 21.0
    }

    override fun onLocationChanged(location: Location) {

        gpsBearing = location.bearing
        gpsSpeed = location.speed
        lat = location.latitude.toFloat()
        lon = location.longitude.toFloat()
        alt = location.altitude.toFloat()
        timeOfFix = location.time

        var t: Float = (360 - gpsBearing - this.deviceOrientation)
        if (t < 0){
            t += 360;
        }
        if(t > 360){
            t -= 360
        }

        t = t

    }

}

class RationaleDialog : DialogFragment() {
    private var finishActivity = false
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val requestCode =
            arguments?.getInt(ARGUMENT_PERMISSION_REQUEST_CODE) ?: 0
        finishActivity =
            arguments?.getBoolean(ARGUMENT_FINISH_ACTIVITY) ?: false
        return AlertDialog.Builder(requireContext())
            .setMessage(R.string.permission_rationale_location)
            .setPositiveButton(android.R.string.ok) { dialog, which -> // After click on Ok, request the permission.
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestCode
                )
                finishActivity = false
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (finishActivity) {
            Toast.makeText(
                activity,
                R.string.permission_required_toast,
                Toast.LENGTH_SHORT
            ).show()
            activity?.finish()
        }
    }

    companion object {
        private const val ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode"
        private const val ARGUMENT_FINISH_ACTIVITY = "finish"

        /**
         * Creates a new instance of a dialog displaying the rationale for the use of the location
         * permission.
         *
         *
         * The permission is requested after clicking 'ok'.
         *
         * @param requestCode    Id of the request that is used to request the permission. It is
         * returned to the
         * [androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback].
         * @param finishActivity Whether the calling Activity should be finished if the dialog is
         * cancelled.
         */
        fun newInstance(requestCode: Int, finishActivity: Boolean): RationaleDialog {
            val arguments = Bundle().apply {
                putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode)
                putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)
            }
            return RationaleDialog().apply {
                this.arguments = arguments
            }
        }
    }
}