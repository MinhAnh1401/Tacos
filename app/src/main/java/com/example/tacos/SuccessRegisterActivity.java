package com.example.tacos;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SuccessRegisterActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_success_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.success_register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        TextView tvTitleRegistrationSuccessfully = findViewById(R.id.tv_title_registration_successfully);
        textGradient(tvTitleRegistrationSuccessfully);

        String phone = getIntent().getStringExtra("userPhone");

        Button btnGetStarted = findViewById(R.id.btn_get_started);

        animateTextView(tvTitleRegistrationSuccessfully);

        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(SuccessRegisterActivity.this, WelcomeNewUserActivity.class);
            intent.putExtra("userPhone", phone);
            startActivity(intent);
            finish();
        });
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
}
