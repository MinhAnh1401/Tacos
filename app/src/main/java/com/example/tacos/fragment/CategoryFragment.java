package com.example.tacos.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tacos.database.DatabaseHelper;
import com.example.tacos.R;

import java.util.List;

import jp.wasabeef.blurry.Blurry;

public class CategoryFragment extends Fragment {
    private ListView listView;
    private String categoryType;
    private int userId;

    public static CategoryFragment newInstance(int userId, String categoryType) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        args.putString("categoryType", categoryType);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        listView = view.findViewById(R.id.listView);

        if (getArguments() != null) {
            userId = getArguments().getInt("userId");
            categoryType = getArguments().getString("categoryType");
        }

        loadCategories();

        return view;
    }

    private void loadCategories() {
        try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
            List<String> categories = dbHelper.getCategoryNamesByType(userId, categoryType);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_list_item_1, categories);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedCategory = adapter.getItem(position);

                if (getParentFragment() instanceof AddFragment) {
                    AddFragment addFragment = (AddFragment) getParentFragment();
                    addFragment.setCategoryToEditText(selectedCategory);
                } else if (getParentFragment() instanceof BudgetsFragment) {
                    BudgetsFragment budgetsFragment = (BudgetsFragment) getParentFragment();
                    budgetsFragment.setCategoryToEditText(selectedCategory);
                }
            });

            listView.setOnItemLongClickListener((parent, view, position, id) -> {
                String selectedCategory = adapter.getItem(position);

                assert selectedCategory != null;
                alertConfirm(
                        requireContext(),
                        "Delete category",
                        "Are you sure you want to delete " +
                                selectedCategory.toLowerCase() + "?",
                        "Delete",
                        R.drawable.dangerous_alert,
                        v -> {
                            int categoryId = dbHelper.getCategoryIdByName(userId, selectedCategory);
                            int countTransactions = dbHelper.getTransactionCountByCategoryId(userId, categoryId);

                            alertConfirm(
                                    requireContext(),
                                    "Delete transaction",
                                    "If you delete " + selectedCategory + ", " +
                                            countTransactions + (countTransactions <=1 ? " transaction" : " transactions") +
                                            " named that category will also be deleted.",
                                    "Confirm",
                                    R.drawable.dangerous_alert,
                                    v1 -> {
                                        dbHelper.deleteTransactionsByCategoryId(userId, categoryId);
                                        dbHelper.deleteCategory(userId, categoryId);
                                        loadCategories();
                                    }
                            );
                        }
                );

                return true;
            });
        } catch (Exception e) {
            alertSuccess(
                    requireContext(),
                    "Error",
                    e.getMessage(),
                    R.drawable.background_alert
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
