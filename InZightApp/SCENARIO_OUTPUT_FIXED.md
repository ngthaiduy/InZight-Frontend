# Scenario Output - Fixed to Match Test Case

## Changes Made:

### 1. **Display Actual Amounts Instead of Percentages** ✅
- Changed from showing percentages (41%, 59%, 20%) to actual VND amounts
- Now displays: "20.000.000 VNĐ" instead of "59%"

### 2. **Updated Labels** ✅
- Changed "SPENDING" → "CURRENT EXPENSE" and "ADJUSTED EXPENSE"
- Changed "SAVINGS" → "CURRENT INCOME" and "ADJUSTED INCOME"
- Changed "Income With 5% Earnings" → "Adjusted Scenario"

### 3. **Removed Investment Cards** ✅
- Removed INVESTMENT cards (not relevant to Scenario feature)
- Changed GridLayout from 2x3 to 2x2 (only Income and Expense)

### 4. **Updated Code Logic** ✅
- Removed percentage calculation logic
- Now directly displays formatted currency amounts

## Expected Output After Fix:

### Grid Display (2x2):
```
┌──────────────────┬──────────────────┐
│ CURRENT INCOME   │ ADJUSTED INCOME  │
│ 20.000.000 VNĐ   │ 20.000.000 VNĐ   │
└──────────────────┴──────────────────┘
┌──────────────────┬──────────────────┐
│ CURRENT EXPENSE  │ ADJUSTED EXPENSE  │
│ 14.000.000 VNĐ   │ 14.420.000 VNĐ   │
└──────────────────┴──────────────────┘
```

### Legend:
- Blue dot: "Current Income"
- Green dot: "Adjusted Scenario"

### Scenario Description:
- "Scenario: Lạm phát thấp for 10 years"

## Test Case Values:

With Input:
- Income: 20,000,000 VND
- Expense: 14,000,000 VND
- Duration: 10 years

Expected Display:
- Current Income: **20.000.000 VNĐ**
- Adjusted Income: **20.000.000 VNĐ** (no change)
- Current Expense: **14.000.000 VNĐ**
- Adjusted Expense: **14.420.000 VNĐ** (+3%)

## Verification:

- [x] Removed percentage calculation
- [x] Display actual VND amounts
- [x] Updated labels to match test case
- [x] Removed Investment cards
- [x] Changed legend text
- [x] Grid now shows 2x2 instead of 2x3

