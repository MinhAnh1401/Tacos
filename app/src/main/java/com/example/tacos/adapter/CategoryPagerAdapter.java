package com.example.tacos.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tacos.fragment.CategoryFragment;

public class CategoryPagerAdapter extends FragmentStateAdapter {
    private final int userId;

    public CategoryPagerAdapter(@NonNull Fragment fragment, int userId) {
        super(fragment);
        this.userId = userId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String categoryType;
        if (position == 0) {
            categoryType = "Expense";
        } else {
            categoryType = "Income";
        }
        return CategoryFragment.newInstance(userId, categoryType);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
