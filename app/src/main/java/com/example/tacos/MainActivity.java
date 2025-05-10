package com.example.tacos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import jp.wasabeef.blurry.Blurry;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomMenu;

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        detectKeyboard();

        TextView tvAppName = findViewById(R.id.title);
        animateTextView(tvAppName);

        if (savedInstanceState == null) {
            String phone = getIntent().getStringExtra("userPhone");
            HomeFragment homeFragment = new HomeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("userPhone", phone);
            homeFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        }

        ImageView ivLogout = findViewById(R.id.iv_logout);
        ivLogout.setOnClickListener(v ->
                alertSuccess(MainActivity.this,
                        "Logout",
                        "Are you sure you want to logout?",
                        "Logout",
                        R.drawable.background_alert,
                        v1 -> {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                )
        );
        ivLogout.setOnTouchListener((v, event) -> {
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

        bottomMenu = findViewById(R.id.bottom_menu);
        bottomMenu.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home) {
                replaceFragment(new HomeFragment(), "CEM");
                return true;
            } else if (id == R.id.transactions) {
                replaceFragment(new TransactionsFragment(), "Transactions");
                return true;
            } else if (id == R.id.add) {
                replaceFragment(new AddFragment(), "Add transaction");
                return true;
            } else if (id == R.id.budgets) {
                replaceFragment(new BudgetsFragment(), "Budgets");
                return true;
            } else if (id == R.id.profile) {
                replaceFragment(new ProfileFragment(), "Profile");
                return true;
            } else {
                return false;
            }
        });
    }

    private void replaceFragment(Fragment fragment, String title) {
        String phone = getIntent().getStringExtra("userPhone");
        Bundle bundle = new Bundle();
        bundle.putString("userPhone", phone);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        TextView tvAppName = findViewById(R.id.title);
        tvAppName.setText(title);
        animateTextView(tvAppName);
    }

    public void alertSuccess(Context context, String title, String message, String confirmButtonText,
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

    private void detectKeyboard() {
        final View rootView = findViewById(R.id.main);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > 100) {
                bottomMenu.setVisibility(View.GONE);
            } else {
                bottomMenu.setVisibility(View.VISIBLE);
            }
        });
    }
}