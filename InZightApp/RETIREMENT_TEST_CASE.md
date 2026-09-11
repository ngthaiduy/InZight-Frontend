# Retirement Planning - Specific Test Case

## Problem Statement

**Current Situation:**
- Current Age: 30 years old (assuming, since retiring at 65)
- Current Monthly Income: 15,000,000 VND
- Current Savings: 100,000,000 VND
- Monthly Expenses: 7,000,000 VND
- Monthly Savings: 8,000,000 VND
- Expected Annual Return (EAR): 5%
- Inflation Rate: 3%
- Retirement Age: 65 years old

**Question:** How much money should I save to live without needing income after retirement at age 65?

---

## Input Data for Testing

### Retirement Input Fragment:
```
Current Age: 30
Retirement Age: 65
Current Savings: 100,000,000 VND
Monthly Expense: 7,000,000 VND
Monthly Savings Contribution: 8,000,000 VND
Expected Annual Return: 5%
Expected Inflation Rate: 3%
```

### Calculation Parameters:
- Years to Retirement: 65 - 30 = 35 years
- Monthly Return Rate: 5% / 12 = 0.4167% per month
- Total Months: 35 × 12 = 420 months

---

## Expected Calculation Steps

### Step 1: Calculate Future Value of Current Savings
```
FV_current = 100,000,000 × (1 + 0.05)^35
FV_current = 100,000,000 × 5.516
FV_current ≈ 551,600,000 VND
```

### Step 2: Calculate Future Value of Monthly Contributions (Simple, No Compound Interest)
```
Monthly contribution: 8,000,000 VND
Years to retirement: 35 years

FV_contributions = Monthly Amount × 12 months × Years
FV_contributions = 8,000,000 × 12 × 35
FV_contributions = 8,000,000 × 420
FV_contributions = 3,360,000,000 VND (3.36 billion VND)
```

### Step 3: Total Future Value at Retirement
```
Total FV = FV_current + FV_contributions
Total FV = 551,600,000 + 3,360,000,000
Total FV = 3,911,600,000 VND (≈ 3.91 billion VND)
```

### Step 4: Calculate Monthly Expense at Retirement (Adjusted for Inflation)
```
Current monthly expense: 7,000,000 VND
Years to retirement: 35 years
Inflation rate: 3%

Monthly expense at retirement = 7,000,000 × (1 + 0.03)^35
Monthly expense at retirement = 7,000,000 × 2.814
Monthly expense at retirement ≈ 19,698,000 VND
Annual expense at retirement ≈ 236,376,000 VND
```

### Step 5: Calculate Total Needed for Retirement (20 years post-retirement)
```
Assuming 20 years of retirement:
- Annual expense grows with inflation each year
- Discounted by return rate

Total Needed ≈ 236,376,000 × 20 × (adjusted for inflation and discounting)
Total Needed ≈ 4,727,520,000 VND (simplified calculation)
```

### Step 6: Calculate Retirement Gap
```
Retirement Gap = Total Needed - Total Future Value
Retirement Gap = 4,727,520,000 - 3,911,600,000
Retirement Gap = 815,920,000 VND (Gap exists!)
```

**Result:** There is a retirement gap of ~816 million VND. You need to save more monthly.

### Step 7: Calculate Sustainable Years
```
With Total FV = 3,911,600,000 VND
Annual expense at retirement = 236,376,000 VND (growing 3% each year)

Sustainable years calculation:
- Year 1: 3,911,600,000 - 236,376,000 = 3,675,224,000 remaining
- Year 2: 3,675,224,000 - 243,467,280 = 3,431,756,720 remaining
- Year 3: 3,431,756,720 - 250,771,298 = 3,180,985,422 remaining
- ... continues until funds are depleted

Estimated sustainable years: 15-16 years
```

### Step 8: Calculate Monthly Savings Needed to Close the Gap
```
Retirement Gap = 815,920,000 VND
Years to retirement = 35 years
Monthly rate = 5% / 12 = 0.004167

Monthly Savings Needed = Gap × monthlyRate / ((1 + monthlyRate)^420 - 1)
Monthly Savings Needed ≈ 815,920,000 × 0.004167 / (8.310 - 1)
Monthly Savings Needed ≈ 465,000 VND/month additional

Total Monthly Savings Needed = 8,000,000 + 465,000 = 8,465,000 VND/month
```

---

## Expected API Response

```json
{
  "futureValue": 3911600000,
  "sustainableYears": 16,
  "monthlySavingsNeeded": 465000,
  "retirementGap": 815920000,
  "totalNeeded": 4727520000,
  "projections": [
    {
      "age": 65,
      "remainingSavings": 14583600000,
      "annualExpense": 236376000,
      "netCashFlow": 14347224000
    },
    {
      "age": 66,
      "remainingSavings": 14310375672,
      "annualExpense": 243467280,
      "netCashFlow": 14066908392
    },
    {
      "age": 67,
      "remainingSavings": 14066908392,
      "annualExpense": 250771298,
      "netCashFlow": 13816137094
    },
    {
      "age": 68,
      "remainingSavings": 13816137094,
      "annualExpense": 258294437,
      "netCashFlow": 13557842657
    },
    {
      "age": 69,
      "remainingSavings": 13557842657,
      "annualExpense": 266043271,
      "netCashFlow": 13291799386
    }
  ]
}
```

---

## Expected UI Display

### Retirement Result Pension Fragment:

**Summary Cards:**
- **Total Funds at Retirement:** 3.911.600.000 VNĐ
- **Sustainable Years:** 16 years
- **Monthly Savings Needed:** 465.000 VNĐ (Additional)
- **Retirement Gap:** 815.920.000 VNĐ (Need to save more)

**Details:**
- **Current Age:** 30
- **Retirement Age:** 65
- **Monthly Expense:** 7.000.000 VNĐ
- **Monthly Savings Needed:** 465.000 VNĐ (additional to current 8M)
- **Total Monthly Savings Needed:** 8.465.000 VNĐ
- **% of Income to Save:** ~56% of income (8.465M / 15M)

**Chart:**
- Shows 5-year projection after retirement
- Green bars: Remaining Savings (decreasing)
- Red bars: Annual Expenses (increasing with inflation)
- Chart title: "Pension vs Expenses"

---

## Analysis & Recommendations

### Current Status: ⚠️ NEEDS IMPROVEMENT

**Key Findings:**
1. **Future Value:** ~3.91 billion VND at retirement
2. **Sustainable Years:** 16 years (may not be enough if you live longer)
3. **Retirement Gap:** ~816 million VND (gap exists!)
4. **Monthly Savings Needed:** Additional 465,000 VND/month (total 8.465M/month)

### Recommendations:

1. **Increase Monthly Savings:**
   - Current monthly saving: 8,000,000 VND
   - Recommended: 8,465,000 VND/month (add 465K more)
   - Or better: 9,000,000 VND/month for safety margin

2. **Consider Increasing Return Rate:**
   - Current: 5% (conservative)
   - If you can achieve 7-8% return:
     - Future value of current savings will be higher
     - But monthly contributions still calculated simply (no compound)
     - Could help close the gap faster

3. **Investment Strategy:**
   - With 5% return, you're likely using savings accounts or bonds
   - Consider diversifying:
     - 60% in moderate risk (7-8% return)
     - 40% in conservative (5% return)
   - This could increase overall return to 6-7%

4. **Inflation Protection:**
   - Current inflation: 3%
   - Your savings rate (8M/month) already accounts for this
   - Continue monitoring inflation and adjust if needed

---

## Test Steps

1. **Open Retirement Planning Feature**
2. **Enter Input:**
   - Current Age: 30
   - Retirement Age: 65
   - Current Savings: 100,000,000
   - Monthly Expense: 7,000,000
   - Monthly Savings: 8,000,000
   - Annual Return: 5
   - Inflation Rate: 3
3. **Click "CALCULATE PENSION"**
4. **Verify Results:**
   - Future Value: ~3.91 billion VND
   - Sustainable Years: 15-16 years
   - Monthly Savings Needed: ~465,000 VND (additional)
   - Retirement Gap: ~816 million VND
5. **Check Chart:**
   - Should show decreasing savings over 5 years
   - Expenses increasing with inflation
6. **Verify Currency Formatting:**
   - All amounts in format: "X.XXX.XXX VNĐ"

---

## Alternative Scenarios

### Scenario 1: Retire Earlier (Age 60)
- Years to retirement: 30 years
- Future Value: ~3.36 billion VND (100M × 1.05^30 + 8M × 12 × 30)
- Sustainable Years: ~14 years
- **Result:** Less sustainable, not recommended!

### Scenario 2: Increase Monthly Savings to 10M
- Future Value: ~4.2 billion VND (100M × 1.05^35 + 10M × 12 × 35)
- Sustainable Years: ~17 years
- **Result:** Better, but still need more!

### Scenario 3: Increase Monthly Savings to 12M
- Future Value: ~5.04 billion VND (100M × 1.05^35 + 12M × 12 × 35)
- Sustainable Years: ~21 years
- **Result:** Much better! Closer to 20-year target!

---

## Conclusion

**Answer to your question:**

With your current situation:
- **Current Savings:** 100 million VND
- **Monthly Savings:** 8 million VND (simple calculation, no compound interest)
- **Return Rate:** 5% (only applies to current savings)
- **Inflation:** 3%

**You will have approximately 3.91 billion VND at retirement (age 65), which can sustain you for about 16 years.**

**However, there is a gap of ~816 million VND. You need to save an additional 465,000 VND per month (total 8.465M/month) to close this gap.**

**Recommendations:**
- **Option 1:** Increase monthly savings to 8.5-9 million VND
- **Option 2:** Increase monthly savings to 10-12 million VND for better safety margin (20+ years sustainability)
- **Option 3:** Consider increasing return rate on current savings (invest in higher return options)
- **Option 4:** Plan to work a few more years (retire at 67-68 instead of 65)

