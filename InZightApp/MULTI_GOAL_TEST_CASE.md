# Multi-Goal Plan Test Case

## Test Case: Multiple Goals Planning

### Input
User wants to create multiple financial goals:
1. **Buy a House**
   - Target Amount: 2,000,000,000 VND (2 billion)
   - Monthly Saving: 20,000,000 VND (20 million)
   - Interest Rate: 5% per year
   - Priority: High (1)

2. **Buy a Car**
   - Target Amount: 500,000,000 VND (500 million)
   - Monthly Saving: 10,000,000 VND (10 million)
   - Interest Rate: 5% per year
   - Priority: Medium (2)

3. **Vacation Fund**
   - Target Amount: 50,000,000 VND (50 million)
   - Monthly Saving: 5,000,000 VND (5 million)
   - Interest Rate: 5% per year
   - Priority: Low (3)

### Expected Calculations

#### Goal 1: Buy a House
- Target: 2,000,000,000 VND
- Monthly Saving: 20,000,000 VND
- Interest Rate: 5% per year (0.4167% per month)
- Formula: FV = PMT × [((1 + r)^n - 1) / r]
- Solving for n: n = log(1 + FV×r/PMT) / log(1 + r)
  - n = log(1 + 2,000,000,000 × 0.004167 / 20,000,000) / log(1.004167)
  - n = log(1 + 41.67) / log(1.004167)
  - n = log(42.67) / log(1.004167)
  - n = 3.754 / 0.004155
  - n ≈ 904 months ≈ 75.3 years

**Wait, this seems too long. Let me recalculate:**
- Actually, with 20M/month and 5% interest, it should be much faster.
- Let me use the correct formula:
  - FV = PMT × [((1 + r)^n - 1) / r]
  - 2,000,000,000 = 20,000,000 × [((1.004167)^n - 1) / 0.004167]
  - 100 = ((1.004167)^n - 1) / 0.004167
  - 0.4167 = (1.004167)^n - 1
  - 1.4167 = (1.004167)^n
  - n = log(1.4167) / log(1.004167)
  - n = 0.348 / 0.004155
  - n ≈ 84 months ≈ 7 years

**Corrected Calculation:**
- Months needed: ~84 months (7 years)
- Total with interest: ~2,000,000,000 VND
- Interest earned: ~320,000,000 VND

#### Goal 2: Buy a Car
- Target: 500,000,000 VND
- Monthly Saving: 10,000,000 VND
- Interest Rate: 5% per year
- n = log(1 + 500,000,000 × 0.004167 / 10,000,000) / log(1.004167)
- n = log(1 + 20.835) / log(1.004167)
- n = log(21.835) / log(1.004167)
- n = 3.084 / 0.004155
- n ≈ 742 months ≈ 61.8 years

**This is also too long. Recalculating:**
- 500,000,000 = 10,000,000 × [((1.004167)^n - 1) / 0.004167]
- 50 = ((1.004167)^n - 1) / 0.004167
- 0.20835 = (1.004167)^n - 1
- 1.20835 = (1.004167)^n
- n = log(1.20835) / log(1.004167)
- n = 0.189 / 0.004155
- n ≈ 45.5 months ≈ 3.8 years

**Corrected:**
- Months needed: ~46 months (3.8 years)
- Total with interest: ~500,000,000 VND
- Interest earned: ~40,000,000 VND

#### Goal 3: Vacation Fund
- Target: 50,000,000 VND
- Monthly Saving: 5,000,000 VND
- Interest Rate: 5% per year
- n = log(1 + 50,000,000 × 0.004167 / 5,000,000) / log(1.004167)
- n = log(1 + 4.167) / log(1.004167)
- n = log(5.167) / log(1.004167)
- n = 1.642 / 0.004155
- n ≈ 395 months ≈ 32.9 years

**Recalculating:**
- 50,000,000 = 5,000,000 × [((1.004167)^n - 1) / 0.004167]
- 10 = ((1.004167)^n - 1) / 0.004167
- 0.04167 = (1.004167)^n - 1
- 1.04167 = (1.004167)^n
- n = log(1.04167) / log(1.004167)
- n = 0.0408 / 0.004155
- n ≈ 9.8 months ≈ 0.8 years

**Corrected:**
- Months needed: ~10 months
- Total with interest: ~50,000,000 VND
- Interest earned: ~2,000,000 VND

### Summary
- **Total Monthly Savings Needed**: 35,000,000 VND/month (20M + 10M + 5M)
- **Total Target Amount**: 2,550,000,000 VND
- **Longest Goal**: Buy a House (7 years)
- **Shortest Goal**: Vacation Fund (10 months)

### Expected AI Recommendations

Based on the goals, AI should recommend:
1. **Prioritize goals by urgency** - Focus on vacation fund first (shortest), then car, then house
2. **Emergency fund allocation** - Suggest allocating 20% of monthly savings (7M/month) for emergency
3. **Investment strategy** - If total monthly savings > 5M, suggest investing 30% (10.5M/month) in growth ETFs
4. **Job recommendations** - Since total monthly savings is 35M (high), suggest multiple income sources

### Output Format

```
MULTI-GOAL PLAN RESULTS
=======================

GOALS:
1. Buy a House
   - Target: 2.000.000.000 VNĐ
   - Monthly Saving: 20.000.000 VNĐ
   - Time Needed: 84 months (7 years)
   - Status: On Track

2. Buy a Car
   - Target: 500.000.000 VNĐ
   - Monthly Saving: 10.000.000 VNĐ
   - Time Needed: 46 months (3.8 years)
   - Status: On Track

3. Vacation Fund
   - Target: 50.000.000 VNĐ
   - Monthly Saving: 5.000.000 VNĐ
   - Time Needed: 10 months
   - Status: On Track

TOTAL MONTHLY SAVINGS: 35.000.000 VNĐ

AI RECOMMENDATIONS:
==================

INVESTMENT RECOMMENDATIONS:
1. Prioritize goals by urgency
   - Focus on goals with shorter deadlines first

2. Allocate 7M/month for emergency fund
   - Build emergency fund before other goals

3. Auto-invest 10.5M/month in growth ETFs
   - Grow savings faster with investments

JOB RECOMMENDATIONS:
Easy Jobs:
- Grab delivery: +300K
- Sell 2nd hand clothes: +500K
- Private tutoring: +400K

Skill-based Jobs:
- Write blogs or SEO posts: 50k/post
- Create social posters: 200K/design
- Build simple website: 500-1M

WARNING:
You have many goals. Consider prioritizing to avoid spreading savings too thin.
```

