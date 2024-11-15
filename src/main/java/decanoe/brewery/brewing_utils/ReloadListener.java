package decanoe.brewery.brewing_utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import decanoe.brewery.Brewery;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return new Identifier("brewery", "reload_listener");
    }

    @Override
    public void reload(ResourceManager manager) {
        for (Entry<Identifier, Resource> entry : manager.findResources("configs", id ->
                id.toString().equals("brewery:configs/config.json")).entrySet()) {
            try {
                Brewery.LOGGER.info("Loading config");

                Resource resource = entry.getValue();

                String content = new String(resource.getInputStream().readAllBytes());
                Map<String, ?> json = new Gson().fromJson(content, Map.class);

                // =================================== CONFIGS ======================================
                if (json.containsKey("MAX_INGREDIENT_COUNT"))
                    ModPotionUtils.MAX_INGREDIENT_COUNT = toInt(json.get("MAX_INGREDIENT_COUNT"));
                if (json.containsKey("MAX_EFFECT_DURATION"))
                    ModPotionUtils.MAX_INGREDIENT_COUNT = toInt(json.get("MAX_EFFECT_DURATION"));


            } catch (Exception e) {
                Brewery.LOGGER.info("Error while loading config");
            }
        }


        ModPotionUtils.Ingredients.clear();

        for (Entry<Identifier, Resource> entry : manager.findResources("ingredients", id->id.toString().endsWith(".json")).entrySet()) {
            try {
                Brewery.LOGGER.info("Loading ingredient : " + entry.getKey().toString());

                Resource resource = entry.getValue();

                String content = new String(resource.getInputStream().readAllBytes());
                Map<String, ?> json = new Gson().fromJson(content, Map.class);

                if (json == null || !json.containsKey("item") || !json.containsKey("effects")) {
                    Brewery.LOGGER.info("Error while loading ingredient : " + entry.getKey().toString());
                    continue;
                }

                Item item = Registries.ITEM.get(new Identifier(toStr(json.get("item"))));
                if (item == Items.AIR) {
                    Brewery.LOGGER.info("Error while loading ingredient, item doesn't exist : " + json.get("item"));
                    continue;
                }

                Potion base = switch (toStr(json.get("base"))) {
                    case "awkward" -> Potions.AWKWARD;
                    case "mundane" -> Potions.MUNDANE;
                    case "rocky" -> ModPotionUtils.PotionBases.ROCKY_BASE_POTION;
                    case "stew" -> ModPotionUtils.PotionBases.STEW_BASE_POTION;
                    case "thick" -> Potions.THICK;
                    default -> null;
                };

                List<IngredientType> effects = parseEffects(json);

                if (base == null)
                    ModPotionUtils.Ingredients.register(item, effects);
                else
                    ModPotionUtils.Ingredients.register(item, base, effects);

            } catch (Exception e) {
                Brewery.LOGGER.info("Error while loading ingredient : " + entry.getKey().toString(), e);
            }
        }

        ModPotionUtils.Ingredients.printInfos();
    }

    public static String toStr(Object o) {
        return String.valueOf(o);
    }

    public static int toInt(Object o) {
        return Double.valueOf(toStr(o)).intValue();
    }

    public static float toFlt(Object o) {
        return Double.valueOf(toStr(o)).floatValue();
    }

    public static List<IngredientType> parseEffects(Map<String, ?> json) {
        List<Map<String, ?>> list;
        try {
            list = (List<Map<String, ?>>) json.get("effects");
        } catch (Exception e) {
            Brewery.LOGGER.info("Error while parsing effects");
            return List.of();
        }

        List<IngredientType> effects = new ArrayList<>();
        for (Map<String, ?> e : list) {
            IngredientType ingredientType = parseEffect(e);
            if (ingredientType != null) effects.add(ingredientType);
        }

        return effects;
    }

    public static IngredientType parseEffect(Map<String, ?> json) {
        if (!json.containsKey("type")) return null;

        switch (toStr(json.get("type"))) {
            case "effect" -> {
                if (json.containsKey("effect")) {
                    StatusEffect effect = Registries.STATUS_EFFECT.get(new Identifier(toStr(json.get("effect"))));
                    if (effect == null) return null;

                    if (json.containsKey("duration")) {
                        if (json.containsKey("amplifier")) {
                            return new IngredientType.IngredientEffect(effect, toInt(json.get("duration")), toInt(json.get("amplifier")));
                        }
                        return new IngredientType.IngredientEffect(effect, toInt(json.get("duration")));
                    }
                    return new IngredientType.IngredientEffect(effect);
                }
                return null;
            }

            case "color" -> {
                if (json.containsKey("color"))
                    return new IngredientType.IngredientColor(Integer.decode('#' + toStr(json.get("color"))));
                return new IngredientType.IngredientColor();
            }

            case "duration" -> {
                if (json.containsKey("multiplier"))
                    return new IngredientType.IngredientDuration(toFlt(json.get("multiplier")));
                return new IngredientType.IngredientDuration();
            }

            case "duration_infinite" -> {
                return new IngredientType.IngredientInfinite();
            }

            case "amplifier" -> {
                return new IngredientType.IngredientAmplifier();
            }

            case "corruption" -> {
                return new IngredientType.IngredientCorruption();
            }

            case "alteration" -> {
                return new IngredientType.IngredientAlteration();
            }

            case "cure" -> {
                return new IngredientType.IngredientCure();
            }

            case "invert_cure" -> {
                return new IngredientType.IngredientInvertCure();
            }

            case "hide_effect" -> {
                return new IngredientType.IngredientHideEffect();
            }

            case "show_recipe" -> {
                return new IngredientType.IngredientShowRecipe();
            }

            case "enchantment_glint" -> {
                return new IngredientType.IngredientGlint();
            }

            default -> {
                return null;
            }
        }
    }
}
