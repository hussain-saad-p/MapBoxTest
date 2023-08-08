package com.example.mygroovyapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.location


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 123
        private const val REQUEST_CODE_BACKGROUND = 143
    }

    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpMapBox()
        askLocationPermissions()
    }

    private fun setUpMapBox() {
        mapView = findViewById(R.id.mapView)
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)
        mapboxMap = mapView?.getMapboxMap()
    }


    /**
     * Permissions Methods
     */
    private fun askLocationPermissions() = if (ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_CODE
        )
        askBackgroundPermission()
    } else {
        Toast.makeText(
            this@MainActivity,
            "Location Permissions Provided",
            Toast.LENGTH_SHORT
        ).show()
        showCurrentLocationOnMap()
    }

    private fun askBackgroundPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_CODE_BACKGROUND
                );
            } else Toast.makeText(
                this@MainActivity,
                "Background Location Permissions Provided",
                Toast.LENGTH_SHORT
            ).show()
        } else Toast.makeText(
            this@MainActivity,
            "Location Permissions Provided",
            Toast.LENGTH_SHORT
        ).show()
    } else Toast.makeText(
        this@MainActivity,
        "Background Location Permissions Not Required",
        Toast.LENGTH_SHORT
    ).show()


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
                showCurrentLocationOnMap()
            } else {
                // Permission Denied
                Toast.makeText(this, "your message", Toast.LENGTH_SHORT)
                    .show()
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun showCurrentLocationOnMap() {
        mapView?.getMapboxMap()?.loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            mapView?.location?.updateSettings {// After the style is loaded, initialize the Location component
                enabled = true
                pulsingEnabled = true
            }
        }
        setLocationPuck()
    }

    private fun setLocationPuck() {
        mapView?.location?.locationPuck = LocationPuck2D(
            topImage = AppCompatResources.getDrawable(
                this,
                com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_icon
            ),
            bearingImage = AppCompatResources.getDrawable(
                this,
                com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_bearing_icon
            ),
            shadowImage = AppCompatResources.getDrawable(
                this,
                com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_stroke_icon
            ),
            scaleExpression = interpolate {
                linear()
                zoom()
                stop {
                    literal(0.0)
                    literal(0.6)
                }
                stop {
                    literal(20.0)
                    literal(1.0)
                }
            }.toJson()
        )

        // set initial camera position
//        val initialCameraOptions = CameraOptions.Builder()
//            .center(Point.fromLngLat(-74.0066, 40.7135))
//            .pitch(45.0)
//            .zoom(15.5)
//            .bearing(-17.6)
//            .build()
//
//        val mapInitOptions = MapInitOptions(this, resourceOptions, mapOptions, plugins, initialCameraOptions, true)
//        mapView = MapView(this, mapInitOptions)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {

            mapView?.getMapboxMap()?.setCamera(
                CameraOptions.Builder()
                    .center(
                        Point.fromLngLat(
                            it.longitude, it.latitude
                        )
                    )
                    .zoom(15.5)
                    .bearing(-17.6)
                    .build()
            )
        }

        findViewById<View>(R.id.floatingActionButton).setOnClickListener {
            LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {

                mapView?.getMapboxMap()?.setCamera(
                    CameraOptions.Builder()
                        .center(
                            Point.fromLngLat(
                                it.longitude, it.latitude
                            )
                        )
                        .zoom(15.5)
                        .bearing(-17.6)
                        .build()
                )
            }
        }

    }


    /**
     * MapBox Lifecycle Methods
     */
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }
}