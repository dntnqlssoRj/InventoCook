package inventocook.service;

import inventocook.model.Ingredient;
import inventocook.model.Recipe;
import java.util.ArrayList;
import java.util.List;

public class RecipeRecommender {
    // 베이스라인 스텁 — 추후 구현 예정
    public List<Recipe> recommend(List<Ingredient> have, List<Recipe> db, boolean prioritizeExpiring) {
        return new ArrayList<>(db); // TODO: 충족률/임박 가중치 정렬
    }
}

