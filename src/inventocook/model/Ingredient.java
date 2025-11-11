package inventocook.model;

import java.time.LocalDate;

public class Ingredient {
    public String name;
    public String category;
    public LocalDate expiry;
    public int qty;

    public Ingredient(String name, String category, LocalDate expiry, int qty) {
        this.name = name;
        this.category = category;
        this.expiry = expiry;
        this.qty = qty;
    }
}

