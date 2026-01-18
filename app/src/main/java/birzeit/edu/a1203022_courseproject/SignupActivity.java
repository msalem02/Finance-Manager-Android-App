package birzeit.edu.a1203022_courseproject;


import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextFirstName, editTextLastName,editTextPassword, editTextConfirmPassword;

    private AppDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        dbHelper = new AppDatabaseHelper(this);
        editTextEmail=findViewById(R.id.editTextSignUpEmail);
        editTextFirstName=findViewById(R.id.editTextFirstName);
        editTextLastName=findViewById(R.id.editTextLastName);
        editTextPassword=findViewById(R.id.editTextSignUpPassword);
        editTextConfirmPassword=findViewById(R.id.editTextConfirmPassword);
        Button buttonCreate=findViewById(R.id.buttonCreateAccount);

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignUp();
            }
        });
    }

    private void handleSignUp() {
        String email=editTextEmail.getText().toString().trim();
        String first=editTextFirstName.getText().toString().trim();
        String last=editTextLastName.getText().toString().trim();
        String pass=editTextPassword.getText().toString();
        String confirm=editTextConfirmPassword.getText().toString();

        // Email
        if (!isValidEmail(email)) {
            editTextEmail.setError("Invalid email");
            return;
        }

        // First name 3–10
        if (first.length() < 3 || first.length() > 10) {
            editTextFirstName.setError("3 to 10 characters");
            return;
        }

        // Last name 3–10
        if (last.length() < 3 || last.length() > 10) {
            editTextLastName.setError("3 to 10 characters");
            return;
        }

        // Password rules
        if (!isValidPassword(pass)) {
            editTextPassword.setError("6-12 chars, 1 upper, 1 lower, 1 digit");
            return;
        }

        // Confirm password
        if (!pass.equals(confirm)) {
            editTextConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Check if email already exists
        if (dbHelper.getUserByEmail(email) != null) {
            editTextEmail.setError("Email already registered");
            return;
        }

        User user=new User(email, first, last, pass);
        boolean ok=dbHelper.registerUser(user);

        if (ok) {
            Toast.makeText(this, "Account created. You can sign in now.", Toast.LENGTH_SHORT).show();
            finish(); // go back to login
        } else {
            Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 6 || password.length() > 12)
            return false;

        boolean hasUpper = false, hasLower = false, hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }
}
