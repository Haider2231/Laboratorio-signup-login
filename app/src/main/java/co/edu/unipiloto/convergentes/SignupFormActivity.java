package co.edu.unipiloto.convergentes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignupFormActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword;
    private RadioGroup rgGender;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_form);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        rgGender = findViewById(R.id.rgGender);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        MaterialButton btnGoLogin = findViewById(R.id.btnGoLoginFromSignup);

        btnRegister.setOnClickListener(v -> tryRegister());
        btnGoLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginFormActivity.class)));
    }

    private void tryRegister() {
        String name = safe(etName);
        String email = safe(etEmail);
        String pass = safe(etPassword);

        if (TextUtils.isEmpty(name)) {
            etName.setError("Requerido"); return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido"); return;
        }
        if (pass.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres"); return;
        }
        int checked = rgGender.getCheckedRadioButtonId();
        if (checked == -1) {
            Toast.makeText(this, "Seleccione género", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rb = findViewById(checked);
        String gender = rb.getText().toString();

        // Aquí solo mostramos un resumen (en un proyecto real guardarías en DB/API).
        String msg = "Registro OK:\n" + name + "\n" + email + "\n" + gender;
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        // Ir a Login para que pruebe el acceso (solo demo)
        startActivity(new Intent(this, LoginFormActivity.class));
        finish();
    }

    private String safe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
