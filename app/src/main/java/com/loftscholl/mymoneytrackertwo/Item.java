package com.loftscholl.mymoneytrackertwo;

/**
 * Created by Гео on 26.06.2017.
 */

public class Item {
    public static final String TYPE_EXPENSE = "expense";
    public static final String TYPE_INCOME = "income";
    final String name;
    final String type;
    final int price;
    int id;

    public Item(String name, int price, String type) {
        this.name = name;
        this.price = price;
        this.type = type;
    }
}
