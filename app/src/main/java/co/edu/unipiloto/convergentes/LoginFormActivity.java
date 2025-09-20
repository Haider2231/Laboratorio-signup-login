package co.edu.unipiloto.convergentes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginFormActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnGoRegister = findViewById(R.id.btnGoRegisterFromLogin);

        btnLogin.setOnClickListener(v -> tryLogin());
        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, SignupFormActivity.class)));
    }

    private void tryLogin() {
        String email = safe(etEmail);
        String pass  = safe(etPassword);

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido"); return;
        }
        if (TextUtils.isEmpty(pass)) {
            etPassword.setError("Ingrese la contraseña"); return;
        }

        // Demo: Acepta cualquier combinación válida de formato.
        Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show();

        // Regresar al Main o continuar a otra Activity (demo)
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private String safe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
