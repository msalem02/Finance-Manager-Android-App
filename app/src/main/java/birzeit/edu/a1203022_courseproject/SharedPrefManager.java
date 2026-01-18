package birzeit.edu.a1203022_courseproject;


import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String SHARED_PREF_NAME="FinanceAppPrefs";
    private static final int    SHARED_PREF_MODE=Context.MODE_PRIVATE;
    public static final String KEY_REMEMBER_EMAIL="remember_email";
    public static final String KEY_CURRENT_USER="current_user_email";
    public static final String KEY_THEME="theme";
    public static final String KEY_DEFAULT_PERIOD="default_period";
    private static SharedPrefManager instance;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private SharedPrefManager(Context context) {
        sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME, SHARED_PREF_MODE);
        editor=sharedPreferences.edit();
    }

    public static SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance=new SharedPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    public void writeString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String readString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void clearKey(String key) {
        editor.remove(key);
        editor.apply();
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}
