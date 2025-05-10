package com.example.tacos;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.text.TextPaint;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

import jp.wasabeef.blurry.Blurry;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {
    private final List<UserTransaction> transactions;
    public Context context;
    private final OnEditTransactionListener editListener;
    private final String selectedDate;

    public TransactionsAdapter(List<UserTransaction> transactions, Context context,
                               OnTransactionDeletedListener listener,
                               OnEditTransactionListener editListener,
                               String selectedDate) {
        this.transactions = transactions;
        this.context = context;
        this.editListener = editListener;
        this.selectedDate = selectedDate;
    }

    @NonNull
    @Override
    public TransactionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale", "MissingInflatedId", "LocalSuppress", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull TransactionsAdapter.ViewHolder holder, int position) {
        UserTransaction transaction = transactions.get(position);

        holder.llTransactionItem.setBackgroundResource(transaction.getCategoryType().equals("Expense") ?
                R.drawable.dangerous_widget : R.drawable.success_widget);

        holder.tvTransactionName.setText(transaction.getCategoryName());

        holder.llTransactionDetails.setVisibility(View.GONE);

        setScaleAnimation2(holder.llTransactionHeader, holder.llTransactionItem);
        try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
            Pair<Double, Double> totals = dbHelper.getIncomeAndExpenseByDate(transaction.getUserId(), selectedDate);
            double totalExpenseDate = totals.second;
            double totalIncomeDate = totals.first;
            double percentage = getPercentage(position, totalExpenseDate, totalIncomeDate);
            holder.tvPercentage.setText(String.format("%.0f", percentage) + "%");

            holder.llTransactionHeader.setOnClickListener(v -> {
                toggleTransactionDetails(holder.llTransactionDetails);
                setProgressBars(holder, percentage, transaction.getCategoryType());
                animateTextViewPercentage(holder.tvPercentage, percentage);
            });

            setProgressBars(holder, percentage, transaction.getCategoryType());
        } catch (Exception e) {
            alertSuccess(
                    context,
                    "Error",
                    e.getMessage(),
                    R.drawable.dangerous_alert
            );
        }

        holder.tvPercentage.setTextColor(transaction.getCategoryType().equals("Expense") ?
                Color.parseColor("#640000") : Color.parseColor("#006400"));


        holder.tvTime.setText(transaction.getDate().toLocalTime().toString());
        holder.tvAmount.setText("$ " + String.format("%.2f", Math.abs(transaction.getAmount())));

        holder.tvNoteTransaction.setText(transaction.getNote());

        setScaleAnimation(holder.ivEdit);
        holder.ivEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditTransaction(transaction);
            }
        });

        setScaleAnimation(holder.ivDelete);
        holder.ivDelete.setOnClickListener(v ->
            alertConfirm(context,
                    "Delete transaction",
                    "Are you sure you want to delete this transaction?" + "\n\n" +
                            "Transaction date: " + transaction.getDate().toLocalDate().toString() + " " +
                            transaction.getDate().toLocalTime().toString() + "\n\n" +
                            "Type: " + transaction.getCategoryType() + "\n" +
                            "Category: " + transaction.getCategoryName() + "\n" +
                            "Amount: $ " + transaction.getAmount() + "\n" +
                            "Note: " + transaction.getNote(),
                    "Delete",
                    R.drawable.dangerous_alert,
                    v1 -> deleteTransaction(transaction, position)
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

    private void animateTextViewPercentage(final TextView textView, double targetValue) {
        if (targetValue == 0) {
            textView.setText("0 %");
            textView.setScaleX(1f);
            textView.setScaleY(1f);
            return;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0, (float) targetValue);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                textView.setText(String.format("%.1f", animatedValue) + " %");

                float scale = 1 + animatedValue / (float) targetValue;
                textView.setScaleX(scale);
                textView.setScaleY(scale);
            }
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                ValueAnimator shrinkAnimator = ValueAnimator.ofFloat(textView.getScaleX(), 1f);
                shrinkAnimator.setDuration(1000);
                shrinkAnimator.setInterpolator(new DecelerateInterpolator());

                shrinkAnimator.addUpdateListener(animation1 -> {
                    float scale = (float) animation1.getAnimatedValue();
                    textView.setScaleX(scale);
                    textView.setScaleY(scale);
                });

                shrinkAnimator.start();
            }
        });

        animator.start();
    }

    private void toggleTransactionDetails(final View detailsView) {
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

    private void setProgressBars(ViewHolder holder, double percentage, String categoryType) {
        if (categoryType.equals("Expense")) {
            holder.pbIncome.setVisibility(View.GONE);
            holder.pbExpense.setVisibility(View.VISIBLE);
            animateProgressBar(holder.pbExpense, (int) percentage);
        } else {
            holder.pbIncome.setVisibility(View.VISIBLE);
            holder.pbExpense.setVisibility(View.GONE);
            animateProgressBar(holder.pbIncome, (int) percentage);
        }
    }

    private void animateProgressBar(ProgressBar progressBar, int targetProgress) {
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, targetProgress);
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();
    }

    private double getPercentage(int position, double totalExpense, double totalIncome) {
        UserTransaction transaction = transactions.get(position);
        if (transaction.getCategoryType().equals("Expense")) {
            return (transaction.getAmount() / totalExpense) * 100;
        } else {
            return (transaction.getAmount() / totalIncome) * 100;

        }
    }

    private void deleteTransaction(UserTransaction transaction, int position) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
            int userId = transaction.getUserId();
            int categoryId = dbHelper.getCategoryId(userId, transaction.getCategoryName());
            double amountToDelete = Math.abs(transaction.getAmount());
            String date = transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String note = transaction.getNote();

            int transactionId = dbHelper.getTransactionId(userId, categoryId, amountToDelete, date, note);
            dbHelper.deleteTransaction(transactionId);
            transactions.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, transactions.size());

            Fragment fragment = ((AppCompatActivity) context).getSupportFragmentManager()
                                                        .findFragmentById(R.id.fragment_container);
            if (fragment instanceof TransactionsFragment) {
                ((TransactionsFragment) fragment).loadTransactionsForDate(selectedDate, userId);
            }

            alertSuccess(
                    context,
                    "Successful deletion",
                    "Transaction has been deleted successfully",
                    R.drawable.background_alert
            );
        } catch (Exception e) {
            Log.e("transactionsAdapter", "Error deleting transaction", e);
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
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTransactionName, tvTime, tvAmount, tvPercentage, tvNoteTransaction;
        public ImageView ivEdit, ivDelete;
        public EditText editTextCategory;
        public ProgressBar pbIncome, pbExpense;
        public LinearLayout llTransactionItem, llTransactionDetails, llTransactionHeader;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTransactionName = itemView.findViewById(R.id.tv_transaction_name);
            tvTime = itemView.findViewById(R.id.tv_time_transaction);
            tvAmount = itemView.findViewById(R.id.tv_amount_transaction);
            tvPercentage = itemView.findViewById(R.id.tv_percentage_transaction);
            tvNoteTransaction = itemView.findViewById(R.id.tv_note_transaction);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            editTextCategory = itemView.findViewById(R.id.editTextCategory);
            pbIncome = itemView.findViewById(R.id.pb_income_transaction);
            pbExpense = itemView.findViewById(R.id.pb_expense_transaction);
            llTransactionItem = itemView.findViewById(R.id.ll_transaction_item);
            llTransactionDetails = itemView.findViewById(R.id.ll_transaction_details);
            llTransactionHeader = itemView.findViewById(R.id.ll_transaction_header);
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
