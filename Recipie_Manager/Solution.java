import java.util.*;

public class RecipeManagerImpl implements RecipeManager {

    static class Ingredient {
        int quantity;
        int caloriesPerUnit;
        double costPerUnit;

        Ingredient(int quantity) {
            this.quantity = quantity;
        }

        Ingredient(int quantity,
                   int caloriesPerUnit,
                   double costPerUnit) {
            this.quantity = quantity;
            this.caloriesPerUnit = caloriesPerUnit;
            this.costPerUnit = costPerUnit;
        }
    }

    static class Recipe {
        Map<String, Ingredient> ingredients =
                new HashMap<>();

        int servings = -1;

        Set<String> tags =
                new HashSet<>();
    }

    private Map<String, Recipe> recipes;

    public RecipeManagerImpl() {
        recipes = new HashMap<>();
    }

    // ---------------- LEVEL 1 ----------------

    @Override
    public String addRecipe(String recipeName) {

        if (recipes.containsKey(recipeName))
            return "false";

        recipes.put(recipeName,
                new Recipe());

        return "true";
    }

    @Override
    public String addIngredient(String recipeName,
                                String ingredientName,
                                String quantity) {

        Recipe recipe =
                recipes.get(recipeName);

        if (recipe == null)
            return "";

        recipe.ingredients.put(
                ingredientName,
                new Ingredient(
                        Integer.parseInt(quantity)));

        return quantity;
    }

    @Override
    public String getRecipe(String recipeName) {

        Recipe recipe =
                recipes.get(recipeName);

        if (recipe == null
                || recipe.ingredients.isEmpty())
            return "";

        List<String> names =
                new ArrayList<>(
                        recipe.ingredients.keySet());

        Collections.sort(names);

        List<String> result =
                new ArrayList<>();

        for (String ing : names) {

            result.add(
                    ing + "("
                            + recipe.ingredients
                            .get(ing)
                            .quantity
                            + ")");
        }

        return String.join(", ", result);
    }

    @Override
    public String removeIngredient(String recipeName,
                                   String ingredientName) {

        Recipe recipe =
                recipes.get(recipeName);

        if (recipe == null
                || !recipe.ingredients.containsKey(
                ingredientName))
            return "false";

        recipe.ingredients.remove(
                ingredientName);

        return "true";
    }

    @Override
    public String deleteRecipe(String recipeName) {

        if (!recipes.containsKey(
                recipeName))
            return "false";

        recipes.remove(recipeName);

        return "true";
    }

    // ---------------- LEVEL 2 ----------------

    @Override
    public String addIngredientWithProps(
            String recipeName,
            String ingredientName,
            String quantity,
            String caloriesPerUnit,
            String costPerUnit) {

        Recipe recipe =
                recipes.get(recipeName);

        if (recipe == null)
            return "";

        recipe.ingredients.put(
                ingredientName,
                new Ingredient(
                        Integer.parseInt(quantity),
                        Integer.parseInt(
                                caloriesPerUnit),
                        Double.parseDouble(
                                costPerUnit)));

        return quantity;
    }

    @Override
    public String getTotalCalories(
            String recipeName) {

        Recipe recipe =
                recipes.get(recipeName);

        if (recipe == null)
            return "";

        long total = 0;

        for (Ingredient i :
                recipe.ingredients.values()) {

            total +=
                    (long) i.quantity
                            * i.caloriesPerUnit;
        }

        return String.valueOf(total);
    }

    @Override
    public String getTotalCost(
            String recipeName) {

        Recipe recipe =
                recipes.get(recipeName);

        if (recipe == null)
            return "";

        double total = 0;

        for (Ingredient i :
                recipe.ingredients.values()) {

            total +=
                    i.quantity
                            * i.costPerUnit;
        }

        return String.valueOf(
                (int) total);
    }

    @Override
    public String findRecipesByIngredient(
            String ingredientName) {

        List<String> result =
                new ArrayList<>();

        for (String recipe :
                recipes.keySet()) {

            if (recipes.get(recipe)
                    .ingredients
                    .containsKey(
                            ingredientName)) {

                result.add(recipe);
            }
        }

        Collections.sort(result);

        return String.join(
                ", ",
                result);
    }

    @Override
    public String getMostExpensiveRecipes(
            String n) {

        int limit =
                Integer.parseInt(n);

        List<String> names =
                new ArrayList<>(
                        recipes.keySet());

        names.sort((a, b) -> {

            int ca =
                    Integer.parseInt(
                            getTotalCost(a));

            int cb =
                    Integer.parseInt(
                            getTotalCost(b));

            if (ca != cb)
                return cb - ca;

            return a.compareTo(b);
        });

        List<String> result =
                new ArrayList<>();

        for (int i = 0;
             i < Math.min(limit,
                     names.size());
             i++) {

            String r = names.get(i);

            result.add(
                    r + "("
                            + getTotalCost(r)
                            + ")");
        }

        return String.join(
                ", ",
                result);
    }

    // ---------------- LEVEL 3 ----------------

    @Override
    public String setServingSize(
            String recipeName,
            String servings) {

        Recipe recipe =
                recipes.get(recipeName);

        if (recipe == null)
            return "";

        recipe.servings =
                Integer.parseInt(servings);

        return servings;
    }

    @Override
    public String scaleRecipe(
            String recipeName,
            String targetServings) {

        Recipe recipe =
                recipes.get(recipeName);

        if (recipe == null
                || recipe.servings <= 0)
            return "";

        int target =
                Integer.parseInt(
                        targetServings);

        List<String> names =
                new ArrayList<>(
                        recipe.ingredients.keySet());

        Collections.sort(names);

        List<String> result =
                new ArrayList<>();

        for (String ing : names) {

            Ingredient i =
                    recipe.ingredients
                            .get(ing);

            int scaled =
                    (i.quantity
                            * target)
                            / recipe.servings;

            result.add(
                    ing + "("
                            + scaled
                            + ")");
        }

        return String.join(
                ", ",
                result);
    }

    @Override
    public String getCaloriesPerServing(
            String recipeName) {

        Recipe recipe =
                recipes.get(recipeName);

        if (recipe == null
                || recipe.servings <= 0)
            return "";

        int total =
                Integer.parseInt(
                        getTotalCalories(
                                recipeName));

        return String.valueOf(
                total
                        / recipe.servings);
    }

    @Override
    public String addRecipeTag(
            String recipeName,
            String tag) {

        Recipe recipe =
                recipes.get(recipeName);

        if (recipe == null)
            return "false";

        recipe.tags.add(tag);

        return "true";
    }

    @Override
    public String findRecipesByTag(
            String tag) {

        List<String> result =
                new ArrayList<>();

        for (String recipe :
                recipes.keySet()) {

            if (recipes.get(recipe)
                    .tags
                    .contains(tag)) {

                result.add(recipe);
            }
        }

        Collections.sort(result);

        return String.join(
                ", ",
                result);
    }

    @Override
    public String findRecipesInBudget(
            String maxCostPerServing) {

        double limit =
                Double.parseDouble(
                        maxCostPerServing);

        List<String> result =
                new ArrayList<>();

        for (String recipeName :
                recipes.keySet()) {

            Recipe recipe =
                    recipes.get(recipeName);

            if (recipe.servings <= 0)
                continue;

            double cost =
                    Double.parseDouble(
                            getTotalCost(
                                    recipeName));

            double perServing =
                    cost
                            / recipe.servings;

            if (perServing <= limit)
                result.add(recipeName);
        }

        Collections.sort(result);

        return String.join(
                ", ",
                result);
    }

    // -------- LEVEL 4 STUBS --------

    @Override
    public String createMealPlan(String planName) {
        return "";
    }

    @Override
    public String addRecipeToMealPlan(String planName,
                                      String recipeName,
                                      String servings) {
        return "";
    }

    @Override
    public String getMealPlanShoppingList(String planName) {
        return "";
    }

    @Override
    public String getMealPlanCost(String planName) {
        return "";
    }

    @Override
    public String getMealPlanCalories(String planName) {
        return "";
    }

    @Override
    public String suggestSimilarRecipes(String recipeName,
                                        String n) {
        return "";
    }

    @Override
    public String optimizeMealPlan(String planName,
                                   String maxCalories,
                                   String maxCost) {
        return "";
    }
}
