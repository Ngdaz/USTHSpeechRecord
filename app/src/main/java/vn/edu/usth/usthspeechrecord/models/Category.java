package vn.edu.usth.usthspeechrecord.models;

public class Category {
    private String catName;
    private String domainId;
    private int catNum;

    public Category(String catName, String domainId, int catNum) {
        this.catName = catName;
        this.domainId = domainId;
        this.catNum = catNum;
    }

    public String getCatName() {
        return catName;
    }

    public int getCatNum() {
        return catNum;
    }

    public String getDomainId() {
        return domainId;
    }
}
