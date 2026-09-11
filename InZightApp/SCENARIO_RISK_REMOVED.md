# Scenario Feature - Risk Removed

## Changes Made:

### 1. **Removed Risk UI Elements** ✅
- Removed entire "Risk Tolerance" section from `fragment_scenario_input.xml`
- Removed `sliderRisk` and `tvRiskValue` from layout
- Removed all Risk-related TextViews and Slider

### 2. **Removed Risk Logic from Code** ✅
- Removed `sliderRisk` and `tvRiskValue` variable declarations
- Removed Risk slider initialization from `initViews()`
- Removed Risk slider listener from `setupListeners()`
- Removed Risk-based preset selection logic

### 3. **Simplified Preset Selection** ✅
- Changed from Risk-based preset selection to default preset
- Now always uses `"inflation_low"` as default preset
- Logic simplified: `String preset = "inflation_low";`

## Before vs After:

### Before:
```java
// Determine preset based on risk level
String preset = "inflation_low"; // Default
double riskValue = sliderRisk.getValue();
if (riskValue >= 80) {
    preset = "crisis_severe";
} else if (riskValue >= 60) {
    preset = "crisis_medium";
} else if (riskValue >= 40) {
    preset = "crisis_light";
} else if (riskValue >= 20) {
    preset = "inflation_high";
}
```

### After:
```java
// Use default preset (inflation_low)
String preset = "inflation_low";
```

## Current Behavior:

1. **User Input:**
   - Monthly Income
   - Monthly Expenses
   - Duration (years)

2. **Calculation:**
   - Always uses "inflation_low" preset
   - Preset values: income change = 0%, expense change = 3%

3. **Output:**
   - Shows adjusted income and expense based on inflation_low scenario
   - Displays yearly projections

## Available Presets (for future use if needed):

- `inflation_low`: income 0%, expense 3%
- `inflation_high`: income 0%, expense 10%
- `crisis_light`: income -10%, expense 2%
- `crisis_medium`: income -30%, expense 5%
- `crisis_severe`: income -50%, expense 10%
- `pandemic`: income -40%, expense 15%

## Testing Checklist:

- [ ] Risk slider is no longer visible in UI
- [ ] Risk-related text is removed
- [ ] Scenario calculation works with default preset
- [ ] No errors when generating scenario
- [ ] Results display correctly

