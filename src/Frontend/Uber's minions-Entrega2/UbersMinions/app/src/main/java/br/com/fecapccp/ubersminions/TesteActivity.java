package br.com.fecapccp.ubersminions;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.util.List;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.tasks.OnSuccessListener;

public class TesteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText endereco;
    private Button pesquisar;
    private Button localAtual;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste);

        endereco = findViewById(R.id.etEndereco);
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
                String local = endereco.getText().toString();
                if (!local.isEmpty()) {
                    pesquisarLocal(local);
                }
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        localAtual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verifica permissão
                if (ActivityCompat.checkSelfPermission(TesteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(TesteActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(TesteActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
                    return;
                }

                // Pega a localização atual
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate(); // Reinicia a Activity para pegar a localização
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
            }
        }
    }



    // Por padrão vai mostrar a FECAP
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng fecap = new LatLng(-23.5572348, -46.6369578);
        mMap.addMarker(new MarkerOptions().position(fecap).title("Marcador na FECAP"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fecap, 17));
    }


    private void pesquisarLocal(String local) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> listaEndereco = geocoder.getFromLocationName(local, 1);
            if (listaEndereco != null && listaEndereco.size() > 0) {
                Address endereco = listaEndereco.get(0);
                LatLng latLng = new LatLng(endereco.getLatitude(), endereco.getLongitude());

                // Limpar marcadores anteriores
                mMap.clear();

                // Adicionar marcador no local pesquisado
                mMap.addMarker(new MarkerOptions().position(latLng).title(local));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {
                Toast.makeText(this, "Local não encontrado", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao buscar localização", Toast.LENGTH_SHORT).show();
        }
    }
}
