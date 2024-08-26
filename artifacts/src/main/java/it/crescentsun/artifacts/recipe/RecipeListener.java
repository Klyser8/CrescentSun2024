package it.crescentsun.artifacts.recipe;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import it.crescentsun.artifacts.Artifacts;
import it.crescentsun.artifacts.api.ArtifactUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeListener implements Listener {

    private final Artifacts plugin;

    public RecipeListener(Artifacts plugin) {
        this.plugin = plugin;
    }

    /**
     * Prevents crafting with custom artifact items.
     *
     */
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();

        // Check if the recipe is either a ShapedRecipe or a ShapelessRecipe
        if (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe) {
            // Initialize flags for detecting the presence of an Artifact
            boolean hasArtifact = false;
            boolean usesArtifact = false;

            // Iterate through the crafting inventory to check if it contains an Artifact
            for (ItemStack item : event.getInventory().getContents()) {
                if (item == null) {
                    continue;
                }

                if (ArtifactUtil.isArtifact(item)) {
                    hasArtifact = true;
                }
            }

            // If the crafting inventory has an Artifact, check if the recipe uses an Artifact
            if (hasArtifact) {
                List<RecipeChoice> ingredients;

                // If the recipe is a ShapedRecipe, get the list of ingredients from the choice map
                if (recipe instanceof ShapedRecipe) {
                    ingredients = new ArrayList<>(((ShapedRecipe) recipe).getChoiceMap().values());
                } else {
                    // If the recipe is a ShapelessRecipe, get the list of ingredients directly
                    ingredients = ((ShapelessRecipe) recipe).getChoiceList();
                }

                // Iterate through the ingredients to check if any of them are Artifacts
                for (RecipeChoice choice : ingredients) {
                    if (choice instanceof RecipeChoice.ExactChoice) {
                        for (ItemStack choiceItem : ((RecipeChoice.ExactChoice) choice).getChoices()) {
                            if (ArtifactUtil.isArtifact(choiceItem)) {
                                usesArtifact = true;
                                break;
                            }
                        }
                    }
                    if (usesArtifact) {
                        break;
                    }
                }

                // If the recipe doesn't use an Artifact, set the result to AIR
                if (!usesArtifact) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRecipeBookClick(PlayerRecipeBookClickEvent event) {
        NamespacedKey recipeKey = event.getRecipe();
        Player player = event.getPlayer();

        // Check if the clicked recipe is one of your custom artifact recipes
        if (isCustomArtifactRecipe(recipeKey)) {
            Recipe recipe = Bukkit.getRecipe(recipeKey);

            if (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe) {
                List<ItemStack> ingredients = getRecipeIngredients(recipe);

                // Check if the player has the required artifacts in their inventory
                if (!playerHasRequiredArtifacts(player, ingredients)) {
                    event.setCancelled(true);
                } else {
                    // Cancel the event to prevent the default behavior
                    event.setCancelled(true);

                    // Set the items in the crafting table manually
                    setItemsInCraftingTable(player, recipe);
                }
            }
        }
    }

    private boolean isCustomArtifactRecipe(NamespacedKey recipeKey) {
        return recipeKey.getNamespace().equals("artifacts");
    }

    /**
     * Retrieves a list of recipe ingredients for the given recipe.
     * Only supports ShapedRecipe.
     *
     * @param recipe The recipe to extract ingredients from.
     * @return A list of ItemStacks representing the ingredients in the recipe, or null if the recipe is not supported.
     */
    private List<ItemStack> getRecipeIngredients(Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            // Obtain the list of RecipeChoices from the ShapedRecipe
            List<RecipeChoice> choices = new ArrayList<>(((ShapedRecipe) recipe).getChoiceMap().values());
            List<ItemStack> ingredients = new ArrayList<>();
            // Iterate through the choices to extract ExactChoices and their ItemStacks
            for (RecipeChoice choice : choices) {
                if (choice instanceof RecipeChoice.ExactChoice) {
                    ingredients.addAll(((RecipeChoice.ExactChoice) choice).getChoices());
                }
            }
            return ingredients;
        }
        return null;
    }

    /**
     * Checks if the player has the required artifacts in their inventory to craft the recipe.
     *
     * @param player      The player to check the inventory of.
     * @param ingredients The list of ItemStacks representing the required artifacts.
     * @return True if the player has the required artifacts, otherwise false.
     */
    private boolean playerHasRequiredArtifacts(Player player, List<ItemStack> ingredients) {
        for (ItemStack ingredient : ingredients) {
            if (ArtifactUtil.isArtifact(ingredient)) {
                int requiredAmount = ingredient.getAmount();
                int playerAmount = 0;

                // Iterate through the player's inventory and count the number of matching artifact items
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.isSimilar(ingredient)) {
                        playerAmount += item.getAmount();
                    }
                }

                // Check if the player has the required amount of the artifact
                if (playerAmount < requiredAmount) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Sets the items in the player's CraftingInventory based on the given recipe.
     * Supports both ShapedRecipe and ShapelessRecipe.
     *
     * @param player The player whose CraftingInventory should be updated.
     * @param recipe The recipe to use for setting items in the CraftingInventory.
     */
    private void setItemsInCraftingTable(Player player, Recipe recipe) {
        // Find the player's CraftingInventory if they have one open
        CraftingInventory craftingInventory = findCraftingInventory(player);
        if (craftingInventory != null) {
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                // Get the ingredient map and shape from the ShapedRecipe
                Map<Character, ItemStack> ingredientMap = shapedRecipe.getIngredientMap();
                String[] shape = shapedRecipe.getShape();

                // Iterate through the shape and set the corresponding items in the CraftingInventory
                for (int row = 0; row < shape.length; row++) {
                    for (int col = 0; col < shape[row].length(); col++) {
                        char ingredientChar = shape[row].charAt(col);
                        ItemStack ingredient = ingredientMap.get(ingredientChar);
                        int index = row * 3 + col + 1;
                        craftingInventory.setItem(index, ingredient != null ? ingredient.clone() : null);
                    }
                }
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                // Get the list of ingredients from the ShapelessRecipe
                List<ItemStack> ingredients = shapelessRecipe.getIngredientList();
                // Set the items in the CraftingInventory based on the list of ingredients
                for (int i = 0; i < ingredients.size(); i++) {
                    craftingInventory.setItem(i, ingredients.get(i).clone());
                }
            }
        }
    }

    /**
     * Finds and returns the CraftingInventory object for the player, if they have one open.
     *
     * @param player The player to find the CraftingInventory for.
     * @return The CraftingInventory object if the player has one open, otherwise null.
     */
    private CraftingInventory findCraftingInventory(Player player) {
        // Get the top inventory for the player, which should be the crafting inventory when open
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        // Check if the top inventory is a CraftingInventory and return it if so
        if (topInventory instanceof CraftingInventory) {
            return (CraftingInventory) topInventory;
        }
        return null;
    }

}
