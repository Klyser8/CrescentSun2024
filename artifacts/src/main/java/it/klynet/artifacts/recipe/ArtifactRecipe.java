package it.klynet.artifacts.recipe;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.ArrayList;
import java.util.List;

public class ArtifactRecipe {
    private ItemStack result;
    private List<RecipeChoice> ingredients;

    public ArtifactRecipe(ItemStack result) {
        this.result = result;
        this.ingredients = new ArrayList<>();
    }

    public ArtifactRecipe addIngredient(RecipeChoice ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    public ItemStack getResult() {
        return result;
    }

    public List<RecipeChoice> getIngredients() {
        return ingredients;
    }
}
