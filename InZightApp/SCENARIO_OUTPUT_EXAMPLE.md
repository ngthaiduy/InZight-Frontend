# Scenario Simulation - Output Example

## Test Input
```
Monthly Income: 20,000,000 VND
Monthly Expenses: 14,000,000 VND
Duration: 10 years
```

## Expected Output Display

### Screen 1: Input Screen
```
┌─────────────────────────────────┐
│  Scenario Simulation            │
├─────────────────────────────────┤
│  Monthly Income                 │
│  20.000.000 VNĐ                 │
│                                 │
│  Monthly Expenses               │
│  14.000.000 VNĐ                 │
│                                 │
│  Duration                       │
│  [====●----] 10 năm             │
│                                 │
│  [    GENERATE    ]             │
└─────────────────────────────────┘
```

### Screen 2: Result Screen

#### Top Section - Financial Summary:
```
┌─────────────────────────────────┐
│  Scenario Results                │
├─────────────────────────────────┤
│  Scenario: Low Inflation         │
│  Duration: 10 years              │
└─────────────────────────────────┘

┌──────────────┬──────────────┐
│ Current      │ Optimized    │
│ Income       │ Income       │
│ 20.000.000   │ 20.000.000   │
│ VNĐ          │ VNĐ          │
└──────────────┴──────────────┘

┌──────────────┬──────────────┐
│ Current      │ Adjusted     │
│ Expense      │ Expense      │
│ 14.000.000   │ 14.420.000   │
│ VNĐ          │ VNĐ          │
└──────────────┴──────────────┘
```

#### Chart Section:
```
┌─────────────────────────────────┐
│  Financial Projection            │
│                                 │
│  20M │                          │
│      │                          │
│  15M │    ────────────────      │ (Expense - increasing)
│      │   ╱                      │
│  10M │  ╱                       │
│      │ ╱                        │
│   5M │╱───────────────          │ (Income - flat)
│      │                          │
│   0M └──────────────────────────│
│      2024 2026 2028 2030 2032  │
│                                 │
│  Legend:                        │
│  ● Income  ● Expense            │
└─────────────────────────────────┘
```

#### Yearly Projections Table:
```
┌─────────────────────────────────┐
│  Yearly Projections              │
├─────────────────────────────────┤
│  Year  Income      Expense   Net │
│  2024  20.0M      14.0M     6.0M│
│  2026  20.0M      14.9M     5.1M│
│  2028  20.0M      15.8M     4.2M│
│  2030  20.0M      16.7M     3.3M│
│  2032  20.0M      17.7M     2.3M│
│  2034  20.0M      18.8M     1.2M│
└─────────────────────────────────┘
```

## Detailed Output Values

### Immediate Impact:
| Metric | Value | Change |
|--------|-------|--------|
| Original Income | 20,000,000 VND | - |
| Adjusted Income | 20,000,000 VND | 0% (no change) |
| Original Expense | 14,000,000 VND | - |
| Adjusted Expense | 14,420,000 VND | +3% |
| Original Net | 6,000,000 VND | - |
| New Net | 5,580,000 VND | -7% |
| Net Change | -420,000 VND | -420,000 VND/month |

### Year-by-Year Breakdown:

| Year | Income (VND) | Expense (VND) | Net (VND) | Cumulative Impact (VND) |
|------|--------------|---------------|-----------|-------------------------|
| 2024 | 20,000,000 | 14,000,000 | 6,000,000 | 0 |
| 2025 | 20,000,000 | 14,420,000 | 5,580,000 | -420,000 |
| 2026 | 20,000,000 | 14,852,600 | 5,147,400 | -1,272,600 |
| 2027 | 20,000,000 | 15,298,178 | 4,701,822 | -2,070,778 |
| 2028 | 20,000,000 | 15,757,123 | 4,242,877 | -2,827,901 |
| 2029 | 20,000,000 | 16,229,837 | 3,770,163 | -3,657,738 |
| 2030 | 20,000,000 | 16,716,732 | 3,283,268 | -4,374,470 |
| 2031 | 20,000,000 | 17,218,234 | 2,781,766 | -5,156,704 |
| 2032 | 20,000,000 | 17,734,781 | 2,265,219 | -5,891,485 |
| 2033 | 20,000,000 | 18,266,824 | 1,733,176 | -6,658,309 |
| 2034 | 20,000,000 | 18,814,829 | 1,185,171 | -7,473,138 |

### Key Insights Display:
```
┌─────────────────────────────────┐
│  Key Insights                    │
├─────────────────────────────────┤
│  • Income remains constant       │
│  • Expenses increase 3% annually │
│  • Monthly savings decrease 80% │
│    over 10 years                 │
│  • Total impact: -7.47M VND      │
│                                 │
│  Recommendation:                 │
│  Consider increasing income or   │
│  controlling expense growth      │
└─────────────────────────────────┘
```

## API Response Format

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
      "expense": 15757123,
      "net": 4242877,
      "cumulativeImpact": -2827901
    },
    {
      "year": 2030,
      "income": 20000000,
      "expense": 16716732,
      "net": 3283268,
      "cumulativeImpact": -4374470
    },
    {
      "year": 2032,
      "income": 20000000,
      "expense": 17734781,
      "net": 2265219,
      "cumulativeImpact": -5891485
    },
    {
      "year": 2034,
      "income": 20000000,
      "expense": 18814829,
      "net": 1185171,
      "cumulativeImpact": -7473138
    }
  ]
}
```

## Currency Formatting

All amounts should be displayed in Vietnamese currency format:
- Format: `X.XXX.XXX VNĐ`
- Example: `20.000.000 VNĐ`
- Use dot (.) as thousands separator
- Always include "VNĐ" suffix

## Chart Data Points

The chart should display:
- **X-axis**: Years (2024, 2026, 2028, 2030, 2032, 2034)
- **Y-axis**: Amount in millions (0M to 20M)
- **Income Line**: Flat at 20M (blue or green)
- **Expense Line**: Increasing from 14M to 18.8M (red or orange)
- **Legend**: Clear labels for each line

## Validation Checklist

- [ ] All amounts formatted correctly (X.XXX.XXX VNĐ)
- [ ] Chart displays both income and expense lines
- [ ] Chart shows correct trend (expense increasing, income flat)
- [ ] Yearly projections table shows every 2 years
- [ ] Cumulative impact calculated correctly
- [ ] Scenario name displayed correctly
- [ ] Duration displayed correctly
- [ ] No Risk-related elements visible

