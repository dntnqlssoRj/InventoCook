package inventocook.repo;

import inventocook.model.Ingredient;
import java.util.List;

public interface InventoryRepository {
    List<Ingredient> findAll();
    void saveAll(List<Ingredient> list);
}

