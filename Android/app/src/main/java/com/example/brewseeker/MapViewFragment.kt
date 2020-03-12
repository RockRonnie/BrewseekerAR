package com.example.brewseeker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.VectorSource
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.net.URI
import java.net.URISyntaxException


@Suppress("DEPRECATION")
class MapViewFragment: Fragment(), OnMapReadyCallback, PermissionsListener {

    companion object {
        fun newInstance(): MapViewFragment {
            return MapViewFragment()
        }
    }



    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap
    private lateinit var ctx: Context
    private lateinit var permissionManager: PermissionsManager
    private lateinit var startButton: Button

    private lateinit var locationComponent: LocationComponent

    //variables for locations
    private lateinit var originPoint: Point
    private lateinit var destinationPoint: Point

    private var destinationMarker: Marker? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var currentRoute: DirectionsRoute? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ctx = activity as Context
        return inflater.inflate(R.layout.mapview_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.e("CTX: $ctx")

        startButton = view.findViewById(R.id.startButton)

        //Setup the MapView
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        startButton.setOnClickListener {
            val options = NavigationLauncherOptions.builder()
                .directionsRoute(currentRoute)
                .shouldSimulateRoute(true)
                .build()
            NavigationLauncher.startNavigation(this.activity,options)
        }

    }

    @SuppressLint("LogNotTimber")
    override fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap

        // Custom map style has been loaded and map is now ready
        map.setStyle(Style.Builder().fromUri("mapbox://styles/rockronnie/ck7aiczfz0k8d1iqx11g95bmo")) {
            //Adding a vector source layer

            val vectorSource = VectorSource("bars", "rockronnie.ck7ddlkgh01j92ko4rtvua7uj-99tvc")
            Log.e("vector", "vector: $vectorSource")
            try {
                val source = GeoJsonSource("geojson-source", URI("assets://geoJsonBars"))
                it.addSource(source)
                Log.e("GeoJsonSoruce", "got geoJson $source")

            } catch (exception: URISyntaxException) {

                Log.d("TAG", "$exception")
            }
            enableLocationComponent(it)
            it.addSource(vectorSource)
        }

        map.addOnMapClickListener { point ->
            destinationMarker?.let{
                map.removeMarker(it)
            }
            originPoint = Point.fromLngLat(locationComponent.lastKnownLocation!!.longitude,
                locationComponent.lastKnownLocation!!.latitude)

            destinationMarker = map.addMarker(MarkerOptions().position(point))
            destinationPoint = Point.fromLngLat(point.longitude, point.latitude)
            startButton.isEnabled = true
            getRoute(originPoint, destinationPoint)

            Timber.e("destination Destination: $destinationPoint")

            true
        }

        map.addOnMapLongClickListener {

            // Convert LatLng coordinates to screen pixel and only query the rendered features.
            val pixel = map.projection.toScreenLocation(it)
            val features = map.queryRenderedFeatures(pixel)


            // Get the first feature within the list if one exist
            if (features.isNotEmpty()) {
                val feature = features[0]

                // Get the places name and Description from features
                val placeName = feature.getStringProperty("name")
                val placeDesc = feature.getStringProperty("description")

                if(placeName != null){
                    Toast.makeText(ctx, "Place: $placeName", Toast.LENGTH_LONG).show()
                    Toast.makeText(ctx, "Description: $placeDesc", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(ctx, "Cannot show Point, please try again", Toast.LENGTH_SHORT)
                }

                //Working on Info Window for the app
                /**
                val infoWindow: View = View.inflate(ctx, R.layout.infowindow, null)

                // Set place name
                val titleTV: TextView? = infoWindow.findViewById(R.id.title)
                titleTV?.text = placeName

                // Set place description
                val descTV: TextView? = infoWindow.findViewById(R.id.desc)
                descTV?.text = placeDesc

                infoWindowFragment = InfoWindowFragment.newInstance()
                myManager = this.fragmentManager!!
                myTransaction = myManager.beginTransaction()
                myTransaction.add(R.id.infowindow, infoWindowFragment).commit()
                Log.e("fragment", "myTransaction: $myTransaction, myManager: $myManager")
                 */

                // Ensure the feature has properties defined
                for ((key, value) in feature.properties()!!.entrySet()) {
                    // Log all the properties
                    Log.d("TAG", String.format("%s = %s", key, value))
                }
            }
            Log.e("longClick", "long click activated!")
            true
        }
    }

    /**
     * Initialize the Maps SDK's LocationComponent
     */
    private fun enableLocationComponent(loadedMapStyle: Style){

        if (PermissionsManager.areLocationPermissionsGranted(ctx)) {

            locationComponent = map.locationComponent

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(ctx)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(ctx, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            // Get an instance of the LocationComponent and then adjust its settings
            map.locationComponent.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS

                // Set the origin geopoint for getRoute
                originPoint = Point.fromLngLat(this.lastKnownLocation!!.longitude, this.lastKnownLocation!!.latitude)
            }
        } else {
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this.activity)
        }
    }

    //Draw the path from originPoint to destinationPoint
    private fun getRoute(origin: Point, destination: Point) {

        Mapbox.getAccessToken()?.let {
            NavigationRoute.builder(ctx)
                .accessToken(it)
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(object: Callback<DirectionsResponse> {
                    override fun onResponse(
                        call: Call<DirectionsResponse>,
                        response: Response<DirectionsResponse>
                    ) {
                        val body = response.body() ?: return
                        if(body.routes().count() == 0){
                            Timber.e("MainActivity No routes found")
                            return
                        }
                        if(navigationMapRoute != null){
                            navigationMapRoute?.updateRouteArrowVisibilityTo(false)
                        }else{
                            navigationMapRoute = NavigationMapRoute(null, mapView, map)
                        }
                        currentRoute = response.body()?.routes()?.first()
                        if (currentRoute != null){
                            navigationMapRoute?.addRoute(currentRoute)
                        }
                    }
                    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                        Timber.e( "Not getting any response!")
                    }
                })
        }
    }


    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {

    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            if (map.style != null) {
                enableLocationComponent(map.style!!)
                Timber.e("Lat: ${originPoint.latitude()}, long: ${originPoint.longitude()}")
            }
        } else {
            Toast.makeText(ctx, "Please grant the app permission to use your location!", Toast.LENGTH_LONG).show()
        }
    }



    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Prevent leaks
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}