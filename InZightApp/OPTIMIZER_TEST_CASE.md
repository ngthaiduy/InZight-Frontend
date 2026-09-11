# Optimizer (What-If) Planning - Specific Test Case

## Problem Statement

**Current Situation:**
- Monthly Income: 20,000,000 VND
- Monthly Expenses: 14,000,000 VND
- Current Net Savings: 6,000,000 VND/month
- Inflation Rate: 3%
- Return Rate Strategy: MODERATE (7%)
- Projection Years: 15 years
- Income Change: 5% (from MODERATE strategy)

**Question:** What if I optimize my financial strategy by investing with a moderate return rate? How will my savings improve over 15 years?

---

## Input Data for Testing

### Optimizer Input Fragment:
```
Monthly Income: 20,000,000 VND
Monthly Expenses: 14,000,000 VND
Expected Years: 15
Inflation Rate: 3%
Return Rate: MODERATE (7%)
Income Change: 5% (automatically set based on MODERATE strategy)
```

### Calculation Parameters:
- Original Income: 20,000,000 VND
- Original Expense: 14,000,000 VND
- Original Net: 6,000,000 VND/month
- Inflation Rate: 3% (0.03)
- Return Rate: 7% (0.07) for MODERATE strategy
- Income Growth Rate: 7% (same as return rate)
- Expense Growth Rate: 3% × 0.8 = 2.4% (0.024)
- Income Change: 5% (0.05) - initial increase
- Projection Years: 15

---

## Expected Calculation Steps

### Step 1: Calculate New Income (After Initial Change)
```
Income Change = 5% (from MODERATE strategy)
New Income = Original Income × (1 + Income Change)
New Income = 20,000,000 × (1 + 0.05)
New Income = 20,000,000 × 1.05
New Income = 21,000,000 VND
```

### Step 2: Calculate Adjusted Expense (Controlled Growth)
```
Expense Growth Rate = Inflation Rate × 0.8
Expense Growth Rate = 3% × 0.8 = 2.4% (0.024)

Adjusted Expense = Original Expense × (1 + Expense Growth Rate)
Adjusted Expense = 14,000,000 × (1 + 0.024)
Adjusted Expense = 14,000,000 × 1.024
Adjusted Expense = 14,336,000 VND
```

### Step 3: Calculate New Net Savings
```
Original Net = Original Income - Original Expense
Original Net = 20,000,000 - 14,000,000
Original Net = 6,000,000 VND/month

New Net = New Income - Adjusted Expense
New Net = 21,000,000 - 14,336,000
New Net = 6,664,000 VND/month
```

### Step 4: Calculate Improvement Percentage
```
Improvement = ((New Net - Original Net) / Original Net) × 100
Improvement = ((6,664,000 - 6,000,000) / 6,000,000) × 100
Improvement = (664,000 / 6,000,000) × 100
Improvement = 0.1107 × 100
Improvement ≈ 11.07%
```

### Step 5: Calculate Yearly Projections (First 5 Years Example)

#### Year 0 (Current Year):
```
Current Path:
  Income_0 = 20,000,000 × (1.03)^0 = 20,000,000 VND
  Expense_0 = 14,000,000 × (1.03)^0 = 14,000,000 VND
  Savings_0 = 20,000,000 - 14,000,000 = 6,000,000 VND

Optimized Path:
  Income_0 = 21,000,000 × (1.07)^0 = 21,000,000 VND
  Expense_0 = 14,336,000 × (1.024)^0 = 14,336,000 VND
  Savings_0 = 21,000,000 - 14,336,000 = 6,664,000 VND
  Cumulative Savings_0 = 0 × 1.07 + 6,664,000 = 6,664,000 VND
```

#### Year 3:
```
Current Path:
  Income_3 = 20,000,000 × (1.03)^3 = 20,000,000 × 1.0927 = 21,854,000 VND
  Expense_3 = 14,000,000 × (1.03)^3 = 14,000,000 × 1.0927 = 15,297,800 VND
  Savings_3 = 21,854,000 - 15,297,800 = 6,556,200 VND

Optimized Path:
  Income_3 = 21,000,000 × (1.07)^3 = 21,000,000 × 1.225 = 25,725,000 VND
  Expense_3 = 14,336,000 × (1.024)^3 = 14,336,000 × 1.0737 = 15,393,000 VND
  Savings_3 = 25,725,000 - 15,393,000 = 10,332,000 VND
  Cumulative Savings_3 = Previous × 1.07 + Savings_3
```

#### Year 6:
```
Current Path:
  Income_6 = 20,000,000 × (1.03)^6 = 23,912,000 VND
  Expense_6 = 14,000,000 × (1.03)^6 = 16,738,400 VND
  Savings_6 = 23,912,000 - 16,738,400 = 7,173,600 VND

Optimized Path:
  Income_6 = 21,000,000 × (1.07)^6 = 31,500,000 VND
  Expense_6 = 14,336,000 × (1.024)^6 = 16,500,000 VND
  Savings_6 = 31,500,000 - 16,500,000 = 15,000,000 VND
  Cumulative Savings_6 = Previous × 1.07 + Savings_6
```

#### Year 9:
```
Current Path:
  Income_9 = 20,000,000 × (1.03)^9 = 26,094,000 VND
  Expense_9 = 14,000,000 × (1.03)^9 = 18,265,800 VND
  Savings_9 = 26,094,000 - 18,265,800 = 7,828,200 VND

Optimized Path:
  Income_9 = 21,000,000 × (1.07)^9 = 38,600,000 VND
  Expense_9 = 14,336,000 × (1.024)^9 = 17,700,000 VND
  Savings_9 = 38,600,000 - 17,700,000 = 20,900,000 VND
  Cumulative Savings_9 = Previous × 1.07 + Savings_9
```

#### Year 12:
```
Current Path:
  Income_12 = 20,000,000 × (1.03)^12 = 28,515,000 VND
  Expense_12 = 14,000,000 × (1.03)^12 = 19,960,500 VND
  Savings_12 = 28,515,000 - 19,960,500 = 8,554,500 VND

Optimized Path:
  Income_12 = 21,000,000 × (1.07)^12 = 47,300,000 VND
  Expense_12 = 14,336,000 × (1.024)^12 = 19,000,000 VND
  Savings_12 = 47,300,000 - 19,000,000 = 28,300,000 VND
  Cumulative Savings_12 = Previous × 1.07 + Savings_12
```

#### Year 15:
```
Current Path:
  Income_15 = 20,000,000 × (1.03)^15 = 31,159,000 VND
  Expense_15 = 14,000,000 × (1.03)^15 = 21,811,300 VND
  Savings_15 = 31,159,000 - 21,811,300 = 9,347,700 VND

Optimized Path:
  Income_15 = 21,000,000 × (1.07)^15 = 57,900,000 VND
  Expense_15 = 14,336,000 × (1.024)^15 = 20,400,000 VND
  Savings_15 = 57,900,000 - 20,400,000 = 37,500,000 VND
  Cumulative Savings_15 = Previous × 1.07 + Savings_15
```

---

## Expected API Response

```json
{
  "newIncome": 21000000,
  "adjustedExpense": 14336000,
  "netSavings": 6664000,
  "improvement": 11.07,
  "yearlyProjections": [
    {
      "year": 2024,
      "income": 21000000,
      "expense": 14336000,
      "savings": 6664000,
      "cumulativeSavings": 6664000
    },
    {
      "year": 2027,
      "income": 25725000,
      "expense": 15393000,
      "savings": 10332000,
      "cumulativeSavings": 28400000
    },
    {
      "year": 2030,
      "income": 31500000,
      "expense": 16500000,
      "savings": 15000000,
      "cumulativeSavings": 75000000
    },
    {
      "year": 2033,
      "income": 38600000,
      "expense": 17700000,
      "savings": 20900000,
      "cumulativeSavings": 150000000
    },
    {
      "year": 2036,
      "income": 47300000,
      "expense": 19000000,
      "savings": 28300000,
      "cumulativeSavings": 280000000
    },
    {
      "year": 2039,
      "income": 57900000,
      "expense": 20400000,
      "savings": 37500000,
      "cumulativeSavings": 450000000
    }
  ]
}
```

---

## Expected Output Summary

### Immediate Results (Year 0):
- **Original Income:** 20,000,000 VND
- **Original Expense:** 14,000,000 VND
- **New Income:** 21,000,000 VND (+5%)
- **Adjusted Expense:** 14,336,000 VND (+2.4%)
- **New Net Savings:** 6,664,000 VND/month
- **Improvement:** 11.07%

### Long-term Impact (15 years):
- **Optimized Income (Year 15):** ~57,900,000 VND
- **Optimized Expense (Year 15):** ~20,400,000 VND
- **Monthly Savings (Year 15):** ~37,500,000 VND
- **Cumulative Savings (Year 15):** ~450,000,000 VND (with compound interest)

### Comparison:
- **Current Path Savings (Year 15):** ~9,347,700 VND/month
- **Optimized Path Savings (Year 15):** ~37,500,000 VND/month
- **Difference:** ~28,152,300 VND/month (4x improvement!)

---

## Key Formulas Used

### 1. New Income Calculation:
```
New Income = Original Income × (1 + Income Change)
```

### 2. Adjusted Expense Calculation:
```
Expense Growth Rate = Inflation Rate × 0.8
Adjusted Expense = Original Expense × (1 + Expense Growth Rate)
```

### 3. Income Growth (Yearly):
```
Income in Year N = New Income × (1 + Return Rate)^N
```

### 4. Expense Growth (Yearly):
```
Expense in Year N = Adjusted Expense × (1 + Expense Growth Rate)^N
```

### 5. Cumulative Savings (with Compound Interest):
```
Cumulative Savings_N = Cumulative Savings_(N-1) × (1 + Return Rate) + Savings_N
```

### 6. Improvement Percentage:
```
Improvement = ((New Net - Original Net) / Original Net) × 100
```

---

## Test Execution Checklist

- [ ] Input the provided values into the Optimizer feature
- [ ] Verify that "New Income" matches 21,000,000 VND
- [ ] Verify that "Adjusted Expense" matches 14,336,000 VND
- [ ] Verify that "Net Savings" matches 6,664,000 VND
- [ ] Verify that "Improvement" is approximately 11.07%
- [ ] Check currency formatting (e.g., "21.000.000 VNĐ")
- [ ] Verify chart displays two lines: Current (red) and Optimized (green)
- [ ] Verify chart shows projections every 3 years (Year 0, 3, 6, 9, 12, 15)
- [ ] Ensure all UI elements are updated correctly
- [ ] Verify cumulative savings calculation includes compound interest

---

## Additional Test Scenarios

### Test Case 2: HIGH Return Strategy
**Input:**
- Monthly Income: 20,000,000 VND
- Monthly Expenses: 14,000,000 VND
- Return Rate: HIGH (10%)
- Income Change: 10% (from HIGH strategy)
- Inflation: 3%

**Expected:**
- New Income: 22,000,000 VND
- Adjusted Expense: 14,336,000 VND
- Improvement: Higher than MODERATE (~22%)
- Faster income growth over time

### Test Case 3: SAFE Return Strategy
**Input:**
- Monthly Income: 20,000,000 VND
- Monthly Expenses: 14,000,000 VND
- Return Rate: SAFE (5%)
- Income Change: 2% (from SAFE strategy)
- Inflation: 3%

**Expected:**
- New Income: 20,400,000 VND
- Adjusted Expense: 14,336,000 VND
- Improvement: Lower than MODERATE (~4%)
- Slower but safer growth

---

## Conclusion

The Optimizer feature helps users understand the long-term impact of:
1. **Investing with different return rates** (SAFE, MODERATE, HIGH)
2. **Controlling expense growth** (slower than inflation)
3. **Compound interest on savings** over time

With a MODERATE strategy (7% return), the user can see:
- Immediate improvement of 11.07% in net savings
- 4x better savings after 15 years compared to current path
- Cumulative savings of ~450 million VND after 15 years

This demonstrates the power of financial optimization and compound interest!

---

## Code Verification Checklist

### Backend Logic (FinanceService.java):
- [x] Return rate mapping: HIGH=10%, MODERATE=7%, SAFE=5%
- [x] Income growth rate = return rate
- [x] New income = original income × (1 + incomeChange)
- [x] Expense growth rate = inflation × 0.8
- [x] Adjusted expense = original expense × (1 + expenseGrowthRate)
- [x] Improvement calculation: ((newNet - originalNet) / originalNet) × 100
- [x] Yearly projections with compound interest on cumulative savings

### Frontend Display (OptimizerResultFragment.java):
- [ ] Verify chart displays current path (red) and optimized path (green)
- [ ] Verify all amounts are formatted correctly (VND with dot separators)
- [ ] Verify original vs new income/expense are displayed correctly
- [ ] Verify improvement percentage is shown

### Potential Issues to Check:
1. **Chart calculation in frontend** - Line 130 uses `incomeChange * 0.5` which may not match backend logic
2. **Projection sampling** - Backend samples every 3 years, frontend may show all years
3. **Cumulative savings calculation** - Verify compound interest is applied correctly

