package com.example.inzightapp.utils;

import com.example.inzightapp.R;

public class CategoryUtils {

    public static int getIconForCategory(String categoryName) {
        if (categoryName == null) {
            return R.drawable.ic_category_default;
        }

        String lowerName = categoryName.trim().toLowerCase();

        // Expense
        if (lowerName.contains("food") && !lowerName.contains("drink")) return R.drawable.ic_expense_food;
        if (lowerName.contains("groceries")) return R.drawable.ic_expense_groceries;
        if (lowerName.contains("shopping")) return R.drawable.ic_expense_shopping;
        if (lowerName.contains("entertainment")) return R.drawable.ic_expense_entertainment;
        if (lowerName.contains("food") && lowerName.contains("drink")) return R.drawable.ic_expense_food_drink;
        if (lowerName.contains("transport")) return R.drawable.ic_expense_transport;
        if (lowerName.contains("housing")) return R.drawable.ic_expense_housing;
        if (lowerName.contains("health") || lowerName.contains("fitness")) return R.drawable.ic_expense_health;
        if (lowerName.contains("education")) return R.drawable.ic_expense_education;
        if (lowerName.contains("travel")) return R.drawable.ic_expense_travel;
        if (lowerName.contains("gifts") && lowerName.contains("donations")) return R.drawable.ic_expense_gifts;
        if (lowerName.contains("pets")) return R.drawable.ic_expense_pets;
        if (lowerName.contains("subscriptions") || lowerName.contains("membership")) return R.drawable.ic_expense_subscriptions;
        if (lowerName.contains("car") && lowerName.contains("maintenance")) return R.drawable.ic_expense_car;

        // Income
        if (lowerName.contains("salary")) return R.drawable.ic_income_salary;
        if (lowerName.contains("bonus")) return R.drawable.ic_income_bonus;
        if (lowerName.contains("freelance") || lowerName.contains("side job")) return R.drawable.ic_income_freelance;
        if (lowerName.contains("business")) return R.drawable.ic_income_business;
        if (lowerName.contains("investment")) return R.drawable.ic_income_investment;
        if (lowerName.contains("rental")) return R.drawable.ic_income_rental;
        if (lowerName.contains("gift") && !lowerName.contains("donation")) return R.drawable.ic_income_gift;
        if (lowerName.contains("refund") || lowerName.contains("rebate")) return R.drawable.ic_income_refund;

        // Fallback for "Other"
        if (lowerName.contains("other")) {
            return R.drawable.ic_expense_other;
        }

        return R.drawable.ic_category_default;
    }
}
