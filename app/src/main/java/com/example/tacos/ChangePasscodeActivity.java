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
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import javax.crypto.SecretKey;

import jp.wasabeef.blurry.Blurry;

public class ChangePasscodeActivity extends AppCompatActivity {
    private EditText etOldPasscode, etNewPasscode, etNewPasscodeConfirm;
    private LinearLayout llChangePasscode;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_passcode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.change_passcode), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        etOldPasscode = findViewById(R.id.et_old_passcode);
        etNewPasscode = findViewById(R.id.et_new_passcode);
        etNewPasscodeConfirm = findViewById(R.id.et_new_passcode_confirm);
        CheckBox cbShowOldPasscode = findViewById(R.id.cb_show_old_passcode);
        CheckBox cbShowNewPasscode = findViewById(R.id.cb_show_new_passcode);
        Button btnConfirm = findViewById(R.id.btn_confirm_change_passcode);
        Button btnCancel = findViewById(R.id.btn_cancel_change_passcode);
        llChangePasscode = findViewById(R.id.ll_change_passcode);

        llChangePasscode.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
                llChangePasscode.clearFocus();
            }
            return false;
        });

        cbShowOldPasscode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etOldPasscode.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etOldPasscode.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etOldPasscode.setSelection(etOldPasscode.getText().length());
        });

        cbShowNewPasscode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etNewPasscode.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                etNewPasscodeConfirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etNewPasscode.setTransformationMethod(PasswordTransformationMethod.getInstance());
                etNewPasscodeConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etNewPasscode.setSelection(etNewPasscode.getText().length());
            etNewPasscodeConfirm.setSelection(etNewPasscodeConfirm.getText().length());
        });

        btnConfirm.setOnClickListener(v -> {
            hideKeyboard();
            try {
                if (validateInput()) {
                    int userId = getIntent().getIntExtra("userId", -1);
                    String newPasscode = etNewPasscode.getText().toString().trim();
                    String secretKeyStringFromDB = getIntent().getStringExtra("secretKeyStringFromDB");
                    SecretKey secretKey = EncryptionUtils.stringToKey(secretKeyStringFromDB);
                    String encryptedNewPasscode = EncryptionUtils.encrypt(newPasscode, secretKey);

                    try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
                        dbHelper.updatePasscode(userId, encryptedNewPasscode);
                        Toast.makeText(this, "Passcode updated successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Log.e("changePasscode", "Error updating passcode", e.getCause());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean validateInput() throws Exception {
        String oldPasscode = etOldPasscode.getText().toString().trim();
        String newPasscode = etNewPasscode.getText().toString().trim();
        String newPasscodeConfirm = etNewPasscodeConfirm.getText().toString().trim();
        String encryptedPasscodeFromDB = getIntent().getStringExtra("encryptedPasscodeFromDB");
        String secretKeyStringFromDB = getIntent().getStringExtra("secretKeyStringFromDB");
        SecretKey secretKey = EncryptionUtils.stringToKey(secretKeyStringFromDB);
        String encryptedOldPasscode = EncryptionUtils.encrypt(oldPasscode, secretKey);

        if (oldPasscode.isEmpty() || newPasscode.isEmpty() || newPasscodeConfirm.isEmpty()) {
            alertSuccess(
                    this,
                    "Empty fields",
                    "Please fill in all fields!",
                    R.drawable.dangerous_alert
            );
            return false;
        } else if (!(encryptedOldPasscode.equals(encryptedPasscodeFromDB))) {
            alertSuccess(
                    this,
                    "Incorrect old passcode",
                    "You entered the old password incorrectly!",
                    R.drawable.dangerous_alert
            );
            return false;
        } else if (newPasscode.length() != 6 || !newPasscode.matches("\\d+")) {
            alertSuccess(
                    this,
                    "Invalid new passcode",
                    "New passcode must be 6 digits!",
                    R.drawable.dangerous_alert
            );
            return false;
        } else if (!newPasscode.equals(newPasscodeConfirm)) {
            alertSuccess(
                    this,
                    "Invalid new passcode",
                    "New passcodes do not match!",
                    R.drawable.dangerous_alert
            );
            return false;
        }

        return true;
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
}
