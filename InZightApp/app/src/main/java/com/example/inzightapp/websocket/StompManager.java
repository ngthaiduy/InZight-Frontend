package com.example.inzightapp.websocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.LifecycleEvent;
import ua.naiksoftware.stomp.dto.StompHeader;
import ua.naiksoftware.stomp.dto.StompMessage;

/**
 * Quản lý kết nối STOMP dùng chung (singleton đơn giản) cho toàn app.
 * Giữ token từ SharedPreferences và auto reconnect nhẹ.
 */
public class StompManager {

    private static final String TAG = "StompManager";
    private static StompManager instance;

    private final Context appContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private StompClient stompClient;
    private final AtomicBoolean connecting = new AtomicBoolean(false);

    private String baseWsUrl;

    public interface MessageHandler {
        void onMessage(StompMessage message);
    }

    private StompManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static synchronized StompManager getInstance(Context context) {
        if (instance == null) {
            instance = new StompManager(context);
        }
        return instance;
    }

    /**
     * Khởi tạo kết nối. Base WS url ví dụ:
     * ws://10.0.2.2:8080/ws/websocket (emulator) hoặc wss://domain/ws/websocket (prod)
     */
    public void connect(String wsUrl, @Nullable Runnable onConnected, @Nullable Runnable onError) {
        if (stompClient != null && stompClient.isConnected()) {
            if (onConnected != null) onConnected.run();
            return;
        }
        if (connecting.get()) return;
        connecting.set(true);

        Map<String, String> headers = new HashMap<>();
        SharedPreferences prefs = appContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        if (token == null) {
            Log.w(TAG, "No jwt_token in SharedPreferences; aborting WS connect");
            connecting.set(false);
            if (onError != null) mainHandler.post(onError);
            return;
        }
        headers.put("Authorization", "Bearer " + token);

        // Đưa token vào query để backend interceptor bắt được trong handshake HTTP
        String wsEndpoint = wsUrl;
        wsEndpoint = wsUrl + (wsUrl.contains("?") ? "&" : "?") + "token=" + token;
        Log.d(TAG, "Connecting WS to: " + wsEndpoint);

        this.baseWsUrl = wsEndpoint;
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsEndpoint);

        stompClient.lifecycle()
                .subscribe(lifecycleEvent -> {
                    if (lifecycleEvent.getType() == LifecycleEvent.Type.OPENED) {
                        connecting.set(false);
                                if (onConnected != null) {
                                    mainHandler.post(onConnected);
                                }
                    } else if (lifecycleEvent.getType() == LifecycleEvent.Type.ERROR
                            || lifecycleEvent.getType() == LifecycleEvent.Type.CLOSED) {
                        connecting.set(false);
                                Throwable cause = lifecycleEvent.getException();
                                Log.e(TAG, "STOMP error/closed: " + lifecycleEvent.getMessage(), cause);
                                if (onError != null) {
                                    mainHandler.post(onError);
                                }
                            }
                        },
                        throwable -> {
                            connecting.set(false);
                            Log.e(TAG, "STOMP lifecycle exception", throwable);
                            if (onError != null) {
                                mainHandler.post(onError);
                    }
                });

        // Convert header map to the library's header list type
        List<StompHeader> stompHeaders = new ArrayList<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            stompHeaders.add(new StompHeader(entry.getKey(), entry.getValue()));
        }

        stompClient.connect(stompHeaders);
    }

    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }
    }

    public void subscribe(String destination, MessageHandler handler) {
        subscribeInternal(destination, handler, 3);
    }

    private void subscribeInternal(String destination, MessageHandler handler, int retries) {
        if (stompClient == null || !stompClient.isConnected()) {
            if (retries > 0) {
                // Thử lại nhẹ sau 200ms khi CONNECTED frame tới
                mainHandler.postDelayed(() -> subscribeInternal(destination, handler, retries - 1), 200);
            } else {
                Log.w(TAG, "subscribe called when stomp not connected, giving up");
            }
            return;
        }
        stompClient.topic(destination).subscribe(handler::onMessage);
    }

    public void send(String destination, String payload) {
        if (stompClient == null || !stompClient.isConnected()) {
            Log.w(TAG, "send called when stomp not connected");
            return;
        }
        stompClient.send(destination, payload)
                .subscribe(
                        () -> { /* no-op on success */ },
                        throwable -> Log.e(TAG, "Error sending STOMP message", throwable)
                );
    }
}

