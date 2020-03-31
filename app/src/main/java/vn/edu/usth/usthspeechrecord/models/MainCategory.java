package vn.edu.usth.usthspeechrecord.models;

import java.util.List;

public class MainCategory {
    String name;
    private int id;
    private List<Category> categoryList;

    public MainCategory(String name, int id, List<Category> categoryList) {
        this.name = name;
        this.id = id;
        this.categoryList = categoryList;
    }

    public List<Category> getCategoryList() {
        return categoryList;
    }

    public int getCatNum() {
        return id;
    }

    public String getName() {
        return name;
    }
}
