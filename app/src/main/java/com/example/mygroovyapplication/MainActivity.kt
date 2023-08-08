package com.example.mygroovyapplication

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.MapView
import com.mapbox.maps.Style


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 123
        private const val REQUEST_CODE_BACKGROUND = 143
    }

    private var mapView: MapView? = null
    lateinit var permissionsManager: PermissionsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        setUpMapBox()
        askLocationPermissions()
    }

    private fun setUpMapBox() {
        mapView = findViewById(R.id.mapView)
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)
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
    } else Toast.makeText(
        this@MainActivity,
        "Location Permissions Provided",
        Toast.LENGTH_SHORT
    ).show()

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
            } else {
                // Permission Denied
                Toast.makeText(this, "your message", Toast.LENGTH_SHORT)
                    .show()
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
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