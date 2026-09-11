# Debug Calculation - Tìm Nguyên Nhân Sai Số

## Vấn Đề:
- Total Future Value: **3.911.601.537 VND** ✅ (ĐÚNG)
- Retirement Gap: **50.581.043 VND** ❌ (SAI - phải ~815,920,000)
- Monthly Savings Needed: **44.522 VND** ❌ (SAI - phải ~465,000)

## Phân Tích:

Nếu Total Future Value đúng nhưng Gap sai, có nghĩa là **Total Needed** đang được tính sai.

### Kiểm Tra Logic:

#### 1. Total Future Value (Đúng)
```
FV_current = 100M × (1.05)^35 = 551,601,500
FV_contributions = 8M × 12 × 35 = 3,360,000,000
Total FV = 3,911,601,500 ✅
```

#### 2. Total Needed (Cần kiểm tra)
```
yearlyExpenseAtRetirement = monthlyExpense × 12 × (1 + inflation)^years
                          = 7M × 12 × (1.03)^35
                          = 84M × 2.813862
                          = 236,364,408 VND

Total Needed = Σ (expense_i / (1 + return)^i) for i = 0 to 19
```

#### 3. Retirement Gap
```
Gap = Total Needed - Total Future Value
```

Nếu Gap = 50,581,043 thì:
```
Total Needed = 3,911,601,500 + 50,581,043 = 3,962,182,543 VND
```

Nhưng theo test case, Total Needed phải là ~4,727,520,000 VND.

**Vấn đề:** Total Needed đang thiếu khoảng 765 triệu VND!

## Nguyên Nhân Có Thể:

1. **Cách tính yearlyExpenseAtRetirement sai:**
   - Code hiện tại: `monthlyExpense * 12 * Math.pow(1 + inflationRate, years)`
   - Có thể đúng, nhưng cần verify

2. **Cách tính Total Needed sai:**
   - Code hiện tại tính từng năm với discounting
   - Có thể có lỗi trong vòng lặp

3. **Backend chưa rebuild:**
   - Code đã sửa nhưng chưa compile lại

## Giải Pháp:

1. **Kiểm tra lại công thức tính Total Needed:**
   ```java
   for (int i = 0; i < yearsAfterRetirement; i++) {
       double expenseInYear = yearlyExpenseAtRetirement * Math.pow(1 + inflationRate, i);
       totalNeeded += expenseInYear / Math.pow(1 + yearlyReturn, i);
   }
   ```

2. **Verify với tính toán thủ công:**
   ```
   Year 0: 236,364,408 / 1.05^0 = 236,364,408
   Year 1: 243,457,340 / 1.05^1 = 231,864,133
   Year 2: 250,761,060 / 1.05^2 = 227,447,220
   ...
   Year 19: ~400,000,000 / 1.05^19 = ~158,000,000
   
   Tổng ≈ 4,727,520,000
   ```

3. **Rebuild backend và test lại**

