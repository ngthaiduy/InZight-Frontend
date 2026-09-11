# Scenario AI Recommendations - Proposal

## Current Situation

### Hardcoded Content:
1. **Investment Suggestions:**
   - "Auto-invest 4M/month in growth ETFs"
   - "Allocate 3M/month in emergency fundings"
   - "Set monthly discretionary spending cap"

2. **Job Recommendations:**
   - Easy Jobs: Grab delivery, Sell 2nd hand clothes, Private tutoring, etc.
   - Skill-based Jobs: Write blogs, Create social posters, Build website

3. **Warning Message:**
   - "Unless you set spending limits, unexpected expenses could force you to pull from investments prematurely."

### Problem:
- All content is hardcoded in XML layout
- Not personalized based on user's financial situation
- Same recommendations for everyone regardless of income/expense
- Cannot adapt to different scenarios (inflation_low, crisis, etc.)

---

## Proposed Solution: AI-Generated Recommendations

### Option 1: Add Recommendations to Backend API Response

#### Backend Changes:
1. **Add Recommendations to ScenarioResultResponse:**
```java
public class ScenarioResultResponse {
    // ... existing fields ...
    public List<InvestmentRecommendation> investmentRecommendations;
    public List<JobRecommendation> jobRecommendations;
    public String warningMessage;
}

public class InvestmentRecommendation {
    public String title;
    public String description;
    public double suggestedAmount; // in VND
    public String icon; // icon name
}

public class JobRecommendation {
    public String title;
    public String category; // "easy" or "skill-based"
    public String estimatedEarning; // "+300K" or "50k/post"
    public String description;
}
```

2. **Add AI Service in Backend:**
```java
@Service
public class ScenarioRecommendationService {
    
    public List<InvestmentRecommendation> generateInvestmentRecommendations(
        double income, double expense, double netChange, String preset) {
        
        // Call AI service (e.g., OpenAI, Gemini, or local LLM)
        String prompt = buildInvestmentPrompt(income, expense, netChange, preset);
        String aiResponse = aiService.generate(prompt);
        return parseInvestmentRecommendations(aiResponse);
    }
    
    public List<JobRecommendation> generateJobRecommendations(
        double income, double expense, double netChange) {
        
        // Calculate how much extra income needed
        double extraIncomeNeeded = Math.abs(netChange);
        
        String prompt = buildJobPrompt(income, expense, extraIncomeNeeded);
        String aiResponse = aiService.generate(prompt);
        return parseJobRecommendations(aiResponse);
    }
    
    private String buildInvestmentPrompt(double income, double expense, 
                                        double netChange, String preset) {
        return String.format(
            "Based on the following financial scenario:\n" +
            "- Monthly Income: %.0f VND\n" +
            "- Monthly Expense: %.0f VND\n" +
            "- Net Change: %.0f VND/month\n" +
            "- Scenario: %s\n\n" +
            "Generate 3 personalized investment recommendations. " +
            "Each recommendation should include:\n" +
            "1. Title (e.g., 'Auto-invest X/month in growth ETFs')\n" +
            "2. Description (brief explanation)\n" +
            "3. Suggested monthly amount in VND\n" +
            "Format as JSON array.",
            income, expense, netChange, preset);
    }
}
```

### Option 2: Frontend Calls AI Directly

#### Frontend Changes:
1. **Add AI Recommendation Generation in ScenarioResultFragment:**
```java
private void generateAIRecommendations() {
    Bundle args = getArguments();
    if (args == null) return;
    
    double originalIncome = args.getDouble("originalIncome", 0);
    double originalExpense = args.getDouble("originalExpense", 0);
    double netChange = args.getDouble("netChange", 0);
    String preset = args.getString("preset", "");
    
    // Build prompt for AI
    String prompt = String.format(
        "I have monthly income of %.0f VND and expenses of %.0f VND. " +
        "Under %s scenario, my net savings will change by %.0f VND/month. " +
        "Please provide:\n" +
        "1. 3 investment recommendations with specific amounts\n" +
        "2. 5 job suggestions (mix of easy and skill-based)\n" +
        "3. A warning message about spending limits\n" +
        "Format as JSON.",
        originalIncome, originalExpense, preset, netChange);
    
    // Call Chat API
    ChatMessageRequest request = new ChatMessageRequest(userId, null, prompt);
    chatApiService.chatWithAi(request).enqueue(new Callback<ChatMessageResponse>() {
        @Override
        public void onResponse(Call<ChatMessageResponse> call, 
                              Response<ChatMessageResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                String aiResponse = response.body().getContent();
                parseAndDisplayRecommendations(aiResponse);
            }
        }
    });
}
```

### Option 3: Hybrid Approach (Recommended)

#### Backend generates structured recommendations using AI:
1. Backend calls AI service internally
2. Returns structured JSON in API response
3. Frontend displays dynamically

#### Implementation Steps:

**Step 1: Update Backend Response Model**
```java
// In ScenarioResultResponse
public RecommendationData recommendations;
```

**Step 2: Add Recommendation Generation in Backend**
```java
// In FinanceService.calcScenario()
ScenarioResult result = calculateScenario(...);

// Generate AI recommendations
RecommendationData recommendations = recommendationService.generate(
    req.income, req.expense, result.netChange, req.preset);

result.setRecommendations(recommendations);
```

**Step 3: Update Frontend to Display Dynamically**
```java
// In ScenarioResultFragment
private void displayRecommendations(RecommendationData recommendations) {
    if (recommendations == null) return;
    
    // Display investment recommendations
    displayInvestmentRecommendations(recommendations.investments);
    
    // Display job recommendations
    displayJobRecommendations(recommendations.easyJobs, recommendations.skillJobs);
    
    // Display warning
    displayWarning(recommendations.warningMessage);
}
```

---

## Recommended Implementation

### Phase 1: Backend AI Integration
1. Add AI service dependency (OpenAI, Gemini, or local LLM)
2. Create `RecommendationService` to generate personalized recommendations
3. Update `ScenarioResult` to include recommendations
4. Update API response model

### Phase 2: Frontend Dynamic Display
1. Create RecyclerView for recommendations (replace hardcoded layout)
2. Create adapter for InvestmentRecommendation and JobRecommendation
3. Update ScenarioResultFragment to parse and display recommendations
4. Add loading state while AI generates recommendations

### Phase 3: Caching & Optimization
1. Cache recommendations for same input (avoid repeated AI calls)
2. Add fallback to default recommendations if AI fails
3. Add user feedback mechanism (thumbs up/down on recommendations)

---

## Example AI Prompt Template

```
You are a financial advisor. Based on the following scenario:

User Financial Situation:
- Monthly Income: {income} VND
- Monthly Expenses: {expense} VND
- Current Net Savings: {netSavings} VND/month
- Scenario: {presetName}
- Projected Net Change: {netChange} VND/month
- Duration: {years} years

Please provide personalized recommendations:

1. Investment Recommendations (3 items):
   - Each should include: title, description, suggested monthly amount
   - Amounts should be realistic based on user's net savings
   - Focus on growth ETFs, emergency funds, or spending limits

2. Job Recommendations:
   - 3-5 easy jobs (no skill requirements)
   - 2-3 skill-based jobs
   - Each should include: job title, estimated monthly earning
   - Earnings should help close the financial gap

3. Warning Message:
   - One sentence about spending limits and investment risks

Format response as JSON:
{
  "investments": [
    {"title": "...", "description": "...", "amount": 4000000}
  ],
  "easyJobs": [
    {"title": "...", "earning": "+300K"}
  ],
  "skillJobs": [
    {"title": "...", "earning": "50k/post"}
  ],
  "warning": "..."
}
```

---

## Benefits

1. **Personalized:** Recommendations adapt to user's financial situation
2. **Dynamic:** Different scenarios get different recommendations
3. **Scalable:** Easy to add new recommendation types
4. **Maintainable:** No hardcoded content to update
5. **Intelligent:** AI can consider multiple factors (income, expense, gap, scenario type)

---

## Fallback Strategy

If AI service is unavailable or fails:
1. Use rule-based recommendations based on netChange value
2. Show default recommendations from a template
3. Display message: "AI recommendations unavailable, showing general advice"

