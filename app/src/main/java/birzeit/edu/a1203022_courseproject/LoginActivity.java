package birzeit.edu.a1203022_courseproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail,editTextPassword;
    private CheckBox checkBoxRemember;
    private AppDatabaseHelper dbHelper;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper=new AppDatabaseHelper(this);
        prefManager=SharedPrefManager.getInstance(this);
        editTextEmail=findViewById(R.id.editTextEmail);
        editTextPassword=findViewById(R.id.editTextPassword);
        checkBoxRemember=findViewById(R.id.checkBoxRemember);
        Button buttonSignIn=findViewById(R.id.buttonSignIn);
        Button buttonSignUp=findViewById(R.id.buttonSignUp);

        // fill email if "remember me" used
        String rememberedEmail = prefManager.readString(SharedPrefManager.KEY_REMEMBER_EMAIL, "");
        if (!TextUtils.isEmpty(rememberedEmail)) {
            editTextEmail.setText(rememberedEmail);
            checkBoxRemember.setChecked(true);
        }

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignIn();
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleSignIn() {
        String email=editTextEmail.getText().toString().trim();
        String password=editTextPassword.getText().toString();
        if (!isValidEmail(email)) {
            editTextEmail.setError("Enter a valid email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password required");
            return;
        }
        User user=dbHelper.getUserByEmail(email);
        if (user == null || !password.equals(user.getPassword())) {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remember-me logic
        if (checkBoxRemember.isChecked()) {
            prefManager.writeString(SharedPrefManager.KEY_REMEMBER_EMAIL, email);
        } else {
            prefManager.clearKey(SharedPrefManager.KEY_REMEMBER_EMAIL);
        }

        // Save current logged user
        prefManager.writeString(SharedPrefManager.KEY_CURRENT_USER, email);

        Toast.makeText(this, "Welcome " + user.getFirstName(), Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
