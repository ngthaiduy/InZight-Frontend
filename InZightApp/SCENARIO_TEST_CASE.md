# Scenario Simulation - Test Case

## Problem Statement

**Current Situation:**
- Monthly Income: 20,000,000 VND
- Monthly Expenses: 14,000,000 VND
- Current Net Savings: 6,000,000 VND/month
- Scenario Preset: inflation_low (default, no Risk selection)
- Projection Years: 10 years

**Question:** What will happen to my finances under a low inflation scenario over the next 10 years?

---

## Input Data for Testing

### Scenario Input Fragment:
```
Monthly Income: 20,000,000 VND
Monthly Expenses: 14,000,000 VND
Duration: 10 years
Preset: inflation_low (automatically selected, no Risk slider)
```

### Calculation Parameters:
- Original Income: 20,000,000 VND
- Original Expense: 14,000,000 VND
- Original Net: 6,000,000 VND/month
- Preset: "inflation_low"
- Income Change Rate: 0% (no change)
- Expense Change Rate: 3% (low inflation)
- Projection Years: 10

---

## Expected Calculation Steps

### Step 1: Calculate Adjusted Income (After Scenario)
```
Income Change Rate = 0% (from inflation_low preset)
Adjusted Income = Original Income × (1 + Income Change Rate)
Adjusted Income = 20,000,000 × (1 + 0)
Adjusted Income = 20,000,000 VND
```

### Step 2: Calculate Adjusted Expense (After Scenario)
```
Expense Change Rate = 3% (from inflation_low preset)
Adjusted Expense = Original Expense × (1 + Expense Change Rate)
Adjusted Expense = 14,000,000 × (1 + 0.03)
Adjusted Expense = 14,000,000 × 1.03
Adjusted Expense = 14,420,000 VND
```

### Step 3: Calculate Net Change
```
Original Net = Original Income - Original Expense
Original Net = 20,000,000 - 14,000,000
Original Net = 6,000,000 VND/month

New Net = Adjusted Income - Adjusted Expense
New Net = 20,000,000 - 14,420,000
New Net = 5,580,000 VND/month

Net Change = New Net - Original Net
Net Change = 5,580,000 - 6,000,000
Net Change = -420,000 VND/month (decrease)
```

### Step 4: Calculate Yearly Projections (Sample Years)

#### Year 0 (Current Year):
```
Income_0 = 20,000,000 × (1.00)^0 = 20,000,000 VND
Expense_0 = 14,000,000 × (1.03)^0 = 14,000,000 VND
Net_0 = 20,000,000 - 14,000,000 = 6,000,000 VND
Cumulative Impact_0 = 0 VND
```

#### Year 2:
```
Income_2 = 20,000,000 × (1.00)^2 = 20,000,000 VND
Expense_2 = 14,000,000 × (1.03)^2 = 14,000,000 × 1.0609 = 14,852,600 VND
Net_2 = 20,000,000 - 14,852,600 = 5,147,400 VND

Cumulative Impact calculation:
Year 0: Net_0 - Original Net = 6,000,000 - 6,000,000 = 0
Year 1: Net_1 - Original Net = 5,580,000 - 6,000,000 = -420,000
Year 2: Net_2 - Original Net = 5,147,400 - 6,000,000 = -852,600
Cumulative Impact_2 = 0 + (-420,000) + (-852,600) = -1,272,600 VND
```

#### Year 4:
```
Income_4 = 20,000,000 × (1.00)^4 = 20,000,000 VND
Expense_4 = 14,000,000 × (1.03)^4 = 14,000,000 × 1.1255 = 15,757,000 VND
Net_4 = 20,000,000 - 15,757,000 = 4,243,000 VND
Cumulative Impact_4 = Sum of (Net_i - Original Net) for i = 0 to 4
                    ≈ -2,500,000 VND
```

#### Year 6:
```
Income_6 = 20,000,000 × (1.00)^6 = 20,000,000 VND
Expense_6 = 14,000,000 × (1.03)^6 = 14,000,000 × 1.1941 = 16,717,400 VND
Net_6 = 20,000,000 - 16,717,400 = 3,282,600 VND
Cumulative Impact_6 ≈ -4,200,000 VND
```

#### Year 8:
```
Income_8 = 20,000,000 × (1.00)^8 = 20,000,000 VND
Expense_8 = 14,000,000 × (1.03)^8 = 14,000,000 × 1.2668 = 17,735,200 VND
Net_8 = 20,000,000 - 17,735,200 = 2,264,800 VND
Cumulative Impact_8 ≈ -6,100,000 VND
```

#### Year 10:
```
Income_10 = 20,000,000 × (1.00)^10 = 20,000,000 VND
Expense_10 = 14,000,000 × (1.03)^10 = 14,000,000 × 1.3439 = 18,814,600 VND
Net_10 = 20,000,000 - 18,814,600 = 1,185,400 VND
Cumulative Impact_10 ≈ -8,200,000 VND
```

---

## Expected API Response

```json
{
  "adjustedIncome": 20000000,
  "adjustedExpense": 14420000,
  "netChange": -420000,
  "preset": "inflation_low",
      "presetName": "Lạm phát thấp",
  "yearlyProjections": [
    {
      "year": 2024,
      "income": 20000000,
      "expense": 14000000,
      "net": 6000000,
      "cumulativeImpact": 0
    },
    {
      "year": 2026,
      "income": 20000000,
      "expense": 14852600,
      "net": 5147400,
      "cumulativeImpact": -1272600
    },
    {
      "year": 2028,
      "income": 20000000,
      "expense": 15757000,
      "net": 4243000,
      "cumulativeImpact": -2500000
    },
    {
      "year": 2030,
      "income": 20000000,
      "expense": 16717400,
      "net": 3282600,
      "cumulativeImpact": -4200000
    },
    {
      "year": 2032,
      "income": 20000000,
      "expense": 17735200,
      "net": 2264800,
      "cumulativeImpact": -6100000
    },
    {
      "year": 2034,
      "income": 20000000,
      "expense": 18814600,
      "net": 1185400,
      "cumulativeImpact": -8200000
    }
  ]
}
```

---

## Expected Output Summary

### Immediate Results (Year 0):
- **Original Income:** 20,000,000 VND
- **Original Expense:** 14,000,000 VND
- **Adjusted Income:** 20,000,000 VND (no change)
- **Adjusted Expense:** 14,420,000 VND (+3%)
- **Net Change:** -420,000 VND/month (decrease)

### Long-term Impact (10 years):
- **Income (Year 10):** 20,000,000 VND (unchanged)
- **Expense (Year 10):** 18,814,600 VND (+34.4% from original)
- **Net Savings (Year 10):** 1,185,400 VND/month (down from 6,000,000)
- **Cumulative Impact:** -8,200,000 VND (total loss over 10 years)

### Key Insights:
- **Income remains constant** (0% growth)
- **Expenses increase by 3% annually** due to inflation
- **Net savings decrease over time** as expenses outpace income
- **After 10 years, monthly savings drop by 80%** (from 6M to 1.2M)

---

## Key Formulas Used

### 1. Adjusted Income Calculation:
```
Adjusted Income = Original Income × (1 + Income Change Rate)
```

### 2. Adjusted Expense Calculation:
```
Adjusted Expense = Original Expense × (1 + Expense Change Rate)
```

### 3. Income Growth (Yearly):
```
Income in Year N = Original Income × (1 + Income Change Rate)^N
```

### 4. Expense Growth (Yearly):
```
Expense in Year N = Original Expense × (1 + Expense Change Rate)^N
```

### 5. Net Savings (Yearly):
```
Net in Year N = Income_N - Expense_N
```

### 6. Cumulative Impact:
```
Cumulative Impact_N = Σ(Net_i - Original Net) for i = 0 to N
```

---

## Test Execution Checklist

### Input Testing:
- [ ] Enter Monthly Income: 20,000,000 VND
- [ ] Enter Monthly Expenses: 14,000,000 VND
- [ ] Set Duration slider to 10 years
- [ ] Verify Risk slider is NOT visible
- [ ] Click "GENERATE" button

### Output Verification:
- [ ] Verify "Adjusted Income" displays: 20.000.000 VNĐ
- [ ] Verify "Adjusted Expense" displays: 14.420.000 VNĐ
- [ ] Verify "Original Income" displays: 20.000.000 VNĐ
- [ ] Verify "Original Expense" displays: 14.000.000 VNĐ
- [ ] Verify currency formatting uses dot separators
- [ ] Verify chart displays yearly projections
- [ ] Verify chart shows income line (flat) and expense line (increasing)
- [ ] Verify scenario description shows "Low Inflation for 10 years"

### Calculation Verification:
- [ ] Adjusted Income = 20,000,000 VND (no change)
- [ ] Adjusted Expense = 14,420,000 VND (3% increase)
- [ ] Net Change = -420,000 VND/month
- [ ] Year 10 expense ≈ 18,814,600 VND
- [ ] Year 10 net ≈ 1,185,400 VND/month

---

## Additional Test Scenarios

### Test Case 2: High Inflation Scenario
**Note:** This would require changing preset to "inflation_high" (currently not available in UI, but can be tested via API)

**Input:**
- Monthly Income: 20,000,000 VND
- Monthly Expenses: 14,000,000 VND
- Preset: inflation_high (10% expense increase)

**Expected:**
- Adjusted Income: 20,000,000 VND
- Adjusted Expense: 15,400,000 VND (+10%)
- Net Change: -1,400,000 VND/month
- More severe impact over time

### Test Case 3: Crisis Scenario
**Note:** This would require changing preset to "crisis_light" (currently not available in UI)

**Input:**
- Monthly Income: 20,000,000 VND
- Monthly Expenses: 14,000,000 VND
- Preset: crisis_light (income -10%, expense +2%)

**Expected:**
- Adjusted Income: 18,000,000 VND (-10%)
- Adjusted Expense: 14,280,000 VND (+2%)
- Net Change: -2,280,000 VND/month
- Significant negative impact

---

## UI/UX Verification

### Elements to Check:
- [ ] No Risk slider visible
- [ ] No Risk-related text visible
- [ ] Duration slider works correctly
- [ ] Income and Expense input fields format currency correctly
- [ ] Generate button is enabled when inputs are valid
- [ ] Progress bar shows during API call
- [ ] Results screen displays all values correctly
- [ ] Chart renders properly with data points
- [ ] Back button navigates correctly

---

## Conclusion

The Scenario feature now:
1. **Uses default preset** (inflation_low) without Risk selection
2. **Shows realistic financial impact** of inflation over time
3. **Demonstrates that even low inflation (3%)** can significantly reduce savings over 10 years
4. **Provides yearly projections** to help users understand long-term effects

**Key Takeaway:** Even with stable income and low inflation, expenses growing at 3% annually will reduce monthly savings by 80% over 10 years. This highlights the importance of:
- Increasing income over time
- Controlling expense growth
- Investing savings to outpace inflation

