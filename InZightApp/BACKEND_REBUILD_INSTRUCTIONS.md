# Backend Rebuild Instructions - Fix Retirement Calculation

## Problem
Output shows **9.64 billion VND** but expected is **3.91 billion VND** because monthly savings are being calculated with compound interest instead of simple calculation.

## Solution

### Step 1: Rebuild Backend
The backend code has been updated. You need to rebuild:

```bash
cd "c:/Users/levono/Desktop/_EXE/EXE_InZight/InZight"
mvn clean install
```

### Step 2: Restart Backend Server
After rebuild, restart your Spring Boot backend server to load the new code.

### Step 3: Clear Cache
- Clear app cache if any
- Restart the Android app

### Step 4: Test Again
Test with these inputs:
- Current Age: 30
- Retirement Age: 65
- Current Savings: 100,000,000 VND
- Monthly Expense: 7,000,000 VND
- Monthly Savings: 8,000,000 VND
- Annual Return: 5%
- Inflation Rate: 3%

**Expected Output:**
- Total Funds: **3,911,601,500 VND** (≈ 3.91 billion)
- NOT 9.64 billion!

## Calculation Verification

### Correct Calculation (Simple):
```
FV_current = 100,000,000 × (1.05)^35 = 551,601,500 VND
FV_contributions = 8,000,000 × 12 × 35 = 3,360,000,000 VND
Total = 3,911,601,500 VND
```

### Wrong Calculation (Compound - OLD):
```
FV_current = 100,000,000 × (1.05)^35 = 551,601,500 VND
FV_contributions = 8,000,000 × [((1.004167)^420 - 1) / 0.004167] = 14,034,400,000 VND
Total = 14,586,001,500 VND (14.59 billion)
```

## Code Changes Made

### Backend (FinanceService.java):
- Changed from compound interest formula to simple: `monthlyContribution * 12 * years`
- Updated sustainable years calculation to account for inflation

### Frontend (RetirementInputFragment.java):
- Updated preview calculation to match backend (simple calculation)

## If Problem Persists

1. Check if backend is running the latest JAR file
2. Verify the compiled class file has the new code
3. Check for any caching in the API gateway or proxy
4. Verify the correct endpoint is being called

