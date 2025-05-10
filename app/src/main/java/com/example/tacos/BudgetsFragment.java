package com.example.tacos;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.blurry.Blurry;

public class BudgetsFragment extends Fragment implements OnBudgetDeletedListener {
    private EditText etAddAmount, etAddEndDate, etAddEndTime, etCategoryNameBudget, etFrequencyBudget, etAddNote;
    private TextView tvAmountTotalBudget, tvNoData, tvAddCategory, tvIncomeBudget, tvExpenseBudget;
    private RecyclerView recyclerView;
    private Button btnCreateBudget;
    private TabLayout tabLayoutBudget;
    private ViewPager2 viewPagerBudget;
    private ImageView ivExtendBudget, ivPlusBudget, ivMinusBudget, ivAddCategoryBudget;
    private LinearLayout llCategoryTypeBudget, llCreateBudget, llOverViewBudget;
    private Button btnCreateCategoryBudget, btnOpenBudgetForm;
    private RadioButton rbExpenseBudget, rbIncomeBudget;
    private CategoryPagerAdapter categoryAdapter;
    private ImageButton ibClearAmountBudget, ibClearNoteBudget;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budgets, container, false);

        initViews(view);

        getParentFragmentManager().setFragmentResultListener("addBudget", this, (requestKey, bundle1) -> {
            boolean isBudgetAdded = bundle1.getBoolean("isBudgetAdded");
            if (isBudgetAdded) {
                try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
                    Bundle bundle = getArguments();
                    assert bundle != null;
                    String phone = bundle.getString("userPhone");
                    User user = dbHelper.getUser(phone);
                    int userId = user.getUserId();
                    loadBudgets(userId);
                } catch (Exception e) {
                    alertSuccess(
                            getContext(),
                            "Error",
                            e.getMessage(),
                            R.drawable.dangerous_alert
                    );
                }
            }
        });

        loadData();
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.rv_budget);
        tvAmountTotalBudget = view.findViewById(R.id.tv_amount_total_budgets);
        tvIncomeBudget = view.findViewById(R.id.tv_amount_income_budget);
        tvExpenseBudget = view.findViewById(R.id.tv_amount_expense_budget);
        tvNoData = view.findViewById(R.id.tv_no_data_budget);
        btnCreateBudget = view.findViewById(R.id.btn_create_budget);
        etAddAmount = view.findViewById(R.id.et_add_amount_budget);
        tvAddCategory = view.findViewById(R.id.tv_add_category_budget);
        etAddEndDate = view.findViewById(R.id.et_add_end_date_budget);
        etAddEndTime = view.findViewById(R.id.et_add_end_time_budget);
        etCategoryNameBudget = view.findViewById(R.id.et_category_name_budget);
        etFrequencyBudget = view.findViewById(R.id.et_add_frequency_budget);
        etAddNote = view.findViewById(R.id.et_add_note_budget);
        tabLayoutBudget = view.findViewById(R.id.tabLayout_budget);
        viewPagerBudget = view.findViewById(R.id.viewPager_budget);
        ibClearAmountBudget = view.findViewById(R.id.ib_clear_budget);
        ibClearNoteBudget = view.findViewById(R.id.ib_clear_note_budget);
        ivExtendBudget = view.findViewById(R.id.iv_extend_budget);
        ivPlusBudget = view.findViewById(R.id.iv_plus_budget);
        ivAddCategoryBudget = view.findViewById(R.id.iv_add_category_budget);
        ivMinusBudget = view.findViewById(R.id.iv_minus_budget);
        llOverViewBudget = view.findViewById(R.id.ll_overview_budget);
        llCategoryTypeBudget = view.findViewById(R.id.ll_category_type_budget);
        llCreateBudget = view.findViewById(R.id.ll_create_budget);
        btnCreateCategoryBudget = view.findViewById(R.id.btn_create_category_budget);
        rbExpenseBudget = view.findViewById(R.id.rb_expense_budget);
        rbIncomeBudget = view.findViewById(R.id.rb_income_budget);
        btnOpenBudgetForm = view.findViewById(R.id.btn_open_budget_form);
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility", "ResourceType"})
    private void loadData() {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                String phone = bundle.getString("userPhone");
                User user = dbHelper.getUser(phone);

                if (user != null) {
                    int userId = user.getUserId();

                    updateTotalBudgetTextView(dbHelper.getTotalBudgetAllTime(userId));

                    updateIncomeExpenseBudgetTextView(userId);

                    llCreateBudget.setVisibility(View.GONE);
                    btnOpenBudgetForm.setOnClickListener(v -> {
                        if (llCreateBudget.getVisibility() == View.GONE) {
                            llCreateBudget.setVisibility(View.VISIBLE);
                        } else {
                            llCreateBudget.setVisibility(View.GONE);
                        }
                    });

                    LocalDateTime now = LocalDateTime.now();

                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    etAddEndDate.setText(now.format(dateFormatter));

                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    etAddEndTime.setText(now.format(timeFormatter));

                    etFrequencyBudget.setText("Monthly");

                    etAddEndDate.setOnClickListener(v -> {
                        final Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                                (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                                    calendar.set(selectedYear, selectedMonth, selectedDay);
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    etAddEndDate.setText(dateFormat.format(calendar.getTime()));
                                }, year, month, day);

                        datePickerDialog.show();
                        hideKeyboard(v);
                    });

                    etAddEndTime.setOnClickListener(v -> {
                        final Calendar calendar = Calendar.getInstance();
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                                (timePicker, selectedHour, selectedMinute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    calendar.set(Calendar.MINUTE, selectedMinute);
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                    etAddEndTime.setText(timeFormat.format(calendar.getTime()));
                                }, hour, minute, true);

                        timePickerDialog.show();
                        hideKeyboard(v);
                    });

                    etFrequencyBudget.setOnClickListener(v -> showRepeatOptionsDialog());

                    ibClearAmountBudget.setOnClickListener(v -> etAddAmount.setText(""));

                    ibClearNoteBudget.setOnClickListener(v -> etAddNote.setText(""));

                    categoryAdapter = new CategoryPagerAdapter(this, user.getUserId());

                    viewPagerBudget.setAdapter(categoryAdapter);

                    new TabLayoutMediator(tabLayoutBudget, viewPagerBudget, (tab, position) -> {
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

                    btnCreateCategoryBudget.setOnClickListener(v -> {
                        hideKeyboard(v);

                        String categoryType = rbExpenseBudget.isChecked() ? "Expense" : "Income";
                        String categoryName = etCategoryNameBudget.getText().toString().trim();

                        if (categoryName.isEmpty()) {
                            Toast.makeText(getContext(), "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                            etCategoryNameBudget.findFocus();
                        } else if (!rbExpenseBudget.isChecked() && !rbIncomeBudget.isChecked()) {
                            Toast.makeText(getContext(), "PLease select category type", Toast.LENGTH_SHORT).show();
                            rbExpenseBudget.findFocus();
                        } else {
                            dbHelper.createCategory(userId, capitalizeFirstLetter(categoryName), categoryType);
                            tvAddCategory.setText(capitalizeFirstLetter(etCategoryNameBudget.getText().toString().trim()));
                            etCategoryNameBudget.setText("");
                            llCategoryTypeBudget.setVisibility(View.GONE);
                            ivMinusBudget.setVisibility(View.GONE);
                        }
                    });

                    btnCreateBudget.setOnClickListener(v -> {
                        if (validateInput()) {
                            String amount = etAddAmount.getText().toString().trim();
                            double amountValue = Double.parseDouble(amount);
                            String category = tvAddCategory.getText().toString().trim();
                            int categoryId = dbHelper.getCategoryId(userId, category);
                            String note = capitalizeFirstLetter(etAddNote.getText().toString().trim());
                            String startDateTime = LocalDateTime.now().format(DateTimeFormatter
                                                                    .ofPattern("yyyy-MM-dd HH:mm"));
                            String endDateTime = etAddEndDate.getText().toString().trim() + " " +
                                                    etAddEndTime.getText().toString().trim();
                            String frequency = etFrequencyBudget.getText().toString().trim();

                            dbHelper.createBudget(userId, categoryId, amountValue, startDateTime,
                                                                    endDateTime, frequency, note);
                            hideKeyboard(v);

                            alertSuccess(
                                    getContext(),
                                    "Successful creation",
                                    "Frequency: " + frequency +  "\n" +
                                            "Creation date: " + startDateTime + "\n" +
                                            "Settle date: " + endDateTime + "\n\n" +
                                            "Category: " + category + "\n" +
                                            "Amount: $ " + amount + "\n" +
                                            "Note: " + note,
                                    R.drawable.background_alert
                            );

                            llCreateBudget.setVisibility(View.GONE);
                            resetFields();
                            loadData();

                            Bundle result = new Bundle();
                            result.putBoolean("isBudgetAdded", true);
                            getParentFragmentManager().setFragmentResult("addBudget", result);
                        }
                    });

                    loadBudgets(userId);
                } else {
                    alertSuccess(
                            getContext(),
                            "Error",
                            "User not found",
                            R.drawable.dangerous_alert
                    );
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
            ivExtendBudget.animate()
                    .rotation(0f)
                    .setDuration(100)
                    .start();
        } else {
            ivExtendBudget.animate()
                    .rotation(180f)
                    .setDuration(100)
                    .start();
        }
        isExtended = !isExtended;
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

    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonListeners() {
        setScaleAnimation(ibClearAmountBudget);
        setScaleAnimation(ibClearNoteBudget);
        setScaleAnimation(etAddAmount);
        ivAddCategoryBudget.setOnTouchListener((v, event) -> {
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

        ivAddCategoryBudget.setOnClickListener(v -> {
            toggleCategoryType();
            refreshData();
        });

        tvAddCategory.setOnClickListener(v -> {
            toggleExtendBudget();
            toggleCategoryType();
            refreshData();
        });

        setScaleAnimation(tvAddCategory);

        ivExtendBudget.setOnTouchListener((v, event) -> {
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

        ivExtendBudget.setOnClickListener(v -> {
            toggleCategoryType();
            refreshData();
        });

        setScaleAnimation(ivPlusBudget);
        ivPlusBudget.setOnClickListener(v -> {
            toggleCategoryDetails(true);
            refreshData();
        });

        setScaleAnimation(etCategoryNameBudget);

        setScaleAnimation(ivMinusBudget);
        ivMinusBudget.setOnClickListener(v -> {
            toggleCategoryDetails(false);
            refreshData();
        });
    }

    public void toggleCategoryType() {
        hideKeyboard(getView());
        tvAddCategory.findFocus();

        boolean isCategoryVisible = tabLayoutBudget.getVisibility() == View.VISIBLE;

        if (!isCategoryVisible && llCategoryTypeBudget.getVisibility() == View.GONE) {
            tabLayoutBudget.setVisibility(View.VISIBLE);
            viewPagerBudget.setVisibility(View.VISIBLE);
            ivPlusBudget.setVisibility(View.VISIBLE);
            ivMinusBudget.setVisibility(View.GONE);
            llCategoryTypeBudget.setVisibility(View.GONE);
        } else if (!isCategoryVisible && llCategoryTypeBudget.getVisibility() == View.VISIBLE){
            tabLayoutBudget.setVisibility(View.GONE);
            viewPagerBudget.setVisibility(View.GONE);
            ivPlusBudget.setVisibility(View.GONE);
            ivMinusBudget.setVisibility(View.GONE);
            llCategoryTypeBudget.setVisibility(View.GONE);
        } else {
            tabLayoutBudget.setVisibility(View.GONE);
            viewPagerBudget.setVisibility(View.GONE);
            ivPlusBudget.setVisibility(View.GONE);
            ivMinusBudget.setVisibility(View.GONE);
            llCategoryTypeBudget.setVisibility(View.GONE);
        }
        refreshData();
    }

    private void toggleCategoryDetails(boolean showDetails) {
        if (showDetails) {
            if (tabLayoutBudget.getVisibility() == View.VISIBLE) {
                tabLayoutBudget.setVisibility(View.GONE);
                viewPagerBudget.setVisibility(View.GONE);
                ivPlusBudget.setVisibility(View.GONE);
                llCategoryTypeBudget.setVisibility(View.VISIBLE);
                ivMinusBudget.setVisibility(View.VISIBLE);
            }
        } else {
            hideKeyboard(llCategoryTypeBudget);
            if (llCategoryTypeBudget.getVisibility() == View.VISIBLE) {
                tabLayoutBudget.setVisibility(View.VISIBLE);
                viewPagerBudget.setVisibility(View.VISIBLE);
                ivPlusBudget.setVisibility(View.VISIBLE);
                llCategoryTypeBudget.setVisibility(View.GONE);
                ivMinusBudget.setVisibility(View.GONE);
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

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void updateIncomeExpenseBudgetTextView(int userId) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            Pair<Double, Double> incomeExpenseBudgetAllTime = dbHelper.getIncomeExpenseBudgetAllTime(userId);
            double incomeBudget = incomeExpenseBudgetAllTime.first;
            double expenseBudget = incomeExpenseBudgetAllTime.second;
        tvIncomeBudget.setText("$ " + String.format("%.2f", incomeBudget));
        tvExpenseBudget.setText("$ " + String.format("%.2f", expenseBudget));
        } catch (Exception e) {
            alertSuccess(
                    getContext(),
                    "Error",
                    e.getMessage(),
                    R.drawable.dangerous_alert
            );
        }
    }

    private void showRepeatOptionsDialog() {
        String[] repeatOptions = {"Weekly", "Monthly", "Yearly"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select frequency of repeat");

        builder.setSingleChoiceItems(repeatOptions, -1, (dialog, which) -> {
            String selectedOption = repeatOptions[which];
            etFrequencyBudget.setText(selectedOption);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    public String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public void setCategoryToEditText(String categoryName) {
        tvAddCategory.setText(categoryName);
        tabLayoutBudget.setVisibility(View.GONE);
        viewPagerBudget.setVisibility(View.GONE);
        ivPlusBudget.setVisibility(View.GONE);
    }

    private void resetFields() {
        etAddAmount.setText("");
        tvAddCategory.setText("");
        etAddNote.setText("");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        etAddEndDate.setText(now.format(dateFormatter));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        etAddEndTime.setText(now.format(timeFormatter));
    }

    private boolean validateInput() {

        String amount = etAddAmount.getText().toString().trim();
        String category = tvAddCategory.getText().toString().trim();
        String note = etAddNote.getText().toString().trim();
        String endDate = etAddEndDate.getText().toString().trim();
        String endTime = etAddEndTime.getText().toString().trim();

        if (amount.isEmpty()) {
            Toast.makeText(getContext(), "Amount cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        double amountValue = Double.parseDouble(amount);

        if (amountValue <= 0) {
            Toast.makeText(getContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (category.isEmpty()) {
            Toast.makeText(getContext(), "Category cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (note.isEmpty()) {
            Toast.makeText(getContext(), "Note cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (LocalDate.parse(endDate).isEqual(LocalDate.now()) && LocalTime.parse(endTime).isBefore(LocalTime.now())) {
            Toast.makeText(getContext(), "Time cannot be in the past", Toast.LENGTH_SHORT).show();
            return false;
        } else if (LocalDate.parse(endDate).isBefore(LocalDate.now())) {
            Toast.makeText(getContext(), "Date cannot be in the past", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @SuppressLint("SimpleDateFormat")
    private void openEditBudgetDialog(Budget budget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_budget, null);
        builder.setView(dialogView);

        TextView tvTitleEditBudget = dialogView.findViewById(R.id.tv_title_edit_budget);
        textGradient(tvTitleEditBudget);
        EditText etCategoryName = dialogView.findViewById(R.id.et_category_edit_budget);
        EditText etAmount = dialogView.findViewById(R.id.et_amount_edit_budget);
        EditText etNote = dialogView.findViewById(R.id.et_note_edit_budget);
        EditText etEndDate = dialogView.findViewById(R.id.et_end_date);
        EditText etEndTime = dialogView.findViewById(R.id.et_end_time);
        EditText etFrequency = dialogView.findViewById(R.id.et_frequency);
        Button btnUpdate = dialogView.findViewById(R.id.btn_update_budget);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_edit_budget);

        etEndDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(selectedYear, selectedMonth, selectedDay);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        etEndDate.setText(dateFormat.format(calendar.getTime()));
                    }, year, month, day);

            datePickerDialog.show();
            hideKeyboard(v);
        });

        etEndTime.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                    (timePicker, selectedHour, selectedMinute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        etEndTime.setText(timeFormat.format(calendar.getTime()));
                    }, hour, minute, true);

            timePickerDialog.show();
            hideKeyboard(v);
        });

        etFrequency.setOnClickListener(v -> {
            String[] repeatOptionsOnEdit = {"Weekly", "Monthly", "Yearly"};

            AlertDialog.Builder builderRepeatOption = new AlertDialog.Builder(requireContext());
            builderRepeatOption.setTitle("Select frequency of repeat");

            builderRepeatOption.setSingleChoiceItems(repeatOptionsOnEdit, -1, (dialog, which) -> {
                String selectedOption = repeatOptionsOnEdit[which];
                etFrequency.setText(selectedOption);
                dialog.dismiss();
            });

            builderRepeatOption.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builderRepeatOption.create().show();
        });

        setDialogValues(budget, etCategoryName, etAmount, etEndDate, etEndTime, etFrequency, etNote);

        View rootView = ((Activity) requireContext()).getWindow().getDecorView().findViewById(android.R.id.content);
        Blurry.with(getContext())
                .radius(10)
                .sampling(4)
                .onto((ViewGroup) rootView);

        AlertDialog dialog = builder.create();
        setupDialogAppearance(dialog, dialogView);

        btnUpdate.setOnClickListener(v -> updateBudget(budget, etCategoryName, etAmount, etEndDate,
                etEndTime, etFrequency, etNote, dialog));
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnDismissListener(d -> Blurry.delete((ViewGroup) rootView));

        dialog.show();
    }

    private void setDialogValues(Budget budget, EditText etCategoryName, EditText etAmount,
                                 EditText etEndDate, EditText etEndTime, EditText etFrequency, EditText etNote) {
        etAmount.setText(String.valueOf(budget.getAmount()));
        etEndDate.setText(budget.getEndDate().toLocalDate().toString());
        etEndTime.setText(budget.getEndDate().toLocalTime().toString());
        etFrequency.setText(budget.getFrequency());
        etCategoryName.setText(budget.getCategoryName());
        etNote.setText(budget.getNote());
    }

    private void setupDialogAppearance(AlertDialog dialog, View dialogView) {
        dialog.setOnShowListener(dialogInterface -> {
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogView.setBackgroundResource(R.drawable.background_alert);
        });
    }

    private void updateBudget(Budget budget, EditText etCategoryName, EditText etAmount,
                              EditText etEndDate, EditText etEndTime, EditText etFrequency,
                              EditText etNote, AlertDialog dialog) {
        String amountString = etAmount.getText().toString();
        if (amountString.isEmpty() || etEndDate.getText().toString().isEmpty() ||
                etEndTime.getText().toString().isEmpty() || etFrequency.getText().toString().isEmpty() ||
                etNote.getText().toString().isEmpty() || etCategoryName.getText().toString().isEmpty()) {
            alertSuccess(
                    getContext(),
                    "Empty fields",
                    "Please fill in all fields!",
                    R.drawable.dangerous_alert
            );
            return;
        }
        budget.setAmount(Double.parseDouble(amountString));

        if (Double.parseDouble(amountString) <= 0) {
            alertSuccess(
                    getContext(),
                    "Invalid amount",
                    "Amount must be greater than 0",
                    R.drawable.dangerous_alert
            );
            return;
        }

        LocalDateTime dateTimeObject = LocalDateTime.parse(
                etEndDate.getText().toString() + " " + etEndTime.getText().toString(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
        if (dateTimeObject.isBefore(LocalDateTime.now())) {
            alertSuccess(
                    getContext(),
                    "Invalid date and time",
                    "Date and time cannot be in the past",
                    R.drawable.dangerous_alert
            );
            return;
        }
        budget.setEndDate(dateTimeObject);

        budget.setCategoryName(capitalizeFirstLetter(etCategoryName.getText().toString()));
        budget.setFrequency(etFrequency.getText().toString());
        budget.setNote(capitalizeFirstLetter(etNote.getText().toString()));

        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            dbHelper.updateBudget(budget);

            assert getArguments() != null;
            int userId = dbHelper.getUser(getArguments().getString("userPhone")).getUserId();
            updateTotalBudget(userId);
            loadBudgets(userId);

            dialog.dismiss();

            alertSuccess(
                    getContext(),
                    "Successful update",
                    "Frequency: " + budget.getFrequency() +  "\n" +
                            "Creation date: " + budget.getStartDate().format(DateTimeFormatter
                                                            .ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
                            "Settle date: " + budget.getEndDate().format(DateTimeFormatter
                                                            .ofPattern("yyyy-MM-dd HH:mm")) + "\n\n" +
                            "Category: " + budget.getCategoryName() + "\n" +
                            "Amount: $ " + budget.getAmount() + "\n" +
                            "Note: " + budget.getNote(),
                    R.drawable.background_alert
            );
        } catch (Exception e) {
            alertSuccess(
                    getContext(),
                    "Error",
                    e.getMessage(),
                    R.drawable.dangerous_alert
            );
        }
        onResume();
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        assert bundle != null;
        String phone = bundle.getString("userPhone");
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            User user = dbHelper.getUser(phone);
            int userId = user.getUserId();
            updateTotalBudget(userId);
            updateIncomeExpenseBudgetTextView(userId);
            loadBudgets(userId);
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void updateTotalBudgetTextView(double totalBudget) {
        if (totalBudget < 0) {
            tvAmountTotalBudget.setText("$ " + String.format("%.2f", Math.abs(totalBudget)));
            tvAmountTotalBudget.setTextColor(Color.parseColor("#640000"));
            tvAmountTotalBudget.setShadowLayer(10, 0, 0, Color.parseColor("#640000"));
        } else if (totalBudget > 0) {
            tvAmountTotalBudget.setText("$ " + String.format("%.2f", totalBudget));
            tvAmountTotalBudget.setTextColor(Color.parseColor("#006400"));
            tvAmountTotalBudget.setShadowLayer(10, 0, 0, Color.parseColor("#006400"));
        } else {
            tvAmountTotalBudget.setText("$ " + String.format("%.2f", totalBudget));
            tvAmountTotalBudget.setTextColor(Color.parseColor("#000000"));
        }
    }

    public void updateTotalBudget(int userId) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            double totalBudget = dbHelper.getTotalBudgetAllTime(userId);
            updateTotalBudgetTextView(totalBudget);
        } catch (Exception e) {
            alertSuccess(
                    getContext(),
                    "Error",
                    e.getMessage(),
                    R.drawable.dangerous_alert
            );
        }
    }

    @Override
    public void onBudgetDeleted(int userId) {
        onResume();
    }

    public void loadBudgets(int userId) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            List<Budget> budgets = dbHelper.getBudgets(userId);
            if (budgets.isEmpty()) {
                tvNoData.setVisibility(View.VISIBLE);
                llOverViewBudget.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvNoData.setVisibility(View.GONE);
                llOverViewBudget.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                BudgetsAdapter adapter = new BudgetsAdapter(budgets, getContext(), this,
                        this::openEditBudgetDialog);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
