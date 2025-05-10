package com.example.tacos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String databaseName = "Tocas_Project.db";
    private static final int databaseVersion = 1;
    public DatabaseHelper(Context context) {
        super(context, databaseName, null, databaseVersion);
    }

    public void onCreate (SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE User ("
                + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "full_name TEXT NOT NULL, "
                + "phone TEXT NOT NULL CHECK(phone LIKE '0_________') UNIQUE, "
                + "password TEXT NOT NULL, "
                + "secret_key TEXT NOT NULL, "
                + "creation_date TEXT NOT NULL)";
        db.execSQL(createUserTable);

        String createCategoryTable = "CREATE TABLE Category ("
                + "category_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "category_name TEXT NOT NULL, "
                + "category_type TEXT NOT NULL CHECK(category_type IN ('Expense', 'Income')), "
                + "FOREIGN KEY(user_id) REFERENCES User(user_id))";
        db.execSQL(createCategoryTable);

        String createTransactionTable = "CREATE TABLE UserTransaction ("
                + "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "category_id INTEGER NOT NULL, "
                + "amount REAL NOT NULL, "
                + "date TEXT NOT NULL, "
                + "note TEXT NOT NULL, "
                + "FOREIGN KEY(user_id) REFERENCES User(user_id), "
                + "FOREIGN KEY(category_id) REFERENCES Category(category_id))";
        db.execSQL(createTransactionTable);

        String createBudgetTable = "CREATE TABLE Budget ("
                + "budget_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "category_id INTEGER NOT NULL, "
                + "amount REAL NOT NULL, "
                + "start_date TEXT NOT NULL, "
                + "end_date TEXT NOT NULL, "
                + "frequency TEXT NOT NULL CHECK(frequency IN ('Weekly', 'Monthly', 'Yearly')), "
                + "note TEXT NOT NULL, "
                + "FOREIGN KEY(user_id) REFERENCES User(user_id), "
                + "FOREIGN KEY(category_id) REFERENCES Category(category_id))";
        db.execSQL(createBudgetTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Budget");
        db.execSQL("DROP TABLE IF EXISTS UserTransaction");
        db.execSQL("DROP TABLE IF EXISTS Category");
        db.execSQL("DROP TABLE IF EXISTS User");
        onCreate(db);
    }

    public void checkDatabase() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        while (cursor.moveToNext()) {
            Log.d("Database", "Table name: " + cursor.getString(0));
        }
        cursor.close();
    }

    public boolean isPhoneExists(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM User WHERE phone = ?";
        Cursor cursor = db.rawQuery(query, new String[]{phone});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    public void addUser(String fullName, String phone, String password, String secretKey, String creationDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("phone", phone);
        values.put("password", password);
        values.put("secret_key", secretKey);
        values.put("creation_date", creationDate);

        long result = db.insert("User", null, values);
        db.close();

        if (result == -1) {
            Log.e("DatabaseError", "Failed to insert user: " + phone);
        }
    }

    public String getEncryptedPasscode(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT password FROM User WHERE phone = ?";
        Cursor cursor = db.rawQuery(query, new String[]{phone});

        String encryptedPasscode = null;

        if (cursor.moveToFirst()) {
            int passwordIndex = cursor.getColumnIndex("password");
            if (passwordIndex != -1) {
                encryptedPasscode = cursor.getString(passwordIndex);
            } else {
                Log.e("DatabaseHelper", "Column index not found");
            }
            cursor.close();
        }
        db.close();
        return encryptedPasscode;
    }

    public String getSecretKey(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT secret_key FROM User WHERE phone = ?";
        Cursor cursor = db.rawQuery(query, new String[]{phone});

        String secretKey = null;

        if (cursor.moveToFirst()) {
            int secretKeyIndex = cursor.getColumnIndex("secret_key");
            if (secretKeyIndex != -1) {
                secretKey = cursor.getString(secretKeyIndex);
            } else {
                Log.e("DatabaseHelper", "Column index not found");
            }
            cursor.close();
        }
        db.close();
        return secretKey;
    }

    public void updateUserName (int userId, String newUsername) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", newUsername);
        db.update("User", values, "user_id = ?",
                new String[]{String.valueOf(userId)});
        db.close();
    }

    public void updatePhone (int userId, String newPhone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("phone", newPhone);
        db.update("User", values, "user_id = ?",
                new String[]{String.valueOf(userId)});
        db.close();
    }

    public void updatePasscode (int userId, String newPasscode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPasscode);
        db.update("User", values, "user_id = ?",
                new String[]{String.valueOf(userId)});
        db.close();
    }

    public User getUser(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM User WHERE phone = ?";
        Cursor cursor = db.rawQuery(query, new String[]{phone});
        User user = null;
        if (cursor.moveToFirst()) {
            int userIdIndex = cursor.getColumnIndex("user_id");
            int fullNameIndex = cursor.getColumnIndex("full_name");
            int passwordIndex = cursor.getColumnIndex("password");

            if (userIdIndex != -1 && fullNameIndex != -1 && passwordIndex != -1) {
                int userId = cursor.getInt(userIdIndex);
                String fullName = cursor.getString(fullNameIndex);
                String password = cursor.getString(passwordIndex);
                user = new User(userId, fullName, phone, password);
            } else {
                Log.e("DatabaseHelper", "Column index not found");
            }
        }
        cursor.close();
        return user;
    }

    public double getTotalBalanceAllTime(int userId) {
        double totalIncome = 0;
        double totalExpense = 0;

        SQLiteDatabase db = this.getReadableDatabase();

        String incomeQuery = "SELECT SUM(amount) FROM UserTransaction " +
                "JOIN Category ON UserTransaction.category_id = Category.category_id " +
                "WHERE UserTransaction.user_id = ? AND Category.category_type = 'Income'";

        Cursor incomeCursor = db.rawQuery(incomeQuery, new String[]{String.valueOf(userId)});
        if (incomeCursor.moveToFirst()) {
            totalIncome = incomeCursor.getDouble(0);
        }
        incomeCursor.close();

        String expenseQuery = "SELECT SUM(amount) FROM UserTransaction " +
                "JOIN Category ON UserTransaction.category_id = Category.category_id " +
                "WHERE UserTransaction.user_id = ? AND Category.category_type = 'Expense'";

        Cursor expenseCursor = db.rawQuery(expenseQuery, new String[]{String.valueOf(userId)});
        if (expenseCursor.moveToFirst()) {
            totalExpense = expenseCursor.getDouble(0);
        }
        expenseCursor.close();

        return totalIncome - totalExpense;
    }

    public double getTotalBudgetAllTime(int userId) {
        double totalBudgetIncome = 0;
        double totalBudgetExpense = 0;

        SQLiteDatabase db = this.getReadableDatabase();

        String incomeQuery = "SELECT SUM(amount) FROM Budget " +
                "JOIN Category ON Budget.category_id = Category.category_id " +
                "WHERE Budget.user_id = ? AND Category.category_type = 'Income'";

        Cursor incomeCursor = db.rawQuery(incomeQuery, new String[]{String.valueOf(userId)});
        if (incomeCursor.moveToFirst()) {
            totalBudgetIncome = incomeCursor.getDouble(0);
        }
        incomeCursor.close();

        String expenseQuery = "SELECT SUM(amount) FROM Budget " +
                "JOIN Category ON Budget.category_id = Category.category_id " +
                "WHERE Budget.user_id = ? AND Category.category_type = 'Expense'";

        Cursor expenseCursor = db.rawQuery(expenseQuery, new String[]{String.valueOf(userId)});
        if (expenseCursor.moveToFirst()) {
            totalBudgetExpense = expenseCursor.getDouble(0);
        }
        expenseCursor.close();

        return totalBudgetIncome - totalBudgetExpense;
    }

    public Pair<Double, Double> getIncomeExpenseBudgetAllTime(int userId) {
        double totalIncome = 0;
        double totalExpense = 0;

        SQLiteDatabase db = this.getReadableDatabase();

        String incomeQuery = "SELECT SUM(amount) FROM Budget " +
                "JOIN Category ON Budget.category_id = Category.category_id " +
                "WHERE Budget.user_id = ? AND Category.category_type = 'Income'";

        Cursor incomeCursor = db.rawQuery(incomeQuery, new String[]{String.valueOf(userId)});
        if (incomeCursor.moveToFirst()) {
            totalIncome = incomeCursor.getDouble(0);
        }
        incomeCursor.close();

        String expenseQuery = "SELECT SUM(amount) FROM Budget " +
                "JOIN Category ON Budget.category_id = Category.category_id " +
                "WHERE Budget.user_id = ? AND Category.category_type = 'Expense'";

        Cursor expenseCursor = db.rawQuery(expenseQuery, new String[]{String.valueOf(userId)});
        if (expenseCursor.moveToFirst()) {
            totalExpense = expenseCursor.getDouble(0);
        }
        expenseCursor.close();

        return new Pair<>(totalIncome, totalExpense);
    }

    public double getTotalForCurrentMonth(int userId, String transactionType) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(UserTransaction.amount) AS total " +
                "FROM UserTransaction " +
                "JOIN Category ON UserTransaction.category_id = Category.category_id " +
                "WHERE UserTransaction.user_id = ? " +
                "AND Category.category_type = ? " +
                "AND strftime('%Y-%m', UserTransaction.date) = strftime('%Y-%m', 'now')";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), transactionType});

        if (cursor.moveToFirst()) {
            int totalIndex = cursor.getColumnIndex("total");

            if (totalIndex != -1) {
                total = cursor.getDouble(totalIndex);
            } else {
                Log.e("DatabaseHelper", "Column index not found");
            }
        }

        cursor.close();
        return total;
    }

    public double getTotalForLastMonth(int userId, String transactionType) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(UserTransaction.amount) AS total " +
                "FROM UserTransaction " +
                "JOIN Category ON UserTransaction.category_id = Category.category_id " +
                "WHERE UserTransaction.user_id = ? " +
                "AND Category.category_type = ? " +
                "AND date(UserTransaction.date) BETWEEN date('now', 'start of month', '-1 month') " +
                "AND date('now', 'start of month', '-1 day')";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), transactionType});
        if (cursor.moveToFirst()) {
            int totalIndex = cursor.getColumnIndex("total");
            if (totalIndex != -1) {
                total = cursor.getDouble(totalIndex);
            } else {
                Log.e("DatabaseHelper", "Column index not found");
            }
        }
        cursor.close();
        return total;
    }

    public List<Pair<String, Double>> getTopSpendingWeek(int userId, int topLimit) {
        List<Pair<String, Double>> topSpending = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfWeek = today.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = today.with(DayOfWeek.SUNDAY).atTime(23, 59);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String startDateTime = startOfWeek.format(dateTimeFormatter);
        String endDateTime = endOfWeek.format(dateTimeFormatter);

        String query = "SELECT Category.category_name, SUM(UserTransaction.amount) AS total_amount " +
                "FROM UserTransaction " +
                "JOIN Category ON UserTransaction.category_id = Category.category_id " +
                "WHERE UserTransaction.user_id = ? AND Category.category_type = 'Expense' " +
                "AND UserTransaction.date BETWEEN ? AND ? " +
                "GROUP BY Category.category_name " +
                "ORDER BY total_amount DESC " +
                "LIMIT ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), startDateTime, endDateTime, String.valueOf(topLimit)});

        if (cursor.moveToFirst()) {
            do {
                int categoryNameIndex = cursor.getColumnIndex("category_name");
                int totalAmountIndex = cursor.getColumnIndex("total_amount");

                if (categoryNameIndex != -1 && totalAmountIndex != -1) {
                    String categoryName = cursor.getString(categoryNameIndex);
                    double totalAmount = cursor.getDouble(totalAmountIndex);

                    topSpending.add(new Pair<>(categoryName, totalAmount));
                } else {
                    Log.e("DatabaseHelper", "Column index not found");
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return topSpending;
    }

    public List<Pair<String, Double>> getTopSpendingMonth(int userId, int topLimit) {
        List<Pair<String, Double>> topSpending = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String startDateTime = startOfMonth.format(dateTimeFormatter);
        String endDateTime = endOfMonth.format(dateTimeFormatter);

        String query = "SELECT Category.category_name, SUM(UserTransaction.amount) AS total_amount " +
                "FROM UserTransaction " +
                "JOIN Category ON UserTransaction.category_id = Category.category_id " +
                "WHERE UserTransaction.user_id = ? AND Category.category_type = 'Expense' " +
                "AND UserTransaction.date BETWEEN ? AND ? " +
                "GROUP BY Category.category_name " +
                "ORDER BY total_amount DESC " +
                "LIMIT ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), startDateTime, endDateTime, String.valueOf(topLimit)});

        if (cursor.moveToFirst()) {
            do {
                int categoryNameIndex = cursor.getColumnIndex("category_name");
                int totalAmountIndex = cursor.getColumnIndex("total_amount");

                if (categoryNameIndex != -1 && totalAmountIndex != -1) {
                    String categoryName = cursor.getString(categoryNameIndex);
                    double totalAmount = cursor.getDouble(totalAmountIndex);

                    topSpending.add(new Pair<>(categoryName, totalAmount));
                } else {
                    Log.e("DatabaseHelper", "Column index not found");
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return topSpending;
    }

    public List<Pair<String, Double>> getTopSpendingYear(int userId, int topLimit) {
        List<Pair<String, Double>> topSpending = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfYear = today.withDayOfYear(1).atStartOfDay();
        LocalDateTime endOfYear = today.withDayOfYear(today.lengthOfYear()).atTime(23, 59);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String startDateTime = startOfYear.format(dateTimeFormatter);
        String endDateTime = endOfYear.format(dateTimeFormatter);

        String query = "SELECT Category.category_name, SUM(UserTransaction.amount) AS total_amount " +
                "FROM UserTransaction " +
                "JOIN Category ON UserTransaction.category_id = Category.category_id " +
                "WHERE UserTransaction.user_id = ? AND Category.category_type = 'Expense' " +
                "AND UserTransaction.date BETWEEN ? AND ? " +
                "GROUP BY Category.category_name " +
                "ORDER BY total_amount DESC " +
                "LIMIT ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), startDateTime, endDateTime, String.valueOf(topLimit)});

        if (cursor.moveToFirst()) {
            do {
                int categoryNameIndex = cursor.getColumnIndex("category_name");
                int totalAmountIndex = cursor.getColumnIndex("total_amount");

                if (categoryNameIndex != -1 && totalAmountIndex != -1) {
                    String categoryName = cursor.getString(categoryNameIndex);
                    double totalAmount = cursor.getDouble(totalAmountIndex);

                    topSpending.add(new Pair<>(categoryName, totalAmount));
                } else {
                    Log.e("DatabaseHelper", "Column index not found");
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return topSpending;
    }

    public List<Pair<String, String>> getRecentTransactions(int userId) {
        List<Pair<String, String>> recentTransactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT Category.category_name, UserTransaction.amount, UserTransaction.date, Category.category_type, UserTransaction.note " +
                "FROM UserTransaction " +
                "JOIN Category ON UserTransaction.category_id = Category.category_id " +
                "WHERE UserTransaction.user_id = ? " +
                "ORDER BY UserTransaction.date DESC " +
                "LIMIT 3";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        while (cursor.moveToNext()) {
            int categoryNameIndex = cursor.getColumnIndex("category_name");
            int amountIndex = cursor.getColumnIndex("amount");
            int dateIndex = cursor.getColumnIndex("date");
            int categoryTypeIndex = cursor.getColumnIndex("category_type");
            int noteIndex = cursor.getColumnIndex("note");

            if (categoryNameIndex != -1 && amountIndex != -1 && dateIndex != -1 && categoryTypeIndex != -1 && noteIndex != -1) {
                String categoryName = cursor.getString(categoryNameIndex);
                double amount = cursor.getDouble(amountIndex);
                String dateString = cursor.getString(dateIndex);
                String categoryType = cursor.getString(categoryTypeIndex);
                String note = cursor.getString(noteIndex);

                LocalDateTime date = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                recentTransactions.add(new Pair<>(categoryName + " - " + amount, formattedDate + " - " + categoryType + " - " + note));
            } else {
                Log.e("DatabaseHelper", "Column index not found");
            }
        }
        cursor.close();

        return recentTransactions;
    }

    public List<UserTransaction> getTransactionsByDate(int userId, String date) {
        List<UserTransaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT ut.*, c.category_name, c.category_type " +
                "FROM UserTransaction ut " +
                "JOIN Category c ON ut.category_id = c.category_id " +
                "WHERE ut.user_id = ? AND strftime('%Y-%m-%d', ut.date) = ? " +
                "ORDER BY ut.date DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), date});
        if (cursor.moveToFirst()) {
            do {
                int transactionIdIndex = cursor.getColumnIndex("transaction_id");
                int userIdIndex = cursor.getColumnIndex("user_id");
                int categoryIdIndex = cursor.getColumnIndex("category_id");
                int amountIndex = cursor.getColumnIndex("amount");
                int noteIndex = cursor.getColumnIndex("note");
                int dateIndex = cursor.getColumnIndex("date");
                int categoryNameIndex = cursor.getColumnIndex("category_name");
                int categoryTypeIndex = cursor.getColumnIndex("category_type");

                if (transactionIdIndex != -1 && userIdIndex != -1 && categoryIdIndex != -1 &&
                        amountIndex != -1 && noteIndex != -1 && dateIndex != -1 &&
                        categoryNameIndex != -1 && categoryTypeIndex != -1) {

                    int transactionId = cursor.getInt(transactionIdIndex);
                    int userIdFromDb = cursor.getInt(userIdIndex);
                    int categoryId = cursor.getInt(categoryIdIndex);
                    double amount = cursor.getDouble(amountIndex);
                    String note = cursor.getString(noteIndex);
                    String dateString = cursor.getString(dateIndex);
                    String categoryName = cursor.getString(categoryNameIndex);
                    String categoryType = cursor.getString(categoryTypeIndex);

                    LocalDateTime dateTime = LocalDateTime.parse(dateString,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                    UserTransaction transaction = new UserTransaction(transactionId, userIdFromDb,
                                                                categoryId, amount, note, dateTime);
                    transaction.setCategoryName(categoryName);
                    transaction.setCategoryType(categoryType);

                    transactions.add(transaction);
                } else {
                    Log.e("DatabaseHelper", "Column index not found");
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        return transactions;
    }

    public List<Budget> getBudgets(int userId) {
        List<Budget> budgets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT b.*, c.category_name, c.category_type " +
                "FROM Budget b " +
                "JOIN Category c ON b.category_id = c.category_id " +
                "WHERE b.user_id = ?" +
                "ORDER BY b.end_date ASC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int budgetIdIndex = cursor.getColumnIndex("budget_id");
                int userIdIndex = cursor.getColumnIndex("user_id");
                int categoryIdIndex = cursor.getColumnIndex("category_id");
                int amountIndex = cursor.getColumnIndex("amount");
                int noteIndex = cursor.getColumnIndex("note");
                int startDateIndex = cursor.getColumnIndex("start_date");
                int endDateIndex = cursor.getColumnIndex("end_date");
                int frequencyIndex = cursor.getColumnIndex("frequency");
                int categoryNameIndex = cursor.getColumnIndex("category_name");
                int categoryTypeIndex = cursor.getColumnIndex("category_type");

                if (budgetIdIndex != -1 && userIdIndex != -1 && categoryIdIndex != -1 &&
                        amountIndex != -1 && startDateIndex != -1 && endDateIndex != -1 &&
                        frequencyIndex != -1 && categoryNameIndex != -1 &&
                        categoryTypeIndex != -1 && noteIndex != -1) {
                    int budgetId = cursor.getInt(budgetIdIndex);
                    int userIdFromDb = cursor.getInt(userIdIndex);
                    int categoryId = cursor.getInt(categoryIdIndex);
                    double amount = cursor.getDouble(amountIndex);
                    String startDateString = cursor.getString(startDateIndex);
                    String endDateString = cursor.getString(endDateIndex);
                    String categoryName = cursor.getString(categoryNameIndex);
                    String categoryType = cursor.getString(categoryTypeIndex);

                    LocalDateTime startDate = LocalDateTime.parse(startDateString,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    LocalDateTime endDate = LocalDateTime.parse(endDateString,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    String frequency = cursor.getString(frequencyIndex);
                    String note = cursor.getString(noteIndex);

                    Budget budget = new Budget(budgetId, userIdFromDb, categoryId, amount,
                            note, startDate, endDate, frequency);
                    budget.setCategoryName(categoryName);
                    budget.setCategoryType(categoryType);

                    budgets.add(budget);
                } else {
                    Log.e("DatabaseHelper", "Column index not found");
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return budgets;
    }

    public Pair<Double, Double> getIncomeAndExpenseByDate(int userId, String date) {
        double totalIncome = 0;
        double totalExpense = 0;

        SQLiteDatabase db = this.getReadableDatabase();

        String incomeQuery = "SELECT SUM(ut.amount) " +
                "FROM UserTransaction ut " +
                "JOIN Category c ON ut.category_id = c.category_id " +
                "WHERE ut.user_id = ? " +
                "AND strftime('%Y-%m-%d', ut.date) = ? " +
                "AND c.category_type = 'Income'";

        Cursor incomeCursor = db.rawQuery(incomeQuery, new String[]{String.valueOf(userId), date});
        if (incomeCursor.moveToFirst()) {
            totalIncome = incomeCursor.getDouble(0);
        }
        incomeCursor.close();

        String expenseQuery = "SELECT SUM(ut.amount) " +
                "FROM UserTransaction ut " +
                "JOIN Category c ON ut.category_id = c.category_id " +
                "WHERE ut.user_id = ? " +
                "AND strftime('%Y-%m-%d', ut.date) = ? " +
                "AND c.category_type = 'Expense'";

        Cursor expenseCursor = db.rawQuery(expenseQuery, new String[]{String.valueOf(userId), date});
        if (expenseCursor.moveToFirst()) {
            totalExpense = expenseCursor.getDouble(0);
        }
        expenseCursor.close();

        return new Pair<>(totalIncome, totalExpense);
    }

    public List<Pair<String, Double>> getTransactionsByCategoryThisMonth(int userId, String categoryType) {
        List<Pair<String, Double>> transactions = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.category_name, SUM(ut.amount) FROM UserTransaction ut " +
                "JOIN Category c ON ut.category_id = c.category_id " +
                "WHERE ut.user_id = ? AND c.category_type = ? " +
                "AND strftime('%Y-%m', ut.date) = strftime('%Y-%m', 'now') " +
                "GROUP BY c.category_name";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), categoryType});

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                double amount = cursor.getDouble(1);
                transactions.add(new Pair<>(category, amount));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return transactions;
    }

    public List<Double> getExpenseIncome12MonthsThisYear(int userId, String categoryType) {
        List<Double> expenseIncome = new ArrayList<>(Collections.nCopies(12, 0.00));
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT strftime('%m', ut.date) AS month, SUM(ut.amount) AS total_amount " +
                "FROM UserTransaction ut " +
                "JOIN Category c ON ut.category_id = c.category_id " +
                "WHERE ut.user_id = ? AND c.category_type = ? " +
                "AND strftime('%Y', ut.date) = strftime('%Y', 'now') " +
                "GROUP BY month " +
                "ORDER BY month";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), categoryType});

        if (cursor.moveToFirst()) {
            do {
                int monthIndex = Integer.parseInt(cursor.getString(0)) - 1;
                double amount = cursor.getDouble(1);
                expenseIncome.set(monthIndex, amount);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return expenseIncome;
    }

    public List<Pair<String, Double>> getExpenseTransactionsThisWeek(int userId) {
        List<Pair<String, Double>> expenseTransactions = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase()) {
            String query = "SELECT c.category_name, SUM(ut.amount) FROM UserTransaction ut " +
                    "JOIN Category c ON ut.category_id = c.category_id " +
                    "WHERE ut.user_id = ? AND c.category_type = 'Expense' " +
                    "AND strftime('%Y-%m-%d', ut.date) BETWEEN " +
                    "date('now', 'weekday 0', '-6 days') AND " +
                    "date('now', 'weekday 0', '+0 days') " +
                    "GROUP BY c.category_name";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(0);
                    double amount = cursor.getDouble(1);
                    expenseTransactions.add(new Pair<>(category, amount));
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
        }

        return expenseTransactions;
    }

    public List<Pair<String, Double>> getExpenseTransactionsThisYear(int userId) {
        List<Pair<String, Double>> expenseTransactions = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase()) {
            String query = "SELECT c.category_name, SUM(ut.amount) FROM UserTransaction ut " +
                    "JOIN Category c ON ut.category_id = c.category_id " +
                    "WHERE ut.user_id = ? AND c.category_type = 'Expense' " +
                    "AND strftime('%Y', ut.date) = strftime('%Y', 'now') " +
                    "GROUP BY c.category_name";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(0);
                    double amount = cursor.getDouble(1);
                    expenseTransactions.add(new Pair<>(category, amount));
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
        }

        return expenseTransactions;
    }

    public int getTransactionId(int userId, int categoryId, double amount, String fullDate, String note) {
        int transactionId = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT transaction_id FROM UserTransaction " +
                        "WHERE user_id = ? AND category_id = ? AND amount = ? AND date = ? AND note = ?",
                new String[]{String.valueOf(userId), String.valueOf(categoryId), String.valueOf(amount), fullDate, note});

        if (cursor.moveToFirst()) {
            int transactionIdIndex = cursor.getColumnIndex("transaction_id");
            if (transactionIdIndex != -1) {
                transactionId = cursor.getInt(transactionIdIndex);
            } else {
                Log.e("DatabaseHelper", "Column index not found");
            }
        }
        cursor.close();
        db.close();
        return transactionId;
    }

    public int getBudgetId(int userId, int categoryId, double amount, String startDate, String endDate, String note) {
        int budgetId = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT budget_id FROM Budget " +
                        "WHERE user_id = ? AND category_id = ? AND amount = ? AND start_date = ? AND end_date = ? AND note = ?",
                new String[]{String.valueOf(userId), String.valueOf(categoryId), String.valueOf(amount), startDate, endDate, note});

        if (cursor.moveToFirst()) {
            int budgetIdIndex = cursor.getColumnIndex("budget_id");
            if (budgetIdIndex != -1) {
                budgetId = cursor.getInt(budgetIdIndex);
            } else {
                Log.e("DatabaseHelper", "Column index not found");
            }
        }
        cursor.close();
        db.close();
        return budgetId;
    }

    public void updateTransaction(UserTransaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("category_id", transaction.getCategoryId());
        values.put("amount", transaction.getAmount());

        String dateTimeString = transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        values.put("date", dateTimeString);
        values.put("note", transaction.getNote());

        db.update("UserTransaction", values, "transaction_id = ?",
                new String[]{String.valueOf(transaction.getTransactionId())});
        db.close();
    }

    public void updateBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("category_id", budget.getCategoryId());
        values.put("amount", budget.getAmount());
        String startDateTimeString = budget.getStartDate().format(DateTimeFormatter
                                                    .ofPattern("yyyy-MM-dd HH:mm"));
        values.put("start_date", startDateTimeString);
        String endDateTimeString = budget.getEndDate().format(DateTimeFormatter
                                                    .ofPattern("yyyy-MM-dd HH:mm"));
        values.put("end_date", endDateTimeString);
        values.put("frequency", budget.getFrequency());
        values.put("note", budget.getNote());

        db.update("Budget", values, "budget_id = ?",
                new String[]{String.valueOf(budget.getBudgetId())});
        db.close();
    }

    public void deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete("UserTransaction", "transaction_id = ?",
                new String[]{String.valueOf(transactionId)});
        db.close();

        if (rowsAffected == 0) {
            Log.e("Database", "Transaction ID not found: " + transactionId);
        } else {
            Log.d("Database", "Transaction deleted successfully: " + transactionId);
        }
    }

    public void deleteBudget(int budgetId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete("Budget", "budget_id = ?",
                new String[]{String.valueOf(budgetId)});
        db.close();

        if (rowsAffected == 0) {
            Log.e("Database", "Budget ID not found: " + budgetId);
        } else {
            Log.d("Database", "Budget deleted successfully: " + budgetId);
        }
    }

    public int getCategoryId(int userId, String categoryName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT category_id FROM Category WHERE user_id = ? AND category_name = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), categoryName});
        int categoryId = -1;
        if (cursor.moveToFirst()) {
            categoryId = cursor.getInt(0);
        }
        cursor.close();
        return categoryId;
    }

    public String getCategoryType(int userId, int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT category_type FROM Category WHERE category_id = ? AND user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId), String.valueOf(userId)});
        String categoryType = "";
        if (cursor.moveToFirst()) {
            categoryType = cursor.getString(0);
        }
        cursor.close();
        return categoryType;
    }

    public void addTransaction(int userId, int categoryId, double amount,
                               String date, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("category_id", categoryId);
        values.put("amount", amount);
        values.put("date", date);
        values.put("note", note);
        db.insert("UserTransaction", null, values);
        db.close();
    }

    public void createBudget(int userId, int categoryId, double amount, String startDate,
                             String endDate, String frequency, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("category_id", categoryId);
        values.put("amount", amount);
        values.put("start_date", startDate);
        values.put("end_date", endDate);
        values.put("frequency", frequency);
        values.put("note", note);
        db.insert("Budget", null, values);
        db.close();
    }

    public List<String> getCategoryNamesByType(int userId, String categoryType) {
        List<String> categoryNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT category_name FROM Category WHERE user_id = ? AND category_type = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), categoryType})) {
            if (cursor.moveToFirst()) {
                do {
                    categoryNames.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fetching categories", e);
        }
        return categoryNames;
    }

    public void createCategory(int userId, String categoryName, String categoryType) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM Category WHERE user_id = ? AND category_name = ? AND category_type = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), categoryName, categoryType});

        if (cursor.getCount() > 0) {
            Log.i("DatabaseHelper", "Category already exists: " + categoryName);
        } else {
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("category_name", categoryName);
            values.put("category_type", categoryType);

            db.insert("Category", null, values);
            Log.i("DatabaseHelper", "Category created: " + categoryName);
        }
        cursor.close();
        db.close();
    }


    public String getCreationDate(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT creation_date FROM User WHERE user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        String creationDate = "";
        if (cursor.moveToFirst()) {
            creationDate = cursor.getString(0);
        }
        cursor.close();
        return creationDate;
    }

    public double getTotalExpenseIncomeAllTime(int userId, String categoryType) {
        double totalExpenseIncomeAllTime = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT SUM(amount) FROM UserTransaction " +
                "JOIN Category ON UserTransaction.category_id = Category.category_id " +
                "WHERE UserTransaction.user_id = ? AND Category.category_type = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), categoryType});
        if (cursor.moveToFirst()) {
            totalExpenseIncomeAllTime = cursor.getDouble(0);
        }
        cursor.close();
        db.close();

        return totalExpenseIncomeAllTime;
    }

    public int getTotalTransactions(int userId) {
        int totalTransactions = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM UserTransaction WHERE user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            totalTransactions = cursor.getInt(0);
        }
        cursor.close();
        db.close();

        return totalTransactions;
    }

    public int getTotalTransactionsExpenseIncome(int userId, String categoryType) {
        int totalTransactionsExpenseIncome = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM UserTransaction " +
                "JOIN Category ON UserTransaction.category_id = Category.category_id " +
                "WHERE UserTransaction.user_id = ? AND Category.category_type = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), categoryType});
        if (cursor.moveToFirst()) {
            totalTransactionsExpenseIncome = cursor.getInt(0);
        }
        cursor.close();
        db.close();

        return totalTransactionsExpenseIncome;
    }

    public void resetAccount(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM UserTransaction WHERE user_id = ?", new String[]{String.valueOf(userId)});
        db.execSQL("DELETE FROM Budget WHERE user_id = ?", new String[]{String.valueOf(userId)});
        db.execSQL("DELETE FROM Category WHERE user_id = ?", new String[]{String.valueOf(userId)});
        db.close();
    }

    public void deleteAccount(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM UserTransaction WHERE user_id = ?", new String[]{String.valueOf(userId)});
        db.execSQL("DELETE FROM Budget WHERE user_id = ?", new String[]{String.valueOf(userId)});
        db.execSQL("DELETE FROM Category WHERE user_id = ?", new String[]{String.valueOf(userId)});
        db.execSQL("DELETE FROM User WHERE user_id = ?", new String[]{String.valueOf(userId)});
        db.close();
    }

    public int getCategoryIdByName(int userId, String categoryName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT category_id FROM Category WHERE user_id = ? AND category_name = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), categoryName});
        int categoryId = -1;
        if (cursor.moveToFirst()) {
            categoryId = cursor.getInt(0);
        }
        cursor.close();
        return categoryId;
    }

    public void deleteCategory(int userId, int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Category", "user_id = ? AND category_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(categoryId)});
        db.close();
    }

    public int getTransactionCountByCategoryId(int userId, int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM UserTransaction " +
                                        "WHERE user_id = ? AND category_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(categoryId)});

        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public void deleteTransactionsByCategoryId(int userId, int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("UserTransaction", "user_id = ? AND category_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(categoryId)});
        db.close();
    }

}

