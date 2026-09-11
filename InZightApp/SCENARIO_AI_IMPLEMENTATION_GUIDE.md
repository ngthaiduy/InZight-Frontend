# Scenario AI Recommendations - Implementation Guide

## Tóm tắt

Các phần trong hình (Spending Limits & Investments, Boost Your Income, job suggestions) **CÓ LIÊN QUAN** đến Scenario feature và hiện đang bị **HARDCODE** trong XML layout.

## Giải pháp đã implement

### 1. Rule-based Recommendations (Hiện tại)

Đã tạo `ScenarioRecommendationGenerator` để tự động generate recommendations dựa trên:
- Income/Expense của user
- Net change từ scenario
- Preset type (inflation_low, crisis, etc.)

**Ưu điểm:**
- Không cần AI service
- Nhanh, không tốn chi phí
- Recommendations phù hợp với tình hình tài chính

**Nhược điểm:**
- Chưa thông minh như AI
- Recommendations có thể lặp lại

### 2. Cấu trúc đã tạo

#### Models:
- `ScenarioRecommendationResponse.java` - Model cho recommendations
- `ScenarioRecommendationResponse.InvestmentRecommendation` - Investment suggestions
- `ScenarioRecommendationResponse.JobRecommendation` - Job suggestions

#### Generator:
- `ScenarioRecommendationGenerator.java` - Utility class để generate recommendations

#### Fragment:
- `ScenarioResultFragment.java` - Đã thêm method `generateAndDisplayRecommendations()`

## Cách nâng cấp lên AI

### Option 1: Backend AI Integration (Khuyến nghị)

**Bước 1: Thêm AI Service vào Backend**
```java
// In FinanceService.java
public ScenarioResult calcScenario(ScenarioInput req) {
    // ... existing calculation ...
    
    // Generate AI recommendations
    ScenarioRecommendationResponse recommendations = 
        aiRecommendationService.generateRecommendations(
            req.income, req.expense, result.netChange, req.preset);
    
    result.setRecommendations(recommendations);
    return result;
}
```

**Bước 2: Tạo AI Service**
```java
@Service
public class AIRecommendationService {
    
    @Autowired
    private ChatApiService chatApiService; // Your existing AI chat service
    
    public ScenarioRecommendationResponse generateRecommendations(
            double income, double expense, double netChange, String preset) {
        
        String prompt = buildPrompt(income, expense, netChange, preset);
        
        // Call AI (OpenAI, Gemini, or your existing chat AI)
        ChatMessageRequest request = new ChatMessageRequest(userId, null, prompt);
        ChatMessageResponse response = chatApiService.chatWithAi(request);
        
        // Parse AI response to JSON
        return parseAIResponse(response.getContent());
    }
    
    private String buildPrompt(double income, double expense, 
                              double netChange, String preset) {
        return String.format(
            "You are a financial advisor. Based on:\n" +
            "- Income: %.0f VND/month\n" +
            "- Expense: %.0f VND/month\n" +
            "- Net Change: %.0f VND/month\n" +
            "- Scenario: %s\n\n" +
            "Provide personalized recommendations in JSON format:\n" +
            "{\n" +
            "  \"investments\": [\n" +
            "    {\"title\": \"...\", \"description\": \"...\", \"amount\": 4000000, \"icon\": \"ic_finance\"}\n" +
            "  ],\n" +
            "  \"easyJobs\": [\n" +
            "    {\"title\": \"...\", \"category\": \"easy\", \"earning\": \"+300K\", \"description\": \"...\"}\n" +
            "  ],\n" +
            "  \"skillJobs\": [\n" +
            "    {\"title\": \"...\", \"category\": \"skill-based\", \"earning\": \"50k/post\", \"description\": \"...\"}\n" +
            "  ],\n" +
            "  \"warning\": \"...\"\n" +
            "}",
            income, expense, netChange, preset);
    }
}
```

### Option 2: Frontend AI Call

**Trong ScenarioResultFragment.java:**
```java
private void generateAndDisplayRecommendations(double originalIncome, 
                                               double originalExpense, 
                                               double netChange, 
                                               String preset) {
    // Build prompt
    String prompt = String.format(
        "I have income %.0f VND, expense %.0f VND, net change %.0f VND under %s scenario. " +
        "Provide personalized recommendations in JSON format...",
        originalIncome, originalExpense, netChange, preset);
    
    // Call AI
    ChatMessageRequest request = new ChatMessageRequest(userId, null, prompt);
    chatApiService.chatWithAi(request).enqueue(new Callback<ChatMessageResponse>() {
        @Override
        public void onResponse(Call<ChatMessageResponse> call, 
                              Response<ChatMessageResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                String aiResponse = response.body().getContent();
                ScenarioRecommendationResponse recommendations = 
                    parseAIResponse(aiResponse);
                displayRecommendations(recommendations);
            } else {
                // Fallback to rule-based
                ScenarioRecommendationResponse recommendations = 
                    ScenarioRecommendationGenerator.generateRecommendations(
                        originalIncome, originalExpense, netChange, preset);
                displayRecommendations(recommendations);
            }
        }
    });
}
```

## Cách update Layout để hiển thị dynamic

### Hiện tại: Hardcoded trong XML
```xml
<TextView
    android:text="Auto-invest 4M/month in growth ETFs"/>
```

### Giải pháp: Dùng RecyclerView

**1. Thay thế hardcoded LinearLayout bằng RecyclerView:**
```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerInvestments"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:nestedScrollingEnabled="false"/>
```

**2. Tạo Adapter:**
```java
public class InvestmentRecommendationAdapter extends 
    RecyclerView.Adapter<InvestmentRecommendationAdapter.ViewHolder> {
    
    private List<ScenarioRecommendationResponse.InvestmentRecommendation> items;
    
    // ... adapter implementation ...
}
```

**3. Update Fragment:**
```java
private void displayInvestmentRecommendations(
        List<ScenarioRecommendationResponse.InvestmentRecommendation> investments) {
    RecyclerView recycler = rootView.findViewById(R.id.recyclerInvestments);
    InvestmentRecommendationAdapter adapter = 
        new InvestmentRecommendationAdapter(investments);
    recycler.setLayoutManager(new LinearLayoutManager(getContext()));
    recycler.setAdapter(adapter);
}
```

## Kế hoạch triển khai

### Phase 1: Rule-based (Đã hoàn thành ✅)
- [x] Tạo models
- [x] Tạo generator
- [x] Update fragment để gọi generator
- [ ] Update layout để hiển thị dynamic (cần RecyclerView)

### Phase 2: Layout Dynamic Display
- [ ] Thay hardcoded TextViews bằng RecyclerView
- [ ] Tạo adapters cho investments và jobs
- [ ] Test với rule-based recommendations

### Phase 3: AI Integration
- [ ] Tích hợp AI service (backend hoặc frontend)
- [ ] Parse AI response
- [ ] Fallback to rule-based nếu AI fail
- [ ] Cache recommendations để tránh gọi AI nhiều lần

## Lưu ý

1. **Hiện tại:** Recommendations được generate nhưng chưa hiển thị vì layout vẫn hardcode
2. **Cần làm:** Update layout để dùng RecyclerView thay vì hardcoded TextViews
3. **AI:** Có thể tích hợp sau, hiện tại rule-based đã đủ tốt cho MVP

## Test

Để test recommendations:
```java
ScenarioRecommendationResponse recs = 
    ScenarioRecommendationGenerator.generateRecommendations(
        15_000_000, // income
        7_000_000,  // expense
        -500_000,   // netChange (negative = crisis)
        "crisis_light");
        
// recs.investments sẽ có emergency fund suggestions
// recs.easyJobs sẽ có nhiều job suggestions hơn
// recs.warningMessage sẽ cảnh báo về spending limits
```

