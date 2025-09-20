package co.edu.unipiloto.convergentes;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton btnGoLogin = findViewById(R.id.btnGoLogin);
        MaterialButton btnGoRegister = findViewById(R.id.btnGoRegister);

        btnGoLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginFormActivity.class)));

        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, SignupFormActivity.class)));
    }
}
