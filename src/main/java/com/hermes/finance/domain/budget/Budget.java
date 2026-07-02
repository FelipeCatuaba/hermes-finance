package com.hermes.finance.domain.budget;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Budget {
    private UUID id;
    private UUID userId;
    private UUID categoryId;
    private int month;
    private int year;
    private BigDecimal amountLimit;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public BigDecimal getAmountLimit() { return amountLimit; }
    public void setAmountLimit(BigDecimal amountLimit) { this.amountLimit = amountLimit; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
