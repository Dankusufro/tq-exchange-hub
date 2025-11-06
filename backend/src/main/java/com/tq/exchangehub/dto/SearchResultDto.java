package com.tq.exchangehub.dto;

import java.util.ArrayList;
import java.util.List;

public class SearchResultDto {

    private String query;
    private List<CategoryDto> categories = new ArrayList<>();
    private List<ItemSummaryDto> items = new ArrayList<>();
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<CategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDto> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
    }

    public List<ItemSummaryDto> getItems() {
        return items;
    }

    public void setItems(List<ItemSummaryDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
