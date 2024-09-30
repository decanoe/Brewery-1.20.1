package decanoe.brewery.brewing_utils;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.gson.Gson;
import decanoe.brewery.Brewery;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
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

                // TODO @Decanoe json is the config Map, read the values you want in it using .get()

            } catch (Exception e) {
                Brewery.LOGGER.info("Error while loading config");
            }
        }

        for (Entry<Identifier, Resource> entry : manager.findResources("ingredients", id->id.toString().endsWith(".json")).entrySet()) {
            try {
                Brewery.LOGGER.info("Loading ingredient : " + entry.getKey().toString());

                Resource resource = entry.getValue();

                String content = new String(resource.getInputStream().readAllBytes());
                Map<String, ?> json = new Gson().fromJson(content, Map.class);

                if (json == null || !json.containsKey("item") || !json.containsKey("base") || !json.containsKey("effects")) {
                    Brewery.LOGGER.info("Error while loading ingredient : " + entry.getKey().toString());
                    return;
                }

                Item item = Registries.ITEM.get(new Identifier(str(json.get("item"))));
                if (item == null) {
                    Brewery.LOGGER.info("Error while loading ingredient, item doesn't exist : " + json.get("item"));
                    return;
                }

                Potion base = switch (str(json.get("base"))) {
                    case "brewery:awkward" -> Potions.AWKWARD;
                    case "brewery:mundane" -> Potions.MUNDANE;
                    case "brewery:rocky" -> ModPotionUtils.PotionBases.ROCKY_BASE_POTION;
                    case "brewery:stew" -> ModPotionUtils.PotionBases.STEW_BASE_POTION;
                    case "brewery:thick" -> Potions.THICK;
                    default -> null;
                };
                if (base == null) {
                    Brewery.LOGGER.info("Error while loading ingredient, base doesn't exist : " + json.get("base"));
                    return;
                }

                List<IngredientType> effects = parseEffects(json);

                ModPotionUtils.Ingredients.register(item, base, effects);

            } catch (Exception e) {
                Brewery.LOGGER.info("Error while loading ingredient : " + entry.getKey().toString(), e);
            }
        }
    }

    public static String str(Object o) {
        return String.valueOf(o);
    }

    public static List<IngredientType> parseEffects(Map<String, ?> json) {
        return List.of(new IngredientType.IngredientEffect(StatusEffects.BAD_OMEN));
    }
}
