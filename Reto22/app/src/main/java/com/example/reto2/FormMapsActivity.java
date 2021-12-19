package com.example.reto2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reto2.datos.DBHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.reto2.databinding.ActivityFormMapsBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FormMapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private final int REQUEST_CODE_GALLERY = 999;
    private Button insertSuc, getSuc, deleteSuc, updateSuc, chooseSuc;
    private EditText id, name, address;
    private ImageView imgSelectedSuc;
    private TextView location;

    private DBHelper dbHelper;
    private GoogleMap mMap;
    private ActivityFormMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFormMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        insertSuc = (Button) findViewById(R.id.btnInsertarSucursal);
        deleteSuc = (Button) findViewById(R.id.btnEliminarSucursal);
        getSuc = (Button) findViewById(R.id.btnConsultarSucursal);
        updateSuc = (Button) findViewById(R.id.btnActualizarSucursal);
        chooseSuc = (Button) findViewById(R.id.btnChooseSucursal);

        imgSelectedSuc = (ImageView) findViewById(R.id.imgSelectedSucursal);
        id = (EditText) findViewById(R.id.edtIdSucursal);
        name = (EditText) findViewById(R.id.edtNameSucursal);
        address = (EditText) findViewById(R.id.edtAddressSucursal);
        location = (TextView) findViewById(R.id.tvLocationSucursal);

        dbHelper = new DBHelper(getApplicationContext());

        chooseSuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(
                        FormMapsActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
            }
        });

        insertSuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idInsert = id.getText().toString();
                String nameInsert = name.getText().toString();
                String addressInsert = address.getText().toString();
                String locationInsert = location.getText().toString();
                byte[] imageInsert = imageViewToByte(imgSelectedSuc);
                dbHelper.insertData(nameInsert, addressInsert, locationInsert, imageInsert, "SUCURSALES");
            }
        });

        getSuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = dbHelper.getDataById(name.getText().toString(), id.getText().toString().trim());
                while (cursor.moveToNext()) {
                    id.setText(cursor.getString(1));
                    name.setText(cursor.getString(2));
                    address.setText(cursor.getString(3));
                    location.setText(cursor.getString(4));
                    byte[] img = cursor.getBlob(5);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                    imgSelectedSuc.setImageBitmap(bitmap);
                }
            }
        });

        deleteSuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = findViewById(R.id.linearLayoutForm);
                dbHelper.deleteDataById(name.getText().toString().trim(), id.getText().toString().trim());
                Snackbar.make(view, "Eliminado", Snackbar.LENGTH_SHORT).show();
            }
        });

        updateSuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dbHelper.updateProductoById(
                            id.getText().toString(),
                            name,
                            address,
                            location,
                            imgSelectedSuc
                    );
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        int zoom = 5;
        LatLng bogota = new LatLng(3.52, -72);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bogota,zoom));

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                location.setText(latLng.latitude+","+latLng.longitude);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                googleMap.clear();
                googleMap.addMarker(markerOptions);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }else{
                Toast.makeText(getApplicationContext(), "Sin Permisos", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imgSelectedSuc.setImageBitmap(bitmap);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public byte[] imageViewToByte(ImageView imageView){
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

}