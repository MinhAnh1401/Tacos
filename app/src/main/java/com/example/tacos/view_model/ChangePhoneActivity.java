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

import com.example.tacos.database.EncryptionUtils;
import com.example.tacos.R;
import com.example.tacos.database.DatabaseHelper;

import javax.crypto.SecretKey;

import jp.wasabeef.blurry.Blurry;

public class ChangePhoneActivity extends AppCompatActivity {
    private EditText etOldPhone, etNewPhone, etRequirePasscode;
    private LinearLayout llEditPhone;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_phone);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.change_phone), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        etOldPhone = findViewById(R.id.et_old_phone);
        etNewPhone = findViewById(R.id.et_new_phone);
        etRequirePasscode = findViewById(R.id.et_require_passcode);
        CheckBox cbShowRequirePasscode = findViewById(R.id.cb_show_require_passcode);
        Button btnConfirm = findViewById(R.id.btn_confirm);
        Button btnCancel = findViewById(R.id.btn_cancel);
        llEditPhone = findViewById(R.id.ll_edit_phone);

        llEditPhone.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
                llEditPhone.clearFocus();
            }
            return false;
        });
        
        cbShowRequirePasscode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etRequirePasscode.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etRequirePasscode.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etRequirePasscode.setSelection(etRequirePasscode.getText().length());
        });

        btnConfirm.setOnClickListener(v -> {
            hideKeyboard();
            try {
                if (validateInput()) {
                    int userId = getIntent().getIntExtra("userId", -1);
                    String newPhone = etNewPhone.getText().toString().trim();

                    try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
                        if (dbHelper.isPhoneExists(newPhone)) {
                            alertSuccess(
                                    ChangePhoneActivity.this,
                                    "Phone already exists!",
                                    "Please use another phone number!",
                                    R.drawable.dangerous_alert
                            );
                        } else {
                            dbHelper.updatePhone(userId, newPhone);
                            Toast.makeText(ChangePhoneActivity.this, "Phone number updated successfully!",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e) {
                        Log.e("Error", "Error updating phone: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        btnCancel.setOnClickListener(v -> finish());
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

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    
    private boolean validateInput() throws Exception {
        String oldPhone = etOldPhone.getText().toString().trim();
        String newPhone = etNewPhone.getText().toString().trim();
        String requirePasscode = etRequirePasscode.getText().toString().trim();
        String phone = getIntent().getStringExtra("userPhone");
        String encryptedPasscodeFromDB = getIntent().getStringExtra("encryptedPasscodeFromDB");
        String secretKeyStringFromDB = getIntent().getStringExtra("secretKeyStringFromDB");
        SecretKey secretKey = EncryptionUtils.stringToKey(secretKeyStringFromDB);
        String encryptedPasscode = EncryptionUtils.encrypt(requirePasscode, secretKey);
        
        if (oldPhone.isEmpty() || newPhone.isEmpty() || requirePasscode.isEmpty()) {
            alertSuccess(
                    ChangePhoneActivity.this,
                    "Empty fields",
                    "Please fill in all fields!",
                    R.drawable.dangerous_alert
            );
            return false;
        } else if (!oldPhone.equals(phone) ||
                oldPhone.length() != 10 ||
                !oldPhone.startsWith("0") ||
                !oldPhone.matches("\\d+")) {
            alertSuccess(
                    ChangePhoneActivity.this,
                    "Incorrect old phone number",
                    "You entered the old phone number incorrectly!",
                    R.drawable.dangerous_alert
            );
            return false;
        } else if (newPhone.equals(oldPhone)) {
            alertSuccess(
                    ChangePhoneActivity.this,
                    "Invalid new phone number",
                    "New phone number cannot be the same as old phone number!",
                    R.drawable.dangerous_alert
            );
            return false;
        } else if (newPhone.length() != 10 ||
                !newPhone.startsWith("0") ||
                !newPhone.matches("\\d+")) {
            alertSuccess(
                    ChangePhoneActivity.this,
                    "Invalid new phone number",
                    "New phone number must be 10 digits and start with 0!",
                    R.drawable.dangerous_alert
            );
            return false;
        } else if (!(encryptedPasscode.equals(encryptedPasscodeFromDB))) {
            alertSuccess(
                    ChangePhoneActivity.this,
                    "Incorrect passcode",
                    "You entered the passcode incorrectly!",
                    R.drawable.dangerous_alert
            );
            return false;
        }
        return true;
    }
}
