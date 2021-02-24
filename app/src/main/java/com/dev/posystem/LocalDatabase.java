package com.dev.posystem;

import androidx.annotation.NonNull;

public class LocalDatabase {

    private String code;
    private String description;

    public LocalDatabase(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    @Override
    public String toString() {
        return this.code + " - " + this.description;
    }
}
