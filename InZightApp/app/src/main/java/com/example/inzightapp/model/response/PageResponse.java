package com.example.inzightapp.model.response;

import java.util.List;

// Dùng Generic <T> để tái sử dụng cho User, Transaction, Post...
public class PageResponse<T> {
    private List<T> content;      // Quan trọng nhất: Chứa danh sách User
    private int totalPages;       // Tổng số trang
    private long totalElements;   // Tổng số phần tử
    private int size;             // Kích thước trang
    private int number;           // Trang hiện tại (bắt đầu từ 0)
    private boolean last;         // Có phải trang cuối không?

    // Getters và Setters
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getNumber() {
        return number;
    }

    public boolean isLast() {
        return last;
    }
}