package birzeit.edu.a1203022_courseproject;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;



public class HomeFragment extends Fragment {
    private TextView textPeriodLabel, textIncome, textExpenses, textBalance, textCategoryBreakdown;
    private TextView textCustomFrom, textCustomTo;
    private AppDatabaseHelper dbHelper;
    private SharedPrefManager prefManager;
    private String currentUserEmail;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String currentPeriod = "MONTH";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_home, container, false);
        dbHelper= new AppDatabaseHelper(requireContext());
        prefManager= SharedPrefManager.getInstance(requireContext());
        currentUserEmail = prefManager.readString(SharedPrefManager.KEY_CURRENT_USER, "");
        textPeriodLabel= view.findViewById(R.id.textViewPeriodLabel);
        textIncome= view.findViewById(R.id.textViewTotalIncome);
        textExpenses= view.findViewById(R.id.textViewTotalExpenses);
        textBalance= view.findViewById(R.id.textViewBalance);
        textCategoryBreakdown= view.findViewById(R.id.textViewCategoryBreakdown);
        Button buttonDay= view.findViewById(R.id.buttonDay);
        Button buttonWeek= view.findViewById(R.id.buttonWeek);
        Button buttonMonth= view.findViewById(R.id.buttonMonth);
        Button buttonYear= view.findViewById(R.id.buttonYear);
        Button buttonApplyCustom= view.findViewById(R.id.buttonApplyCustom);
        textCustomFrom = view.findViewById(R.id.textViewCustomFrom);
        textCustomTo   = view.findViewById(R.id.textViewCustomTo);

        String todayStr = dateFormat.format(Calendar.getInstance().getTime());
        textCustomFrom.setText(todayStr);
        textCustomTo.setText(todayStr);
        textCustomFrom.setOnClickListener(v -> openDatePickerFor(textCustomFrom));
        textCustomTo.setOnClickListener(v -> openDatePickerFor(textCustomTo));

        String savedPeriod = prefManager.readString(SharedPrefManager.KEY_DEFAULT_PERIOD, "MONTH");
        if (!"DAY".equals(savedPeriod) && !"WEEK".equals(savedPeriod) && !"MONTH".equals(savedPeriod)) {
            savedPeriod = "MONTH";
        }
        currentPeriod = savedPeriod;
        updateSummaryForPeriod(currentPeriod);

        buttonDay.setOnClickListener(v -> {
            currentPeriod = "DAY";
            updateSummaryForPeriod("DAY");
        });
        buttonWeek.setOnClickListener(v -> {
            currentPeriod = "WEEK";
            updateSummaryForPeriod("WEEK");
        });
        buttonMonth.setOnClickListener(v -> {
            currentPeriod = "MONTH";
            updateSummaryForPeriod("MONTH");
        });
        buttonYear.setOnClickListener(v -> {
            currentPeriod = "YEAR";
            updateSummaryForPeriod("YEAR");
        });
        buttonApplyCustom.setOnClickListener(v -> applyCustomPeriod());
        return view;
    }

    private void openDatePickerFor(TextView target) {
        Calendar cal = Calendar.getInstance();
        String currentText = target.getText().toString();
        try {
            Date d = dateFormat.parse(currentText);
            if (d != null) {
                cal.setTime(d);
            }
        } catch (Exception ignored) { }
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(year, month, dayOfMonth);
                    target.setText(dateFormat.format(c.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void applyCustomPeriod() {
        String fromStr= textCustomFrom.getText().toString().trim();
        String toStr= textCustomTo.getText().toString().trim();

        try {
            Date from= dateFormat.parse(fromStr);
            Date to= dateFormat.parse(toStr);

            if (from == null || to == null) {
                Toast.makeText(getContext(), "Invalid dates", Toast.LENGTH_SHORT).show();
                return;
            }

            if (from.after(to)) {
                Toast.makeText(getContext(), "From date must be before or equal to To date",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String label = "Custom: " + fromStr + " to " + toStr;
            updateSummaryWithRange(fromStr, toStr, label);
            currentPeriod = "CUSTOM";
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid dates", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSummaryForPeriod(String period) {
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            textIncome.setText("Income: 0.0");
            textExpenses.setText("Expenses: 0.0");
            textBalance.setText("Balance: 0.0");
            textCategoryBreakdown.setText("No user logged in.");
            return;
        }
        String startDate;
        String endDate;
        String label;
        Calendar cal = Calendar.getInstance();
        if ("DAY".equals(period)) {
            startDate = dateFormat.format(cal.getTime());
            endDate   = startDate;
            label = "Today";
        } else if ("WEEK".equals(period)) {
            endDate = dateFormat.format(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, -6);
            startDate = dateFormat.format(cal.getTime());
            label = "Last 7 days";
        } else if ("YEAR".equals(period)) {
            int year = cal.get(Calendar.YEAR);
            cal.set(year, Calendar.JANUARY, 1);
            startDate = dateFormat.format(cal.getTime());
            cal.set(year, Calendar.DECEMBER, 31);
            endDate = dateFormat.format(cal.getTime());
            label = "This Year";
        } else {
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            cal.set(year, month, 1);
            startDate = dateFormat.format(cal.getTime());
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            endDate = dateFormat.format(cal.getTime());
            label = "This Month";
        }
        updateSummaryWithRange(startDate, endDate, label);
    }

    private void updateSummaryWithRange(String startDate, String endDate, String label) {
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            textIncome.setText("Income: 0.0");
            textExpenses.setText("Expenses: 0.0");
            textBalance.setText("Balance: 0.0");
            textCategoryBreakdown.setText("No user logged in.");
            return;
        }
        double totalIncome = dbHelper.getTotalAmount(
                currentUserEmail,
                AppDatabaseHelper.TYPE_INCOME,
                startDate, endDate);
        double totalExpense = dbHelper.getTotalAmount(
                currentUserEmail,
                AppDatabaseHelper.TYPE_EXPENSE,
                startDate, endDate);
        double balance = totalIncome - totalExpense;
        textPeriodLabel.setText(label);
        textIncome.setText(String.format(Locale.getDefault(), "Income: %.2f", totalIncome));
        textExpenses.setText(String.format(Locale.getDefault(), "Expenses: %.2f", totalExpense));
        textBalance.setText(String.format(Locale.getDefault(), "Balance: %.2f", balance));

        List<CategoryTotal> categories = dbHelper.getCategoryTotals(currentUserEmail, AppDatabaseHelper.TYPE_EXPENSE, startDate, endDate);

        if (categories.isEmpty()) {
            textCategoryBreakdown.setText("No expenses in this period.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (CategoryTotal ct : categories) {
                sb.append(String.format(Locale.getDefault(), "%s: %.2f\n", ct.getCategoryName(), ct.getTotalAmount()));
            }
            textCategoryBreakdown.setText(sb.toString());
        }
    }
}
