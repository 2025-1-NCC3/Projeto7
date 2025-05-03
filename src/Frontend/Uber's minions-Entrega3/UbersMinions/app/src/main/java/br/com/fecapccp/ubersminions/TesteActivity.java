package br.com.fecapccp.ubersminions;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class TesteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText endereco, destino;
    private Button pesquisar;
    private Button localAtual;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste);

        endereco = findViewById(R.id.etEndereco);
        destino = findViewById(R.id.etDestino);
        pesquisar = findViewById(R.id.btnPesquisar);
        localAtual = findViewById(R.id.btnLocalAtual);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            Toast.makeText(this, "Google Play Services não disponível", Toast.LENGTH_LONG).show();
        }

        pesquisar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String origemStr = endereco.getText().toString();
                String destinoStr = destino.getText().toString();

                if (!origemStr.isEmpty() && !destinoStr.isEmpty()) {
                    Geocoder geocoder = new Geocoder(TesteActivity.this);
                    try {
                        List<Address> origemLista = geocoder.getFromLocationName(origemStr, 1);
                        List<Address> destinoLista = geocoder.getFromLocationName(destinoStr, 1);

                        if (origemLista != null && !origemLista.isEmpty() &&
                                destinoLista != null && !destinoLista.isEmpty()) {

                            LatLng origemLatLng = new LatLng(origemLista.get(0).getLatitude(), origemLista.get(0).getLongitude());
                            LatLng destinoLatLng = new LatLng(destinoLista.get(0).getLatitude(), destinoLista.get(0).getLongitude());

                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(origemLatLng).title("Origem"));
                            mMap.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origemLatLng, 14));

                            getRoute(origemLatLng, destinoLatLng);

                        } else {
                            Toast.makeText(TesteActivity.this, "Endereço(s) não encontrado(s)", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(TesteActivity.this, "Erro ao buscar endereços", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TesteActivity.this, "Preencha origem e destino", Toast.LENGTH_SHORT).show();
                }
            }
        });

        localAtual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(TesteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(TesteActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(TesteActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
                    return;
                }

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(TesteActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();

                                    LatLng posicaoAtual = new LatLng(latitude, longitude);
                                    mMap.clear();
                                    mMap.addMarker(new MarkerOptions().position(posicaoAtual).title("Você está aqui"));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicaoAtual, 16));
                                } else {
                                    Toast.makeText(TesteActivity.this, "Localização não disponível", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng fecap = new LatLng(-23.5572348, -46.6369578);
        mMap.addMarker(new MarkerOptions().position(fecap).title("Marcador na FECAP"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fecap, 17));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getRoute(LatLng origem, LatLng destino) {
        String apiKey = "AIzaSyCd-aAutuNpkOv1LOtYDFxJBJT5ATt7Oeo"; // Substitua por sua chave
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origem.latitude + "," + origem.longitude +
                "&destination=" + destino.latitude + "," + destino.longitude +
                "&key=" + apiKey;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                final String jsonData = response.body().string();
                try {
                    JSONObject json = new JSONObject(jsonData);
                    JSONArray routes = json.getJSONArray("routes");
                    if (routes.length() == 0) return;

                    JSONObject route = routes.getJSONObject(0);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                    String encodedPoints = overviewPolyline.getString("points");

                    final List<LatLng> decodedPath = decodePolyline(encodedPoints);

                    runOnUiThread(() -> mMap.addPolyline(new PolylineOptions()
                            .addAll(decodedPath)
                            .color(Color.BLUE)
                            .width(10)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lng += dlng;

            LatLng p = new LatLng(lat / 1E5, lng / 1E5);
            poly.add(p);
        }

        return poly;
    }
}
