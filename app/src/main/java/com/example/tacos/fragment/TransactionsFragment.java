package com.example.tacos.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tacos.interfaces.OnTransactionDeletedListener;
import com.example.tacos.R;
import com.example.tacos.response.UserTransaction;
import com.example.tacos.adapter.TransactionsAdapter;
import com.example.tacos.database.DatabaseHelper;
import com.example.tacos.model.User;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.blurry.Blurry;

public class TransactionsFragment extends Fragment implements OnTransactionDeletedListener {
    private TextView tvIncome, tvExpense, tvNoData;
    private CalendarView calendarView;
    private RecyclerView recyclerView;

    private String selectedDate;
    private int userId;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        initViews(view);
        loadData();
        setupCalendarView();

        getParentFragmentManager().setFragmentResultListener("addTransaction", this, (requestKey, bundle1) -> {
            boolean isTransactionAdded = bundle1.getBoolean("isTransactionAdded");
            if (isTransactionAdded) {
                loadTransactionsForDate(selectedDate, userId);
            }
        });

        return view;
    }

    private void initViews(View view) {
        tvIncome = view.findViewById(R.id.tv_amount_income);
        tvExpense = view.findViewById(R.id.tv_amount_expense);
        calendarView = view.findViewById(R.id.cv_calendar);
        recyclerView = view.findViewById(R.id.rv_transaction);
        tvNoData = view.findViewById(R.id.tv_no_data);
    }

    private void loadData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String phone = bundle.getString("userPhone");
            try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
                User user = dbHelper.getUser(phone);
                if (user != null) {
                    userId = user.getUserId();
                    setCurrentDate();
                    loadTransactionsForDate(selectedDate, userId);
                } else {
                    showToast("User not found");
                }
            } catch (Exception e) {
                alertSuccess(
                        requireContext(),
                        "Error",
                        e.getMessage(),
                        R.drawable.background_alert
                );
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void setCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        selectedDate = String.format("%d-%02d-%02d", year, month, day);
        calendarView.setDate(calendar.getTimeInMillis(), true, true);
    }

    @SuppressLint("DefaultLocale")
    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
            loadTransactionsForDate(selectedDate, userId);
        });
    }

    @SuppressLint({"MissingInflatedId", "LocalSuppress", "SimpleDateFormat"})
    private void openEditTransactionDialog(UserTransaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_transaction, null);
        builder.setView(dialogView);

        TextView tvTitleEditTransaction = dialogView.findViewById(R.id.tv_title_edit_transaction);
        textGradient(tvTitleEditTransaction);
        EditText editTextCategory = dialogView.findViewById(R.id.editTextCategory);
        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmount);
        EditText editTextNote = dialogView.findViewById(R.id.editTextNote);
        EditText editTextDate = dialogView.findViewById(R.id.editTextDate);
        EditText editTextTime = dialogView.findViewById(R.id.editTextTime);
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        editTextDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(selectedYear, selectedMonth, selectedDay);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        editTextDate.setText(dateFormat.format(calendar.getTime()));
                    }, year, month, day);

            datePickerDialog.show();
            hideKeyboard(v);
        });

        editTextTime.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                    (timePicker, selectedHour, selectedMinute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        editTextTime.setText(timeFormat.format(calendar.getTime()));
                    }, hour, minute, true);

            timePickerDialog.show();
            hideKeyboard(v);
        });

        setDialogValues(transaction, editTextCategory, editTextAmount, editTextDate, editTextTime, editTextNote);

        View rootView = ((Activity) requireContext()).getWindow().getDecorView().findViewById(android.R.id.content);
        Blurry.with(getContext())
                .radius(10)
                .sampling(4)
                .onto((ViewGroup) rootView);

        AlertDialog dialog = builder.create();
        setupDialogAppearance(dialog, dialogView);

        btnUpdate.setOnClickListener(v -> updateTransaction(transaction, editTextCategory, editTextAmount,
                                                editTextDate, editTextTime, editTextNote, dialog));
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnDismissListener(d -> Blurry.delete((ViewGroup) rootView));

        dialog.show();
    }

    private void setDialogValues(UserTransaction transaction, EditText editTextCategory, EditText editTextAmount,
                                 EditText editTextDate, EditText editTextTime, EditText editTextNote) {
        editTextAmount.setText(String.valueOf(transaction.getAmount()));
        editTextNote.setText(transaction.getNote());
        editTextDate.setText(transaction.getDate().toLocalDate().toString());
        editTextTime.setText(transaction.getDate().toLocalTime().toString());
        editTextCategory.setText(transaction.getCategoryName());
    }

    private void setupDialogAppearance(AlertDialog dialog, View dialogView) {
        dialog.setOnShowListener(dialogInterface -> {
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogView.setBackgroundResource(R.drawable.background_alert);
        });
    }

    private void updateTransaction(UserTransaction transaction, EditText editTextCategory,
                                   EditText editTextAmount, EditText editTextDate, EditText editTextTime,
                                   EditText editTextNote, AlertDialog dialog) {
        try {
            String amountString = editTextAmount.getText().toString();
            if (amountString.isEmpty()) {
                showToast("Amount cannot be empty");
                return;
            }
            double newAmount = Double.parseDouble(amountString);
            if (newAmount <= 0) {
                showToast("Amount must be greater than 0");
                return;
            }

            String newDate = editTextDate.getText().toString();
            String newTime = editTextTime.getText().toString();

            LocalDateTime dateTimeObject = LocalDateTime.parse(newDate + " " +
                    newTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            LocalDateTime currentDateTime = LocalDateTime.now();
            if (dateTimeObject.isAfter(currentDateTime)) {
                showToast("Date and time cannot be in the future");
                return;
            }

            String newCategory = editTextCategory.getText().toString();

            if (newCategory.isEmpty()) {
                showToast("Category cannot be empty");
                return;
            }
            String newNote = capitalizeFirstLetter(editTextNote.getText().toString());
            if (newNote.isEmpty()) {
                showToast("Note cannot be empty");
                return;
            }

            transaction.setAmount(newAmount);
            transaction.setNote(newNote);
            transaction.setDate(dateTimeObject);
            transaction.setCategoryName(newCategory);

            try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
                dbHelper.updateTransaction(transaction);
            }

            loadTransactionsForDate(selectedDate, userId);
            dialog.dismiss();

            alertSuccess(
                    requireContext(),
                    "Successful update",
                    "Transaction date: " + transaction.getDate().format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n\n" +
                            "Category: " + transaction.getCategoryName() + "\n" +
                            "Amount: $ " + transaction.getAmount() + "\n" +
                            "Note: " + transaction.getNote(),
                    R.drawable.background_alert
            );

        } catch (Exception e) {
            showToast("Error: " + e.getMessage());
        }
    }

    public String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public void alertSuccess(Context context, String title, String message, int backgroundResource) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_confirm_layout, null);
        builder.setView(dialogView);

        LinearLayout llConfirmDialog = dialogView.findViewById(R.id.ll_confirm_dialog);
        llConfirmDialog.setBackgroundResource(backgroundResource);

        TextView tvTitle = dialogView.findViewById(R.id.dialog_title_confirm);
        tvTitle.setText(title);
        textGradient(tvTitle);

        TextView tvMessage = dialogView.findViewById(R.id.dialog_message_confirm);
        tvMessage.setText(message);

        View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        Blurry.with(context)
                .radius(10)
                .sampling(4)
                .onto((ViewGroup) rootView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm_confirm);
        btnConfirm.setVisibility(View.GONE);

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_confirm);
        btnCancel.setVisibility(View.GONE);

        dialog.setOnDismissListener(d -> Blurry.delete((ViewGroup) rootView));

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactionsForDate(selectedDate, userId);
    }

    @Override
    public void onTransactionDeleted(String date, int userId) {
        loadTransactionsForDate(date, userId);
    }

    public void loadTransactionsForDate(String selectedDate, int userId) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            List<UserTransaction> transactions = dbHelper.getTransactionsByDate(userId, selectedDate);
            if (transactions.isEmpty()) {
                tvNoData.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvNoData.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                TransactionsAdapter adapter = new TransactionsAdapter(transactions, getContext(), this,
                        this::openEditTransactionDialog, selectedDate);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        } catch (Exception e) {
            alertSuccess(
                    requireContext(),
                    "Error",
                    e.getMessage(),
                    R.drawable.background_alert
            );
        }

        updateIncomeAndExpenseTextViews(userId, selectedDate);
    }

    @SuppressLint("Recycle")
    private void startFadeAnimation(TextView textView) {
        ObjectAnimator fadeInOut = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
        fadeInOut.setDuration(500);

        ObjectAnimator moveX = ObjectAnimator.ofFloat(textView, "translationX", 50f, 0f);
        moveX.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeInOut, moveX);

        animatorSet.start();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void updateIncomeAndExpenseTextViews(int userId, String selectedDate) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            Pair<Double, Double> totals = dbHelper.getIncomeAndExpenseByDate(userId, selectedDate);
            tvIncome.setText("$ " + String.format("%.2f", totals.first));
            tvExpense.setText("$ " + String.format("%.2f", totals.second));
            if (totals.first != 0.0 && totals.second != 0.0) {
                startFadeAnimation(tvIncome);
                startFadeAnimation(tvExpense);
            } else if (totals.first != 0.0) {
                startFadeAnimation(tvIncome);
            } else if (totals.second != 0.0) {
                startFadeAnimation(tvExpense);
            }
        } catch (Exception e) {
            alertSuccess(
                    requireContext(),
                    "Error",
                    e.getMessage(),
                    R.drawable.background_alert
            );
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void textGradient(TextView textView) {
        TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());

        Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                new int[]{
                        Color.parseColor("#FF6464"),
                        Color.parseColor("#6464FF")
                }, null, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(textShader);
    }
}
