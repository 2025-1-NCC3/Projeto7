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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

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
    private static final int INTERVALO_ATUALIZACAO = 6000; // 6 segundos
    private boolean cameraInicializada = false;

    private Marker marcadorOrigem;
    private Marker marcadorDestino;
    private Polyline rotaPolyline;

    private LatLng acidenteLatLng = null;
    private LatLng ultimaLocalizacao = null; // <- NOVO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste);

        endereco = findViewById(R.id.etEndereco);
        destino = findViewById(R.id.etDestino);
        localAtual = findViewById(R.id.btnComecar);
        Button btnSinalizarAcidente = findViewById(R.id.btnSinalizarAcidente);
        Button btnVerificarAcidente = findViewById(R.id.btnRecalcularRota);
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

        localAtual.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
                return;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng posicaoAtual = new LatLng(latitude, longitude);
                    ultimaLocalizacao = posicaoAtual; // <- ARMAZENA POSIÇÃO INICIAL

                    Geocoder geocoder = new Geocoder(this);
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (!addresses.isEmpty()) {
                            endereco.setText(addresses.get(0).getAddressLine(0));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (!cameraInicializada) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicaoAtual, 14));
                        cameraInicializada = true;
                    }

                    String destinoStr = destino.getText().toString();
                    if (!destinoStr.isEmpty()) {
                        try {
                            List<Address> destinoLista = geocoder.getFromLocationName(destinoStr, 1);
                            if (destinoLista != null && !destinoLista.isEmpty()) {
                                LatLng destinoLatLng = new LatLng(destinoLista.get(0).getLatitude(), destinoLista.get(0).getLongitude());

                                if (marcadorOrigem != null) marcadorOrigem.remove();
                                if (marcadorDestino != null) marcadorDestino.remove();

                                marcadorOrigem = mMap.addMarker(new MarkerOptions().position(posicaoAtual).title("Você está aqui"));
                                marcadorDestino = mMap.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino"));

                                getRoute(posicaoAtual, destinoLatLng);
                                iniciarAtualizacaoAutomaticaDeRota();
                            } else {
                                Toast.makeText(this, "Destino não encontrado", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Erro ao buscar destino", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(this, "Localização não disponível", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnSinalizarAcidente.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
                return;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    new AlertDialog.Builder(this)
                            .setTitle("Confirmar sinalização de acidente")
                            .setMessage("Deseja sinalizar um acidente nas coordenadas:\nLatitude: " + lat + "\nLongitude: " + lng)
                            .setPositiveButton("Sim", (dialog, which) -> {
                                acidenteLatLng = new LatLng(lat, lng);

                                mMap.addMarker(new MarkerOptions()
                                        .position(acidenteLatLng)
                                        .title("Acidente sinalizado")
                                        .snippet("Usuário marcou um acidente aqui")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.acidente)));

                                Toast.makeText(this, "Acidente sinalizado!", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                } else {
                    Toast.makeText(this, "Localização não disponível", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnVerificarAcidente.setOnClickListener(v -> {
            if (acidenteLatLng == null) {
                Toast.makeText(this, "Nenhum acidente sinalizado.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Recalculando rota evitando o acidente...", Toast.LENGTH_SHORT).show();
            LatLng novaOrigem = new LatLng(acidenteLatLng.latitude + 0.001, acidenteLatLng.longitude);

            String destinoStr = destino.getText().toString();
            if (!destinoStr.isEmpty()) {
                Geocoder geocoder = new Geocoder(this);
                try {
                    List<Address> destinoLista = geocoder.getFromLocationName(destinoStr, 1);
                    if (destinoLista != null && !destinoLista.isEmpty()) {
                        LatLng destinoLatLng = new LatLng(destinoLista.get(0).getLatitude(), destinoLista.get(0).getLongitude());

                        if (marcadorOrigem != null) marcadorOrigem.remove();
                        if (marcadorDestino != null) marcadorDestino.remove();

                        marcadorOrigem = mMap.addMarker(new MarkerOptions().position(novaOrigem).title("Nova origem (evitando acidente)"));
                        marcadorDestino = mMap.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino"));

                        getRoute(novaOrigem, destinoLatLng);
                        ultimaLocalizacao = novaOrigem; // <- Atualiza última localização
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                    return;
                }

                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng localAtualLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                        boolean estaParado = ultimaLocalizacao != null && calcularDistancia(localAtualLatLng, ultimaLocalizacao) < 10.0; // 10 metros

                        LatLng origemUsada;
                        if (estaParado && acidenteLatLng != null &&
                                calcularDistancia(localAtualLatLng, acidenteLatLng) < 20.0) {
                            origemUsada = new LatLng(acidenteLatLng.latitude + 0.001, acidenteLatLng.longitude);
                        } else {
                            origemUsada = localAtualLatLng;
                        }

                        ultimaLocalizacao = localAtualLatLng;

                        String destinoStr = destino.getText().toString();
                        if (!destinoStr.isEmpty()) {
                            Geocoder geocoder = new Geocoder(TesteActivity.this);
                            try {
                                List<Address> destinoLista = geocoder.getFromLocationName(destinoStr, 1);
                                if (destinoLista != null && !destinoLista.isEmpty()) {
                                    LatLng destinoLatLng = new LatLng(destinoLista.get(0).getLatitude(), destinoLista.get(0).getLongitude());

                                    if (marcadorOrigem != null) marcadorOrigem.remove();
                                    if (marcadorDestino != null) marcadorDestino.remove();

                                    marcadorOrigem = mMap.addMarker(new MarkerOptions().position(origemUsada).title("Você"));
                                    marcadorDestino = mMap.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino"));

                                    getRoute(origemUsada, destinoLatLng);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                handler.postDelayed(this, INTERVALO_ATUALIZACAO);
            }
        };

        handler.post(rotaAutoRunnable);
    }

    private double calcularDistancia(LatLng a, LatLng b) {
        float[] results = new float[1];
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results);
        return results[0]; // distância em metros
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

                    runOnUiThread(() -> {
                        if (rotaPolyline != null) rotaPolyline.remove();

                        rotaPolyline = mMap.addPolyline(new PolylineOptions()
                                .addAll(decodedPath)
                                .color(Color.BLUE)
                                .width(10));
                    });
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

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }

        return poly;
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
}
