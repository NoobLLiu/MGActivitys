package cn.gmzc.mgactivitys.model;

public class ShopItem {
    private String type;
    private String name;
    private double price;
    private int dailyLimit;
    private int purchasedToday;

    public ShopItem() {}

    public ShopItem(String type, String name, double price, int dailyLimit, int purchasedToday) {
        this.type = type;
        this.name = name;
        this.price = price;
        this.dailyLimit = dailyLimit;
        this.purchasedToday = purchasedToday;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(int dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public int getPurchasedToday() {
        return purchasedToday;
    }

    public void setPurchasedToday(int purchasedToday) {
        this.purchasedToday = purchasedToday;
    }
}
