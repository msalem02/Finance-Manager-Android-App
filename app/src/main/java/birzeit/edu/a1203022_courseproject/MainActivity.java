package birzeit.edu.a1203022_courseproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private SharedPrefManager prefManager;
    private AppDatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Apply saved theme
        prefManager=SharedPrefManager.getInstance(this);
        String theme=prefManager.readString(SharedPrefManager.KEY_THEME, "LIGHT");
        if ("DARK".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout=findViewById(R.id.drawer_layout);
        navigationView=findViewById(R.id.nav_view);

        // DB helper for getting user info
        dbHelper = new AppDatabaseHelper(this);
        //Set header "Welcome (name)" and email
        View headerView = navigationView.getHeaderView(0);
        TextView headerWelcome=headerView.findViewById(R.id.textViewHeaderWelcome);
        TextView headerEmail=headerView.findViewById(R.id.textViewHeaderEmail);

        String currentEmail=prefManager.readString(SharedPrefManager.KEY_CURRENT_USER, "");
        if (currentEmail!=null && !currentEmail.isEmpty()) {
            headerEmail.setText(currentEmail);
        } else {
            headerEmail.setText("");
        }
        String welcomeName = "";
        if (currentEmail != null && !currentEmail.isEmpty()) {
            User user=dbHelper.getUserByEmail(currentEmail);
            if (user != null && user.getFirstName() != null) {
                welcomeName = user.getFirstName();
            }
        }

        if (welcomeName.isEmpty()) {
            headerWelcome.setText("Welcome");
        } else {
            headerWelcome.setText("Welcome " + welcomeName);
        }

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment selectedFragment = null;

        if (id == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (id == R.id.nav_income) {
            selectedFragment = new IncomeFragment();
        } else if (id == R.id.nav_expenses) {
            selectedFragment = new ExpensesFragment();
        } else if (id == R.id.nav_budgets) {
            selectedFragment = new BudgetsFragment();
        } else if (id == R.id.nav_settings) {
            selectedFragment = new SettingsFragment();
        } else if (id == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else if (id == R.id.nav_logout) {
            handleLogout();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleLogout() {
        prefManager.writeString(SharedPrefManager.KEY_CURRENT_USER, "");
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
