package inventocook.service;

import inventocook.model.Ingredient;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ExpirationChecker {
    public List<Ingredient> dueWithin(List<Ingredient> all, int days) {
        LocalDate now = LocalDate.now();
        LocalDate limit = now.plusDays(days);
        return all.stream()
                .filter(i -> i.expiry != null && !i.expiry.isAfter(limit))
                .sorted((a,b) -> a.expiry.compareTo(b.expiry))
                .collect(Collectors.toList());
    }
}

