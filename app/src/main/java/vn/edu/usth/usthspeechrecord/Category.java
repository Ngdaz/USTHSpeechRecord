package vn.edu.usth.usthspeechrecord;

public class Category {
    private String catName;
    private int catNum;

    public Category() {
        catName = "";
        catNum = 0;
    }

    public Category(String catName, int catNum) {
        this.catName = catName;
        this.catNum = catNum;
    }

    public String getCatName() {
        return catName;
    }

    public int getCatNum() {
        return catNum;
    }
}
