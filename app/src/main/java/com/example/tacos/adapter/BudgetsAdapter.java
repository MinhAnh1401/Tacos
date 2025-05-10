package com.example.tacos.adapter;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tacos.interfaces.OnBudgetDeletedListener;
import com.example.tacos.interfaces.OnEditBudgetListener;
import com.example.tacos.R;
import com.example.tacos.database.DatabaseHelper;
import com.example.tacos.fragment.BudgetsFragment;
import com.example.tacos.model.Budget;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.blurry.Blurry;

public class BudgetsAdapter extends RecyclerView.Adapter<BudgetsAdapter.ViewHolder> {
    private final List<Budget> budgets;
    public Context context;
    private final OnEditBudgetListener editListener;

    public BudgetsAdapter(List<Budget> budgets, Context context,
                          OnBudgetDeletedListener listener,
                          OnEditBudgetListener editListener) {
        this.budgets = budgets;
        this.context = context;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale", "NotifyDataSetChanged", "CutPasteId", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = budgets.get(position);

        holder.llBudgetItem.setBackgroundResource(budget.getCategoryType().equals("Expense") ?
                R.drawable.dangerous_widget : R.drawable.success_widget);

        holder.tvBudgetName.setText(budget.getCategoryName());

        holder.llBudgetDetails.setVisibility(View.GONE);

        setScaleAnimation2(holder.llBudgetHeader, holder.llBudgetItem);
        holder.llBudgetHeader.setOnClickListener(v -> toggleBudgetDetails(holder.llBudgetDetails));

        holder.tvStartDate.setText(budget.getStartDate().toLocalDate().toString());
        holder.tvStartTime.setText(budget.getStartDate().toLocalTime().toString());
        holder.tvEndDate.setText(budget.getEndDate().toLocalDate().toString());
        holder.tvEndTime.setText(budget.getEndDate().toLocalTime().toString());
        LocalDateTime endDateTime = budget.getEndDate();
        LocalDateTime currentDateTime = LocalDateTime.now();

        Duration dateTimeCountdown = Duration.between(currentDateTime, endDateTime);
        long daysLeft = dateTimeCountdown.toDays();
        long hoursLeft = dateTimeCountdown.toHours() % 24;
        long minutesLeft = dateTimeCountdown.toMinutes() % 60;

        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = budget.getEndDate().toLocalDate();

        int currentDayOfMonth = currentDate.getDayOfMonth();

        long daysUntilEndDate = Duration.between(currentDate.atStartOfDay(), endDate.atStartOfDay()).toDays();

        int daysPassedPercent = (int) ((currentDayOfMonth * 100) / (currentDayOfMonth + daysUntilEndDate));

        holder.pbDateLeftBudget.setProgress(daysPassedPercent);

        animateProgressBar(holder.pbDateLeftBudget, daysPassedPercent);

        if (currentDateTime.isAfter(endDateTime)) {
            long absDaysLeft = Math.abs(daysLeft);
            long absHoursLeft = Math.abs(hoursLeft);
            long absMinutesLeft = Math.abs(minutesLeft);

            holder.tvDaysLeft.setBackgroundResource(R.drawable.dangerous_bold_widget);
            holder.pbDateLeftBudget.setVisibility(View.GONE);

            if (absDaysLeft > 0) {
                holder.tvDaysLeft.setText(absDaysLeft + " day" + (absDaysLeft > 1 ? "s" : "") + " overdue");
            } else if (absHoursLeft > 0) {
                holder.tvDaysLeft.setText(absHoursLeft + " hour" + (absHoursLeft > 1 ? "s" : "") + " overdue");
            } else {
                holder.tvDaysLeft.setText(absMinutesLeft + " minute" + (absMinutesLeft > 1 ? "s" : "") + " overdue");
            }
        } else {
            if (daysLeft > 0) {
                holder.tvDaysLeft.setText(daysLeft == 1 ? "1 day left" : daysLeft + " days left");
            } else if (daysLeft == 0) {
                if (hoursLeft > 0) {
                    holder.tvDaysLeft.setText(hoursLeft + " hour" + (hoursLeft > 1 ? "s" : "") + " " + minutesLeft + " minutes left");
                } else if (hoursLeft == 0 && minutesLeft > 0) {
                    holder.tvDaysLeft.setText(minutesLeft + " minute" + (minutesLeft > 1 ? "s" : "") + " left");
                } else {
                    holder.tvDaysLeft.setBackgroundResource(R.drawable.dangerous_bold_widget);
                    holder.pbDateLeftBudget.setVisibility(View.GONE);
                    holder.tvDaysLeft.setText("Time is up!");
                }
            }
        }

        holder.tvAmount.setText("$ " + String.format("%.2f", Math.abs(budget.getAmount())));

        holder.tvFrequencyBudget.setText(budget.getFrequency());
        textGradient(holder.tvFrequencyBudget);

        holder.tvNoteBudget.setText(budget.getNote());

        setScaleAnimation(holder.ivEdit);
        holder.ivEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditBudget(budget);
            }
        });

        setScaleAnimation(holder.ivDelete);
        holder.ivDelete.setOnClickListener(v ->
            alertConfirm(context,
                    "Delete budget",
                    "Are you sure you want to delete this budget?" + "\n\n" +
                            "Frequency: " + budget.getFrequency() + "\n" +
                            "Creation date: " + budget.getStartDate().toLocalDate().toString() + " " +
                                                budget.getStartDate().toLocalTime().toString() + "\n" +
                            "Settlement date: " + budget.getEndDate().toLocalDate().toString() + " " +
                                                budget.getEndDate().toLocalTime().toString() + "\n\n" +
                            "Type: " + budget.getCategoryType() + "\n" +
                            "Category: " + budget.getCategoryName() + "\n" +
                            "Amount: $ " + budget.getAmount() + "\n" +
                            "Note: " + budget.getNote(),
                    "Delete",
                    R.drawable.dangerous_alert,
                    v1 -> deleteBudget(budget, position)
            )
        );

        setScaleAnimation(holder.ivSettleBudget);
        holder.ivSettleBudget.setOnClickListener(v ->
            alertConfirm(context,
                    "Settle budget",
                    "Are you sure you want to settle this budget?" + "\n\n" +
                            "Frequency: " + budget.getFrequency() + "\n" +
                            "Creation date: " + budget.getStartDate().toLocalDate().toString() + " " +
                                                budget.getStartDate().toLocalTime().toString() + "\n" +
                            "Settlement date: " + budget.getEndDate().toLocalDate().toString() + " " +
                                                budget.getEndDate().toLocalTime().toString() + "\n\n" +
                            "Type: " + budget.getCategoryType() + "\n" +
                            "Category: " + budget.getCategoryName() + "\n" +
                            "Amount: $ " + budget.getAmount() + "\n" +
                            "Note: " + budget.getNote(),
                    "Settle",
                    R.drawable.primary_alert,
                    v1 -> addTransactionAndUpdateBudget(budget, position)
            )
        );
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
    private void setScaleAnimation2(View view, LinearLayout llFocus) {
        final boolean[] isScaledUp = {false};

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_CANCEL:
                    if (!isScaledUp[0]) {
                        llFocus.animate()
                                .scaleX(1.05f)
                                .scaleY(1.05f)
                                .setDuration(200)
                                .setInterpolator(new OvershootInterpolator())
                                .start();
                        isScaledUp[0] = true;
                    } else {
                        llFocus.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .setInterpolator(new OvershootInterpolator())
                                .start();
                        isScaledUp[0] = false;
                    }
                    break;
            }
            return false;
        });
    }

    private void toggleBudgetDetails(final View detailsView) {
        if (detailsView.getVisibility() == View.VISIBLE) {
            detailsView.animate()
                    .alpha(0f)
                    .setDuration(0)
                    .withEndAction(() -> detailsView.setVisibility(View.GONE));
        } else {
            detailsView.setVisibility(View.VISIBLE);
            detailsView.setAlpha(0f);
            detailsView.animate()
                    .alpha(1f)
                    .setDuration(200);
        }
    }

    private void animateProgressBar(ProgressBar progressBar, int targetProgress) {
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, targetProgress);
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();
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

    private void deleteBudget(Budget budget, int position) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
            int userId = budget.getUserId();
            int categoryId = dbHelper.getCategoryId(userId, budget.getCategoryName());
            double amountToDelete = Math.abs(budget.getAmount());
            String startDate = budget.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String endDate = budget.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String note = budget.getNote();

            int budgetId = dbHelper.getBudgetId(userId, categoryId, amountToDelete, startDate, endDate, note);
            dbHelper.deleteBudget(budgetId);
            budgets.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, budgets.size());

            Fragment fragment = ((AppCompatActivity) context).getSupportFragmentManager()
                                                        .findFragmentById(R.id.fragment_container);
            if (fragment instanceof BudgetsFragment) {
                ((BudgetsFragment) fragment).loadBudgets(userId);
                ((BudgetsFragment) fragment).updateIncomeExpenseBudgetTextView(userId);
                ((BudgetsFragment) fragment).updateTotalBudget(userId);
            }

            alertSuccess(
                    context,
                    "Successful deletion",
                    "Budget has been deleted successfully.",
                    R.drawable.background_alert
            );
        } catch (Exception e) {
            Log.e("Error", "Error deleting budget: " + e.getMessage());
        }

    }

    private void addTransactionAndUpdateBudget(Budget budget, int position) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
            int userId = budget.getUserId();
            String categoryName = budget.getCategoryName();
            String categoryType = budget.getCategoryType();
            int categoryId = dbHelper.getCategoryId(userId, categoryName);
            double amount = Math.abs(budget.getAmount());
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String note = budget.getNote();

            double currentBalance = dbHelper.getTotalBalanceAllTime(userId);

            if (Objects.equals(categoryType, "Expense") && amount > currentBalance) {
                alertSuccess(
                        context,
                        "Insufficient balance",
                        "Your balance is not sufficient to settle this budget.",
                        R.drawable.dangerous_alert
                );
                return;
            }

            dbHelper.addTransaction(userId, categoryId, amount, currentDateTime, note);

            String frequency = budget.getFrequency();
            switch (frequency) {
                case "Weekly":
                    budget.setEndDate(budget.getEndDate().plusWeeks(1));
                    break;
                case "Monthly":
                    budget.setEndDate(budget.getEndDate().plusMonths(1));
                    break;
                case "Yearly":
                    budget.setEndDate(budget.getEndDate().plusYears(1));
                    break;
            }

            dbHelper.updateBudget(budget);
            notifyItemChanged(position);

            Fragment fragment = ((AppCompatActivity) context).getSupportFragmentManager()
                                                    .findFragmentById(R.id.fragment_container);
            if (fragment instanceof BudgetsFragment) {
                ((BudgetsFragment) fragment).loadBudgets(userId);
                ((BudgetsFragment) fragment).updateTotalBudget(userId);
            }

            alertSuccess(
                    context,
                    "Successful settlement",
                    "Budget has been settled successfully.",
                    R.drawable.background_alert
            );

        } catch (Exception e) {
            alertSuccess(
                    context,
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

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvBudgetName, tvStartDate, tvStartTime, tvEndDate, tvEndTime, tvDaysLeft,
                tvAmount, tvFrequencyBudget, tvNoteBudget;
        public ImageView ivEdit, ivDelete;
        public ImageView ivSettleBudget;
        public LinearLayout llBudgetItem, llBudgetDetails, llBudgetHeader;
        public ProgressBar pbDateLeftBudget;

        public ViewHolder(View itemView) {
            super(itemView);
            tvBudgetName = itemView.findViewById(R.id.tv_budget_name);
            tvStartDate = itemView.findViewById(R.id.tv_start_date_budget);
            tvStartTime = itemView.findViewById(R.id.tv_start_time_budget);
            tvEndDate = itemView.findViewById(R.id.tv_end_date_budget);
            tvEndTime = itemView.findViewById(R.id.tv_end_time_budget);
            tvDaysLeft = itemView.findViewById(R.id.tv_days_left_budget);
            tvAmount = itemView.findViewById(R.id.tv_amount_budget);
            tvFrequencyBudget = itemView.findViewById(R.id.tv_frequency_budget);
            tvNoteBudget = itemView.findViewById(R.id.tv_note_budget);
            ivEdit = itemView.findViewById(R.id.iv_edit_budget);
            ivDelete = itemView.findViewById(R.id.iv_delete_budget);
            ivSettleBudget = itemView.findViewById(R.id.iv_settle_budget);
            llBudgetItem = itemView.findViewById(R.id.ll_budget_item);
            llBudgetDetails = itemView.findViewById(R.id.ll_budget_details);
            llBudgetHeader = itemView.findViewById(R.id.ll_budget_header);
            pbDateLeftBudget = itemView.findViewById(R.id.pb_date_left_budget);
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
