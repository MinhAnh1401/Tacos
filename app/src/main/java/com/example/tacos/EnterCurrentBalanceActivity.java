package com.example.tacos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jp.wasabeef.blurry.Blurry;

public class EnterCurrentBalanceActivity extends AppCompatActivity {
    private EditText etCurrentBalance;

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enter_current_balance);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.enter_current_balance), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        TextView tvEnterCurrentBalance = findViewById(R.id.tv_enter_current_balance);
        animateTextView(tvEnterCurrentBalance);

        etCurrentBalance = findViewById(R.id.et_current_balance);
        Button btnNext = findViewById(R.id.btn_next_2);

        etCurrentBalance.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateFocus(v);
            } else {
                resetAnimation(v);
            }
        });

        String phone = getIntent().getStringExtra("userPhone");
        try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
            User user = dbHelper.getUser(phone);
            int userId = user.getUserId();

            createDefaultCategories(dbHelper, userId);

            btnNext.setOnClickListener(v -> {
                String currentBalanceString = etCurrentBalance.getText().toString().trim();

                if (currentBalanceString.isEmpty()) {
                    alertSuccess(
                            EnterCurrentBalanceActivity.this,
                            "Empty field",
                            "Please enter your current balance!",
                            R.drawable.dangerous_alert
                    );
                } else if (Double.parseDouble(currentBalanceString) < 0) {
                    alertSuccess(
                            EnterCurrentBalanceActivity.this,
                            "Invalid balance",
                            "Current balance cannot be negative!",
                            R.drawable.dangerous_alert
                    );
                } else {
                    double currentBalance = Double.parseDouble(currentBalanceString);

                    int categoryId = dbHelper.getCategoryId(userId, "Other income");
                    LocalDateTime dateTime = LocalDateTime.now();
                    String date = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    dbHelper.addTransaction(userId, categoryId, currentBalance, date, "Current balance");

                    Intent intent = new Intent(EnterCurrentBalanceActivity.this, MainActivity.class);
                    intent.putExtra("userPhone", phone);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            alertSuccess(
                    EnterCurrentBalanceActivity.this,
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

    private void createDefaultCategories(DatabaseHelper dbHelper, int userId) {
        try {
            dbHelper.createCategory(userId, "Food & Dining", "Expense");
            dbHelper.createCategory(userId, "Transportation", "Expense");
            dbHelper.createCategory(userId, "Shopping", "Expense");
            dbHelper.createCategory(userId, "Entertainment", "Expense");
            dbHelper.createCategory(userId, "Health & Fitness", "Expense");
            dbHelper.createCategory(userId, "Education", "Expense");
            dbHelper.createCategory(userId, "Salary", "Income");
            dbHelper.createCategory(userId, "Bonus", "Income");
            dbHelper.createCategory(userId, "Investment", "Income");
            dbHelper.createCategory(userId, "Freelance", "Income");
            dbHelper.createCategory(userId, "Business", "Income");
            dbHelper.createCategory(userId, "Other income", "Income");
        } catch (Exception e) {
            alertSuccess(
                    EnterCurrentBalanceActivity.this,
                    "Error",
                    e.getMessage(),
                    R.drawable.dangerous_alert
            );
        }
    }

    private void animateTextView(TextView textView) {
        textView.setTranslationY(-100f);
        textView.setAlpha(0f);

        textView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void animateFocus(View view) {
        view.animate().translationY(-10).setDuration(200).start();
    }

    private void resetAnimation(View view) {
        view.animate().translationY(0).setDuration(200).start();
    }
}
