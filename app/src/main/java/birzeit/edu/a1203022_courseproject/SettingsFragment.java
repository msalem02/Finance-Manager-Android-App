package birzeit.edu.a1203022_courseproject;


import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;



public class SettingsFragment extends Fragment {
    private RadioGroup radioGroupTheme, radioGroupPeriod,radioGroupCategoryType;
    private RadioButton radioThemeLight, radioThemeDark;
    private RadioButton radioPeriodDay, radioPeriodWeek,radioPeriodMonth;
    private RadioButton radioCategoryIncome,radioCategoryExpense;
    private RecyclerView recyclerViewCategories;
    private EditText editTextNewCategory;
    private Button buttonAddCategory;
    private AppDatabaseHelper dbHelper;
    private SharedPrefManager prefManager;
    private String currentUserEmail;
    private String currentCategoryType=AppDatabaseHelper.TYPE_EXPENSE; // default
    private List<Category> categoryList=new ArrayList<>();
    private CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_settings, container, false);
        dbHelper=new AppDatabaseHelper(requireContext());
        prefManager=SharedPrefManager.getInstance(requireContext());
        currentUserEmail=prefManager.readString(SharedPrefManager.KEY_CURRENT_USER, "");
        radioGroupTheme=view.findViewById(R.id.radioGroupTheme);
        radioThemeLight=view.findViewById(R.id.radioThemeLight);
        radioThemeDark=view.findViewById(R.id.radioThemeDark);
        radioGroupPeriod=view.findViewById(R.id.radioGroupPeriod);
        radioPeriodDay=view.findViewById(R.id.radioPeriodDay);
        radioPeriodWeek=view.findViewById(R.id.radioPeriodWeek);
        radioPeriodMonth=view.findViewById(R.id.radioPeriodMonth);
        radioGroupCategoryType=view.findViewById(R.id.radioGroupCategoryType);
        radioCategoryIncome=view.findViewById(R.id.radioCategoryIncome);
        radioCategoryExpense=view.findViewById(R.id.radioCategoryExpense);
        recyclerViewCategories=view.findViewById(R.id.recyclerViewCategories);
        editTextNewCategory=view.findViewById(R.id.editTextNewCategory);
        buttonAddCategory=view.findViewById(R.id.buttonAddCategory);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryAdapter = new CategoryAdapter(categoryList);
        recyclerViewCategories.setAdapter(categoryAdapter);

        // Load saved settings
        loadThemeSetting();
        loadDefaultPeriodSetting();
        setupThemeListener();
        setupPeriodListener();

        // Category type
        radioCategoryExpense.setChecked(true); // default
        radioGroupCategoryType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCategoryIncome) {
                currentCategoryType=AppDatabaseHelper.TYPE_INCOME;
            } else {
                currentCategoryType=AppDatabaseHelper.TYPE_EXPENSE;
            }
            loadCategories();
        });

        buttonAddCategory.setOnClickListener(v -> addCategory());

        // load categories for default type
        loadCategories();
        return view;
    }


    private void loadThemeSetting() {
        String theme = prefManager.readString(SharedPrefManager.KEY_THEME, "LIGHT");
        if ("DARK".equals(theme)) {
            radioThemeDark.setChecked(true);
        } else {
            radioThemeLight.setChecked(true);
        }
    }

    private void setupThemeListener() {
        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String theme;
            if (checkedId==R.id.radioThemeDark) {
                theme = "DARK";
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                theme = "LIGHT";
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            prefManager.writeString(SharedPrefManager.KEY_THEME, theme);
            Toast.makeText(getContext(), "Theme saved", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadDefaultPeriodSetting() {
        String period = prefManager.readString(SharedPrefManager.KEY_DEFAULT_PERIOD, "MONTH");
        switch (period) {
            case "DAY":
                radioPeriodDay.setChecked(true);
                break;
            case "WEEK":
                radioPeriodWeek.setChecked(true);
                break;
            default:
                radioPeriodMonth.setChecked(true);
                break;
        }
    }

    private void setupPeriodListener() {
        radioGroupPeriod.setOnCheckedChangeListener((group, checkedId) -> {
            String period;
            if (checkedId == R.id.radioPeriodDay) {
                period = "DAY";
            } else if (checkedId == R.id.radioPeriodWeek) {
                period = "WEEK";
            } else {
                period = "MONTH";
            }
            prefManager.writeString(SharedPrefManager.KEY_DEFAULT_PERIOD, period);
            Toast.makeText(getContext(), "Default period saved", Toast.LENGTH_SHORT).show();
        });
    }


    private void loadCategories() {
        if (TextUtils.isEmpty(currentUserEmail)) {
            categoryList.clear();
            categoryAdapter.notifyDataSetChanged();
            return;
        }
        categoryList.clear();
        categoryList.addAll(dbHelper.getCategories(currentUserEmail, currentCategoryType));
        categoryAdapter.notifyDataSetChanged();
    }

    private void addCategory() {
        String name = editTextNewCategory.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            editTextNewCategory.setError("Enter category name");
            return;
        }
        if (TextUtils.isEmpty(currentUserEmail)) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        dbHelper.insertCategory(currentUserEmail, name, currentCategoryType);
        editTextNewCategory.setText("");
        loadCategories();
        Toast.makeText(getContext(), "Category added", Toast.LENGTH_SHORT).show();
    }


    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
        private final List<Category> items;
        CategoryAdapter(List<Category> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            return new CategoryViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            Category c = items.get(position);
            holder.textName.setText(c.getName());
            holder.buttonEdit.setOnClickListener(v -> showEditDialog(c));
            holder.buttonDelete.setOnClickListener(v -> {
                dbHelper.deleteCategory(c.getId());
                Toast.makeText(getContext(), "Category deleted", Toast.LENGTH_SHORT).show();
                loadCategories();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView textName;
            ImageButton buttonEdit, buttonDelete;
            CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                textName= itemView.findViewById(R.id.textViewCategoryName);
                buttonEdit=itemView.findViewById(R.id.buttonEditCategory);
                buttonDelete=itemView.findViewById(R.id.buttonDeleteCategory);
            }
        }
    }

    private void showEditDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit category");
        final EditText input = new EditText(requireContext());
        input.setText(category.getName());
        input.setSelection(category.getName().length());
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!TextUtils.isEmpty(newName)) {
                dbHelper.updateCategory(category.getId(), newName);
                Toast.makeText(getContext(), "Category updated", Toast.LENGTH_SHORT).show();
                loadCategories();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
