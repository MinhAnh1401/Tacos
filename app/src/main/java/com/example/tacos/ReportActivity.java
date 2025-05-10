package com.example.tacos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.blurry.Blurry;

public class ReportActivity extends AppCompatActivity {
    private TextView tvReportTitle, tvReportTitle2, tvExpenseReport, tvIncomeReport,
                    tvExpenseComparedToLastMonth, tvIncomeComparedToLastMonth,
                    tvPercentageExpenseReport, tvPercentageIncomeReport;

    private ImageView ivBackInReport, ivArrowUpExpense, ivArrowDownExpense, ivArrowUpIncome, ivArrowDownIncome;

    private void initView(View view){
        tvReportTitle = findViewById(R.id.tv_report_title);
        tvReportTitle2 = findViewById(R.id.tv_report_title_2);
        tvExpenseReport = findViewById(R.id.tv_expense_report_value);
        tvIncomeReport = findViewById(R.id.tv_income_report_value);
        tvExpenseComparedToLastMonth = findViewById(R.id.tv_expense_compared_to_last_month);
        tvIncomeComparedToLastMonth = findViewById(R.id.tv_income_compared_to_last_month);
        tvPercentageExpenseReport = findViewById(R.id.tv_percentage_expense_report);
        tvPercentageIncomeReport = findViewById(R.id.tv_percentage_income_report);

        ivBackInReport = findViewById(R.id.iv_back_in_report);
        ivArrowUpExpense = findViewById(R.id.iv_arrow_up_expense);
        ivArrowDownExpense = findViewById(R.id.iv_arrow_down_expense);
        ivArrowUpIncome = findViewById(R.id.iv_arrow_up_income);
        ivArrowDownIncome = findViewById(R.id.iv_arrow_down_income);
    }

    @SuppressLint({"MissingInflatedId", "DefaultLocale", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.report_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        initView(findViewById(R.id.report_layout));

        int userId = getIntent().getIntExtra("userId", -1);

        setScaleAnimation(ivBackInReport);
        ivBackInReport.setOnClickListener(v -> finish());

        LocalDateTime now = LocalDateTime.now();

        String currentMonth = now.getMonth().toString().toLowerCase();
        String showCurrentMonth = currentMonth.substring(0, 1).toUpperCase() + currentMonth.substring(1);
        tvReportTitle.setText(" " + showCurrentMonth + " ");
        textGradient(tvReportTitle);

        tvReportTitle2.setText(" " + now.getYear() + " ");
        textGradient(tvReportTitle2);

        String previousMonth = now.minusMonths(1).getMonth().toString().toLowerCase();
        String showPreviousMonth = previousMonth.substring(0, 1).toUpperCase() + previousMonth.substring(1);
        tvExpenseComparedToLastMonth.setText(showPreviousMonth);
        tvIncomeComparedToLastMonth.setText(showPreviousMonth);

        try (DatabaseHelper db = new DatabaseHelper(this)) {
            double expenseAmountThisMonth = db.getTotalForCurrentMonth(userId, "Expense");
            tvExpenseReport.post(() -> tvExpenseReport.setText("$ " + String.format("%.2f", expenseAmountThisMonth)));

            double incomeAmountThisMonth = db.getTotalForCurrentMonth(userId, "Income");
            tvIncomeReport.setText("$ " + String.format("%.2f", incomeAmountThisMonth));

            loadExpenseIncomePercentage(userId, "Expense", ivArrowUpExpense, ivArrowDownExpense, tvPercentageExpenseReport);
            loadExpenseIncomePercentage(userId, "Income", ivArrowUpIncome, ivArrowDownIncome, tvPercentageIncomeReport);
        } catch (Exception e) {
            alertSuccess(
                    this,
                    "Error",
                    String.valueOf(e.getCause()),
                    R.drawable.dangerous_alert
            );
        }

        setupBarChart();
    }

    public void setupBarChart() {
        BarChart barChart = findViewById(R.id.bc_report);
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            int userId = getIntent().getIntExtra("userId", -1);

            Description description = new Description();
            description.setText("");
            barChart.setDescription(description);
            barChart.setDrawGridBackground(false);

            List<Double> expenseData = db.getExpenseIncome12MonthsThisYear(userId, "Expense");
            List<Double> incomeData = db.getExpenseIncome12MonthsThisYear(userId, "Income");

            List<BarEntry> expenseEntries = new ArrayList<>();
            List<BarEntry> incomeEntries = new ArrayList<>();
            String[] monthLabels = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                                                    "Aug", "Sep", "Oct", "Nov", "Dec"};

            for (int i = 0; i < 12; i++) {
                expenseEntries.add(new BarEntry(i, expenseData.get(i).floatValue()));
                incomeEntries.add(new BarEntry(i, incomeData.get(i).floatValue()));
            }

            BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Expense");
            expenseDataSet.setColor(Color.parseColor("#960000"));
            BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Income");
            incomeDataSet.setColor(Color.parseColor("#009600"));

            BarData barData = new BarData(expenseDataSet, incomeDataSet);
            barData.setBarWidth(0.2f);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(monthLabels));
            xAxis.setLabelCount(monthLabels.length);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);

            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f);
            leftAxis.setDrawLabels(false);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setDrawGridLines(false);
            barChart.getAxisRight().setEnabled(false);

            barData.setBarWidth(0.3f);
            float groupSpace = 0.25f;
            float barSpace = 0.05f;
            barChart.setData(barData);
            barChart.groupBars(0f, groupSpace, barSpace);

            xAxis.setAxisMaximum(12);
            barChart.animateY(1200);
            barChart.invalidate();
        } catch (Exception e) {
            alertSuccess(
                    this,
                    "Error",
                    String.valueOf(e.getCause()),
                    R.drawable.dangerous_alert
            );
        }
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

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void loadExpenseIncomePercentage(int userId, String categoryType, ImageView ivArrowUp, ImageView ivArrowDown, TextView tvPercentageReport) {
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            double amountThisMonth = db.getTotalForCurrentMonth(userId, categoryType);
            double amountLastMonth = db.getTotalForLastMonth(userId, categoryType);

            if (amountLastMonth == 0 || amountThisMonth == 0) {
                ivArrowUp.setVisibility(View.GONE);
                ivArrowDown.setVisibility(View.GONE);
                tvPercentageReport.setText("N/A");
            } else {
                double difference = amountThisMonth - amountLastMonth;

                if (difference > 0) {
                    ivArrowUp.setVisibility(View.VISIBLE);
                    ivArrowDown.setVisibility(View.GONE);
                    double percentage = (difference / amountLastMonth) * 100;
                    tvPercentageReport.setText(String.format("%.2f", percentage) + " %");
                } else if (difference < 0) {
                    ivArrowUp.setVisibility(View.GONE);
                    ivArrowDown.setVisibility(View.VISIBLE);
                    double percentage = (Math.abs(difference) / amountLastMonth) * 100;
                    tvPercentageReport.setText(String.format("%.2f", percentage) + " %");
                } else {
                    ivArrowUp.setVisibility(View.GONE);
                    ivArrowDown.setVisibility(View.GONE);
                    double percentage = (Math.abs(difference) / amountLastMonth) * 100;
                    tvPercentageReport.setText(String.format("%.2f", percentage) + " %");
                }
            }
        } catch (Exception e) {
            alertSuccess(
                    this,
                    "Error",
                    String.valueOf(e.getCause()),
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
}
