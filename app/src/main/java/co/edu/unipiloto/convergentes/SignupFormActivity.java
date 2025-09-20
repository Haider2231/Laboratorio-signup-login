package co.edu.unipiloto.convergentes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;



import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SignupFormActivity extends AppCompatActivity {

    private static final int REQ_FINE_LOCATION = 1001;

    private TextInputEditText etName, etUsername, etEmail, etPassword, etConfirm, etDob, etAddress, etLat, etLng;
    private RadioGroup rgGender;
    private Spinner spRole;
    private FusedLocationProviderClient fused;
    private TextInputLayout tilAddress;


    private Long dobMillis = null; // almacena selección del date picker

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_form);

        // Inputs
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        etDob = findViewById(R.id.etDob);
        etAddress = findViewById(R.id.etAddress);

        tilAddress = findViewById(R.id.tilAddress);
        etLat = findViewById(R.id.etLat);
        etLng = findViewById(R.id.etLng);

        rgGender = findViewById(R.id.rgGender);
        spRole = findViewById(R.id.spRole);

        // Spinner roles (si no usas entries en XML)
        if (spRole.getAdapter() == null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    this, R.array.roles, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spRole.setAdapter(adapter);
        }

        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        MaterialButton btnGoLogin = findViewById(R.id.btnGoLoginFromSignup);
        MaterialButton btnGetLocation = findViewById(R.id.btnGetLocation);
        tilAddress.setEndIconOnClickListener(v -> geocodeAddress());
        // DatePicker (solo fechas pasadas)
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Fecha de nacimiento")
                .setCalendarConstraints(constraints)
                // opcional: selección inicial en hoy (UTC)
                // .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();


        etDob.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "dob"));
        datePicker.addOnPositiveButtonClickListener(selection -> {
            dobMillis = selection;
            etDob.setText(datePicker.getHeaderText()); // o formatear a tu gusto
        });

        // Geolocalización
        fused = LocationServices.getFusedLocationProviderClient(this);
        btnGetLocation.setOnClickListener(v -> getLastLocation());

        // Acciones
        btnRegister.setOnClickListener(v -> tryRegister());
        btnGoLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginFormActivity.class)));
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_FINE_LOCATION);
            return;
        }
        fused.getLastLocation().addOnSuccessListener(this, loc -> {
            if (loc != null) {
                etLat.setText(String.valueOf(loc.getLatitude()));
                etLng.setText(String.valueOf(loc.getLongitude()));
                reverseGeocode(loc.getLatitude(), loc.getLongitude());  // ← añade esto
            } else {
                Toast.makeText(this, "No se pudo obtener ubicación. Intenta al aire libre.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error de ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void geocodeAddress() {
        String addr = safe(etAddress);
        if (TextUtils.isEmpty(addr)) {
            etAddress.setError("Ingrese una dirección");
            return;
        }
        if (!Geocoder.isPresent()) {
            Toast.makeText(this, "Geocoder no disponible en este dispositivo", Toast.LENGTH_SHORT).show();
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> res = geocoder.getFromLocationName(addr, 1);
                runOnUiThread(() -> {
                    if (res != null && !res.isEmpty()) {
                        Address a = res.get(0);
                        etLat.setText(String.valueOf(a.getLatitude()));
                        etLng.setText(String.valueOf(a.getLongitude()));
                        etAddress.setError(null);
                        Toast.makeText(this, "Coordenadas obtenidas", Toast.LENGTH_SHORT).show();
                    } else {
                        etAddress.setError("No se encontró esa dirección");
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error geocodificando: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void reverseGeocode(double lat, double lng) {
        if (!Geocoder.isPresent()) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> res = geocoder.getFromLocation(lat, lng, 1);
                runOnUiThread(() -> {
                    if (res != null && !res.isEmpty()) {
                        Address a = res.get(0);
                        // Construye una dirección legible
                        String line = a.getAddressLine(0); // suele ser suficiente
                        etAddress.setText(line != null ? line : (a.getLocality() + ", " + a.getCountryName()));
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error obteniendo dirección: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }


    private void tryRegister() {
        String name = safe(etName);
        String user = safe(etUsername);
        String email = safe(etEmail);
        String pass = safe(etPassword);
        String confirm = safe(etConfirm);
        String address = safe(etAddress);
        String latStr = safe(etLat);
        String lngStr = safe(etLng);

        // Validaciones básicas
        if (TextUtils.isEmpty(name)) { etName.setError("Requerido"); return; }
        if (TextUtils.isEmpty(user)) { etUsername.setError("Requerido"); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido"); return;
        }
        if (pass.length() < 6) { etPassword.setError("Mínimo 6 caracteres"); return; }
        if (!pass.equals(confirm)) { etConfirm.setError("Las contraseñas no coinciden"); return; }

        // Fecha de nacimiento y edad >= 18
        if (dobMillis == null) {
            etDob.setError("Seleccione su fecha de nacimiento");
            return;
        }
        int age = yearsFrom(dobMillis);
        if (age < 18) {
            etDob.setError("Debe ser mayor de 18 años");
            Toast.makeText(this, "Usuario menor de edad: no se puede activar", Toast.LENGTH_LONG).show();
            return;
        } else {
            etDob.setError(null);
        }

        // Dirección: al menos una opción: (a) texto estructurado o (b) lat/lng
        boolean hasAddressText = !TextUtils.isEmpty(address);
        boolean hasLatLng = !TextUtils.isEmpty(latStr) && !TextUtils.isEmpty(lngStr);
        if (!hasAddressText && !hasLatLng) {
            etAddress.setError("Ingrese dirección o use geolocalización");
            Toast.makeText(this, "Complete dirección (texto) o lat/lng", Toast.LENGTH_SHORT).show();
            return;
        }
        etAddress.setError(null);

        // Género
        int checked = rgGender.getCheckedRadioButtonId();
        if (checked == -1) {
            Toast.makeText(this, "Seleccione género", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rb = findViewById(checked);
        String gender = rb.getText().toString();

        // Rol
        String role = spRole.getSelectedItem() != null ? spRole.getSelectedItem().toString() : "N/A";

        // *** Aquí guardarías en DB/API ***
        String msg = "Registro OK:\n" +
                "Nombre: " + name + "\n" +
                "Usuario: " + user + "\n" +
                "Email: " + email + "\n" +
                "Rol: " + role + "\n" +
                "Género: " + gender + "\n" +
                (hasAddressText ? "Dirección: " + address + "\n" : "") +
                (hasLatLng ? ("Lat: " + latStr + "  Lng: " + lngStr + "\n") : "") +
                "Edad: " + age;
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        // Continuar (demo)
        startActivity(new Intent(this, LoginFormActivity.class));
        finish();
    }

    private int yearsFrom(long millis) {
        Calendar today = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(millis);

        int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            years--; // aún no cumple años este año
        }
        return years;
    }

    private String safe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
