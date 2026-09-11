# Giải Thích Cách Tính: Retirement Gap, Required Monthly Savings, và % of Income to Save

## 1. RETIREMENT GAP (Khoảng trống nghỉ hưu)

### Công thức:
```
Retirement Gap = Total Needed - Total Future Value
```

### Giải thích từng bước:

#### Bước 1: Tính Total Needed (Tổng số tiền cần cho 20 năm nghỉ hưu)
```
Total Needed = Tổng chi phí cho 20 năm sau khi nghỉ hưu (đã điều chỉnh lạm phát và chiết khấu)

Với mỗi năm i (từ 0 đến 19):
  - Chi phí năm i = Chi phí năm đầu × (1 + lạm phát)^i
  - Giá trị chiết khấu = Chi phí năm i / (1 + lợi nhuận)^i
  - Total Needed += Giá trị chiết khấu
```

**Ví dụ với test case:**
- Annual expense at retirement = 236,376,000 VND
- Inflation = 3%
- Return rate = 5%

```
Năm 0: 236,376,000 / (1.05)^0 = 236,376,000
Năm 1: 243,467,280 / (1.05)^1 = 231,873,600
Năm 2: 250,771,298 / (1.05)^2 = 227,456,000
...
Tổng cộng 20 năm ≈ 4,727,520,000 VND
```

#### Bước 2: Tính Total Future Value (Tổng số tiền có khi nghỉ hưu)
```
Total Future Value = FV_current + FV_contributions
                   = 551,601,500 + 3,360,000,000
                   = 3,911,601,500 VND
```

#### Bước 3: Tính Retirement Gap
```
Retirement Gap = 4,727,520,000 - 3,911,601,500
               = 815,918,500 VND
```

**Ý nghĩa:** Đây là số tiền còn thiếu để đủ sống 20 năm sau khi nghỉ hưu.

---

## 2. REQUIRED MONTHLY SAVINGS (Số tiền tiết kiệm hàng tháng cần thêm)

### Công thức:
```
Monthly Savings Needed = Gap × monthlyRate / ((1 + monthlyRate)^totalMonths - 1)
```

### Giải thích:
Đây là công thức tính **số tiền cần tiết kiệm hàng tháng** để đạt được số tiền "Gap" sau một số năm nhất định, **với lãi kép**.

**Công thức gốc (Future Value of Annuity):**
```
FV = PMT × [((1 + r)^n - 1) / r]
```

**Giải ngược để tìm PMT:**
```
PMT = FV × r / ((1 + r)^n - 1)
```

Trong đó:
- `FV` = Retirement Gap (số tiền cần đạt được)
- `r` = Monthly rate = Annual return / 12
- `n` = Total months = Years × 12
- `PMT` = Monthly payment (số tiền tiết kiệm hàng tháng)

**Ví dụ với test case:**
```
Gap = 815,920,000 VND
Years = 35
Monthly rate = 5% / 12 = 0.004167
Total months = 35 × 12 = 420

Monthly Savings = 815,920,000 × 0.004167 / ((1.004167)^420 - 1)
                = 815,920,000 × 0.004167 / (8.310 - 1)
                = 815,920,000 × 0.004167 / 7.310
                = 3,400,000 / 7.310
                ≈ 465,000 VND/tháng
```

**Ý nghĩa:** Bạn cần tiết kiệm thêm **465,000 VND mỗi tháng** (ngoài 8 triệu hiện tại) để đóng kín khoảng trống nghỉ hưu.

**Tổng số tiền tiết kiệm hàng tháng cần:**
```
Total Monthly Savings = 8,000,000 + 465,000 = 8,465,000 VND/tháng
```

---

## 3. % OF INCOME TO SAVE (Phần trăm thu nhập cần tiết kiệm)

### Công thức:
```
% of Income to Save = (Monthly Savings Needed / Estimated Monthly Income) × 100
```

### Giải thích:

#### Bước 1: Ước tính Monthly Income
Nếu không có thông tin thu nhập chính xác, có thể ước tính từ chi tiêu:
```
Estimated Monthly Income = Monthly Expense / Expense Ratio
```

Thông thường, người ta chi tiêu khoảng 70% thu nhập, nên:
```
Estimated Monthly Income = Monthly Expense / 0.7
```

**Ví dụ với test case:**
```
Monthly Expense = 7,000,000 VND
Estimated Monthly Income = 7,000,000 / 0.7 = 10,000,000 VND
```

#### Bước 2: Tính % of Income to Save
```
Monthly Savings Needed = 465,000 VND
Estimated Monthly Income = 10,000,000 VND

% of Income to Save = (465,000 / 10,000,000) × 100
                    = 0.0465 × 100
                    = 4.65%
```

**Hoặc nếu tính từ Total Monthly Savings:**
```
Total Monthly Savings = 8,465,000 VND
Estimated Monthly Income = 10,000,000 VND

% of Income to Save = (8,465,000 / 10,000,000) × 100
                    = 84.65%
```

**Lưu ý:** Trong code hiện tại, có thể đang tính dựa trên `monthlySavingsNeeded` (chỉ phần thêm) hoặc tổng số tiền tiết kiệm. Cần kiểm tra logic trong frontend.

---

## Tóm Tắt Với Test Case

### Input:
- Current Age: 30
- Retirement Age: 65
- Current Savings: 100,000,000 VND
- Monthly Expense: 7,000,000 VND
- Monthly Savings: 8,000,000 VND
- Annual Return: 5%
- Inflation: 3%

### Output Mong Đợi:
1. **Total Future Value:** 3,911,601,500 VND ✅ (đã đúng)
2. **Total Needed:** ~4,727,520,000 VND
3. **Retirement Gap:** ~815,920,000 VND (hiện tại hiển thị 50,581,043 - SAI)
4. **Monthly Savings Needed:** ~465,000 VND (hiện tại hiển thị 44,522 - SAI)
5. **% of Income to Save:** ~4.65% (nếu tính từ monthlySavingsNeeded) hoặc ~84.65% (nếu tính từ total monthly savings)

---

## Vấn Đề Hiện Tại

Từ hình ảnh, tôi thấy:
- Total funds: **3.911.601.537 VND** ✅ (đúng)
- Required Monthly Savings: **44.522 VND** ❌ (sai, phải ~465,000)
- Retirement Gap: **50.581.043 VND** ❌ (sai, phải ~815,920,000)
- % of Income to Save: **0,45%** ❌ (sai, phải ~4.65%)

**Nguyên nhân có thể:**
1. Backend chưa được rebuild sau khi sửa code
2. Có lỗi trong cách tính Total Needed
3. Có lỗi trong cách tính Monthly Savings Needed

Cần kiểm tra lại code backend và đảm bảo rebuild đúng.

