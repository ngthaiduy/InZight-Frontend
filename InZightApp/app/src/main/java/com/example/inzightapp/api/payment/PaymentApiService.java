package com.example.inzightapp.api.payment;

import com.example.inzightapp.model.response.PaymentResponse;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PaymentApiService {

    @POST("api/pay/create")
    Call<PaymentResponse> createPayment(@Query("plan") String plan);

    @POST("api/pay/verify-payment")
    Call<VerifyPaymentResponse> verifyPayment(@Query("orderCode") Long orderCode);

    // Inner class cho verify payment response
    class VerifyPaymentResponse {
        private boolean success;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

