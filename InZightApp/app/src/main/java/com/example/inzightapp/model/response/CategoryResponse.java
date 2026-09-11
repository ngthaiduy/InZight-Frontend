package com.example.inzightapp.model.response;

import java.io.Serializable;

public class CategoryResponse implements Serializable {
    private Long id;
    private String name;
    private String type;
    private String iconUrl; // ✅ thêm trường iconUrl

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    @Override
    public String toString() {
        return name;
    }
}