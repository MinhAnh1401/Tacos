package com.example.tacos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.MotionEvent;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import javax.crypto.SecretKey;

import jp.wasabeef.blurry.Blurry;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSION = 1;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
            dbHelper.getWritableDatabase();
            dbHelper.checkDatabase();
        } catch (Exception e) {
            alertSuccess(
                    LoginActivity.this,
                    "Error",
                    e.getMessage(),
                    R.drawable.dangerous_alert
            );
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
        } else {
            accessDatabase();
        }

        TextView tvAppName = findViewById(R.id.tv_app_name);
        EditText etPasscode = findViewById(R.id.et_passcode);
        EditText etPhone = findViewById(R.id.et_phone);
        CheckBox cbShowPasscode = findViewById(R.id.cb_show_passcode);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnRegister = findViewById(R.id.btn_register);
        LinearLayout llLogin = findViewById(R.id.ll_login);

        textGradient(tvAppName);

        cbShowPasscode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPasscode.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPasscode.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPasscode.setSelection(etPasscode.getText().length());
        });

        btnLogin.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String passcode = etPasscode.getText().toString().trim();
            Log.d("LoginActivity", "Phone: " + phone + ", Passcode: " + passcode);

            try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
                String encryptedPasscodeFromDB = dbHelper.getEncryptedPasscode(phone);
                String secretKeyStringFromDB = dbHelper.getSecretKey(phone);
                if (encryptedPasscodeFromDB != null && secretKeyStringFromDB != null) {
                    SecretKey secretKey = EncryptionUtils.stringToKey(secretKeyStringFromDB);
                    String encryptedPasscode = EncryptionUtils.encrypt(passcode, secretKey);

                    if (encryptedPasscode.equals(encryptedPasscodeFromDB)) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("userPhone", phone);
                        startActivity(intent);
                        finish();
                    } else {
                        alertSuccess(
                                LoginActivity.this,
                                "Login failed",
                                "Phone or passcode is incorrect!",
                                R.drawable.dangerous_alert
                        );
                    }
                } else {
                    alertSuccess(
                            LoginActivity.this,
                            "Login failed",
                            "Phone or passcode is incorrect!",
                            R.drawable.dangerous_alert
                    );
                }
            } catch (Exception e) {
                alertSuccess(
                        LoginActivity.this,
                        "Error",
                        e.getMessage(),
                        R.drawable.dangerous_alert
                );
            }
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
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

        llLogin.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
                llLogin.clearFocus();
            }
            return false;
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

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessDatabase();
            }
        }
    }

    private void accessDatabase() {

    }
}
