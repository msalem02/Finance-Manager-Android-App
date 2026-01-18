package birzeit.edu.a1203022_courseproject;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;



public class ProfileFragment extends Fragment {
    private TextView textEmail;
    private EditText editFirstName,editLastName,editPassword,editConfirmPassword;
    private Button buttonSave;
    private AppDatabaseHelper dbHelper;
    private SharedPrefManager prefManager;
    private String currentUserEmail;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_profile, container, false);
        dbHelper=new AppDatabaseHelper(requireContext());
        prefManager=SharedPrefManager.getInstance(requireContext());
        currentUserEmail=prefManager.readString(SharedPrefManager.KEY_CURRENT_USER, "");
        textEmail=view.findViewById(R.id.textViewProfileEmail);
        editFirstName=view.findViewById(R.id.editTextProfileFirstName);
        editLastName=view.findViewById(R.id.editTextProfileLastName);
        editPassword=view.findViewById(R.id.editTextProfilePassword);
        editConfirmPassword=view.findViewById(R.id.editTextProfileConfirmPassword);
        buttonSave=view.findViewById(R.id.buttonSaveProfile);
        loadUserInfo();
        buttonSave.setOnClickListener(v -> saveProfile());
        return view;
    }

    private void loadUserInfo() {
        if (TextUtils.isEmpty(currentUserEmail)) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUser = dbHelper.getUserByEmail(currentUserEmail);
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        textEmail.setText(currentUser.getEmail());
        editFirstName.setText(currentUser.getFirstName());
        editLastName.setText(currentUser.getLastName());
    }

    private void saveProfile() {
        if (currentUser == null) return;
        String first=editFirstName.getText().toString().trim();
        String last=editLastName.getText().toString().trim();
        String pass= editPassword.getText().toString();
        String confirm=editConfirmPassword.getText().toString();

        // validate names 3–10 chars
        if (first.length() < 3 || first.length() > 10) {
            editFirstName.setError("3 to 10 characters");
            return;
        }
        if (last.length() < 3 || last.length() > 10) {
            editLastName.setError("3 to 10 characters");
            return;
        }

        // If user typed a password validate it else keep old one
        String finalPassword = currentUser.getPassword();
        if (!TextUtils.isEmpty(pass) || !TextUtils.isEmpty(confirm)) {
            if (!pass.equals(confirm)) {
                editConfirmPassword.setError("Passwords do not match");
                return;
            }
            if (!isValidPassword(pass)) {
                editPassword.setError("6-12 chars, 1 upper, 1 lower, 1 digit");
                return;
            }
            finalPassword = pass;
        }

        currentUser.setFirstName(first);
        currentUser.setLastName(last);
        currentUser.setPassword(finalPassword);
        int rows=dbHelper.updateUser(currentUser);
        if (rows > 0) {
            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
            // clear password fields
            editPassword.setText("");
            editConfirmPassword.setText("");
        } else {
            Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 6 || password.length() > 12)
            return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c))    hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }
}
