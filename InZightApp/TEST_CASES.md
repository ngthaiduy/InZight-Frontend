# Financial Planning Features - Test Cases

## Overview
This document provides test cases with example inputs and expected outputs for all 4 financial planning features:
1. **Retirement Planning**
2. **Optimizer (What-If)**
3. **Scenario Simulation**
4. **Multi-Goal Planning**

---

## 1. Retirement Planning

### Test Case 1.1: Basic Retirement Calculation
**Input:**
- Current Age: 30
- Retirement Age: 65
- Current Savings: 50,000,000 VND
- Monthly Expense: 10,000,000 VND
- Monthly Savings: 5,000,000 VND (optional)
- Expected Annual Return: 7%
- Expected Inflation Rate: 3%

**Expected Output:**
- Future Value: ~1,200,000,000 - 1,500,000,000 VND (depends on calculation)
- Sustainable Years: 15-20 years
- Monthly Savings Needed: Calculated based on gap
- Retirement Gap: Difference between total needed and future value
- Chart showing 5-year projection after retirement

**Validation:**
- All amounts displayed in VND format with dot separators (e.g., "1.200.000.000 VNĐ")
- Age validation: Current age < Retirement age
- All fields are required except monthly savings

---

### Test Case 1.2: High Return Investment
**Input:**
- Current Age: 25
- Retirement Age: 60
- Current Savings: 100,000,000 VND
- Monthly Expense: 15,000,000 VND
- Monthly Savings: 8,000,000 VND
- Expected Annual Return: 13% (Stock investment)
- Expected Inflation Rate: 3%

**Expected Output:**
- Higher future value due to compound interest
- Lower monthly savings needed
- Smaller or zero retirement gap
- More sustainable years

---

### Test Case 1.3: Conservative Approach
**Input:**
- Current Age: 40
- Retirement Age: 65
- Current Savings: 200,000,000 VND
- Monthly Expense: 8,000,000 VND
- Monthly Savings: 3,000,000 VND
- Expected Annual Return: 5% (Savings)
- Expected Inflation Rate: 3%

**Expected Output:**
- Lower future value
- Higher monthly savings needed
- Larger retirement gap
- Fewer sustainable years

---

## 2. Optimizer (What-If)

### Test Case 2.1: Income Optimization
**Input:**
- Monthly Income: 20,000,000 VND
- Monthly Expenses: 14,000,000 VND
- Expected Years: 15
- Inflation Rate: 3%
- Return Rate: MODERATE (7%)

**Expected Output:**
- Original Income: 20,000,000 VND
- Original Expense: 14,000,000 VND
- New Income: 21,000,000 VND (5% increase)
- Adjusted Expense: 14,280,000 VND (2% increase, controlled)
- Chart showing current path vs optimized path over 15 years
- Net savings improvement visible in chart

**Validation:**
- Income and expenses must be > 0
- Chart displays two lines: current (red) and optimized (green)
- All amounts in VND format

---

### Test Case 2.2: High Return Strategy
**Input:**
- Monthly Income: 30,000,000 VND
- Monthly Expenses: 20,000,000 VND
- Expected Years: 20
- Inflation Rate: 3%
- Return Rate: HIGH (10%)

**Expected Output:**
- New Income: 33,000,000 VND (10% increase)
- Larger gap between current and optimized paths
- More significant savings accumulation

---

### Test Case 2.3: Safe Return Strategy
**Input:**
- Monthly Income: 15,000,000 VND
- Monthly Expenses: 10,000,000 VND
- Expected Years: 10
- Inflation Rate: 3%
- Return Rate: SAFE (2%)

**Expected Output:**
- New Income: 15,300,000 VND (2% increase)
- Smaller improvement but more stable
- Conservative growth projection

---

## 3. Scenario Simulation

### Test Case 3.1: Low Inflation Scenario
**Input:**
- Monthly Income: 25,000,000 VND
- Monthly Expenses: 18,000,000 VND
- Duration: 10 years
- Risk Level: 0-20% (Low Inflation preset)

**Expected Output:**
- Scenario: "Low Inflation"
- Adjusted Income: 25,000,000 VND (0% change)
- Adjusted Expense: 18,540,000 VND (3% increase)
- Grid showing percentages:
  - Current Spending: ~42%
  - New Spending: ~43%
  - Current Savings: ~58%
  - New Savings: ~57%
- Description: "Scenario: Low Inflation for 10 years"

**Validation:**
- Preset mapping: 0-20% risk = inflation_low
- All percentages calculated correctly
- Chart/visualization shows impact

---

### Test Case 3.2: High Inflation Scenario
**Input:**
- Monthly Income: 30,000,000 VND
- Monthly Expenses: 22,000,000 VND
- Duration: 15 years
- Risk Level: 20-40% (High Inflation preset)

**Expected Output:**
- Scenario: "High Inflation"
- Adjusted Income: 30,000,000 VND (0% change)
- Adjusted Expense: 33,000,000 VND (10% increase)
- Higher expense percentage
- Lower savings percentage
- Warning about impact

---

### Test Case 3.3: Crisis Scenario
**Input:**
- Monthly Income: 20,000,000 VND
- Monthly Expenses: 15,000,000 VND
- Duration: 5 years
- Risk Level: 80-100% (Severe Crisis preset)

**Expected Output:**
- Scenario: "Severe Crisis"
- Adjusted Income: 10,000,000 VND (-50% decrease)
- Adjusted Expense: 15,750,000 VND (5% increase)
- Negative net savings
- High risk warning
- Recommendations to reduce expenses

---

## 4. Multi-Goal Planning

### Test Case 4.1: House Purchase Goal
**Input:**
- Goal Name: "Buy a House"
- Target Amount: 2,000,000,000 VND
- Monthly Saving: 20,000,000 VND
- Interest Rate: 5% (optional, default)
- Deadline: 2028-12-31
- Icon: Home icon

**Expected Output:**
- Months Needed: ~72-84 months (6-7 years)
- Years Needed: 6-7 years
- Total with Interest: ~2,100,000,000 - 2,200,000,000 VND
- Interest Earned: ~100,000,000 - 200,000,000 VND
- Target Date: Calculated based on months needed
- Milestones: 4 milestones at 25%, 50%, 75%, 100%
- Status: "On Track" (if months < 24) or "At Risk" (if months > 24)

**Validation:**
- Monthly saving < Target amount
- All amounts > 0
- Icon selected
- Date selected

---

### Test Case 4.2: Emergency Fund Goal
**Input:**
- Goal Name: "Emergency Fund"
- Target Amount: 100,000,000 VND
- Monthly Saving: 5,000,000 VND
- Interest Rate: 3%
- Deadline: 2026-06-30
- Icon: Savings icon

**Expected Output:**
- Months Needed: ~18-20 months
- Years Needed: 1.5-2 years
- Status: "On Track"
- Faster achievement due to lower target

---

### Test Case 4.3: Vacation Goal
**Input:**
- Goal Name: "Europe Vacation"
- Target Amount: 50,000,000 VND
- Monthly Saving: 2,000,000 VND
- Interest Rate: 4%
- Deadline: 2025-12-31
- Icon: Travel icon

**Expected Output:**
- Months Needed: ~24 months
- Years Needed: 2 years
- Status: "On Track" or "Behind" depending on deadline
- Milestones showing progress

---

## Common Validation Rules

### All Features:
1. **Currency Formatting:**
   - All amounts displayed as: "X.XXX.XXX VNĐ"
   - Dot (.) as thousand separator
   - "VNĐ" suffix

2. **Input Validation:**
   - All required fields must be filled
   - Numbers must be > 0
   - Age must be 18-100
   - Retirement age > Current age
   - Monthly saving < Target amount (for goals)

3. **Error Messages:**
   - "Please fill in all required fields"
   - "Please enter valid numbers"
   - "Income and expenses must be greater than 0"
   - "Please select an icon" (for goals)

4. **API Integration:**
   - All calculations done via backend API
   - Progress bar shown during API call
   - Error handling for network failures
   - Toast messages for success/failure

---

## Test Execution Checklist

### Retirement Planning:
- [ ] Input all required fields
- [ ] Test with different return rates (5%, 7%, 13%)
- [ ] Test with different inflation rates (2%, 3%, 5%)
- [ ] Verify future value calculation
- [ ] Verify monthly savings needed
- [ ] Check chart displays correctly
- [ ] Verify currency formatting

### Optimizer:
- [ ] Input income and expenses
- [ ] Test all return rate options (SAFE, MODERATE, HIGH)
- [ ] Verify chart shows two paths
- [ ] Check income/expense adjustments
- [ ] Verify net savings improvement

### Scenario:
- [ ] Input income and expenses
- [ ] Test all risk levels (0-20%, 20-40%, 40-60%, 60-80%, 80-100%)
- [ ] Verify preset selection
- [ ] Check percentage calculations
- [ ] Verify scenario description

### Multi-Goal:
- [ ] Enter goal name
- [ ] Enter target amount
- [ ] Enter monthly saving
- [ ] Select icon
- [ ] Select deadline
- [ ] Verify months/years calculation
- [ ] Check milestones
- [ ] Verify status determination

---

## Expected API Responses

### Retirement API Response:
```json
{
  "futureValue": 1500000000,
  "sustainableYears": 18,
  "monthlySavingsNeeded": 3500000,
  "retirementGap": 500000000,
  "totalNeeded": 2000000000,
  "projections": [...]
}
```

### Optimizer API Response:
```json
{
  "newIncome": 21000000,
  "adjustedExpense": 14280000,
  "netSavings": 6720000,
  "improvement": 12.5,
  "projections": [...]
}
```

### Scenario API Response:
```json
{
  "adjustedIncome": 25000000,
  "adjustedExpense": 18540000,
  "netChange": 6460000,
  "preset": "inflation_low",
  "presetName": "Low Inflation",
  "yearlyProjections": [...]
}
```

### Multi-Goal API Response:
```json
{
  "name": "Buy a House",
  "monthsNeeded": 84,
  "yearsNeeded": 7,
  "totalWithInterest": 2100000000,
  "interestEarned": 100000000,
  "targetDate": "31/12/2028",
  "milestones": [...],
  "priority": 2
}
```

---

## Notes

1. All calculations are performed by the backend API
2. Frontend only formats and displays the results
3. Currency formatting uses Vietnamese format with dot separators
4. All text is in English
5. Error messages are user-friendly and in English
6. Charts and visualizations should update dynamically based on API responses

