package com.example.inzightapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class to manage tutorial/onboarding state
 */
public class TutorialHelper {
    
    private static final String PREFS_NAME = "TutorialPrefs";
    
    // Keys for basic tutorial (first time user)
    private static final String KEY_BASIC_TUTORIAL_COMPLETED = "basic_tutorial_completed";
    private static final String KEY_ADD_TRANSACTION_SHOWN = "add_transaction_tutorial_shown";
    private static final String KEY_HOME_CHART_SHOWN = "home_chart_tutorial_shown";
    private static final String KEY_TRANSACTION_HISTORY_SHOWN = "transaction_history_tutorial_shown";
    
    // Keys for premium tutorial (first time premium user)
    private static final String KEY_PREMIUM_TUTORIAL_COMPLETED = "premium_tutorial_completed";
    private static final String KEY_RETIRE_TUTORIAL_SHOWN = "retire_tutorial_shown";
    private static final String KEY_OPTIMIZER_TUTORIAL_SHOWN = "optimizer_tutorial_shown";
    private static final String KEY_SCENARIO_TUTORIAL_SHOWN = "scenario_tutorial_shown";
    private static final String KEY_MULTI_GOAL_TUTORIAL_SHOWN = "multi_goal_tutorial_shown";
    
    /**
     * Check if basic tutorial should be shown (first time user or user with few transactions)
     * @param transactionCount Number of transactions user has
     */
    public static boolean shouldShowBasicTutorial(Context context, int transactionCount) {
        // Always show tutorial if user has <= 2 transactions
        if (transactionCount <= 2) {
            return true;
        }
        // Otherwise check if tutorial was never completed
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return !prefs.getBoolean(KEY_BASIC_TUTORIAL_COMPLETED, false);
    }
    
    /**
     * Check if basic tutorial should be shown (backward compatibility)
     */
    public static boolean shouldShowBasicTutorial(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return !prefs.getBoolean(KEY_BASIC_TUTORIAL_COMPLETED, false);
    }
    
    /**
     * Check if premium tutorial should be shown (first time premium user)
     */
    public static boolean shouldShowPremiumTutorial(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return !prefs.getBoolean(KEY_PREMIUM_TUTORIAL_COMPLETED, false);
    }
    
    /**
     * Mark basic tutorial as completed
     */
    public static void markBasicTutorialCompleted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_BASIC_TUTORIAL_COMPLETED, true)
                .putBoolean(KEY_ADD_TRANSACTION_SHOWN, true)
                .putBoolean(KEY_HOME_CHART_SHOWN, true)
                .putBoolean(KEY_TRANSACTION_HISTORY_SHOWN, true)
                .apply();
    }
    
    /**
     * Mark premium tutorial as completed
     */
    public static void markPremiumTutorialCompleted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_PREMIUM_TUTORIAL_COMPLETED, true)
                .putBoolean(KEY_RETIRE_TUTORIAL_SHOWN, true)
                .putBoolean(KEY_OPTIMIZER_TUTORIAL_SHOWN, true)
                .putBoolean(KEY_SCENARIO_TUTORIAL_SHOWN, true)
                .putBoolean(KEY_MULTI_GOAL_TUTORIAL_SHOWN, true)
                .apply();
    }
    
    /**
     * Check if specific tutorial step has been shown
     */
    public static boolean isTutorialStepShown(Context context, String stepKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(stepKey, false);
    }
    
    /**
     * Mark specific tutorial step as shown
     */
    public static void markTutorialStepShown(Context context, String stepKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(stepKey, true).apply();
    }
    
    /**
     * Reset all tutorials (for testing)
     */
    public static void resetAllTutorials(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
    
    /**
     * Reset basic tutorial only (for new user registration)
     */
    public static void resetBasicTutorial(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_BASIC_TUTORIAL_COMPLETED, false)
                .putBoolean(KEY_ADD_TRANSACTION_SHOWN, false)
                .putBoolean(KEY_HOME_CHART_SHOWN, false)
                .putBoolean(KEY_TRANSACTION_HISTORY_SHOWN, false)
                .apply();
    }
    
    // Step keys
    public static final String STEP_ADD_TRANSACTION = KEY_ADD_TRANSACTION_SHOWN;
    public static final String STEP_HOME_CHART = KEY_HOME_CHART_SHOWN;
    public static final String STEP_TRANSACTION_HISTORY = KEY_TRANSACTION_HISTORY_SHOWN;
    public static final String STEP_RETIRE = KEY_RETIRE_TUTORIAL_SHOWN;
    public static final String STEP_OPTIMIZER = KEY_OPTIMIZER_TUTORIAL_SHOWN;
    public static final String STEP_SCENARIO = KEY_SCENARIO_TUTORIAL_SHOWN;
    public static final String STEP_MULTI_GOAL = KEY_MULTI_GOAL_TUTORIAL_SHOWN;
}

