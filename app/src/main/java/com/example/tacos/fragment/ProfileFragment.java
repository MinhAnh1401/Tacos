package com.example.tacos.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tacos.view_model.ChangePasscodeActivity;
import com.example.tacos.view_model.ChangePhoneActivity;
import com.example.tacos.view_model.ChangeUserNameActivity;
import com.example.tacos.database.EncryptionUtils;
import com.example.tacos.view_model.LoginActivity;
import com.example.tacos.R;
import com.example.tacos.database.DatabaseHelper;
import com.example.tacos.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import javax.crypto.SecretKey;

import jp.wasabeef.blurry.Blurry;

public class ProfileFragment extends Fragment {
    private TextView tvUserName, tvActiveAccountDay, tvPhone, tvCreationDate, tvTotalBalance,
            tvTotalExpenseAllTime, tvTotalIncomeAllTime, tvTotalTransactions,
            tvTotalTransactionsExpense, tvTotalTransactionsIncome;
    private ImageView ivChangePhone, ivChangeUsername;
    private Button btnResetAccount, btnDeleteAccount, btnChangePasscode;

    private static final int MAX_PIN_ATTEMPTS = 3;
    private int pinAttemptCount = 0;
    private long lockoutEndTime = 0;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        loadData();
        return view;
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvActiveAccountDay = view.findViewById(R.id.tv_active_account_day);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvCreationDate = view.findViewById(R.id.tv_creation_date);
        tvTotalBalance = view.findViewById(R.id.tv_total_balance_all_time);
        tvTotalExpenseAllTime = view.findViewById(R.id.tv_total_expense_all_time);
        tvTotalIncomeAllTime = view.findViewById(R.id.tv_total_income_all_time);
        tvTotalTransactions = view.findViewById(R.id.tv_total_transactions);
        tvTotalTransactionsExpense = view.findViewById(R.id.tv_total_transactions_expense);
        tvTotalTransactionsIncome = view.findViewById(R.id.tv_total_transactions_income);
        ivChangePhone = view.findViewById(R.id.iv_change_phone);
        ivChangeUsername = view.findViewById(R.id.iv_change_user_name);
        btnChangePasscode = view.findViewById(R.id.btn_change_passcode);
        btnResetAccount = view.findViewById(R.id.btn_reset_account);
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale", "MissingInflatedId", "ClickableViewAccessibility"})
    private void loadData() {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            Bundle bundle = getArguments();
            if (bundle == null) return;

            String phone = bundle.getString("userPhone");
            User user = dbHelper.getUser(phone);
            if (user == null) {
                tvUserName.setText("User not found");
                return;
            }

            int userId = user.getUserId();
            String fullPhone = user.getPhone();
            String encryptedPasscodeFromDB = dbHelper.getEncryptedPasscode(phone);
            String secretKeyStringFromDB = dbHelper.getSecretKey(phone);

            String hiddenPhone = maskPhoneNumber(fullPhone);

            tvUserName.setText(user.getFullName());
            textGradient(tvUserName);
            setupCreationDate(dbHelper, userId);
            setupPhoneToggle(fullPhone, hiddenPhone);
            setupAccountStatistics(dbHelper, userId);

            ivChangeUsername.setOnTouchListener((v, event) -> {
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

            ivChangeUsername.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChangeUserNameActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("encryptedPasscodeFromDB", encryptedPasscodeFromDB);
                intent.putExtra("secretKeyStringFromDB", secretKeyStringFromDB);
                startActivity(intent);
            });

            ivChangePhone.setOnTouchListener((v, event) -> {
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

            ivChangePhone.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChangePhoneActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userPhone", phone);
                intent.putExtra("encryptedPasscodeFromDB", encryptedPasscodeFromDB);
                intent.putExtra("secretKeyStringFromDB", secretKeyStringFromDB);
                startActivity(intent);
            });

            btnChangePasscode.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChangePasscodeActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("encryptedPasscodeFromDB", encryptedPasscodeFromDB);
                intent.putExtra("secretKeyStringFromDB", secretKeyStringFromDB);
                startActivity(intent);
            });

            btnResetAccount.setOnClickListener(v ->
                showAlertDialog(
                        requireContext(),
                        "Reset account",
                        "Reset",
                        R.drawable.dangerous_alert,
                        v1 -> alertConfirm(
                                requireContext(),
                                "Reset account",
                                "Are you sure you want to reset your account?\n\n" +
                                "All your transaction and budget data will be deleted from the system," +
                                        " and your account will be reset to its original state.",
                                "Reset",
                                R.drawable.dangerous_alert,
                                v2 -> resetAccount(userId)
                        )
                )
            );

            btnDeleteAccount.setOnClickListener(v ->
                showAlertDialog(
                        requireContext(),
                        "Delete account",
                        "Delete",
                        R.drawable.dangerous_alert,
                        v1 -> alertConfirm(
                                requireContext(),
                                "Delete account",
                                "Are you sure you want to delete your account?\n\n" +
                                "All your data and account information will be deleted from the system," +
                                        " and you will no longer be able to log in to this account.",
                                "Delete",
                                R.drawable.dangerous_alert,
                                v2 -> deleteAccount(userId)
                        )
                )
            );
        } catch (Exception e) {
            alertSuccess(
                    requireContext(),
                    "Error",
                    e.getMessage(),
                    R.drawable.dangerous_alert
            );
        }
    }

    public void onResume() {
        super.onResume();
        loadData();
    }

    public void showAlertDialog(Context context, String title, String buttonText,
                                int backgroundResource, View.OnClickListener onSubmitListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_custom_layout, null);
        builder.setView(dialogView);

        LinearLayout llDialog = dialogView.findViewById(R.id.ll_dialog);
        llDialog.setBackgroundResource(backgroundResource);
        TextView tvTitle = dialogView.findViewById(R.id.dialog_title);
        tvTitle.setText(title);
        textGradient(tvTitle);

        EditText passcodeInput = dialogView.findViewById(R.id.passcode_input);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        Blurry.with(context)
                .radius(10)
                .sampling(4)
                .onto((ViewGroup) rootView);

        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnSubmit.setText(buttonText);
        btnSubmit.setOnClickListener(v -> {
            if (isValidPasscode(passcodeInput.getText().toString())) {
                onSubmitListener.onClick(v);
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Incorrect passcode!", Toast.LENGTH_SHORT).show();
            }
        });
        btnCancel.setOnClickListener(v1 -> dialog.dismiss());

        dialog.setOnDismissListener(d -> Blurry.delete((ViewGroup) rootView));

        dialog.show();
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

    public void alertConfirm(Context context, String title, String message, String confirmButtonText,
                             int backgroundResource, View.OnClickListener onConfirmListener) {
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
        btnConfirm.setText(confirmButtonText);
        btnConfirm.setOnClickListener(v -> {
            onConfirmListener.onClick(v);
            dialog.dismiss();
        });

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_confirm);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnDismissListener(d -> Blurry.delete((ViewGroup) rootView));

        dialog.show();
    }

    private boolean isValidPasscode(String inputPasscode) {
        if (System.currentTimeMillis() < lockoutEndTime) {
            alertSuccess(
                    requireContext(),
                    "Account locked",
                    "Please try again later!",
                    R.drawable.dangerous_alert
            );
            return false;
        }

        Bundle bundle = getArguments();
        if (bundle == null) return false;
        String phone = bundle.getString("userPhone");
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            User user = dbHelper.getUser(phone);
            if (user == null) return false;
            String encryptedPasscodeFromDB = dbHelper.getEncryptedPasscode(phone);
            String secretKeyStringFromDB = dbHelper.getSecretKey(phone);
            SecretKey secretKey = EncryptionUtils.stringToKey(secretKeyStringFromDB);
            String encryptedInputPasscode = EncryptionUtils.encrypt(inputPasscode, secretKey);
            if (encryptedInputPasscode.equals(encryptedPasscodeFromDB)) {
                pinAttemptCount = 0;
                return true;
            }
        } catch ( Exception e) {
            alertSuccess(
                    requireContext(),
                    "Error",
                    e.getMessage(),
                    R.drawable.dangerous_alert
            );
        }

        pinAttemptCount++;
        if (pinAttemptCount >= MAX_PIN_ATTEMPTS) {
            lockoutEndTime = System.currentTimeMillis() + (5 * 60 * 1000);
            alertSuccess(
                    requireContext(),
                    "Account locked",
                    "Please try again after 5 minutes!",
                    R.drawable.dangerous_alert
            );
        }
        return false;
    }

    private String maskPhoneNumber(String phone) {
        return phone.charAt(0) + "•••••" + phone.substring(phone.length() - 4);
    }

    @SuppressLint("SetTextI18n")
    private void setupCreationDate(DatabaseHelper dbHelper, int userId) {
        String creationDateTimeString = dbHelper.getCreationDate(userId);
        LocalDate creationDate = LocalDateTime.parse(creationDateTimeString,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate();
        tvCreationDate.setText(creationDate.toString());
        long activeAccountDays = ChronoUnit.DAYS.between(creationDate, LocalDate.now());
        tvActiveAccountDay.setText(activeAccountDays + " " + (activeAccountDays > 1 ? "days" : "day"));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPhoneToggle(String fullPhone, String hiddenPhone) {
        tvPhone.setText(hiddenPhone);
        tvPhone.setOnClickListener(v -> {
            String currentPhone = tvPhone.getText().toString();
            if (currentPhone.equals(hiddenPhone)) {
                tvPhone.setText(fullPhone);
            } else {
                tvPhone.setText(hiddenPhone);
            }
        });
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setupAccountStatistics(DatabaseHelper dbHelper, int userId) {
        double totalBalance = dbHelper.getTotalBalanceAllTime(userId);
        double totalExpense = dbHelper.getTotalExpenseIncomeAllTime(userId, "Expense");
        double totalIncome = dbHelper.getTotalExpenseIncomeAllTime(userId, "Income");
        int totalTransactions = dbHelper.getTotalTransactions(userId);
        int totalTransactionsExpense = dbHelper.getTotalTransactionsExpenseIncome(userId, "Expense");
        int totalTransactionsIncome = dbHelper.getTotalTransactionsExpenseIncome(userId, "Income");

        tvTotalBalance.setText("$ " + String.format("%.2f", totalBalance));
        tvTotalExpenseAllTime.setText("$ " + String.format("%.2f", totalExpense));
        tvTotalIncomeAllTime.setText("$ " + String.format("%.2f", totalIncome));
        tvTotalTransactions.setText(Integer.toString(totalTransactions));
        tvTotalTransactionsExpense.setText(Integer.toString(totalTransactionsExpense));
        tvTotalTransactionsIncome.setText(Integer.toString(totalTransactionsIncome));
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

    private void resetAccount(int userId) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            dbHelper.resetAccount(userId);
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("resetAccount", "Error resetting account: " + e.getMessage());
        }
    }

    private void deleteAccount(int userId) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            dbHelper.deleteAccount(userId);
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("deleteAccount", "Error deleting account: " + e.getMessage());
        }
    }
}