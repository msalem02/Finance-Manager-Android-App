package birzeit.edu.a1203022_courseproject;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AppDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME ="finance_app.db";
    private static final int DB_VERSION=1;

    //USERS TABLE
    public static final String TABLE_USERS="users";
    public static final String COL_EMAIL="email";
    public static final String COL_FIRST_NAME="first_name";
    public static final String COL_LAST_NAME="last_name";
    public static final String COL_PASSWORD="password";


    //CATEGORIES TABLE
    public static final String TABLE_CATEGORIES="categories";
    public static final String COL_CAT_ID="id";
    public static final String COL_CAT_NAME="name";
    public static final String COL_CAT_TYPE="type";       // INCOME or EXPENSE
    public static final String COL_CAT_USER_EMAIL="user_email";


    // TRANSACTIONS TABLE
    public static final String TABLE_TRANSACTIONS="transactions";
    public static final String COL_TRANS_ID="id";
    public static final String COL_TRANS_USER_EMAIL="user_email";
    public static final String COL_TRANS_TYPE ="type";       // INCOME or EXPENSE
    public static final String COL_TRANS_AMOUNT="amount";
    public static final String COL_TRANS_DATE="date";
    public static final String COL_TRANS_CATEGORY_ID="category_id";
    public static final String COL_TRANS_DESCRIPTION="description";


    // BUDGETS TABLE
    public static final String TABLE_BUDGETS="budgets";
    public static final String COL_BUDGET_ID="id";
    public static final String COL_BUDGET_USER_EMAIL="user_email";
    public static final String COL_BUDGET_CATEGORY_ID="category_id";
    public static final String COL_BUDGET_MONTH="month";
    public static final String COL_BUDGET_YEAR="year";
    public static final String COL_BUDGET_LIMIT="amount_limit";
    public static final String TYPE_INCOME="INCOME";
    public static final String TYPE_EXPENSE="EXPENSE";

    public AppDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // USERS
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" + COL_EMAIL+" TEXT PRIMARY KEY, " + COL_FIRST_NAME+" TEXT, " + COL_LAST_NAME+" TEXT, " + COL_PASSWORD+" TEXT" + ");";

        // CATEGORIES
        String createCategoriesTable = "CREATE TABLE " + TABLE_CATEGORIES + " (" + COL_CAT_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_CAT_NAME+" TEXT, " + COL_CAT_TYPE+" TEXT, " + COL_CAT_USER_EMAIL+" TEXT" + ");";

        // TRANSACTIONS
        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + " (" + COL_TRANS_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_TRANS_USER_EMAIL+ " TEXT, " + COL_TRANS_TYPE+" TEXT, " + COL_TRANS_AMOUNT+" REAL, " + COL_TRANS_DATE+" TEXT, " + COL_TRANS_CATEGORY_ID+" INTEGER, " + COL_TRANS_DESCRIPTION+" TEXT" + ");";

        // BUDGETS
        String createBudgetsTable = "CREATE TABLE " + TABLE_BUDGETS + " (" + COL_BUDGET_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_BUDGET_USER_EMAIL+ " TEXT, " + COL_BUDGET_CATEGORY_ID+ " INTEGER, " + COL_BUDGET_MONTH+" INTEGER, " + COL_BUDGET_YEAR+ " INTEGER, " + COL_BUDGET_LIMIT + " REAL" + ");";

        db.execSQL(createUsersTable);
        db.execSQL(createCategoriesTable);
        db.execSQL(createTransactionsTable);
        db.execSQL(createBudgetsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }


    public boolean registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL,      user.getEmail());
        values.put(COL_FIRST_NAME, user.getFirstName());
        values.put(COL_LAST_NAME,  user.getLastName());
        values.put(COL_PASSWORD,   user.getPassword());
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COL_EMAIL + "=?",
                new String[]{email},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String firstName=cursor.getString(cursor.getColumnIndexOrThrow(COL_FIRST_NAME));
            String lastName=cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_NAME));
            String password=cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD));
            user = new User(email, firstName, lastName, password);
            cursor.close();
        }
        return user;
    }


    public long insertCategory(String userEmail, String name, String type) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CAT_NAME,name);
        values.put(COL_CAT_TYPE,type);
        values.put(COL_CAT_USER_EMAIL,userEmail);
        return db.insert(TABLE_CATEGORIES, null, values);
    }

    public List<Category> getCategories(String userEmail, String type) {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_CATEGORIES,
                null,
                COL_CAT_USER_EMAIL + "=? AND " + COL_CAT_TYPE + "=?",
                new String[]{userEmail, type},
                null, null,
                COL_CAT_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id=cursor.getLong(cursor.getColumnIndexOrThrow(COL_CAT_ID));
                String name=cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_NAME));
                String t=cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_TYPE));
                Category category = new Category(id, name, t, userEmail);
                list.add(category);
            }
            cursor.close();
        }
        return list;
    }

    public int deleteCategory(long categoryId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_CATEGORIES, COL_CAT_ID + "=?",
                new String[]{String.valueOf(categoryId)});
    }

    public int updateCategory(long categoryId, String newName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CAT_NAME, newName);
        return db.update(TABLE_CATEGORIES,
                values,
                COL_CAT_ID + "=?",
                new String[]{String.valueOf(categoryId)});
    }


    public long insertTransaction(String userEmail, String type, double amount, String date, long categoryId, String description) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRANS_USER_EMAIL,userEmail);
        values.put(COL_TRANS_TYPE,type);
        values.put(COL_TRANS_AMOUNT,amount);
        values.put(COL_TRANS_DATE,date);
        values.put(COL_TRANS_CATEGORY_ID,categoryId);
        values.put(COL_TRANS_DESCRIPTION,description);
        return db.insert(TABLE_TRANSACTIONS, null, values);
    }

    // Get all transactions for user + type, sorted by date descending
    public List<TransactionRecord> getTransactions(String userEmail, String type) {
        List<TransactionRecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT t." + COL_TRANS_ID + ", t." + COL_TRANS_AMOUNT + ", t." + COL_TRANS_DATE +
                ", t." + COL_TRANS_DESCRIPTION + ", t." + COL_TRANS_CATEGORY_ID +
                ", c." + COL_CAT_NAME +
                " FROM " + TABLE_TRANSACTIONS + " t" +
                " LEFT JOIN " + TABLE_CATEGORIES + " c ON t." + COL_TRANS_CATEGORY_ID + " = c." + COL_CAT_ID +
                " WHERE t." + COL_TRANS_USER_EMAIL + "=? AND t." + COL_TRANS_TYPE + "=?" +
                " ORDER BY t." + COL_TRANS_DATE + " DESC";

        Cursor cursor = db.rawQuery(sql, new String[]{userEmail, type});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id=cursor.getLong(0);
                double amount=cursor.getDouble(1);
                String date=cursor.getString(2);
                String desc=cursor.getString(3);
                long categoryId=cursor.getLong(4);
                String categoryName=cursor.getString(5);
                TransactionRecord tr = new TransactionRecord();
                tr.setId(id);
                tr.setAmount(amount);
                tr.setDate(date);
                tr.setDescription(desc);
                tr.setCategoryId(categoryId);
                tr.setCategoryName(categoryName);
                tr.setType(type);
                tr.setUserEmail(userEmail);
                list.add(tr);
            }
            cursor.close();
        }
        return list;
    }

    public int deleteTransaction(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_TRANSACTIONS,
                COL_TRANS_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public int updateTransaction(TransactionRecord tr) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRANS_AMOUNT,tr.getAmount());
        values.put(COL_TRANS_DATE,tr.getDate());
        values.put(COL_TRANS_DESCRIPTION,tr.getDescription());
        values.put(COL_TRANS_CATEGORY_ID,tr.getCategoryId());

        return db.update(TABLE_TRANSACTIONS,
                values,
                COL_TRANS_ID + "=?",
                new String[]{String.valueOf(tr.getId())});
    }


    public long insertBudget(String userEmail, long categoryId, int month, int year, double limit) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BUDGET_USER_EMAIL,userEmail);
        values.put(COL_BUDGET_CATEGORY_ID,categoryId);
        values.put(COL_BUDGET_MONTH,month);
        values.put(COL_BUDGET_YEAR,year);
        values.put(COL_BUDGET_LIMIT,limit);
        return db.insert(TABLE_BUDGETS, null, values);
    }

    // Sum income/expense between two dates
    public double getTotalAmount(String userEmail, String type, String startDate, String endDate) {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0.0;

        String sql = "SELECT SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COL_TRANS_USER_EMAIL + "=? AND " + COL_TRANS_TYPE + "=?" +
                " AND " + COL_TRANS_DATE + ">=? AND " + COL_TRANS_DATE + "<=?";

        Cursor cursor = db.rawQuery(sql, new String[]{userEmail, type, startDate, endDate});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }
        return total;
    }
    // Sum EXPENSE for a category in a given month/year
    public double getTotalExpenseForCategoryInMonth(String userEmail, long categoryId, int month, int year) {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0.0;
        String monthStr = String.format(Locale.getDefault(), "%02d", month);
        String yearStr  = String.valueOf(year);

        String sql = "SELECT SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COL_TRANS_USER_EMAIL + "=? AND " + COL_TRANS_TYPE + "=? " +
                " AND " + COL_TRANS_CATEGORY_ID + "=? " +
                " AND strftime('%Y'," + COL_TRANS_DATE + ")=? " +
                " AND strftime('%m'," + COL_TRANS_DATE + ")=?";

        Cursor cursor = db.rawQuery(sql,
                new String[]{userEmail, TYPE_EXPENSE, String.valueOf(categoryId), yearStr, monthStr});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }

        return total;
    }

    // Get budgets for a user in a given month/year (with category name)
    public List<Budget> getBudgetsForUserMonth(String userEmail, int month, int year) {
        List<Budget> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT b." + COL_BUDGET_ID + ", b." + COL_BUDGET_CATEGORY_ID +
                ", b." + COL_BUDGET_MONTH + ", b." + COL_BUDGET_YEAR +
                ", b." + COL_BUDGET_LIMIT +
                ", c." + COL_CAT_NAME +
                " FROM " + TABLE_BUDGETS + " b" +
                " LEFT JOIN " + TABLE_CATEGORIES + " c ON b." + COL_BUDGET_CATEGORY_ID +
                " = c." + COL_CAT_ID +
                " WHERE b." + COL_BUDGET_USER_EMAIL + "=? AND b." + COL_BUDGET_MONTH + "=? " +
                " AND b." + COL_BUDGET_YEAR + "=? " +
                " ORDER BY c." + COL_CAT_NAME + " ASC";

        Cursor cursor = db.rawQuery(sql,
                new String[]{userEmail, String.valueOf(month), String.valueOf(year)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id= cursor.getLong(0);
                long categoryId=cursor.getLong(1);
                int m= cursor.getInt(2);
                int y= cursor.getInt(3);
                double limit= cursor.getDouble(4);
                String catName= cursor.getString(5);
                Budget b = new Budget();
                b.setId(id);
                b.setUserEmail(userEmail);
                b.setCategoryId(categoryId);
                b.setMonth(m);
                b.setYear(y);
                b.setLimit(limit);
                b.setCategoryName(catName);
                list.add(b);
            }
            cursor.close();
        }
        return list;
    }
    // Get budget id if exists for user+category+month+year, or -1 if not
    public long getBudgetId(String userEmail, long categoryId, int month, int year) {
        SQLiteDatabase db = getReadableDatabase();
        long id = -1;

        String sql = "SELECT " + COL_BUDGET_ID +
                " FROM " + TABLE_BUDGETS +
                " WHERE " + COL_BUDGET_USER_EMAIL + "=? AND " +
                COL_BUDGET_CATEGORY_ID + "=? AND " +
                COL_BUDGET_MONTH + "=? AND " +
                COL_BUDGET_YEAR + "=?";

        Cursor cursor = db.rawQuery(sql, new String[]{userEmail, String.valueOf(categoryId), String.valueOf(month), String.valueOf(year)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                id = cursor.getLong(0);
            }
            cursor.close();
        }
        return id;
    }
    public int updateBudgetAmount(long budgetId, double newLimit) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BUDGET_LIMIT, newLimit);
        return db.update(TABLE_BUDGETS, values, COL_BUDGET_ID + "=?", new String[]{String.valueOf(budgetId)});
    }
    public List<CategoryTotal> getCategoryTotals(String userEmail, String type, String startDate, String endDate) {
        List<CategoryTotal> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT c." + COL_CAT_NAME + ", SUM(t." + COL_TRANS_AMOUNT + ")" +
                " FROM " + TABLE_TRANSACTIONS + " t" +
                " LEFT JOIN " + TABLE_CATEGORIES + " c " +
                " ON t." + COL_TRANS_CATEGORY_ID + " = c." + COL_CAT_ID +
                " WHERE t." + COL_TRANS_USER_EMAIL + "=? AND t." + COL_TRANS_TYPE + "=? " +
                " AND t." + COL_TRANS_DATE + ">=? AND t." + COL_TRANS_DATE + "<=?" +
                " GROUP BY c." + COL_CAT_NAME +
                " ORDER BY SUM(t." + COL_TRANS_AMOUNT + ") DESC";

        Cursor cursor = db.rawQuery(sql, new String[]{userEmail, type, startDate, endDate});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name  = cursor.getString(0);
                double total = cursor.getDouble(1);
                CategoryTotal ct = new CategoryTotal(name, total);
                list.add(ct);
            }
            cursor.close();
        }

        return list;
    }
    // Update user info (name and/or password)
    public int updateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FIRST_NAME,user.getFirstName());
        values.put(COL_LAST_NAME,user.getLastName());
        values.put(COL_PASSWORD,user.getPassword());

        return db.update(TABLE_USERS,
                values,
                COL_EMAIL + "=?",
                new String[]{user.getEmail()});
    }

}
