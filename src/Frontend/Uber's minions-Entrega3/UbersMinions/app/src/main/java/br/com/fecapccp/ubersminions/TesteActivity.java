package br.com.fecapccp.ubersminions;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
    private Button localAtual;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private Handler handler = new Handler();
    private Runnable rotaAutoRunnable;
    private static final int INTERVALO_ATUALIZACAO = 10000; // 10 segundos
    private boolean cameraInicializada = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste);

        endereco = findViewById(R.id.etEndereco);
        destino = findViewById(R.id.etDestino);
        localAtual = findViewById(R.id.btnComecar);
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

                                    // (Opcional) Atualiza campo de origem com o endereço
                                    Geocoder geocoder = new Geocoder(TesteActivity.this);
                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                        if (!addresses.isEmpty()) {
                                            endereco.setText(addresses.get(0).getAddressLine(0));
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    // Marca no mapa
                                    mMap.clear();
                                    mMap.addMarker(new MarkerOptions().position(posicaoAtual).title("Você está aqui"));
                                    if (!cameraInicializada) {
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicaoAtual, 14));
                                        cameraInicializada = true;
                                    }

                                    // Se o destino já estiver preenchido, traça a rota
                                    String destinoStr = destino.getText().toString();
                                    if (!destinoStr.isEmpty()) {
                                        try {
                                            List<Address> destinoLista = geocoder.getFromLocationName(destinoStr, 1);
                                            if (destinoLista != null && !destinoLista.isEmpty()) {
                                                LatLng destinoLatLng = new LatLng(destinoLista.get(0).getLatitude(), destinoLista.get(0).getLongitude());
                                                mMap.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino"));

                                                getRoute(posicaoAtual, destinoLatLng);
                                                iniciarAtualizacaoAutomaticaDeRota();
                                            } else {
                                                Toast.makeText(TesteActivity.this, "Destino não encontrado", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Toast.makeText(TesteActivity.this, "Erro ao buscar destino", Toast.LENGTH_SHORT).show();
                                        }
                                    }

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

    private void iniciarAtualizacaoAutomaticaDeRota() {
        rotaAutoRunnable = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(TesteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(TesteActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Permissão ainda não concedida
                    return;
                }

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(TesteActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    LatLng posicaoAtual = new LatLng(location.getLatitude(), location.getLongitude());
                                    String destinoStr = destino.getText().toString();

                                    if (!destinoStr.isEmpty()) {
                                        Geocoder geocoder = new Geocoder(TesteActivity.this);
                                        try {
                                            List<Address> destinoLista = geocoder.getFromLocationName(destinoStr, 1);
                                            if (destinoLista != null && !destinoLista.isEmpty()) {
                                                LatLng destinoLatLng = new LatLng(destinoLista.get(0).getLatitude(), destinoLista.get(0).getLongitude());

                                                mMap.clear();
                                                mMap.addMarker(new MarkerOptions().position(posicaoAtual).title("Você está aqui"));
                                                mMap.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino"));
                                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicaoAtual, 14));

                                                getRoute(posicaoAtual, destinoLatLng);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });

                // Agendar próxima execução
                handler.postDelayed(this, INTERVALO_ATUALIZACAO);
            }
        };

        handler.post(rotaAutoRunnable); // Começa agora
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
