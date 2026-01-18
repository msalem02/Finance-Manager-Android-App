package birzeit.edu.a1203022_courseproject;

import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import androidx.core.content.ContextCompat;


public class IncomeFragment extends Fragment {
    private EditText editTextAmount, editTextDescription;
    private TextView textViewDate;
    private Spinner spinnerCategory;
    private RecyclerView recyclerView;
    private AppDatabaseHelper dbHelper;
    private SharedPrefManager prefManager;
    private String currentUserEmail;
    private List<Category> incomeCategories = new ArrayList<>();
    private List<TransactionRecord> incomeList = new ArrayList<>();
    private IncomeAdapter adapter;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_income, container, false);
        dbHelper=new AppDatabaseHelper(requireContext());
        prefManager=SharedPrefManager.getInstance(requireContext());
        currentUserEmail=prefManager.readString(SharedPrefManager.KEY_CURRENT_USER, "");
        editTextAmount=view.findViewById(R.id.editTextIncomeAmount);
        editTextDescription=view.findViewById(R.id.editTextIncomeDescription);
        textViewDate= view.findViewById(R.id.textViewIncomeDate);
        spinnerCategory=view.findViewById(R.id.spinnerIncomeCategory);
        recyclerView=view.findViewById(R.id.recyclerViewIncome);
        Button buttonAdd=view.findViewById(R.id.buttonAddIncome);
        textViewDate.setText(dateFormat.format(Calendar.getInstance().getTime()));
        textViewDate.setOnClickListener(v -> openDatePicker());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter=new IncomeAdapter(incomeList);
        recyclerView.setAdapter(adapter);
        loadCategories();
        loadIncomeTransactions();
        buttonAdd.setOnClickListener(v -> addIncome());
        return view;
    }

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(year, month, dayOfMonth);
                    textViewDate.setText(dateFormat.format(c.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void loadCategories() {
        if (TextUtils.isEmpty(currentUserEmail)) {
            return;
        }

        incomeCategories = dbHelper.getCategories(currentUserEmail, AppDatabaseHelper.TYPE_INCOME);
        if (incomeCategories.isEmpty()) {
            dbHelper.insertCategory(currentUserEmail, "Salary", AppDatabaseHelper.TYPE_INCOME);
            dbHelper.insertCategory(currentUserEmail, "Scholarship", AppDatabaseHelper.TYPE_INCOME);
            dbHelper.insertCategory(currentUserEmail, "Other", AppDatabaseHelper.TYPE_INCOME);
            incomeCategories = dbHelper.getCategories(currentUserEmail, AppDatabaseHelper.TYPE_INCOME);
        }

        ArrayAdapter<Category> spinnerAdapter =
                new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item,
                        incomeCategories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);
    }

    private void loadIncomeTransactions() {
        if (TextUtils.isEmpty(currentUserEmail)) return;
        incomeList.clear();
        incomeList.addAll(dbHelper.getTransactions(
                currentUserEmail, AppDatabaseHelper.TYPE_INCOME));
        adapter.notifyDataSetChanged();
    }

    private void addIncome() {
        String amountStr=editTextAmount.getText().toString().trim();
        String dateStr=textViewDate.getText().toString();
        String desc=editTextDescription.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            editTextAmount.setError("Amount required");
            return;
        }
        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please add a category first", Toast.LENGTH_SHORT).show();
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            editTextAmount.setError("Invalid amount");
            return;
        }
        Category selectedCategory=(Category) spinnerCategory.getSelectedItem();
        long catId = selectedCategory.getId();
        dbHelper.insertTransaction(currentUserEmail, AppDatabaseHelper.TYPE_INCOME, amount, dateStr, catId, desc);

        Toast.makeText(getContext(), "Income added", Toast.LENGTH_SHORT).show();
        editTextAmount.setText("");
        editTextDescription.setText("");
        loadIncomeTransactions();
    }

    // EDIT DIALOG
    private void showEditDialog(TransactionRecord tr) {
        AlertDialog.Builder builder =new AlertDialog.Builder(requireContext());
        View dialogView=LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_transaction, null, false);
        TextView textTitle=dialogView.findViewById(R.id.textViewEditTitle);
        TextView textDate=dialogView.findViewById(R.id.textViewEditDate);
        TextView textCategory=dialogView.findViewById(R.id.textViewEditCategory);
        EditText editAmount=dialogView.findViewById(R.id.editTextEditAmount);
        EditText editDesc=dialogView.findViewById(R.id.editTextEditDescription);
        int colorPrimary=ContextCompat.getColor(requireContext(), R.color.text_primary);
        int colorSecondary=ContextCompat.getColor(requireContext(), R.color.text_secondary);
        int colorHint=ContextCompat.getColor(requireContext(), R.color.text_hint);

        textTitle.setTextColor(colorPrimary);
        textDate.setTextColor(colorSecondary);
        textCategory.setTextColor(colorSecondary);
        editAmount.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_input_rounded));
        editAmount.setTextColor(colorPrimary);
        editAmount.setHintTextColor(colorHint);
        editDesc.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_input_rounded));
        editDesc.setTextColor(colorPrimary);
        editDesc.setHintTextColor(colorHint);

        textTitle.setText("Edit income");
        textDate.setText("Date: " + tr.getDate());
        textCategory.setText("Category: " + tr.getCategoryName());
        editAmount.setText(String.valueOf(tr.getAmount()));
        editDesc.setText(tr.getDescription() == null ? "" : tr.getDescription());
        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newAmountStr= editAmount.getText().toString().trim();
            String newDesc= editDesc.getText().toString().trim();
            if (TextUtils.isEmpty(newAmountStr)) {
                Toast.makeText(getContext(), "Amount required", Toast.LENGTH_SHORT).show();
                return;
            }
            double newAmount;
            try {
                newAmount = Double.parseDouble(newAmountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }
            tr.setAmount(newAmount);
            tr.setDescription(newDesc);
            dbHelper.updateTransaction(tr);
            Toast.makeText(getContext(), "Income updated", Toast.LENGTH_SHORT).show();
            loadIncomeTransactions();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }




    private class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder> {
        private final List<TransactionRecord> items;
        IncomeAdapter(List<TransactionRecord> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new IncomeViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
            TransactionRecord tr = items.get(position);
            String line1 = tr.getDate() + "  •  " + tr.getCategoryName();
            String line2 = tr.getAmount() + "  -  " + (TextUtils.isEmpty(tr.getDescription()) ? "" : tr.getDescription());
            holder.text1.setText(line1);
            holder.text2.setText(line2);

            // Click -> Edit
            holder.itemView.setOnClickListener(v -> showEditDialog(tr));

            // Long click -> Delete
            holder.itemView.setOnLongClickListener(v -> {
                dbHelper.deleteTransaction(tr.getId());
                Toast.makeText(getContext(), "Income deleted", Toast.LENGTH_SHORT).show();
                loadIncomeTransactions();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class IncomeViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            IncomeViewHolder(@NonNull View itemView) {
                super(itemView);
                text1=itemView.findViewById(android.R.id.text1);
                text2=itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
