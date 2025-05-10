package com.example.tacos.view_model;

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
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tacos.database.EncryptionUtils;
import com.example.tacos.R;
import com.example.tacos.database.DatabaseHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.crypto.SecretKey;

import jp.wasabeef.blurry.Blurry;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etPhone, etPasscode, etPasscodeConfirm;
    private LinearLayout llRegister;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        TextView tvAppName = findViewById(R.id.tv_app_name);
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etPasscode = findViewById(R.id.et_passcode);
        etPasscodeConfirm = findViewById(R.id.et_passcode_confirm);
        CheckBox cbShowPasscode = findViewById(R.id.cb_show_passcode);
        Button btnSubmit = findViewById(R.id.btn_submit);
        Button btnCancel = findViewById(R.id.btn_cancel);
        llRegister = findViewById(R.id.ll_register);

        textGradient(tvAppName);

        btnSubmit.setOnClickListener(v -> {
            if (validateInput()) {
                String fullName = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String passcode = etPasscode.getText().toString().trim();
                LocalDateTime currentDateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String formattedDateTime = currentDateTime.format(formatter);

                try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
                    if (dbHelper.isPhoneExists(phone)) {
                        alertSuccess(
                                RegisterActivity.this,
                                "Phone already exists!",
                                "This phone number is already registered.\n" +
                                        "Please use another phone number!",
                                R.drawable.dangerous_alert
                        );
                    } else {
                        SecretKey secretKey = EncryptionUtils.generateKey();
                        String encryptedPasscode = EncryptionUtils.encrypt(passcode, secretKey);
                        String secretKeyString = EncryptionUtils.keyToString(secretKey);
                        dbHelper.addUser(formatFullName(fullName), phone, encryptedPasscode, secretKeyString, formattedDateTime);
                        Intent intent = new Intent(RegisterActivity.this, SuccessRegisterActivity.class);
                        intent.putExtra("userPhone", phone);
                        intent.putExtra("secretKey", secretKeyString);
                        startActivity(intent);
                        finish();
                    }
                } catch (Exception e) {
                    Log.e("RegisterActivity", "Error: " + e.getMessage());
                }
            }
        });


        btnCancel.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        cbShowPasscode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPasscode.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                etPasscodeConfirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPasscode.setTransformationMethod(PasswordTransformationMethod.getInstance());
                etPasscodeConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPasscode.setSelection(etPasscode.getText().length());
            etPasscodeConfirm.setSelection(etPasscodeConfirm.getText().length());
        });

        etName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateFocus(v);
            } else {
                resetAnimation(v);
            }
        });

        etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateFocus(v);
            } else {
                resetAnimation(v);
            }
        });

        etPasscode.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateFocus(v);
            } else {
                resetAnimation(v);
            }
        });

        etPasscodeConfirm.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateFocus(v);
            } else {
                resetAnimation(v);
            }
        });

        llRegister.setOnTouchListener((v, event) -> {
            hideKeyboard();
            llRegister.clearFocus();
            return true;
        });

        animateTextView(tvAppName);
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

    public String formatFullName(String fullName) {
        String[] words = fullName.toLowerCase().split("\\s+");

        StringBuilder formattedName = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formattedName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return formattedName.toString().trim();
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

    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String passcode = etPasscode.getText().toString().trim();
        String passcodeConfirm = etPasscodeConfirm.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || passcode.isEmpty() || passcodeConfirm.isEmpty()) {
            alertSuccess(
                    RegisterActivity.this,
                    "Empty fields",
                    "Please fill in all fields!",
                    R.drawable.dangerous_alert
            );
            return false;
        } else if (phone.length() != 10) {
            alertSuccess(
                    RegisterActivity.this,
                    "Invalid phone number",
                    "Phone number must be 10 digits.",
                    R.drawable.dangerous_alert
            );
            return false;
        } else if (!phone.startsWith("0")) {
            alertSuccess(
                    RegisterActivity.this,
                    "Invalid phone number",
                    "Phone number must start with 0.",
                    R.drawable.dangerous_alert
            );
            return false;
        } else if (!phone.matches("\\d+")) {
            alertSuccess(
                    RegisterActivity.this,
                    "Invalid phone number",
                    "Phone number must contain only digits.",
                    R.drawable.dangerous_alert
            );
            return false;
        } else if (passcode.length() != 6 || !passcode.matches("\\d+")) {
            alertSuccess(
                    RegisterActivity.this,
                    "Invalid passcode",
                    "Passcode must be 6 digits.",
                    R.drawable.dangerous_alert
            );
            return false;
        } else if (!passcodeConfirm.equals(passcode)) {
            alertSuccess(
                    RegisterActivity.this,
                    "Invalid passcode",
                    "Passcode does not match.",
                    R.drawable.dangerous_alert
            );
            return false;
        }
        return true;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
