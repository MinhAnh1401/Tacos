package com.example.tacos.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tacos.adapter.CategoryPagerAdapter;
import com.example.tacos.database.DatabaseHelper;
import com.example.tacos.R;
import com.example.tacos.model.User;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import jp.wasabeef.blurry.Blurry;

public class AddFragment extends Fragment {
    private TextView tvTotalBalance;
    private TextView tvAddCategory;
    private EditText etAddAmount, etAddDate, etAddTime, etCategoryName, etAddNote;
    private ImageButton ibClear, ibClearNote;
    private ImageView ibExtend, ibPlus, ibMinus, ivAddCategory;
    private Button btnReset, btnAdd, btnCreateCategory;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private RadioButton rbExpense, rbIncome;
    private CategoryPagerAdapter categoryAdapter;
    private LinearLayout llCategoryType;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        tvTotalBalance = view.findViewById(R.id.tv_amount_total_balance);
        etAddAmount = view.findViewById(R.id.et_add_amount);
        tvAddCategory = view.findViewById(R.id.tv_add_category);
        etAddDate = view.findViewById(R.id.et_add_date);
        etAddTime = view.findViewById(R.id.et_add_time);
        etCategoryName = view.findViewById(R.id.et_category_name);
        etAddNote = view.findViewById(R.id.et_add_note);
        ibClear = view.findViewById(R.id.ib_clear);
        ibClearNote = view.findViewById(R.id.ib_clear_note);
        ibExtend = view.findViewById(R.id.ib_extend);
        ivAddCategory = view.findViewById(R.id.iv_add_category);
        btnReset = view.findViewById(R.id.btn_reset);
        btnAdd = view.findViewById(R.id.btn_add);
        btnCreateCategory = view.findViewById(R.id.btn_create_category);
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        ibPlus = view.findViewById(R.id.ib_plus);
        ibMinus = view.findViewById(R.id.ib_minus);
        rbExpense = view.findViewById(R.id.rb_expense);
        rbIncome = view.findViewById(R.id.rb_income);
        llCategoryType = view.findViewById(R.id.ll_category_type);

        loadData();
        return view;
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale", "SimpleDateFormat"})
    private void loadData() {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                String phone = bundle.getString("userPhone");
                User user = dbHelper.getUser(phone);

                if (user != null) {
                    double totalBalance = dbHelper.getTotalBalanceAllTime(user.getUserId());
                    String formattedTotalBalance = String.format("%.2f", totalBalance);
                    tvTotalBalance.setText("$ " + formattedTotalBalance);

                    LocalDateTime now = LocalDateTime.now();

                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    etAddDate.setText(now.format(dateFormatter));

                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    etAddTime.setText(now.format(timeFormatter));

                    etAddDate.setOnClickListener(v -> {
                        final Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                                (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                                    calendar.set(selectedYear, selectedMonth, selectedDay);
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    etAddDate.setText(dateFormat.format(calendar.getTime()));
                                }, year, month, day);

                        datePickerDialog.show();
                        hideKeyboard(v);
                    });

                    etAddTime.setOnClickListener(v -> {
                        final Calendar calendar = Calendar.getInstance();
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                                (timePicker, selectedHour, selectedMinute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    calendar.set(Calendar.MINUTE, selectedMinute);
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                    etAddTime.setText(timeFormat.format(calendar.getTime()));
                                }, hour, minute, true);

                        timePickerDialog.show();
                        hideKeyboard(v);
                    });

                    ibClear.setOnClickListener(v -> etAddAmount.setText(""));

                    ibClearNote.setOnClickListener(v -> etAddNote.setText(""));

                    categoryAdapter = new CategoryPagerAdapter(this, user.getUserId());

                    viewPager.setAdapter(categoryAdapter);

                    new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                        switch (position) {
                            case 0:
                                tab.setText("Expense");
                                break;
                            case 1:
                                tab.setText("Income");
                                break;
                        }
                    }).attach();

                    setupButtonListeners();

                    btnCreateCategory.setOnClickListener(v -> {
                        hideKeyboard(v);

                        String categoryType = rbExpense.isChecked() ? "Expense" : "Income";
                        String categoryName = etCategoryName.getText().toString().trim();

                        if (categoryName.isEmpty()) {
                            Toast.makeText(getContext(), "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                            etCategoryName.findFocus();
                        } else if (!rbExpense.isChecked() && !rbIncome.isChecked()) {
                            Toast.makeText(getContext(), "PLease select category type", Toast.LENGTH_SHORT).show();
                            rbExpense.findFocus();
                        } else {
                            dbHelper.createCategory(user.getUserId(), capitalizeFirstLetter(categoryName), categoryType);
                            tvAddCategory.setText(capitalizeFirstLetter(etCategoryName.getText().toString().trim()));
                            etCategoryName.setText("");
                            llCategoryType.setVisibility(View.GONE);
                            ibMinus.setVisibility(View.GONE);

                            loadData();
                        }
                    });

                    btnAdd.setOnClickListener(v -> {
                        if (validateInput()) {
                            String amount = etAddAmount.getText().toString().trim();
                            double amountValue = Double.parseDouble(amount);
                            String category = tvAddCategory.getText().toString().trim();
                            int categoryId = dbHelper.getCategoryId(user.getUserId(), category);
                            String note = capitalizeFirstLetter(etAddNote.getText().toString().trim());
                            String date = etAddDate.getText().toString().trim() + " " +
                                            etAddTime.getText().toString().trim();

                            dbHelper.addTransaction(user.getUserId(), categoryId, amountValue, date, note);
                            hideKeyboard(v);

                            alertSuccess(
                                    getContext(),
                                    "Transaction added successfully",
                                    "Transaction date: " + date + "\n\n" +
                                            "Category: " + category + "\n" +
                                            "Amount: $ " + amount + "\n" +
                                            "Note: " + note,
                                    R.drawable.background_alert
                            );

                            resetFields();
                            loadData();

                            Bundle result = new Bundle();
                            result.putBoolean("isTransactionAdded", true);
                            getParentFragmentManager().setFragmentResult("addTransaction", result);
                        }
                    });

                    btnReset.setOnClickListener(v -> resetFields());
                }
            }
        } catch (Exception e) {
            alertSuccess(
                    getContext(),
                    "Error",
                    e.getMessage(),
                    R.drawable.dangerous_alert
            );
        }
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

    boolean isExtended = false;

    private void toggleExtendBudget() {
        if (isExtended) {
            ibExtend.animate()
                    .rotation(0f)
                    .setDuration(100)
                    .start();
        } else {
            ibExtend.animate()
                    .rotation(180f)
                    .setDuration(100)
                    .start();
        }
        isExtended = !isExtended;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonListeners() {
        setScaleAnimation(ibClear);
        setScaleAnimation(ibClearNote);
        setScaleAnimation(etAddAmount);
        setScaleAnimation(etAddNote);

        ivAddCategory.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    toggleExtendBudget();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });
        ivAddCategory.setOnClickListener(v -> {
            toggleCategoryType();
            refreshData();
        });
        tvAddCategory.setOnClickListener(v -> {
            toggleExtendBudget();
            toggleCategoryType();
            refreshData();
        });
        setScaleAnimation(tvAddCategory);
        ibExtend.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.9f)
                            .scaleY(0.9f)
                            .setDuration(100)
                            .start();
                    toggleExtendBudget();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                    break;
            }
            return false;
        });
        ibExtend.setOnClickListener(v -> {
            toggleCategoryType();
            refreshData();
        });
        setScaleAnimation(ibPlus);
        ibPlus.setOnClickListener(v -> {
            toggleCategoryDetails(true);
            refreshData();
        });

        setScaleAnimation(etCategoryName);

        setScaleAnimation(ibMinus);
        ibMinus.setOnClickListener(v -> {
            toggleCategoryDetails(false);
            refreshData();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setScaleAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }

    public void toggleCategoryType() {
        hideKeyboard(getView());
        tvAddCategory.findFocus();

        boolean isCategoryVisible = tabLayout.getVisibility() == View.VISIBLE;

        if (!isCategoryVisible && llCategoryType.getVisibility() == View.GONE) {
            tabLayout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
            ibPlus.setVisibility(View.VISIBLE);
            ibMinus.setVisibility(View.GONE);
            llCategoryType.setVisibility(View.GONE);
        } else if (!isCategoryVisible && llCategoryType.getVisibility() == View.VISIBLE) {
            tabLayout.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            ibPlus.setVisibility(View.GONE);
            ibMinus.setVisibility(View.GONE);
            llCategoryType.setVisibility(View.GONE);
        } else {
            tabLayout.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            ibPlus.setVisibility(View.GONE);
            ibMinus.setVisibility(View.GONE);
            llCategoryType.setVisibility(View.GONE);
        }
        refreshData();
    }

    private void toggleCategoryDetails(boolean showDetails) {
        if (showDetails) {
            if (tabLayout.getVisibility() == View.VISIBLE) {
                tabLayout.setVisibility(View.GONE);
                viewPager.setVisibility(View.GONE);
                ibPlus.setVisibility(View.GONE);
                llCategoryType.setVisibility(View.VISIBLE);
                ibMinus.setVisibility(View.VISIBLE);
            }
        } else {
            hideKeyboard(llCategoryType);
            if (llCategoryType.getVisibility() == View.VISIBLE) {
                tabLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.VISIBLE);
                ibPlus.setVisibility(View.VISIBLE);
                llCategoryType.setVisibility(View.GONE);
                ibMinus.setVisibility(View.GONE);
            }
        }
        refreshData();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshData() {
        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }
    }

    public String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }


    public void setCategoryToEditText(String categoryName) {
        if (viewPager != null) {
            tvAddCategory.setText(categoryName);
            tabLayout.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            ibPlus.setVisibility(View.GONE);
        } else {
            Log.e("AddFragment", "tvAddCategory is null");
        }
    }

    private void resetFields() {
        etAddAmount.setText("");
        tvAddCategory.setText("");
        etAddNote.setText("");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        etAddDate.setText(now.format(dateFormatter));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        etAddTime.setText(now.format(timeFormatter));
    }

    private boolean validateInput() {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            Bundle bundle = getArguments();
            assert bundle != null;
            String phone = bundle.getString("userPhone");
            User user = dbHelper.getUser(phone);

            String amount = etAddAmount.getText().toString().trim();
            String category = tvAddCategory.getText().toString().trim();
            int categoryId = dbHelper.getCategoryId(user.getUserId(), category);
            String categoryType = dbHelper.getCategoryType(user.getUserId(), categoryId);
            String note = etAddNote.getText().toString().trim();
            String date = etAddDate.getText().toString().trim();
            String time = etAddTime.getText().toString().trim();

            if (amount.isEmpty() || category.isEmpty() || note.isEmpty() ||
                    date.isEmpty() || time.isEmpty()) {
                alertSuccess(
                        getContext(),
                        "Empty fields",
                        "Please fill in all fields!",
                        R.drawable.dangerous_alert
                );
                return false;
            }

            double amountValue = Double.parseDouble(amount);
            double totalBalance = Double.parseDouble(tvTotalBalance.getText().toString()
                                                                .substring(2));

            if (amountValue > totalBalance && categoryType.equals("Expense")) {
                alertSuccess(
                        getContext(),
                        "Insufficient balance",
                        "You don't have enough balance to add this transaction!",
                        R.drawable.dangerous_alert
                );
                return false;
            } else if (amountValue <= 0) {
                alertSuccess(
                        getContext(),
                        "Amount",
                        "Invalid amount",
                        R.drawable.dangerous_alert
                );
                return false;
            }

            if (LocalDate.parse(date).isEqual(LocalDate.now()) &&
                LocalTime.parse(time).isAfter(LocalTime.now())) {
                alertSuccess(
                        getContext(),
                        "Time",
                        "Time cannot be in the future",
                        R.drawable.dangerous_alert
                );
                return false;
            } else if (LocalDate.parse(date).isAfter(LocalDate.now())) {
                alertSuccess(
                        getContext(),
                        "Date",
                        "Date cannot be in the future",
                        R.drawable.dangerous_alert
                );
                return false;
            }
            return true;
        } catch (Exception e) {
            alertSuccess(
                    getContext(),
                    "Error",
                    e.getMessage(),
                    R.drawable.dangerous_alert
            );
            return false;
        }
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
