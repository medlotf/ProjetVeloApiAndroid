package com.example.prjapi;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private Spinner spinner,spinner1;
    private TextView txt;
    private String[] sp,sp1,ids;
    private List<String> spinnerArray,spinnerArray1,positions,nums;
    GoogleMap map;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

        spContracts();
        mapFragment.getMapAsync(this);
    }

    public void spContracts() {
        final DBConnections db=new DBConnections(this);
        spinner = (Spinner)findViewById(R.id.spinner);
        sp = new String[]{"Choisie une ville"};
        spinnerArray = new ArrayList<>(Arrays.asList(sp));
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,R.layout.simple_spinner_item,spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        RequestQueue queue= Volley.newRequestQueue(MainActivity.this);
        String url="https://api.jcdecaux.com/vls/v3/contracts?apiKey=7886a12c53604b2668a08582a04795afcc9375b0";
        JsonArrayRequest jsonRequest=new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    String city;
                    for(int i=0;i<response.length();i++)
                    {
                        JSONObject res = response.getJSONObject(i);
                        city = res.getString("name");
                        spinnerArray.add(city);
                        // ADD CITY IN DB
                        db.InsertVilles(city);
                        spinnerArrayAdapter.notifyDataSetChanged();
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                String message = null;
                if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError) {
                    Log.d("cnx","erroooooooor A");
                    //ADD CITIES IN spinnerArray
                    ArrayList<String> arrlist=db.getAllVilles();
                    for (int i = 0; i < arrlist.size(); i++) {
                        spinnerArray.add(arrlist.get(i));
                        spinnerArrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        queue.add(jsonRequest);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String location=spinnerArray.get(position);
                int zoom=10;
                LatLng latIng;

                if(location==null || location.equals("Choisie une ville")){
                    location="Paris";
                    zoom=5;
                }

                if(isNetworkAvailable()){
                    List<Address> adresseList=null;
                    Geocoder geocoder=new Geocoder(MainActivity.this);
                    try {
                        adresseList=geocoder.getFromLocationName(location,1);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    Address address=adresseList.get(0);
                    latIng=new LatLng(address.getLatitude(),address.getLongitude());
                }
                else{
                    latIng=new LatLng(48.856613,2.352222);
                    zoom=5;
                }

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latIng,zoom));

                spStations(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void spStations(final int pos) {
        final DBConnections db1=new DBConnections(this);
        txt = (TextView) findViewById(R.id.textView2);
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        txt.setVisibility(View.INVISIBLE);
        spinner1.setVisibility(View.INVISIBLE);

        if (pos != 0) {
            txt.setVisibility(View.VISIBLE);
            spinner1.setVisibility(View.VISIBLE);
            sp1 = new String[]{"Choisie une station"};
            ids = new String[]{"0"};
            spinnerArray1 = new ArrayList<>(Arrays.asList(sp1));
            nums = new ArrayList<>(Arrays.asList(ids));
            positions=new ArrayList<String>();
            final ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, spinnerArray1);
            spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner1.setAdapter(spinnerArrayAdapter1);

            RequestQueue queue1 = Volley.newRequestQueue(MainActivity.this);
            String url = "https://api.jcdecaux.com/vls/v3/stations?contract=" + spinnerArray.get(pos) + "&apiKey=7886a12c53604b2668a08582a04795afcc9375b0";
            JsonArrayRequest jsonRequest1 = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        String station;
                        String coord;
                        String id;
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject res = response.getJSONObject(i);
                            station = res.getString("name");
                            spinnerArray1.add(station);
                            coord=res.getString("position");
                            positions.add(coord);
                            id=res.getString("number");
                            nums.add(id);


                            JSONObject obj = new JSONObject(coord);

                            db1.InsertStations(id,station,obj.getString("latitude"),obj.getString("longitude"),spinnerArray.get(pos));

                            spinnerArrayAdapter1.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError) {
                        Log.d("arr","errr");
                        //ADD STAIONS IN spinnerArray1
                        ArrayList<String> artist1=db1.getStations();
                        for (int i = 0; i < artist1.size(); i++) {
                            //Log.d("arr",artist1.get(i));
                            spinnerArray1.add(artist1.get(i));
                            spinnerArrayAdapter1.notifyDataSetChanged();
                        }
                    }

                }
            });
            queue1.add(jsonRequest1);

            spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,final int position, long id) {
                    final TextView status=(TextView)findViewById(R.id.status);
                    final TextView dispo=(TextView)findViewById(R.id.disponibility);
                    status.setVisibility(View.INVISIBLE);
                    dispo.setVisibility(View.INVISIBLE);
                    if(position != 0)
                    {
                        if(!isNetworkAvailable()){

                            ArrayList<String> dt=db1.getStation(spinnerArray1.get(position),spinnerArray.get(pos));
                            double v1=Double.valueOf(dt.get(2));
                            double v2=Double.valueOf(dt.get(3));
                            LatLng loc=new LatLng(v1,v2);
                            map.addMarker(new MarkerOptions().position(loc).title(spinnerArray1.get(position)));
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc,18));

                            status.setText("Connect to network to get more data");
                            status.setTextColor(0xFF941a12);
                            status.setVisibility(View.VISIBLE);
                        }
                        else{
                            RequestQueue queue2 = Volley.newRequestQueue(MainActivity.this);
                            String url2="https://api.jcdecaux.com/vls/v3/stations/"+ nums.get(position) +"?contract="+ spinnerArray.get(pos) +"&apiKey=7886a12c53604b2668a08582a04795afcc9375b0";
                            JsonObjectRequest jsonRequest2 = new JsonObjectRequest(Request.Method.GET, url2, null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONObject obj = new JSONObject(positions.get(position));
                                        double v1=Double.valueOf(obj.getString("latitude"));
                                        double v2=Double.valueOf(obj.getString("longitude"));
                                        LatLng loc=new LatLng(v1,v2);
                                        map.addMarker(new MarkerOptions().position(loc).title(spinnerArray1.get(position)));
                                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc,18));

                                        String st=response.getString("status");
                                        status.setText(st);
                                        status.setTextColor(0xFF0d610d);
                                        if(st.equals("CLOSED")){
                                            status.setTextColor(0xFF941a12);
                                        }
                                        status.setVisibility(View.VISIBLE);

                                        String capaTotal= response.getJSONObject("totalStands").getString("capacity");
                                        String capaCurr= response.getJSONObject("mainStands").getString("capacity");
                                        dispo.setText("Dispo:"+capaCurr+"/"+capaTotal);
                                        dispo.setTextColor(0xFF0d610d);
                                        dispo.setVisibility(View.VISIBLE);


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    // get Status if open color  green if CLOSE red => insert in textView
                                    // get mainsStands capacity / totalStands capacity =>inser in textView

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    if (volleyError instanceof NetworkError || volleyError instanceof AuthFailureError || volleyError instanceof NoConnectionError) {

                                        ArrayList<String> dt=db1.getStation(spinnerArray1.get(position),spinnerArray.get(pos));
                                        double v1=Double.valueOf(dt.get(2));
                                        double v2=Double.valueOf(dt.get(3));
                                        LatLng loc=new LatLng(v1,v2);
                                        map.addMarker(new MarkerOptions().position(loc).title(spinnerArray1.get(position)));
                                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc,18));

                                        status.setText("Connect to network to get more data");
                                        status.setTextColor(0xFF941a12);
                                        status.setVisibility(View.VISIBLE);

                                    }
                                }
                            });
                            queue2.add(jsonRequest2);
                        }
                        }

                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
