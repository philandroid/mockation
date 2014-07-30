package com.location.philippweiher.test;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends Activity
        implements OnMapClickListener, OnMapLongClickListener, OnMarkerDragListener,
        ConnectionCallbacks, OnConnectionFailedListener {
    private TextView searchQueryTextView;

    public static final String LAT_FOR_POINT = "latitude";
    public static final String LON_FOR_POINT = "longitude";

    // Object that connects the app to Location Services
    private LocationClient mLocationClient;
    private GoogleMap myMap;

    public static final String ACTION_SERVICE_MESSAGE =
            "com.example.android.mocklocation.ACTION_SERVICE_MESSAGE";

    public static final String LOCATION_PROVIDER = "Fused";

    public static final String ACTION_START =
            "com.example.android.mocklocation.ACTION_START";

    public static final String ACTION_STOP_TEST =
            "com.example.android.mocklocation.ACTION_STOP_TEST";

    //Key for extended data in the Activity's outgoing Intent that records the requested pause
    // value.
    public static final String EXTRA_PAUSE_VALUE =
            "com.example.android.mocklocation.EXTRA_PAUSE_VALUE";

    //Key for extended data in the Activity's outgoing Intent that records the requested interval
    //for mock locations sent to Location Services.
    public static final String EXTRA_SEND_INTERVAL =
            "com.example.android.mocklocation.EXTRA_SEND_INTERVAL";

    private Intent mRequestIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FragmentManager myFragmentManager = getFragmentManager();
        MapFragment myMapFragment
                = (MapFragment) myFragmentManager.findFragmentById(R.id.map);
        myMap = myMapFragment.getMap();

        myMap.setMyLocationEnabled(true);

        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        myMap.setOnMapClickListener(this);
        myMap.setOnMapLongClickListener(this);
        myMap.setOnMarkerDragListener(this);

        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();
        mRequestIntent = new Intent(this, SendMockLocationService.class);
        searchQueryTextView = (TextView) findViewById(R.id.inputSearchField);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_maps, menu);
        return true;
    }

    @Override
    public void onMapClick(LatLng point) {
        myMap.animateCamera(CameraUpdateFactory.newLatLng(point));
    }

    private void onLocationReceived(LatLng point) {
        // Notify SendMockLocationService to loop once through the mock locations
        mRequestIntent.setAction(ACTION_START);
        mRequestIntent.putExtra(LAT_FOR_POINT, point.latitude);
        mRequestIntent.putExtra(LON_FOR_POINT, point.longitude);

        // Start SendMockLocationService
        startService(mRequestIntent);
    }

    /**
     * Defined in layout. Will perform a search to google maps
     *
     * @param view Clicked button
     */
    public void performSearch(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchQueryTextView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        final String query = searchQueryTextView.getText().toString();
        if (query.isEmpty()) {
            Toast.makeText(this, "Query empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geoCoder = new Geocoder(this);
        List<Address> matches = null;
        try {
            matches = geoCoder.getFromLocationName(query, 5);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (matches.isEmpty()) {
            Toast.makeText(this, "No matches found", Toast.LENGTH_SHORT).show();
            return;
        }


        if (matches.size() > 1) {

            // display list dialog
            AddressAdapter adapter = new AddressAdapter(matches, this);
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_listview);
            ListView listView = (ListView) dialog.findViewById(R.id.listViewForAddresses);
            listView.setAdapter(adapter);
            final List<Address> finalMatches = matches;
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    LatLng point = new LatLng(finalMatches.get(i).getLatitude(), finalMatches.get(i).getLongitude());
                    myMap.animateCamera(CameraUpdateFactory.newLatLng(point));
                    myMap.clear();
                    myMap.addMarker(new MarkerOptions()
                                    .position(point)
                                    .draggable(true)
                                    .title(query)
                    );
                    onLocationReceived(point);
                    dialog.dismiss();
                }
            });
            dialog.show();

            return;
        }

        LatLng point = new LatLng(matches.get(0).getLatitude(), matches.get(0).getLongitude());
        myMap.animateCamera(CameraUpdateFactory.newLatLng(point));
        myMap.clear();
        myMap.addMarker(new MarkerOptions()
                        .position(point)
                        .draggable(true)
                        .title(query)
        );
        onLocationReceived(point);

    }

    @Override
    public void onMapLongClick(LatLng point) {
        // just one marker is shown at the same time (if we want more just delete this line)
        myMap.clear();

        myMap.addMarker(new MarkerOptions()
                        .position(point)
                        .draggable(true)
                        .title("" + point.latitude + " " + point.longitude)
        );

        onLocationReceived(point);

    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
