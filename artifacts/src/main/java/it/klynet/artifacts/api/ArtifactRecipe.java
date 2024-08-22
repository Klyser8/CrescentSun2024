package it.klynet.artifacts.api;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a recipe for an artifact.
 */
public abstract class ArtifactRecipe {
    private final NamespacedKey key;
    private final ItemStack result;
    private final Map<Character, ItemStack> ingredients = new HashMap<>(9);

    /**
     * Creates a new artifact recipe.
     *
     * @param key         the key of the recipe
     * @param result      the result of the recipe
     * @param ingredients the ingredients of the recipe. Must be a map of size 9.
     */
    public ArtifactRecipe(NamespacedKey key, ItemStack result,
                          Map<Character, ItemStack> ingredients) {
        this.key = key;
        this.result = result;
        if (ingredients.size() != 9) {
            throw new IllegalArgumentException("Ingredients map must have a size of 9!");
        }
        this.ingredients.putAll(ingredients);
    }

    public Map<Character, ItemStack> getIngredients() {
        return ingredients;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public ItemStack getResult() {
        return result;
    }

}