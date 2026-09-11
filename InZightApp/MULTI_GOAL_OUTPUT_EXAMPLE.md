# Multi-Goal Plan - Output Example

## Test Case Input

### Goal 1: Buy a House
- **Name**: Buy a House
- **Target Amount**: 2,000,000,000 VND
- **Monthly Saving**: 20,000,000 VND
- **Interest Rate**: 5% per year
- **Priority**: High (1)

### Goal 2: Buy a Car
- **Name**: Buy a Car
- **Target Amount**: 500,000,000 VND
- **Monthly Saving**: 10,000,000 VND
- **Interest Rate**: 5% per year
- **Priority**: Medium (2)

### Goal 3: Vacation Fund
- **Name**: Vacation Fund
- **Target Amount**: 50,000,000 VND
- **Monthly Saving**: 5,000,000 VND
- **Interest Rate**: 5% per year
- **Priority**: Low (3)

---

## Expected Output

### SCREEN 1: Multi-Goal Dashboard

```
┌─────────────────────────────────────┐
│  ←  Multi-goal Planner              │
│     Best way to allocate your goal   │
│     spendings                        │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ 🏠 Buy a House                      │
│ ─────────────────────────────────── │
│ Target: 2.000.000.000 VNĐ           │
│ Current: 0 VNĐ (0%)                  │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
│ Deadline: 15/1/2031                  │
│ Time Left: 84 months                 │
│ Status: [On Track]                   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ 🚗 Buy a Car                        │
│ ─────────────────────────────────── │
│ Target: 500.000.000 VNĐ              │
│ Current: 0 VNĐ (0%)                  │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
│ Deadline: 15/7/2027                  │
│ Time Left: 46 months                 │
│ Status: [On Track]                   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ ✈️ Vacation Fund                    │
│ ─────────────────────────────────── │
│ Target: 50.000.000 VNĐ               │
│ Current: 0 VNĐ (0%)                  │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
│ Deadline: 15/11/2024                 │
│ Time Left: 10 months                 │
│ Status: [On Track]                   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ 📊 SUMMARY                          │
│ Total Monthly Savings: 35.000.000   │
│ Total Target: 2.550.000.000 VNĐ     │
│ Longest Goal: 84 months (7 years)    │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ 🤖 AI RECOMMENDATIONS                │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ 💡 Prioritize goals by urgency     │
│ Focus on goals with shorter         │
│ deadlines first                     │
│                                     │
│ [imagebot icon]                     │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ 💰 Allocate 7M/month for emergency  │
│ Build emergency fund before other   │
│ goals                               │
│                                     │
│ [imagebot icon]                     │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ 📈 Auto-invest 10.5M/month in       │
│ growth ETFs                         │
│ Grow savings faster with investments │
│                                     │
│ [imagebot icon]                     │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ ⚠️ WARNING                          │
│ You have many goals. Consider        │
│ prioritizing to avoid spreading      │
│ savings too thin.                    │
│                                     │
│ [imagebot icon]                     │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ 💼 BOOST YOUR INCOME                │
│                                     │
│ Easy Jobs:                          │
│ • Grab delivery: +300K              │
│ • Sell 2nd hand clothes: +500K      │
│ • Private tutoring: +400K           │
│                                     │
│ Skill-based Jobs:                   │
│ • Write blogs or SEO posts: 50k/post│
│ • Create social posters: 200K/design│
│ • Build simple website: 500-1M      │
└─────────────────────────────────────┘
```

---

## Detailed Calculations

### Goal 1: Buy a House
- **Months Needed**: 84 months (7 years)
- **Total with Interest**: ~2,000,000,000 VND
- **Interest Earned**: ~320,000,000 VND
- **Monthly Contribution**: 20,000,000 VND
- **Target Date**: January 15, 2031

### Goal 2: Buy a Car
- **Months Needed**: 46 months (3.8 years)
- **Total with Interest**: ~500,000,000 VND
- **Interest Earned**: ~40,000,000 VND
- **Monthly Contribution**: 10,000,000 VND
- **Target Date**: July 15, 2027

### Goal 3: Vacation Fund
- **Months Needed**: 10 months
- **Total with Interest**: ~50,000,000 VND
- **Interest Earned**: ~2,000,000 VND
- **Monthly Contribution**: 5,000,000 VND
- **Target Date**: November 15, 2024

---

## AI Recommendations Explanation

### Investment Recommendations:
1. **Prioritize by urgency**: Since user has 3 goals with different timelines, AI suggests focusing on the shortest goal (Vacation Fund - 10 months) first, then Car (46 months), then House (84 months).

2. **Emergency fund**: AI detects no emergency fund goal, so suggests allocating 20% of total monthly savings (7M/month) for emergency fund.

3. **Investment**: Since total monthly savings (35M) > 5M threshold, AI suggests investing 30% (10.5M/month) in growth ETFs to accelerate savings.

### Job Recommendations:
Since total monthly savings needed is high (35M/month), AI suggests multiple income sources:
- Easy jobs for quick income
- Skill-based jobs for higher earning potential

### Warning:
AI detects user has 3 goals with total monthly savings of 35M, which might be challenging. Suggests prioritizing to avoid spreading savings too thin.

---

## Notes

- All amounts displayed in Vietnamese currency format (X.XXX.XXX VNĐ)
- Status badges color-coded: Green (On Track), Yellow (Behind), Red (At Risk)
- Progress bars show 0% initially (no savings yet)
- AI recommendations use imagebot icon (same as Chat Finbot)
- Recommendations are personalized based on:
  - Number of goals
  - Total monthly savings needed
  - Goal priorities and timelines
  - Presence of emergency fund goal

