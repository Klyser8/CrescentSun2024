package it.klynet.artifacts.registry;

import it.klynet.artifacts.api.ArtifactRecipe;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ArtifactRecipeRegistry {

    public static final Map<NamespacedKey, Recipe> RECIPES = new HashMap<>();

    public static void registerShapedRecipe(NamespacedKey key, ItemStack result, ArtifactRecipe artifactRecipe) { //
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        Iterator<Character> iterator = artifactRecipe.getIngredients().keySet().iterator();
        String line1 = String.valueOf(iterator.next() + iterator.next() + iterator.next());
        String line2 = String.valueOf(iterator.next() + iterator.next() + iterator.next());
        String line3 = String.valueOf(iterator.next() + iterator.next() + iterator.next());
        recipe.shape(line1, line2, line3);

        for (char c : artifactRecipe.getIngredients().keySet()) {
            RecipeChoice choice = new RecipeChoice.ExactChoice(artifactRecipe.getIngredients().get(c));
            recipe.setIngredient(c, choice);
        }
        RECIPES.put(key, recipe);
        Bukkit.addRecipe(recipe);
    }

    /*    private void registerKlystarixRecipe() {
        ItemStack klystarixItem = getArtifact(KLYSTARIX_KEY).build(1);

        ShapedRecipe recipe = new ShapedRecipe(KLYSTARIX_KEY, klystarixItem);
        recipe.shape("KKK", "KEK", "KKK");

        RecipeChoice shardChoice = new RecipeChoice.ExactChoice(getArtifact(KLYSTARIX_SHARD_KEY).build(1));
        recipe.setIngredient('K', shardChoice);
        RecipeChoice echoShardChoice = new RecipeChoice.ExactChoice(new ItemStack(Material.ECHO_SHARD, 1));
        recipe.setIngredient('E', echoShardChoice);

        Bukkit.addRecipe(recipe);
    }*/

}
