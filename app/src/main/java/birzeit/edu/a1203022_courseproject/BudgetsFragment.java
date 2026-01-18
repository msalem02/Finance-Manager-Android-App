package birzeit.edu.a1203022_courseproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class BudgetsFragment extends Fragment {
    private Spinner spinnerCategory, spinnerMonth, spinnerYear;
    private EditText editTextAmount;
    private RecyclerView recyclerView;
    private AppDatabaseHelper dbHelper;
    private SharedPrefManager prefManager;
    private String currentUserEmail;
    private List<Category> expenseCategories=new ArrayList<>();
    private List<Budget> budgetsList=new ArrayList<>();
    private BudgetsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budgets, container, false);
        dbHelper = new AppDatabaseHelper(requireContext());
        prefManager = SharedPrefManager.getInstance(requireContext());
        currentUserEmail = prefManager.readString(SharedPrefManager.KEY_CURRENT_USER, "");
        spinnerCategory = view.findViewById(R.id.spinnerBudgetCategory);
        spinnerMonth=view.findViewById(R.id.spinnerBudgetMonth);
        spinnerYear=view.findViewById(R.id.spinnerBudgetYear);
        editTextAmount = view.findViewById(R.id.editTextBudgetAmount);
        recyclerView= view.findViewById(R.id.recyclerViewBudgets);
        Button buttonSave=view.findViewById(R.id.buttonSaveBudget);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter=new BudgetsAdapter(budgetsList);
        recyclerView.setAdapter(adapter);
        setupMonthYearSpinners();
        loadExpenseCategories();
        loadBudgetsForSelectedMonthYear();
        buttonSave.setOnClickListener(v -> saveBudget());
        return view;
    }

    private void setupMonthYearSpinners() {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun", "Jul","Aug","Sep","Oct","Nov","Dec"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear - 1; y <= currentYear + 1; y++) {
            years.add(y);
        }
        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // default: current month/year
        spinnerMonth.setSelection(cal.get(Calendar.MONTH));
        int indexYear = years.indexOf(currentYear);
        if (indexYear >= 0) spinnerYear.setSelection(indexYear);
    }

    private void loadExpenseCategories() {
        if (TextUtils.isEmpty(currentUserEmail)) return;
        expenseCategories = dbHelper.getCategories(currentUserEmail, AppDatabaseHelper.TYPE_EXPENSE);

        if (expenseCategories.isEmpty()) {
            dbHelper.insertCategory(currentUserEmail, "Food", AppDatabaseHelper.TYPE_EXPENSE);
            dbHelper.insertCategory(currentUserEmail, "Bills", AppDatabaseHelper.TYPE_EXPENSE);
            dbHelper.insertCategory(currentUserEmail, "Rent", AppDatabaseHelper.TYPE_EXPENSE);
            dbHelper.insertCategory(currentUserEmail, "Other", AppDatabaseHelper.TYPE_EXPENSE);
            expenseCategories = dbHelper.getCategories(
                    currentUserEmail, AppDatabaseHelper.TYPE_EXPENSE);
        }

        ArrayAdapter<Category> adapterCat = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, expenseCategories);
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterCat);
    }

    private int getSelectedMonthNumber() {
        return spinnerMonth.getSelectedItemPosition() + 1; // 1–12
    }
    private int getSelectedYear() {
        return (int) spinnerYear.getSelectedItem();
    }
    private void loadBudgetsForSelectedMonthYear() {
        if (TextUtils.isEmpty(currentUserEmail)) return;
        int month= getSelectedMonthNumber();
        int year= getSelectedYear();
        budgetsList.clear();
        budgetsList.addAll(dbHelper.getBudgetsForUserMonth(currentUserEmail, month, year));

        // compute spent for each budget
        for (Budget b : budgetsList) {
            double spent = dbHelper.getTotalExpenseForCategoryInMonth(currentUserEmail, b.getCategoryId(), month, year);
            b.setSpent(spent);
        }
        adapter.notifyDataSetChanged();
    }

    private void saveBudget() {
        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Add at least one expense category", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = editTextAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            editTextAmount.setError("Enter budget amount");
            return;
        }

        double limit;
        try {
            limit = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            editTextAmount.setError("Invalid number");
            return;
        }

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        long catId= selectedCategory.getId();
        int month= getSelectedMonthNumber();
        int year= getSelectedYear();
        long existingId = dbHelper.getBudgetId(currentUserEmail, catId, month, year);
        if (existingId == -1) {
            dbHelper.insertBudget(currentUserEmail, catId, month, year, limit);
            Toast.makeText(getContext(), "Budget saved", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateBudgetAmount(existingId, limit);
            Toast.makeText(getContext(), "Budget updated", Toast.LENGTH_SHORT).show();
        }
        editTextAmount.setText("");
        loadBudgetsForSelectedMonthYear();
    }


    private class BudgetsAdapter extends RecyclerView.Adapter<BudgetsAdapter.BudgetViewHolder> {
        private final List<Budget> items;
        BudgetsAdapter(List<Budget> items) {
            this.items = items;
        }
        @NonNull
        @Override
        public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new BudgetViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
            Budget b = items.get(position);
            double spent = b.getSpent();
            double limit = b.getLimit();
            double percent = (limit > 0) ? (spent / limit * 100.0) : 0.0;

            String monthYear = String.format("(%02d/%d)", b.getMonth(), b.getYear());
            String line1 = b.getCategoryName() + " " + monthYear;
            String line2 = String.format("Spent %.2f of %.2f (%.0f%%)", spent, limit, percent);
            holder.text1.setText(line1);
            holder.text2.setText(line2);

            if (percent >= 100.0) {
                // Over budget: red
                holder.text2.setTextColor(Color.parseColor("#D32F2F")); // red
            } else if (percent >= 50.0) {
                // 50–99%: orange / warning
                holder.text2.setTextColor(Color.parseColor("#F57C00")); // orange
            } else {
                // Below 50%: normal dark gray
                holder.text2.setTextColor(Color.parseColor("#FF424242"));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
        class BudgetViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            BudgetViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
