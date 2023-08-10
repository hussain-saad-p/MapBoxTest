package com.example.mygroovyapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.annotation.DrawableRes
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
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location

class MainActivity : AppCompatActivity(), OnClickListener {

    companion object {
        private const val REQUEST_CODE = 123
        private const val REQUEST_CODE_BACKGROUND = 143
    }

    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initCode()
        setUpMapBox()
        askLocationPermissions()
    }

    private fun initCode() {
        findViewById<View>(R.id.floatingActionButton).setOnClickListener(this@MainActivity)
    }

    override fun onClick(view: View?) {
        view?.let { v ->
            when (v.id) {
                R.id.floatingActionButton -> moveCameraToCurrentLocation()
            }
        }
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
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
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
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
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
            } else Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT)
                .show()     // Permission Denied
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

                moveCameraToCurrentLocation()
                setLocationPuck()
                addAnnotationToMap()
            }
        }
    }

    private fun moveCameraToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

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
    }

    /**
     * Add Annotations To Map
     */
    private fun addAnnotationToMap() {
//        addMarkerAnnotationToMap()
//        addCircleAnnotationToMap()
//        addPolylineAnnotationToMap()
        addPolygonAnnotationToMap()
    }

    /**
     * Marker Annotation
     */
    private fun addMarkerAnnotationToMap() {
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        bitmapFromDrawableRes(
            this@MainActivity,
            R.drawable.red_marker
        )?.let {
            val annotationApi = mapView?.annotations
            val pointAnnotationManager = mapView?.let { it1 ->
                annotationApi?.createPointAnnotationManager(it1)
            }
            // Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                // Define a geographic coordinate.
                .withPoint(Point.fromLngLat(18.06, 59.31))
                // Specify the bitmap you assigned to the point annotation
                // The bitmap will be added to map style automatically.
                .withIconImage(it)

            // Add the resulting pointAnnotation to the map.
            pointAnnotationManager?.create(pointAnnotationOptions)
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) return null
        return if (sourceDrawable is BitmapDrawable) sourceDrawable.bitmap
        else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    /**
     * Circle Annotation
     */
    private fun addCircleAnnotationToMap() {
        // Create an instance of the Annotation API and get the CircleAnnotationManager.
        val annotationApi = mapView?.annotations
        val circleAnnotationManager =
            mapView?.let { annotationApi?.createCircleAnnotationManager(it) }
        // Set options for the resulting circle layer.
        val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
            // Define a geographic coordinate.
            .withPoint(Point.fromLngLat(14.06, 59.31))
            // Style the circle that will be added to the map.
            .withCircleRadius(8.0)
            .withCircleColor("#ee4e8b")
            .withCircleStrokeWidth(2.0)
            .withCircleStrokeColor("#ffffff")
        // Add the resulting circle to the map.
        circleAnnotationManager?.create(circleAnnotationOptions)
    }

    /**
     * Polyline Annotation
     */
    private fun addPolylineAnnotationToMap() {
        // Create an instance of the Annotation API and get the polyline manager.
        val annotationApi = mapView?.annotations
        val polylineAnnotationManager = mapView?.let {
            annotationApi?.createPolylineAnnotationManager(
                it
            )
        }
        // Define a list of geographic coordinates to be connected.
        val points = listOf(
            Point.fromLngLat(14.94, 79.25),
            Point.fromLngLat(22.18, 79.37)
        )
        // Set options for the resulting line layer.
        val polylineAnnotationOptions: PolylineAnnotationOptions = PolylineAnnotationOptions()
            .withPoints(points)
            // Style the line that will be added to the map.
            .withLineColor("#ee4e8b")
            .withLineWidth(5.0)
        // Add the resulting line to the map.
        polylineAnnotationManager?.create(polylineAnnotationOptions)
    }

    private fun addPolygonAnnotationToMap() {
        // Create an instance of the Annotation API and get the polygon manager.
        val annotationApi = mapView?.annotations
        val polygonAnnotationManager = mapView?.let {
            annotationApi?.createPolygonAnnotationManager(
                it
            )
        }
        // Define a list of geographic coordinates to be connected.
        val points = listOf(
            listOf(
                Point.fromLngLat(17.94, 59.25),
                Point.fromLngLat(18.18, 59.25),
                Point.fromLngLat(18.18, 59.37),
                Point.fromLngLat(17.94, 59.37)
            )
        )
        // Set options for the resulting fill layer.
        val polygonAnnotationOptions: PolygonAnnotationOptions = PolygonAnnotationOptions()
            .withPoints(points)
            // Style the polygon that will be added to the map.
            .withFillColor("#ee4e8b")
            .withFillOpacity(0.4)
        // Add the resulting polygon to the map.
        polygonAnnotationManager?.create(polygonAnnotationOptions)
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