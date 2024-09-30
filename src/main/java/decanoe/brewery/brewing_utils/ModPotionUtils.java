package decanoe.brewery.brewing_utils;

import java.util.*;
import java.util.Map.Entry;

import decanoe.brewery.Brewery;
import decanoe.brewery.brewing_utils.IngredientType.*;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModPotionUtils {
    public static class PotionBases {
        public static final Potion AWKWARD_BREWED_POTION = Registry.register(Registries.POTION, Brewery.makeID("awkward_brewed_potion"), new Potion());
        public static final Potion ELIXIR_BREWED_POTION = Registry.register(Registries.POTION, Brewery.makeID("thick_brewed_potion"), new Potion());
        public static final Potion MUNDANE_BREWED_POTION = Registry.register(Registries.POTION, Brewery.makeID("mudane_brewed_potion"), new Potion());

        public static final Potion ROCKY_BASE_POTION = Registry.register(Registries.POTION, Brewery.makeID("rocky_potion_base"), new Potion());
        public static final Potion ROCKY_BREWED_POTION = Registry.register(Registries.POTION, Brewery.makeID("rocky_brewed_potion"), new Potion());
        public static final Potion STEW_BASE_POTION = Registry.register(Registries.POTION, Brewery.makeID("stew_potion_base"), new Potion());
        public static final Potion STEW_BREWED_POTION = Registry.register(Registries.POTION, Brewery.makeID("brewed_stew_potion"), new Potion());

        public static final Potion FAILED_POTION = Registry.register(Registries.POTION, Brewery.makeID("failed_potion"), new Potion());

        public static HashMap<Potion, Potion> potion_base_variant = new HashMap<>() {{
            put(Potions.AWKWARD, AWKWARD_BREWED_POTION);
            put(Potions.THICK, ELIXIR_BREWED_POTION);
            put(Potions.MUNDANE, MUNDANE_BREWED_POTION);
            put(ROCKY_BASE_POTION, ROCKY_BREWED_POTION);
            put(STEW_BASE_POTION, STEW_BREWED_POTION);
        }};

        public static Potion getVariant(Potion potion) {
            return potion_base_variant.getOrDefault(potion, potion);
        }
        public static Potion getBase(Potion potion) {
            for (Entry<Potion, Potion> entry : potion_base_variant.entrySet()) {
                if (entry.getValue() == potion) return entry.getKey();
            }
            return potion;
        }

        public static Boolean isModifiedBase(Potion potion) {
            return potion_base_variant.containsValue(potion);
        }

        public static Integer getCustomColor(Potion potion) {
            if (potion == FAILED_POTION)
                return 4269354;
            if (potion == ROCKY_BASE_POTION)
                return 8224125;
            return 0;
        }
        public static Integer getCustomColor(ItemStack potion) {
            return getCustomColor(PotionUtil.getPotion(potion));
        }

        public static void registerPotions() {
            // needed even if empty to register potion
            
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
                content.addAfter(Items.POTION, PotionUtil.setPotion(new ItemStack(Items.POTION), ROCKY_BASE_POTION));
                content.addAfter(Items.POTION, PotionUtil.setPotion(new ItemStack(Items.POTION), STEW_BASE_POTION));
            });
        }

        public static ItemStack turnToBase(ItemStack water_bottle, ItemStack ingredient) {
            if (ingredient.isIn(Ingredients.ROCKY_BASE_INGREDIENT))
                water_bottle = PotionUtil.setPotion(water_bottle, ModPotionUtils.PotionBases.ROCKY_BASE_POTION);
            else if (ingredient.isIn(Ingredients.STEW_BASE_INGREDIENT))
                water_bottle = PotionUtil.setPotion(water_bottle, ModPotionUtils.PotionBases.STEW_BASE_POTION);
            else if (ingredient.isIn(Ingredients.THICK_BASE_INGREDIENT))
                water_bottle = PotionUtil.setPotion(water_bottle, Potions.THICK);
            else if (ingredient.isIn(Ingredients.MUNDANE_BASE_INGREDIENT))
                water_bottle = PotionUtil.setPotion(water_bottle, Potions.MUNDANE);
            else if (ingredient.isIn(Ingredients.AWKWARD_BASE_INGREDIENT))
                water_bottle = PotionUtil.setPotion(water_bottle, Potions.AWKWARD);
            else
                return ModPotionUtils.makeFailed(water_bottle);

            if (!water_bottle.getOrCreateNbt().contains(PotionUtil.CUSTOM_POTION_COLOR_KEY) && ModPotionUtils.PotionBases.getCustomColor(water_bottle) != 0) {
                water_bottle.getOrCreateNbt().putInt(PotionUtil.CUSTOM_POTION_COLOR_KEY, ModPotionUtils.PotionBases.getCustomColor(water_bottle));
            }

            return water_bottle;
        }
    }
    public static final class Ingredients {
        public static final TagKey<Item> ROCKY_BASE_INGREDIENT = TagKey.of(RegistryKeys.ITEM, Identifier.of("brewery", "potion_rocky_base_ingredients"));
        public static final TagKey<Item> STEW_BASE_INGREDIENT = TagKey.of(RegistryKeys.ITEM, Identifier.of("brewery", "potion_stew_base_ingredients"));
        public static final TagKey<Item> MUNDANE_BASE_INGREDIENT = TagKey.of(RegistryKeys.ITEM, Identifier.of("brewery", "potion_mundane_base_ingredients"));
        public static final TagKey<Item> AWKWARD_BASE_INGREDIENT = TagKey.of(RegistryKeys.ITEM, Identifier.of("brewery", "potion_awkward_base_ingredients"));
        public static final TagKey<Item> THICK_BASE_INGREDIENT = TagKey.of(RegistryKeys.ITEM, Identifier.of("brewery", "potion_thick_base_ingredients"));

        private static HashMap<Item, PotionIngredientMap> ingredient_map = new HashMap<>();

        public static void register(Item ingredient, Potion potion, List<IngredientType> effects) {
            if (!ingredient_map.containsKey(ingredient))
                ingredient_map.put(ingredient, new PotionIngredientMap(List.of()));
            
            ingredient_map.get(ingredient).put(potion, new ArrayList<>(effects));
        }

        public static void register(Item ingredient, Potion potion, IngredientType effect) {
            register(ingredient, potion, List.of(effect));
        }

        public static void register(Item ingredient, Potion potion, IngredientType... effects) {
            register(ingredient, potion, List.of(effects));
        }

        public static void register(Item ingredient, List<IngredientType> effects) {
            if (ingredient_map.containsKey(ingredient))
                ingredient_map.get(ingredient).putDefault(new ArrayList<>(effects));
            else
                ingredient_map.put(ingredient, new PotionIngredientMap(new ArrayList<>(effects)));
        }

        public static void register(Item ingredient, IngredientType effect) {
            register(ingredient, List.of(effect));
        }
        public static void register(Item ingredient, IngredientType... effects) {
            register(ingredient, List.of(effects));
        }

        private static void registerDyes() {
            register(Items.WHITE_DYE, new IngredientColor(0xffcfd5d6));
            register(Items.ORANGE_DYE, new IngredientColor(0xffe06101));
            register(Items.MAGENTA_DYE, new IngredientColor(0xffa9309f));
            register(Items.LIGHT_BLUE_DYE, new IngredientColor(0xff2489c7));
            register(Items.YELLOW_DYE, new IngredientColor(0xfff1af15));
            register(Items.LIME_DYE, new IngredientColor(0xff5ea918));
            register(Items.PINK_DYE, new IngredientColor(0xffd5658f));
            register(Items.GRAY_DYE, new IngredientColor(0xff373a3e));
            register(Items.LIGHT_GRAY_DYE, new IngredientColor(0xff7d7d73));
            register(Items.CYAN_DYE, new IngredientColor(0xff157788));
            register(Items.PURPLE_DYE, new IngredientColor(0xff64209c));
            register(Items.BLUE_DYE, new IngredientColor(0xff2d2f8f));
            register(Items.BROWN_DYE, new IngredientColor(0xff603c20));
            register(Items.GREEN_DYE, new IngredientColor(0xff495b24));
            register(Items.RED_DYE, new IngredientColor(0xff8e2121));
            register(Items.BLACK_DYE, new IngredientColor(0xff080a0f));
        }

        private static void registerUniversalModifier() {
            register(Items.REDSTONE, new IngredientDuration(1.25f));
            register(Items.COMPASS, new IngredientDuration(2));
            register(Items.RECOVERY_COMPASS, new IngredientInfinite(), new IngredientGlint());
            register(Items.DRAGON_EGG, new IngredientInfinite());

            register(Items.GLOWSTONE_DUST, new IngredientAmplifier());
            register(Items.CLOCK, new IngredientAmplifier());
            register(Items.NETHER_STAR, new IngredientAmplifier(), new IngredientAmplifier(), new IngredientAmplifier(), new IngredientGlint());

            register(Items.FERMENTED_SPIDER_EYE, new IngredientCorruption());

            register(Items.BONE_MEAL, new IngredientAlteration());
            register(Items.BONE, new IngredientAlteration());

            register(Items.MILK_BUCKET, new IngredientCure());
            
            register(Items.PAPER, new IngredientShowRecipe());
            register(Items.NAME_TAG, new IngredientShowRecipe());
            register(Items.MAP, new IngredientShowRecipe());
            
            register(Items.TINTED_GLASS, new IngredientHideEffect());

            register(Items.EXPERIENCE_BOTTLE, new IngredientGlint());
        }
        private static void registerDefault() {
            //#region FOOD

            // raw meat
            for (Item item : List.of(Items.CHICKEN, Items.BEEF, Items.MUTTON, Items.PORKCHOP, Items.RABBIT, Items.COD, Items.SALMON)) {
                register(item, new IngredientEffect(StatusEffects.NAUSEA, 1));
            }

            // cooked meat
            for (Item item : List.of(Items.COOKED_CHICKEN, Items.COOKED_BEEF, Items.COOKED_MUTTON, Items.COOKED_PORKCHOP, Items.COOKED_RABBIT, Items.COOKED_COD, Items.COOKED_SALMON)) {
                register(item, new IngredientEffect(StatusEffects.SATURATION, 1));
            }

            // fruits
            for (Item item : List.of(Items.APPLE, Items.BEETROOT, Items.CARROT, Items.POTATO, Items.PUMPKIN, Items.CARVED_PUMPKIN, Items.JACK_O_LANTERN, Items.MELON_SLICE, Items.MELON, Items.GLOW_BERRIES, Items.SWEET_BERRIES, Items.DRIED_KELP)) {
                register(item, new IngredientEffect(StatusEffects.SATURATION, 1));
            }

            // cooked natural ingredients
            for (Item item : List.of(Items.BAKED_POTATO, Items.BEETROOT_SOUP, Items.BREAD, Items.COOKIE, Items.MUSHROOM_STEW, Items.PUMPKIN_PIE, Items.RABBIT_STEW)) {
                register(item, new IngredientEffect(StatusEffects.SATURATION, 1));
            }

            // gold food
            register(Items.GOLDEN_CARROT, new IngredientEffect(StatusEffects.SATURATION, 1));
            register(Items.GOLDEN_APPLE, new IngredientEffect(StatusEffects.SATURATION, getDuration(5)), new IngredientEffect(StatusEffects.ABSORPTION, getDuration(10)));
            register(Items.ENCHANTED_GOLDEN_APPLE, new IngredientEffect(StatusEffects.SATURATION, getDuration(30)), new IngredientEffect(StatusEffects.HEALTH_BOOST, getDuration(3600)));
            register(Items.GLISTERING_MELON_SLICE, new IngredientEffect(StatusEffects.SATURATION, 1), new IngredientEffect(StatusEffects.REGENERATION, getDuration(5)));

            // other
            register(Items.CAKE, new IngredientEffect(StatusEffects.SATURATION, getDuration(30)));
            register(Items.POISONOUS_POTATO, new IngredientEffect(StatusEffects.POISON, getDuration(5)));
            register(Items.POPPED_CHORUS_FRUIT, new IngredientEffect(StatusEffects.LEVITATION, getDuration(5)));

            // TODO pk c dans le common et pas stew ca ;-;
            register(Items.HONEYCOMB                , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.INSTANT_HEALTH, 1, 0));
            register(Items.HONEY_BLOCK              , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.INSTANT_HEALTH, 1, 1));
            register(Items.HONEY_BOTTLE             , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.INSTANT_HEALTH, 1, 0));

            // TODO en data jusque là
            // #endregion
            //#region FLOWER
            register(Items.ALLIUM,              new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(4)));
            register(Items.AZURE_BLUET,         new IngredientEffect(StatusEffects.BLINDNESS,       getDuration(8)));
            register(Items.BLUE_ORCHID,         new IngredientEffect(StatusEffects.SATURATION,      getDuration(0.35f)));
            register(Items.DANDELION,           new IngredientEffect(StatusEffects.SATURATION,      getDuration(0.35f)));
            register(Items.CORNFLOWER,          new IngredientEffect(StatusEffects.JUMP_BOOST,      getDuration(6)));
            register(Items.LILY_OF_THE_VALLEY,  new IngredientEffect(StatusEffects.POISON,          getDuration(12)));
            register(Items.OXEYE_DAISY,         new IngredientEffect(StatusEffects.REGENERATION,    getDuration(8)));
            register(Items.POPPY,               new IngredientEffect(StatusEffects.NIGHT_VISION,    getDuration(5)));
            register(Items.TORCHFLOWER,         new IngredientEffect(StatusEffects.NIGHT_VISION,    getDuration(5)));
            register(Items.PINK_TULIP,          new IngredientEffect(StatusEffects.WEAKNESS,        getDuration(9)));
            register(Items.RED_TULIP,           new IngredientEffect(StatusEffects.WEAKNESS,        getDuration(9)));
            register(Items.ORANGE_TULIP,        new IngredientEffect(StatusEffects.WEAKNESS,        getDuration(9)));
            register(Items.WHITE_TULIP,         new IngredientEffect(StatusEffects.WEAKNESS,        getDuration(9)));
            register(Items.WITHER_ROSE,         new IngredientEffect(StatusEffects.WITHER,          getDuration(8)));

            register(Items.LILAC,               new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(8)));
            register(Items.PEONY,               new IngredientEffect(StatusEffects.WEAKNESS,        getDuration(18)));
            register(Items.PITCHER_PLANT,       new IngredientEffect(StatusEffects.HASTE,           getDuration(24)));
            register(Items.ROSE_BUSH,           new IngredientEffect(StatusEffects.POISON,          getDuration(24)));
            register(Items.SUNFLOWER,           new IngredientEffect(StatusEffects.SATURATION,      getDuration(1.5f)));

            register(Items.SPORE_BLOSSOM,       new IngredientEffect(StatusEffects.SLOWNESS,        getDuration(8)));
            register(Items.CHORUS_FLOWER,       new IngredientEffect(StatusEffects.LEVITATION,      getDuration(4)));
            //#endregion
            //#region NATURE
            //#endregion
            //#region MUSHROOM
            for (Item item : List.of(Items.CRIMSON_FUNGUS, Items.CRIMSON_ROOTS, Items.TWISTING_VINES, Items.NETHER_WART, Items.NETHER_WART_BLOCK)) {
                register(item, new IngredientCorruption(), new IngredientDuration(0.75f));
            }
            for (Item item : List.of(Items.WARPED_FUNGUS, Items.WARPED_ROOTS, Items.WEEPING_VINES, Items.WARPED_WART_BLOCK, Items.NETHER_SPROUTS)) {
                register(item, new IngredientAlteration(), new IngredientDuration(0.75f));
            }
            for (Item item : List.of(Items.MUSHROOM_STEM, Items.BROWN_MUSHROOM, Items.BROWN_MUSHROOM_BLOCK, Items.RED_MUSHROOM, Items.RED_MUSHROOM_BLOCK)) {
                register(item, new IngredientInvertCure(), new IngredientDuration(0.75f));
            }
            register(Items.PEARLESCENT_FROGLIGHT    , Potions.AWKWARD, new IngredientEffect(StatusEffects.GLOWING, getDuration(10)));
            register(Items.VERDANT_FROGLIGHT        , Potions.AWKWARD, new IngredientEffect(StatusEffects.GLOWING, getDuration(10)));
            register(Items.OCHRE_FROGLIGHT          , Potions.AWKWARD, new IngredientEffect(StatusEffects.GLOWING, getDuration(10)));
            register(Items.SHROOMLIGHT              , Potions.AWKWARD, new IngredientEffect(StatusEffects.GLOWING, getDuration(10)));
            //#endregion

            //#region ANIMAL
            for (Item item : List.of(Items.WHITE_WOOL, Items.ORANGE_WOOL, Items.MAGENTA_WOOL, Items.LIGHT_BLUE_WOOL, Items.YELLOW_WOOL, Items.LIME_WOOL, Items.PINK_WOOL, Items.GRAY_WOOL, Items.LIGHT_GRAY_WOOL, Items.CYAN_WOOL, Items.PURPLE_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL, Items.GREEN_WOOL, Items.RED_WOOL, Items.BLACK_WOOL)){
                register(item, new IngredientEffect(StatusEffects.SLOWNESS, getDuration(5)));
            }
            register(Items.STRING, new IngredientEffect(StatusEffects.SLOWNESS, getDuration(5)));
            register(Items.COBWEB, new IngredientEffect(StatusEffects.SLOWNESS, getDuration(10)));

            register(Items.AXOLOTL_BUCKET, new IngredientEffect(StatusEffects.CONDUIT_POWER, getDuration(5)));
            register(Items.PUFFERFISH_BUCKET, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(5)));
            register(Items.SALMON_BUCKET, new IngredientEffect(StatusEffects.DOLPHINS_GRACE, getDuration(5)));
            register(Items.COD_BUCKET, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(5)));
            register(Items.TROPICAL_FISH_BUCKET, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(5)));
            register(Items.TADPOLE_BUCKET, new IngredientEffect(StatusEffects.JUMP_BOOST, getDuration(5)));
            
            register(Items.TROPICAL_FISH, new IngredientEffect(StatusEffects.STRENGTH, getDuration(5)));
            register(Items.PUFFERFISH, new IngredientEffect(StatusEffects.POISON, getDuration(5)));
            register(Items.NAUTILUS_SHELL, new IngredientEffect(StatusEffects.CONDUIT_POWER, getDuration(5)));
            register(Items.SCUTE, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(5)));

            register(Items.TURTLE_HELMET, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(120), 2), new IngredientEffect(StatusEffects.SLOWNESS, getDuration(120), 2));
            register(Items.TURTLE_EGG, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(5)), new IngredientEffect(StatusEffects.SLOWNESS, getDuration(5)));
            register(Items.SNIFFER_EGG, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(5)));
            register(Items.EGG, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(5)));
            register(Items.FEATHER, new IngredientEffect(StatusEffects.SLOW_FALLING, getDuration(5)));

            register(Items.INK_SAC, new IngredientEffect(StatusEffects.BLINDNESS, getDuration(5)));
            register(Items.GLOW_INK_SAC, new IngredientEffect(StatusEffects.GLOWING, getDuration(5)));

            register(Items.LEATHER, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(5)));
            register(Items.RABBIT_HIDE, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(5)), new IngredientEffect(StatusEffects.SPEED, getDuration(5)));
            register(Items.RABBIT_FOOT, new IngredientEffect(StatusEffects.JUMP_BOOST, getDuration(5)));
            
            register(Items.SPONGE, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(5)));
            register(Items.WET_SPONGE, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(5)));
            
            register(Items.SPIDER_EYE, new IngredientEffect(StatusEffects.POISON, getDuration(5)));
            register(Items.GOAT_HORN, new IngredientEffect(StatusEffects.INSTANT_DAMAGE, 1));
            //#endregion
            //#region MOB
            register(Items.BLAZE_POWDER, new IngredientEffect(StatusEffects.STRENGTH, getDuration(7.5f)));
            register(Items.BLAZE_ROD, new IngredientEffect(StatusEffects.STRENGTH, getDuration(10)));

            register(Items.CREEPER_HEAD, new IngredientEffect(StatusEffects.INSTANT_DAMAGE, 1), new IngredientEffect(StatusEffects.RESISTANCE, getDuration(30)));
            register(Items.DRAGON_HEAD, new IngredientEffect(StatusEffects.HEALTH_BOOST, getDuration(30), 1), new IngredientEffect(StatusEffects.SLOW_FALLING, getDuration(30)));
            register(Items.PIGLIN_HEAD, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(30)), new IngredientEffect(StatusEffects.STRENGTH, getDuration(30)));
            register(Items.SKELETON_SKULL, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(30)), new IngredientEffect(StatusEffects.SLOWNESS, getDuration(30)));
            register(Items.WITHER_SKELETON_SKULL, new IngredientEffect(StatusEffects.WITHER, getDuration(30)), new IngredientEffect(StatusEffects.STRENGTH, getDuration(30)));
            register(Items.ZOMBIE_HEAD, new IngredientEffect(StatusEffects.HUNGER, getDuration(30)), new IngredientEffect(StatusEffects.BAD_OMEN, getDuration(30)));
            register(Items.PLAYER_HEAD, new IngredientEffect(StatusEffects.HERO_OF_THE_VILLAGE, getDuration(30)), new IngredientEffect(StatusEffects.INSTANT_HEALTH, 1));
            
            register(Items.GHAST_TEAR, new IngredientEffect(StatusEffects.REGENERATION, getDuration(15)));
            register(Items.MAGMA_CREAM, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(10)));
            register(Items.PHANTOM_MEMBRANE, new IngredientEffect(StatusEffects.SLOW_FALLING, getDuration(2)));
            register(Items.ROTTEN_FLESH, new IngredientEffect(StatusEffects.HUNGER, getDuration(10)));

            register(Items.SHULKER_BOX, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(30), 2));
            register(Items.SHULKER_SHELL, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(15), 2));
            
            register(Items.SLIME_BALL, new IngredientEffect(StatusEffects.JUMP_BOOST, getDuration(15)));
            //#endregion

            //#region ROCKS
            //          no effects on potions other than rocky potion
            //#endregion
            //#region ORES
            //          no effects on potions other than rocky potion
            //#endregion

            //#region OTHER
            register(Items.SUGAR, new IngredientEffect(StatusEffects.SPEED, getDuration(5)));

            register(Items.ENDER_PEARL, new IngredientEffect(StatusEffects.INVISIBILITY, getDuration(10)));
            register(Items.ENDER_EYE, new IngredientEffect(StatusEffects.INVISIBILITY, getDuration(10)));
            
            register(Items.HEART_OF_THE_SEA, new IngredientEffect(StatusEffects.CONDUIT_POWER, getDuration(900)));
            register(Items.CONDUIT, new IngredientEffect(StatusEffects.CONDUIT_POWER, getDuration(1800)));
            register(Items.TOTEM_OF_UNDYING,
                new IngredientEffect(StatusEffects.ABSORPTION, getDuration(5), 1),
                new IngredientEffect(StatusEffects.REGENERATION, getDuration(45), 1),
                new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(40)));
            register(Items.BEACON,
                new IngredientEffect(StatusEffects.SPEED, getDuration(30)),
                new IngredientEffect(StatusEffects.RESISTANCE, getDuration(30)),
                new IngredientEffect(StatusEffects.STRENGTH, getDuration(30)),
                new IngredientEffect(StatusEffects.JUMP_BOOST, getDuration(30)),
                new IngredientEffect(StatusEffects.HASTE, getDuration(30)),
                new IngredientEffect(StatusEffects.REGENERATION, getDuration(30)));
            //#endregion
        }
        private static void registerStewIngredients() {
            //#region FOOD

            // raw meat
            for (Item item : List.of(Items.CHICKEN, Items.BEEF, Items.MUTTON, Items.PORKCHOP, Items.RABBIT, Items.COD, Items.SALMON)) {
                register(item                       , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.SATURATION, getDuration(60)));
            }
            register(Items.CHICKEN                  , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.POISON, getDuration(5)));
            register(Items.COD                      , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(60)));
            register(Items.SALMON                   , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(60)));

            // cooked meat
            for (Item item : List.of(Items.COOKED_CHICKEN, Items.COOKED_BEEF, Items.COOKED_MUTTON, Items.COOKED_PORKCHOP, Items.COOKED_RABBIT, Items.COOKED_COD, Items.COOKED_SALMON)) {
                register(item                       , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.SATURATION, getDuration(120)));
            }

            // fruits
            for (Item item : List.of(Items.APPLE, Items.BEETROOT, Items.CARROT, Items.POTATO, Items.PUMPKIN, Items.CARVED_PUMPKIN, Items.JACK_O_LANTERN, Items.MELON_SLICE, Items.MELON, Items.GLOW_BERRIES, Items.SWEET_BERRIES, Items.DRIED_KELP)) {
                register(item                       , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.SATURATION, getDuration(60)));
            }
            register(Items.GLOW_BERRIES             , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.GLOWING, getDuration(60)));
            register(Items.JACK_O_LANTERN           , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.GLOWING, getDuration(60)));

            // cooked natural ingredients
            for (Item item : List.of(Items.BAKED_POTATO, Items.BEETROOT_SOUP, Items.BREAD, Items.COOKIE, Items.MUSHROOM_STEW, Items.PUMPKIN_PIE, Items.RABBIT_STEW)) {
                register(item                       , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.SATURATION, getDuration(60), 1));
            }
            register(Items.RABBIT_STEW              , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.JUMP_BOOST, getDuration(60)));

            // gold food
            register(Items.GOLDEN_CARROT            , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.SATURATION, getDuration(120), 2), new IngredientEffect(StatusEffects.NIGHT_VISION, getDuration(120)));
            register(Items.GOLDEN_APPLE             , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.SATURATION, getDuration(60), 1), new IngredientEffect(StatusEffects.ABSORPTION, getDuration(60), 1));
            register(Items.ENCHANTED_GOLDEN_APPLE   , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.SATURATION, getDuration(180), 2), new IngredientEffect(StatusEffects.HEALTH_BOOST, getDuration(3600), 1));
            register(Items.GLISTERING_MELON_SLICE   , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.SATURATION, getDuration(30)), new IngredientEffect(StatusEffects.REGENERATION, getDuration(60)));

            // other
            register(Items.CAKE                     , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.SATURATION, getDuration(360), 1));
            register(Items.POISONOUS_POTATO         , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.POISON, getDuration(20), 1));
            register(Items.POPPED_CHORUS_FRUIT      , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.LEVITATION, getDuration(3), 5));
            
            register(Items.HONEYCOMB                , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.INSTANT_HEALTH, 1, 1));
            register(Items.HONEY_BLOCK              , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.INSTANT_HEALTH, 1, 2));
            register(Items.HONEY_BOTTLE             , PotionBases.STEW_BASE_POTION, new IngredientEffect(StatusEffects.INSTANT_HEALTH, 1, 1));

            //#endregion
            //#region MUSHROOM
            for (Item item : List.of(Items.MUSHROOM_STEM, Items.BROWN_MUSHROOM, Items.BROWN_MUSHROOM_BLOCK, Items.RED_MUSHROOM, Items.RED_MUSHROOM_BLOCK)) {
                register(item, new IngredientDuration(2));
            }
            //#endregion
            //#region ANIMAL
            register(Items.EGG, PotionBases.STEW_BASE_POTION, new IngredientAmplifier());
            //#endregion
        }
        private static void registerRockyIngredients() {
            //#region ROCKS
            // cobble like blocs
            for (Item item : List.of(Items.COBBLESTONE, Items.COBBLED_DEEPSLATE)) {
                register(item, PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.MINING_FATIGUE, getDuration(120)));
            }
            // stone like blocs
            for (Item item : List.of(Items.STONE, Items.DEEPSLATE, Items.TUFF, Items.ANDESITE, Items.DIORITE, Items.GRANITE, Items.CALCITE, Items.DRIPSTONE_BLOCK)) {
                register(item, PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.SLOWNESS, getDuration(30)));
            }
            // mossy blocs
            for (Item item : List.of(Items.MOSS_BLOCK, Items.MOSS_CARPET, Items.MOSSY_COBBLESTONE)) {
                register(item, PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.POISON, getDuration(30)));
            }

            register(Items.BLACKSTONE       , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(120)));
            register(Items.BASALT           , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(120)));
            register(Items.NETHERRACK       , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(120)));
            register(Items.MAGMA_BLOCK      , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(120)));
            
            register(Items.SOUL_SAND        , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WITHER, getDuration(30)));
            register(Items.SOUL_SOIL        , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WITHER, getDuration(30)));
            
            register(Items.GLOWSTONE        , PotionBases.ROCKY_BASE_POTION, new IngredientAmplifier(), new IngredientEffect(StatusEffects.GLOWING, getDuration(120)));
            register(Items.END_STONE        , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.LEVITATION, getDuration(30)));
            
            register(Items.SAND             , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.HUNGER, getDuration(30)));
            register(Items.SANDSTONE        , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.HUNGER, getDuration(30)));
            register(Items.RED_SAND         , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.INSTANT_HEALTH));
            register(Items.RED_SANDSTONE    , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.INSTANT_HEALTH));

            register(Items.DIRT             , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.NAUSEA, getDuration(10)));
            register(Items.COARSE_DIRT      , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.NAUSEA, getDuration(20)));
            register(Items.ROOTED_DIRT      , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.NAUSEA, getDuration(20)));
            
            register(Items.CLAY_BALL        , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.BLINDNESS, getDuration(10)));
            register(Items.CLAY             , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.BLINDNESS, getDuration(10)));

            register(Items.MUD              , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(10)));
            register(Items.PACKED_MUD       , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(10)));
            
            register(Items.FLINT            , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.STRENGTH, getDuration(30)));
            register(Items.POINTED_DRIPSTONE, PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.STRENGTH, getDuration(30)));
            
            register(Items.ICE              , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(120)));
            register(Items.PACKED_ICE       , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(240)));
            register(Items.BLUE_ICE         , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(240)), new IngredientEffect(StatusEffects.DOLPHINS_GRACE, getDuration(240)));
            
            register(Items.OBSIDIAN         , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(240), 2));
            register(Items.OBSIDIAN         , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(240), 2), new IngredientEffect(StatusEffects.INVISIBILITY, getDuration(240)));
            
            register(Items.TERRACOTTA       , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)));
            register(Items.WHITE_TERRACOTTA        , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xffcfd5d6));
            register(Items.ORANGE_TERRACOTTA       , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xffe06101));
            register(Items.MAGENTA_TERRACOTTA      , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xffa9309f));
            register(Items.LIGHT_BLUE_TERRACOTTA   , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xff2489c7));
            register(Items.YELLOW_TERRACOTTA       , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xfff1af15));
            register(Items.LIME_TERRACOTTA         , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xff5ea918));
            register(Items.PINK_TERRACOTTA         , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xffd5658f));
            register(Items.GRAY_TERRACOTTA         , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xff373a3e));
            register(Items.LIGHT_GRAY_TERRACOTTA   , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xff7d7d73));
            register(Items.CYAN_TERRACOTTA         , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xff157788));
            register(Items.PURPLE_TERRACOTTA       , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xff64209c));
            register(Items.BLUE_TERRACOTTA         , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xff2d2f8f));
            register(Items.BROWN_TERRACOTTA        , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xff603c20));
            register(Items.GREEN_TERRACOTTA        , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xff495b24));
            register(Items.RED_TERRACOTTA          , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xff8e2121));
            register(Items.BLACK_TERRACOTTA        , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)), new IngredientColor(0xff080a0f));
            
            register(Items.SCULK            , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.DARKNESS, getDuration(60)));
            register(Items.SCULK_VEIN       , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.DARKNESS, getDuration(45)));

            
            //#endregion

            //#region ORES
            register(Items.COAL                 , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(45)));
            register(Items.COAL_ORE             , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(30)));
            register(Items.DEEPSLATE_COAL_ORE   , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(30)));

            register(Items.ANCIENT_DEBRIS       , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(180)), new IngredientEffect(StatusEffects.RESISTANCE, getDuration(180), 1));
            register(Items.NETHERITE_SCRAP      , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(240)), new IngredientEffect(StatusEffects.RESISTANCE, getDuration(240), 1));
            register(Items.NETHERITE_INGOT      , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(360)), new IngredientEffect(StatusEffects.RESISTANCE, getDuration(360), 2));
            register(Items.NETHERITE_BLOCK      , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, StatusEffectInstance.INFINITE), new IngredientEffect(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 2));

            register(Items.COPPER_INGOT         , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.HASTE, getDuration(180)));
            register(Items.RAW_COPPER           , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.HASTE, getDuration(120)));
            register(Items.COPPER_ORE           , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.HASTE, getDuration(120)));
            register(Items.DEEPSLATE_COPPER_ORE , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.HASTE, getDuration(120)));

            register(Items.GOLD_BLOCK           , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.ABSORPTION, getDuration(360)));
            register(Items.GOLD_INGOT           , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.ABSORPTION, getDuration(180)));
            register(Items.RAW_GOLD_BLOCK       , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.ABSORPTION, getDuration(300)));
            register(Items.RAW_GOLD             , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.ABSORPTION, getDuration(120)));
            register(Items.GOLD_ORE             , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.ABSORPTION, getDuration(120)));
            register(Items.DEEPSLATE_GOLD_ORE   , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.ABSORPTION, getDuration(120)));
            register(Items.GOLD_NUGGET          , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.ABSORPTION, getDuration(30)));
            register(Items.NETHER_GOLD_ORE      , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.ABSORPTION, getDuration(60)));

            register(Items.IRON_BLOCK           , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.STRENGTH, getDuration(360)));
            register(Items.IRON_INGOT           , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.STRENGTH, getDuration(180)));
            register(Items.RAW_IRON_BLOCK       , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.STRENGTH, getDuration(300)));
            register(Items.RAW_IRON             , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.STRENGTH, getDuration(120)));
            register(Items.IRON_ORE             , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.STRENGTH, getDuration(120)));
            register(Items.DEEPSLATE_IRON_ORE   , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.STRENGTH, getDuration(120)));
            register(Items.IRON_NUGGET          , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.STRENGTH, getDuration(30)));

            register(Items.EMERALD              , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.HERO_OF_THE_VILLAGE, getDuration(30)));
            register(Items.EMERALD_BLOCK        , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.HERO_OF_THE_VILLAGE, getDuration(60)));
            register(Items.EMERALD_ORE          , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.HERO_OF_THE_VILLAGE, getDuration(30)));
            register(Items.DEEPSLATE_EMERALD_ORE, PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.HERO_OF_THE_VILLAGE, getDuration(120)));

            register(Items.LAPIS_LAZULI         , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.LUCK, getDuration(120)));
            register(Items.LAPIS_BLOCK          , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.LUCK, getDuration(120), 1));
            register(Items.LAPIS_ORE            , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.LUCK, getDuration(90)));
            register(Items.DEEPSLATE_LAPIS_ORE  , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.LUCK, getDuration(90)));

            register(Items.DIAMOND              , PotionBases.ROCKY_BASE_POTION, new IngredientAmplifier());
            register(Items.DIAMOND_ORE          , PotionBases.ROCKY_BASE_POTION, new IngredientAmplifier());
            register(Items.DEEPSLATE_DIAMOND_ORE, PotionBases.ROCKY_BASE_POTION, new IngredientAmplifier());
            register(Items.DIAMOND_BLOCK        , PotionBases.ROCKY_BASE_POTION, new IngredientAmplifier(), new IngredientAmplifier(), new IngredientAmplifier());

            register(Items.QUARTZ               , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.SPEED, getDuration(180)));
            register(Items.NETHER_QUARTZ_ORE    , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.SPEED, getDuration(120)));

            register(Items.PRISMARINE_SHARD     , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(420)));
            register(Items.PRISMARINE_CRYSTALS  , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.DOLPHINS_GRACE, getDuration(420)));

            register(Items.AMETHYST_SHARD       , PotionBases.ROCKY_BASE_POTION, new IngredientAlteration());
            register(Items.AMETHYST_BLOCK       , PotionBases.ROCKY_BASE_POTION, new IngredientAlteration());

            register(Items.ECHO_SHARD           , PotionBases.ROCKY_BASE_POTION,
                new IngredientEffect(StatusEffects.DARKNESS, StatusEffectInstance.INFINITE),
                new IngredientEffect(StatusEffects.SLOWNESS, StatusEffectInstance.INFINITE, 3),
                new IngredientEffect(StatusEffects.STRENGTH, StatusEffectInstance.INFINITE, 3),
                new IngredientEffect(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 3),
                new IngredientGlint());
            
            register(Items.REDSTONE             , PotionBases.ROCKY_BASE_POTION, new IngredientDuration(2));
            register(Items.REDSTONE_ORE         , PotionBases.ROCKY_BASE_POTION, new IngredientDuration(2));
            register(Items.DEEPSLATE_REDSTONE_ORE,PotionBases.ROCKY_BASE_POTION, new IngredientDuration(2));
            //#endregion
        
            register(Items.TNT          , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.INSTANT_DAMAGE, 1, 3));
            register(Items.END_CRYSTAL  , PotionBases.ROCKY_BASE_POTION, new IngredientEffect(StatusEffects.NIGHT_VISION, 240));
        }
        private static void registerElixirIngredients() {
            //#region FLOWER
            register(Items.ALLIUM,              new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(30)));
            register(Items.AZURE_BLUET,         new IngredientEffect(StatusEffects.BLINDNESS,       getDuration(20)));
            register(Items.BLUE_ORCHID,         new IngredientEffect(StatusEffects.SATURATION,      getDuration(0.35f), 1));
            register(Items.DANDELION,           new IngredientEffect(StatusEffects.SATURATION,      getDuration(0.35f), 1));
            register(Items.CORNFLOWER,          new IngredientEffect(StatusEffects.JUMP_BOOST,      getDuration(30), 1));
            register(Items.LILY_OF_THE_VALLEY,  new IngredientEffect(StatusEffects.POISON,          getDuration(30), 1));
            register(Items.OXEYE_DAISY,         new IngredientEffect(StatusEffects.REGENERATION,    getDuration(20), 1));
            register(Items.POPPY,               new IngredientEffect(StatusEffects.NIGHT_VISION,    getDuration(30)));
            register(Items.TORCHFLOWER,         new IngredientEffect(StatusEffects.NIGHT_VISION,    getDuration(30)));
            register(Items.PINK_TULIP,          new IngredientEffect(StatusEffects.WEAKNESS,        getDuration(20), 1));
            register(Items.RED_TULIP,           new IngredientEffect(StatusEffects.WEAKNESS,        getDuration(20), 1));
            register(Items.ORANGE_TULIP,        new IngredientEffect(StatusEffects.WEAKNESS,        getDuration(20), 1));
            register(Items.WHITE_TULIP,         new IngredientEffect(StatusEffects.WEAKNESS,        getDuration(20), 1));
            register(Items.WITHER_ROSE,         new IngredientEffect(StatusEffects.WITHER,          getDuration(20), 1));

            register(Items.LILAC,               new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(45)));
            register(Items.PEONY,               new IngredientEffect(StatusEffects.WEAKNESS,        getDuration(40), 1));
            register(Items.PITCHER_PLANT,       new IngredientEffect(StatusEffects.HASTE,           getDuration(120), 1));
            register(Items.ROSE_BUSH,           new IngredientEffect(StatusEffects.POISON,          getDuration(35), 1));
            register(Items.SUNFLOWER,           new IngredientEffect(StatusEffects.SATURATION,      getDuration(1.5f), 1));

            register(Items.SPORE_BLOSSOM,       new IngredientEffect(StatusEffects.SLOWNESS,        getDuration(20), 1));
            register(Items.CHORUS_FLOWER,       new IngredientEffect(StatusEffects.LEVITATION,      getDuration(10), 2));
            //#endregion
            
            //#region NATURE
            //#region tree
            // logs
            for (Item item : List.of(Items.OAK_LOG, Items.BIRCH_LOG, Items.SPRUCE_LOG, Items.DARK_OAK_LOG, Items.ACACIA_LOG, Items.JUNGLE_LOG, Items.CHERRY_LOG, Items.MANGROVE_LOG, Items.OAK_WOOD, Items.BIRCH_WOOD, Items.SPRUCE_WOOD, Items.DARK_OAK_WOOD, Items.ACACIA_WOOD, Items.JUNGLE_WOOD, Items.CHERRY_WOOD, Items.MANGROVE_WOOD)) {
                register(item, Potions.THICK, new IngredientAlteration());
            }
            // stripped logs
            for (Item item : List.of(Items.STRIPPED_OAK_LOG, Items.STRIPPED_BIRCH_LOG, Items.STRIPPED_SPRUCE_LOG, Items.STRIPPED_DARK_OAK_LOG, Items.STRIPPED_ACACIA_LOG, Items.STRIPPED_JUNGLE_LOG, Items.STRIPPED_CHERRY_LOG, Items.STRIPPED_MANGROVE_LOG, Items.STRIPPED_OAK_WOOD, Items.STRIPPED_BIRCH_WOOD, Items.STRIPPED_SPRUCE_WOOD, Items.STRIPPED_DARK_OAK_WOOD, Items.STRIPPED_ACACIA_WOOD, Items.STRIPPED_JUNGLE_WOOD, Items.STRIPPED_CHERRY_WOOD, Items.STRIPPED_MANGROVE_WOOD)) {
                register(item, Potions.THICK, new IngredientCorruption());
            }
            // saplings
            for (Item item : List.of(Items.OAK_SAPLING, Items.BIRCH_SAPLING, Items.SPRUCE_SAPLING, Items.DARK_OAK_SAPLING, Items.ACACIA_SAPLING, Items.JUNGLE_SAPLING, Items.CHERRY_SAPLING, Items.MANGROVE_PROPAGULE)) {
                register(item, Potions.THICK, new IngredientEffect(StatusEffects.SATURATION, 1));
            }
            // leaves
            for (Item item : List.of(Items.OAK_LEAVES, Items.BIRCH_LEAVES, Items.SPRUCE_LEAVES, Items.DARK_OAK_LEAVES, Items.ACACIA_LEAVES, Items.JUNGLE_LEAVES, Items.CHERRY_LEAVES, Items.MANGROVE_LEAVES, Items.AZALEA_LEAVES, Items.FLOWERING_AZALEA_LEAVES)) {
                register(item, Potions.THICK, new IngredientEffect(StatusEffects.HASTE, getDuration(120)));
            }
            //#endregion

            // seeds
            register(Items.WHEAT_SEEDS      , Potions.THICK, new IngredientEffect(StatusEffects.HUNGER, getDuration(20)));
            register(Items.BEETROOT_SEEDS   , Potions.THICK, new IngredientEffect(StatusEffects.INSTANT_HEALTH, getDuration(1)));
            register(Items.PUMPKIN_SEEDS    , Potions.THICK, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(60)));
            register(Items.MELON_SEEDS      , Potions.THICK, new IngredientEffect(StatusEffects.REGENERATION, getDuration(30)));
            register(Items.PITCHER_POD      , Potions.THICK, new IngredientEffect(StatusEffects.HEALTH_BOOST, getDuration(30)));
            register(Items.TORCHFLOWER_SEEDS, Potions.THICK, new IngredientEffect(StatusEffects.STRENGTH, getDuration(60), 1));

            // coral
            //#region coral
            register(Items.TUBE_CORAL       , Potions.THICK, new IngredientEffect(StatusEffects.SPEED, getDuration(120)));
            register(Items.TUBE_CORAL_FAN   , Potions.THICK, new IngredientEffect(StatusEffects.SPEED, getDuration(120)));
            register(Items.TUBE_CORAL_BLOCK , Potions.THICK, new IngredientEffect(StatusEffects.SPEED, getDuration(240)));
            register(Items.DEAD_TUBE_CORAL       , Potions.THICK, new IngredientEffect(StatusEffects.SLOWNESS, getDuration(60)));
            register(Items.DEAD_TUBE_CORAL_FAN   , Potions.THICK, new IngredientEffect(StatusEffects.SLOWNESS, getDuration(60)));
            register(Items.DEAD_TUBE_CORAL_BLOCK , Potions.THICK, new IngredientEffect(StatusEffects.SLOWNESS, getDuration(120)));
            
            register(Items.BRAIN_CORAL       , Potions.THICK, new IngredientEffect(StatusEffects.HEALTH_BOOST, getDuration(120)));
            register(Items.BRAIN_CORAL_FAN   , Potions.THICK, new IngredientEffect(StatusEffects.HEALTH_BOOST, getDuration(120)));
            register(Items.BRAIN_CORAL_BLOCK , Potions.THICK, new IngredientEffect(StatusEffects.HEALTH_BOOST, getDuration(240)));
            register(Items.DEAD_BRAIN_CORAL       , Potions.THICK, new IngredientEffect(StatusEffects.WITHER, getDuration(60)));
            register(Items.DEAD_BRAIN_CORAL_FAN   , Potions.THICK, new IngredientEffect(StatusEffects.WITHER, getDuration(60)));
            register(Items.DEAD_BRAIN_CORAL_BLOCK , Potions.THICK, new IngredientEffect(StatusEffects.WITHER, getDuration(120)));
            
            register(Items.BUBBLE_CORAL       , Potions.THICK, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(120)));
            register(Items.BUBBLE_CORAL_FAN   , Potions.THICK, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(120)));
            register(Items.BUBBLE_CORAL_BLOCK , Potions.THICK, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(240)));
            register(Items.DEAD_BUBBLE_CORAL       , Potions.THICK, new IngredientEffect(StatusEffects.MINING_FATIGUE, getDuration(60)));
            register(Items.DEAD_BUBBLE_CORAL_FAN   , Potions.THICK, new IngredientEffect(StatusEffects.MINING_FATIGUE, getDuration(60)));
            register(Items.DEAD_BUBBLE_CORAL_BLOCK , Potions.THICK, new IngredientEffect(StatusEffects.MINING_FATIGUE, getDuration(120)));
            
            register(Items.FIRE_CORAL       , Potions.THICK, new IngredientEffect(StatusEffects.STRENGTH, getDuration(120)));
            register(Items.FIRE_CORAL_FAN   , Potions.THICK, new IngredientEffect(StatusEffects.STRENGTH, getDuration(120)));
            register(Items.FIRE_CORAL_BLOCK , Potions.THICK, new IngredientEffect(StatusEffects.STRENGTH, getDuration(240)));
            register(Items.DEAD_FIRE_CORAL       , Potions.THICK, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)));
            register(Items.DEAD_FIRE_CORAL_FAN   , Potions.THICK, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(60)));
            register(Items.DEAD_FIRE_CORAL_BLOCK , Potions.THICK, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(120)));
            
            register(Items.HORN_CORAL       , Potions.THICK, new IngredientEffect(StatusEffects.LUCK, getDuration(120)));
            register(Items.HORN_CORAL_FAN   , Potions.THICK, new IngredientEffect(StatusEffects.LUCK, getDuration(120)));
            register(Items.HORN_CORAL_BLOCK , Potions.THICK, new IngredientEffect(StatusEffects.LUCK, getDuration(240)));
            register(Items.DEAD_HORN_CORAL       , Potions.THICK, new IngredientEffect(StatusEffects.UNLUCK, getDuration(60)));
            register(Items.DEAD_HORN_CORAL_FAN   , Potions.THICK, new IngredientEffect(StatusEffects.UNLUCK, getDuration(60)));
            register(Items.DEAD_HORN_CORAL_BLOCK , Potions.THICK, new IngredientEffect(StatusEffects.UNLUCK, getDuration(120)));
            //#endregion
            register(Items.SEAGRASS     , Potions.THICK, new IngredientEffect(StatusEffects.DOLPHINS_GRACE, getDuration(120)));
            register(Items.SEA_PICKLE   , Potions.THICK, new IngredientEffect(StatusEffects.CONDUIT_POWER, getDuration(5)));
            register(Items.KELP         , Potions.THICK, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(120)));
            
            register(Items.GRASS        , Potions.THICK, new IngredientEffect(StatusEffects.NIGHT_VISION, getDuration(20)));
            register(Items.TALL_GRASS   , Potions.THICK, new IngredientEffect(StatusEffects.NIGHT_VISION, getDuration(30)));
            register(Items.FERN         , Potions.THICK, new IngredientEffect(StatusEffects.NIGHT_VISION, getDuration(20)));
            register(Items.LARGE_FERN   , Potions.THICK, new IngredientEffect(StatusEffects.NIGHT_VISION, getDuration(30)));
            register(Items.DEAD_BUSH    , Potions.THICK, new IngredientEffect(StatusEffects.BLINDNESS, getDuration(20)));

            register(Items.HANGING_ROOTS        , Potions.THICK, new IngredientAmplifier());
            register(Items.ROOTED_DIRT          , Potions.THICK, new IngredientAmplifier());
            register(Items.MANGROVE_ROOTS       , Potions.THICK, new IngredientAmplifier());
            register(Items.MUDDY_MANGROVE_ROOTS , Potions.THICK, new IngredientAmplifier());

            register(Items.AZALEA           , Potions.THICK, new IngredientEffect(StatusEffects.ABSORPTION, getDuration(60), 2));
            register(Items.FLOWERING_AZALEA , Potions.THICK, new IngredientEffect(StatusEffects.ABSORPTION, getDuration(60), 2));
            
            register(Items.GLOW_BERRIES     , Potions.THICK, new IngredientEffect(StatusEffects.GLOWING, getDuration(60)));
            register(Items.GLOW_LICHEN      , Potions.THICK, new IngredientEffect(StatusEffects.GLOWING, getDuration(60)));
            
            register(Items.SMALL_DRIPLEAF   , Potions.THICK, new IngredientEffect(StatusEffects.SLOW_FALLING, getDuration(60)));
            register(Items.BIG_DRIPLEAF     , Potions.THICK, new IngredientEffect(StatusEffects.SLOW_FALLING, getDuration(120)));
            register(Items.LILY_PAD         , Potions.THICK, new IngredientEffect(StatusEffects.SLOW_FALLING, getDuration(80)));
            
            register(Items.WHEAT            , Potions.THICK, new IngredientEffect(StatusEffects.SATURATION, getDuration(60)));
            register(Items.HAY_BLOCK        , Potions.THICK, new IngredientEffect(StatusEffects.SATURATION, getDuration(120)));
            
            register(Items.MOSS_BLOCK       , Potions.THICK, new IngredientEffect(StatusEffects.POISON, getDuration(20)));
            register(Items.MOSS_CARPET      , Potions.THICK, new IngredientEffect(StatusEffects.POISON, getDuration(60)));
            
            register(Items.HONEYCOMB        , Potions.THICK, new IngredientAmplifier());
            register(Items.HONEY_BLOCK      , Potions.THICK, new IngredientAmplifier());
            register(Items.HONEY_BOTTLE     , Potions.THICK, new IngredientAmplifier());

            // other
            register(Items.PITCHER_PLANT    , Potions.THICK, new IngredientEffect(StatusEffects.HEALTH_BOOST, getDuration(30), 2));
            register(Items.VINE             , Potions.THICK, new IngredientEffect(StatusEffects.JUMP_BOOST, getDuration(20), 2));
            register(Items.SUGAR_CANE       , Potions.THICK, new IngredientEffect(StatusEffects.SPEED, getDuration(20), 3));
            register(Items.COCOA_BEANS      , Potions.THICK, new IngredientEffect(StatusEffects.NAUSEA, getDuration(10), 3));
            register(Items.CHARCOAL         , Potions.THICK, new IngredientEffect(StatusEffects.WITHER, getDuration(30)));
            register(Items.CACTUS           , Potions.THICK, new IngredientEffect(StatusEffects.INSTANT_DAMAGE, 1, 0));
            register(Items.BAMBOO           , Potions.THICK, new IngredientEffect(StatusEffects.SATURATION, 1));
            register(Items.CHORUS_FRUIT     , Potions.THICK, new IngredientEffect(StatusEffects.NIGHT_VISION, getDuration(120)));
            //#endregion
        
            //#region MUSHROOM
            for (Item item : List.of(Items.CRIMSON_FUNGUS, Items.CRIMSON_ROOTS, Items.TWISTING_VINES, Items.NETHER_WART, Items.NETHER_WART_BLOCK)) {
                register(item, new IngredientCorruption());
            }
            for (Item item : List.of(Items.WARPED_FUNGUS, Items.WARPED_ROOTS, Items.WEEPING_VINES, Items.WARPED_WART_BLOCK, Items.NETHER_SPROUTS)) {
                register(item, new IngredientAlteration());
            }
            for (Item item : List.of(Items.MUSHROOM_STEM, Items.BROWN_MUSHROOM, Items.BROWN_MUSHROOM_BLOCK, Items.RED_MUSHROOM, Items.RED_MUSHROOM_BLOCK)) {
                register(item, new IngredientInvertCure());
            }
            //#endregion
        }
        private static void registerAwkwardIngredients() {
            //#region MUSHROOM
            register(Items.CRIMSON_ROOTS            , Potions.AWKWARD, new IngredientEffect(StatusEffects.REGENERATION, getDuration(30)));
            register(Items.WEEPING_VINES            , Potions.AWKWARD, new IngredientEffect(StatusEffects.INSTANT_DAMAGE, 1));
            register(Items.CRIMSON_FUNGUS           , Potions.AWKWARD, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(30)));

            register(Items.NETHER_SPROUTS           , Potions.AWKWARD, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(15)));
            register(Items.WARPED_ROOTS             , Potions.AWKWARD, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(30)));
            register(Items.TWISTING_VINES           , Potions.AWKWARD, new IngredientEffect(StatusEffects.LEVITATION, getDuration(5)));
            register(Items.WARPED_FUNGUS            , Potions.AWKWARD, new IngredientEffect(StatusEffects.INVISIBILITY, getDuration(30)));
            
            register(Items.NETHER_WART              , Potions.AWKWARD, new IngredientEffect(StatusEffects.HEALTH_BOOST, getDuration(30)));
            register(Items.NETHER_WART_BLOCK        , Potions.AWKWARD, new IngredientEffect(StatusEffects.HEALTH_BOOST, getDuration(20), 1));
            register(Items.WARPED_WART_BLOCK        , Potions.AWKWARD, new IngredientEffect(StatusEffects.POISON, getDuration(20), 1));
            
            register(Items.BROWN_MUSHROOM           , Potions.AWKWARD, new IngredientEffect(StatusEffects.HUNGER, getDuration(20)));
            register(Items.BROWN_MUSHROOM_BLOCK     , Potions.AWKWARD, new IngredientEffect(StatusEffects.HUNGER, getDuration(15), 1));
            register(Items.RED_MUSHROOM             , Potions.AWKWARD, new IngredientEffect(StatusEffects.INSTANT_HEALTH, 1));
            register(Items.RED_MUSHROOM_BLOCK       , Potions.AWKWARD, new IngredientEffect(StatusEffects.INSTANT_HEALTH, 1, 1));
            
            register(Items.MUSHROOM_STEM            , Potions.AWKWARD, new IngredientDuration(2));
            register(Items.MUSHROOM_STEW            , Potions.AWKWARD, new IngredientAmplifier());

            register(Items.MYCELIUM                 , Potions.AWKWARD, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(30)));
            register(Items.PODZOL                   , Potions.AWKWARD, new IngredientEffect(StatusEffects.SLOWNESS, getDuration(30)));
            
            register(Items.PEARLESCENT_FROGLIGHT    , Potions.AWKWARD, new IngredientEffect(StatusEffects.LUCK, getDuration(60)));
            register(Items.VERDANT_FROGLIGHT        , Potions.AWKWARD, new IngredientEffect(StatusEffects.HASTE, getDuration(360), 1));
            register(Items.OCHRE_FROGLIGHT          , Potions.AWKWARD, new IngredientEffect(StatusEffects.ABSORPTION, getDuration(1800), 2));
            
            register(Items.SHROOMLIGHT              , Potions.AWKWARD, new IngredientEffect(StatusEffects.GLOWING, getDuration(120)));
            
            register(Items.SCULK                    , Potions.AWKWARD, new IngredientEffect(StatusEffects.DARKNESS, getDuration(60)));
            register(Items.SCULK_VEIN               , Potions.AWKWARD, new IngredientEffect(StatusEffects.DARKNESS, getDuration(45)));
            //#endregion
            //#region MOB
            register(Items.BLAZE_ROD, new IngredientEffect(StatusEffects.STRENGTH, 3600, 1));
            
            register(Items.ROTTEN_FLESH, new IngredientEffect(StatusEffects.HUNGER, getDuration(120)));

            register(Items.SHULKER_BOX, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(180), 2));
            register(Items.SHULKER_SHELL, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(120), 2));
            
            register(Items.SLIME_BALL, new IngredientEffect(StatusEffects.JUMP_BOOST, getDuration(360)));
            //#endregion

            //#region VANILLA
            register(Items.GOLDEN_CARROT, Potions.AWKWARD, new IngredientEffect(StatusEffects.NIGHT_VISION, 3600));
            register(Items.MAGMA_CREAM, Potions.AWKWARD, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, 3600));
            register(Items.RABBIT_FOOT, Potions.AWKWARD, new IngredientEffect(StatusEffects.JUMP_BOOST, 3600));
            register(Items.TURTLE_HELMET, Potions.AWKWARD, new IngredientEffect(StatusEffects.SLOWNESS, 400, 3), new IngredientEffect(StatusEffects.RESISTANCE, 400, 2));
            register(Items.SUGAR, Potions.AWKWARD, new IngredientEffect(StatusEffects.SPEED, 3600));
            register(Items.PUFFERFISH, Potions.AWKWARD, new IngredientEffect(StatusEffects.WATER_BREATHING, 3600));
            register(Items.GLISTERING_MELON_SLICE, Potions.AWKWARD, new IngredientEffect(StatusEffects.INSTANT_HEALTH, 1));
            register(Items.SPIDER_EYE, Potions.AWKWARD, new IngredientEffect(StatusEffects.POISON, 900));
            register(Items.GHAST_TEAR, Potions.AWKWARD, new IngredientEffect(StatusEffects.REGENERATION, 900));
            register(Items.BLAZE_POWDER, Potions.AWKWARD, new IngredientEffect(StatusEffects.STRENGTH, 3600));
            register(Items.PHANTOM_MEMBRANE, Potions.AWKWARD, new IngredientEffect(StatusEffects.SLOW_FALLING, 180));
            //#endregion
        }
        private static void registerMundaneIngredients() {
            //#region ANIMAL
            for (Item item : List.of(Items.WHITE_WOOL, Items.ORANGE_WOOL, Items.MAGENTA_WOOL, Items.LIGHT_BLUE_WOOL, Items.YELLOW_WOOL, Items.LIME_WOOL, Items.PINK_WOOL, Items.GRAY_WOOL, Items.LIGHT_GRAY_WOOL, Items.CYAN_WOOL, Items.PURPLE_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL, Items.GREEN_WOOL, Items.RED_WOOL, Items.BLACK_WOOL)){
                register(item, new IngredientEffect(StatusEffects.SLOWNESS, getDuration(60)));
            }
            register(Items.STRING, new IngredientEffect(StatusEffects.SLOWNESS, getDuration(45)));
            register(Items.COBWEB, new IngredientEffect(StatusEffects.SLOWNESS, getDuration(30), 1));

            register(Items.AXOLOTL_BUCKET, new IngredientEffect(StatusEffects.CONDUIT_POWER, getDuration(120)));
            register(Items.PUFFERFISH_BUCKET, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(120)));
            register(Items.SALMON_BUCKET, new IngredientEffect(StatusEffects.DOLPHINS_GRACE, getDuration(120)));
            register(Items.COD_BUCKET, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(120)));
            register(Items.TROPICAL_FISH_BUCKET, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(120)));
            register(Items.TADPOLE_BUCKET, new IngredientEffect(StatusEffects.JUMP_BOOST, getDuration(120)));

            register(Items.TROPICAL_FISH, new IngredientEffect(StatusEffects.STRENGTH, getDuration(120)));
            register(Items.PUFFERFISH, new IngredientEffect(StatusEffects.POISON, getDuration(80)));
            register(Items.COD, new IngredientEffect(StatusEffects.SATURATION, getDuration(15)));
            register(Items.SALMON, new IngredientEffect(StatusEffects.SATURATION, getDuration(15)));
            register(Items.NAUTILUS_SHELL, new IngredientEffect(StatusEffects.CONDUIT_POWER, getDuration(120)));
            register(Items.SCUTE, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(120), 1));

            register(Items.TURTLE_HELMET, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(360), 3), new IngredientEffect(StatusEffects.SLOWNESS, getDuration(120), 2));
            register(Items.TURTLE_EGG, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(120), 1), new IngredientEffect(StatusEffects.SLOWNESS, getDuration(30)));
            register(Items.SNIFFER_EGG, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(10), 4));
            register(Items.EGG, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(60)));
            register(Items.FEATHER, new IngredientEffect(StatusEffects.SLOW_FALLING, getDuration(180)));

            register(Items.INK_SAC, new IngredientEffect(StatusEffects.BLINDNESS, getDuration(80)));
            register(Items.GLOW_INK_SAC, new IngredientEffect(StatusEffects.GLOWING, getDuration(130)));

            register(Items.LEATHER, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(360)));
            register(Items.RABBIT_HIDE, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(360)), new IngredientEffect(StatusEffects.SPEED, getDuration(360)));
            register(Items.RABBIT_FOOT, new IngredientEffect(StatusEffects.JUMP_BOOST, getDuration(175), 2));

            register(Items.SPONGE, new IngredientEffect(StatusEffects.WATER_BREATHING, getDuration(480)));
            register(Items.WET_SPONGE, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(480)));

            register(Items.SPIDER_EYE, new IngredientEffect(StatusEffects.POISON, getDuration(60)));
            register(Items.GOAT_HORN, new IngredientEffect(StatusEffects.INSTANT_DAMAGE, 2));
            //#endregion
            //#region MOB
            register(Items.BLAZE_POWDER, new IngredientEffect(StatusEffects.STRENGTH, getDuration(120)));
            register(Items.BLAZE_ROD, new IngredientEffect(StatusEffects.STRENGTH, getDuration(180)));

            register(Items.CREEPER_HEAD, new IngredientEffect(StatusEffects.INSTANT_DAMAGE, 2), new IngredientEffect(StatusEffects.RESISTANCE, getDuration(120), 1));
            register(Items.DRAGON_HEAD, new IngredientEffect(StatusEffects.HEALTH_BOOST, getDuration(120), 2), new IngredientEffect(StatusEffects.SLOW_FALLING, getDuration(120)));
            register(Items.PIGLIN_HEAD, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(120)), new IngredientEffect(StatusEffects.STRENGTH, getDuration(120), 1));
            register(Items.SKELETON_SKULL, new IngredientEffect(StatusEffects.WEAKNESS, getDuration(120), 1), new IngredientEffect(StatusEffects.SLOWNESS, getDuration(120)));
            register(Items.WITHER_SKELETON_SKULL, new IngredientEffect(StatusEffects.WITHER, getDuration(120)), new IngredientEffect(StatusEffects.STRENGTH, getDuration(120), 1));
            register(Items.ZOMBIE_HEAD, new IngredientEffect(StatusEffects.HUNGER, getDuration(120)), new IngredientEffect(StatusEffects.BAD_OMEN, getDuration(120), 1));
            register(Items.PLAYER_HEAD, new IngredientEffect(StatusEffects.HERO_OF_THE_VILLAGE, getDuration(120)), new IngredientEffect(StatusEffects.INSTANT_HEALTH, 1, 1));
            
            register(Items.GHAST_TEAR, new IngredientEffect(StatusEffects.REGENERATION, getDuration(360)));
            register(Items.MAGMA_CREAM, new IngredientEffect(StatusEffects.FIRE_RESISTANCE, getDuration(360)));
            register(Items.PHANTOM_MEMBRANE, new IngredientEffect(StatusEffects.SLOW_FALLING, getDuration(360)));
            register(Items.ROTTEN_FLESH, new IngredientEffect(StatusEffects.HUNGER, getDuration(60)));

            register(Items.SHULKER_BOX, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(180), 2));
            register(Items.SHULKER_SHELL, new IngredientEffect(StatusEffects.RESISTANCE, getDuration(120), 2));
            
            register(Items.SLIME_BALL, new IngredientEffect(StatusEffects.JUMP_BOOST, getDuration(360)));
            //#endregion
        }

        private static void registerIngredients() {
            BrewingRecipeRegistry.registerItemRecipe(Items.SPLASH_POTION, Items.CHORUS_FLOWER, Items.LINGERING_POTION);

            //registerDyes();
            //registerDefault();
            //registerUniversalModifier();
            //registerStewIngredients();
            //registerRockyIngredients();
            //registerElixirIngredients();
            //registerAwkwardIngredients();
            //registerMundaneIngredients();

            Brewery.LOGGER.info(ingredient_map.size() + " ingredients registered, have fun !");
            printInfos();
        }

        public static void printInfos() {
            Map<Potion, Integer> nb_ingredients = new HashMap<>();
            Map<Potion, HashMap<StatusEffect, Integer>> effects = new HashMap<>();

            Map<StatusEffect, Integer> default_effects = new HashMap<>();
            int nb_amplifier = 0, default_max_duration = 1;

            for (PotionIngredientMap map : ingredient_map.values()) {
                for (IngredientType i : map.default_effects) {
                    if (i instanceof IngredientEffect) {
                        default_effects.put(((IngredientEffect)i).effect, default_effects.getOrDefault(((IngredientEffect)i).effect, 0) + 1);
                    }
                    else if (i instanceof IngredientDuration) {
                        default_max_duration *= ((IngredientDuration)i).mult;
                    }
                    else if (i instanceof IngredientAmplifier) {
                        nb_amplifier++;
                    }
                }

                for (Entry<Potion, List<IngredientType>> set : map.map.entrySet()) {
                    nb_ingredients.put(set.getKey(), nb_ingredients.getOrDefault(set.getKey(), 0) + 1);

                    for (IngredientType i : set.getValue()) {
                        effects.putIfAbsent(set.getKey(), new HashMap<>());

                        if (i instanceof IngredientEffect) {
                            effects.get(set.getKey()).put(((IngredientEffect)i).effect, effects.get(set.getKey()).getOrDefault(((IngredientEffect)i).effect, 0) + 1);
                        }
                    }
                }
            }

            Brewery.LOGGER.info("nb ingredients:");
            Brewery.LOGGER.info("- default potion: \t" + ingredient_map.size());
            List<Entry<Potion, Integer>> entries = new ArrayList<>(nb_ingredients.entrySet());
            entries.sort(Comparator.comparing(Entry<Potion, Integer>::getValue).reversed());
            for (Entry<Potion, Integer> set : entries) {
                Brewery.LOGGER.info("- " + set.getKey().finishTranslationKey("") + ": \t" + set.getValue());
            }

            Brewery.LOGGER.info("");
            Brewery.LOGGER.info("nb ingredients by effect:");
            Brewery.LOGGER.info("- default potion (" + default_effects.size() + "):");
            List<Entry<StatusEffect, Integer>> entries2 = new ArrayList<>(default_effects.entrySet());
            entries2.sort(Comparator.comparing(Entry<StatusEffect, Integer>::getValue).reversed());
            for (Entry<StatusEffect, Integer> counts : entries2) {
                Brewery.LOGGER.info("\t- " + counts.getKey().getTranslationKey() + ": \t" + counts.getValue());
            }
            for (Entry<Potion, HashMap<StatusEffect, Integer>> set : effects.entrySet()) {
                entries2 = new ArrayList<>(set.getValue().entrySet());
                entries2.sort(Comparator.comparing(Entry<StatusEffect, Integer>::getValue).reversed());
                Brewery.LOGGER.info("- " + set.getKey().finishTranslationKey("") + " (" + entries2.size() + "):");
                for (Entry<StatusEffect, Integer> counts : entries2) {
                    Brewery.LOGGER.info("\t- " + counts.getKey().getTranslationKey() + ": \t" + counts.getValue());
                }
            }
            Brewery.LOGGER.info("");
            Brewery.LOGGER.info("max duration boost: \t" + (default_max_duration - 1));
            Brewery.LOGGER.info("max amplifier boost: \t" + nb_amplifier);
        }

        public static ItemStack applyIngredient(ItemStack potion, ItemStack ingredient) {
            if (!ingredient_map.containsKey(ingredient.getItem()))
                return makeFailed(potion);

            List<IngredientType> i_types = ingredient_map.get(ingredient.getItem()).getIngredientEffects(potion);
            if (i_types.size() == 0)
                return makeFailed(potion);

            for (IngredientType t : i_types) {
                potion = t.applyEffect(potion, ingredient);
            }
            return potion;
        }

        public static boolean isIngredient(ItemStack ingredient) {
            return ingredient_map.containsKey(ingredient.getItem());
        }
    }
    public static final class Effects {
        private static final HashMap<List<StatusEffect>, List<StatusEffect>> map = new HashMap<List<StatusEffect>, List<StatusEffect>>() {{
            put(List.of(StatusEffects.SPEED), List.of(StatusEffects.SLOWNESS));
            put(List.of(StatusEffects.HASTE), List.of(StatusEffects.MINING_FATIGUE));
            put(List.of(StatusEffects.RESISTANCE, StatusEffects.STRENGTH), List.of(StatusEffects.WEAKNESS));
            put(List.of(StatusEffects.INSTANT_DAMAGE), List.of(StatusEffects.INSTANT_DAMAGE));
            put(List.of(StatusEffects.REGENERATION), List.of(StatusEffects.POISON, StatusEffects.WITHER));
            put(List.of(StatusEffects.NIGHT_VISION), List.of(StatusEffects.BLINDNESS, StatusEffects.DARKNESS));
            put(List.of(StatusEffects.SATURATION), List.of(StatusEffects.HUNGER));
            put(List.of(StatusEffects.ABSORPTION, StatusEffects.HEALTH_BOOST), List.of());
            put(List.of(StatusEffects.INVISIBILITY), List.of(StatusEffects.GLOWING));
            put(List.of(StatusEffects.JUMP_BOOST, StatusEffects.LEVITATION), List.of(StatusEffects.SLOW_FALLING));
            put(List.of(StatusEffects.LUCK), List.of(StatusEffects.UNLUCK));
            put(List.of(StatusEffects.HERO_OF_THE_VILLAGE), List.of(StatusEffects.BAD_OMEN));
            put(List.of(StatusEffects.WATER_BREATHING, StatusEffects.DOLPHINS_GRACE, StatusEffects.CONDUIT_POWER), List.of());
            put(List.of(StatusEffects.FIRE_RESISTANCE), List.of());
            put(List.of(), List.of(StatusEffects.NAUSEA));
        }};

        private static class EffectCap {
            public static int INFINITE = -1;

            public int max_amplifier = INFINITE;
            public int max_duration = INFINITE;
            public int max_amplifier_duration = INFINITE;

            public EffectCap(int max_amplifier, int max_duration, int max_amplifier_duration) {
                this.max_amplifier = max_amplifier;
                this.max_duration = max_duration;
                this.max_amplifier_duration = max_amplifier_duration;
            }
            public EffectCap(int max_amplifier, int max_duration) {
                this.max_amplifier = max_amplifier;
                this.max_duration = max_duration;
                this.max_amplifier_duration = max_duration;
            }
            public EffectCap(int max_amplifier) {
                this.max_amplifier = max_amplifier;
            }
            public EffectCap() {
            }
            
            public StatusEffectInstance apply(StatusEffectInstance instance) {
                int amplifier = instance.getAmplifier();
                boolean infinite = instance.isInfinite();
                int duration = infinite?Math.max(max_duration, max_amplifier_duration):instance.getDuration();
                
                if (this.max_amplifier != INFINITE) {
                    amplifier = Math.min(amplifier, this.max_amplifier);
                }
                if (this.max_amplifier == amplifier && max_amplifier_duration != INFINITE) {
                    duration = Math.min(duration, this.max_amplifier_duration);
                    infinite = false;
                }
                if (this.max_duration != INFINITE) {
                    duration = Math.min(duration, this.max_duration);
                    infinite = false;
                }

                return new StatusEffectInstance(instance.getEffectType(), infinite?StatusEffectInstance.INFINITE:duration, amplifier, instance.isAmbient(), instance.shouldShowParticles(), instance.shouldShowIcon(), null, instance.getFactorCalculationData());
            }
        }
        private static final HashMap<StatusEffect, EffectCap> cap_map = new HashMap<>() {{
            put(StatusEffects.SPEED, new EffectCap());
            put(StatusEffects.SLOWNESS, new EffectCap(6, EffectCap.INFINITE, getDuration(60)));
            put(StatusEffects.HASTE, new EffectCap());
            put(StatusEffects.MINING_FATIGUE, new EffectCap(3));
            put(StatusEffects.STRENGTH, new EffectCap());
            put(StatusEffects.INSTANT_HEALTH, new EffectCap(EffectCap.INFINITE, 1));
            put(StatusEffects.INSTANT_DAMAGE, new EffectCap(EffectCap.INFINITE, 1));
            put(StatusEffects.JUMP_BOOST, new EffectCap(126));
            put(StatusEffects.NAUSEA, new EffectCap(0));
            put(StatusEffects.REGENERATION, new EffectCap(5));
            put(StatusEffects.RESISTANCE, new EffectCap(4, EffectCap.INFINITE, getDuration(300)));
            put(StatusEffects.FIRE_RESISTANCE, new EffectCap(0));
            put(StatusEffects.WATER_BREATHING, new EffectCap(0));
            put(StatusEffects.INVISIBILITY, new EffectCap(0));
            put(StatusEffects.BLINDNESS, new EffectCap(0));
            put(StatusEffects.NIGHT_VISION, new EffectCap(0));
            put(StatusEffects.HUNGER, new EffectCap());
            put(StatusEffects.WEAKNESS, new EffectCap());
            put(StatusEffects.POISON, new EffectCap(4));
            put(StatusEffects.WITHER, new EffectCap(5));
            put(StatusEffects.HEALTH_BOOST, new EffectCap(250));
            put(StatusEffects.ABSORPTION, new EffectCap());
            put(StatusEffects.SATURATION, new EffectCap());
            put(StatusEffects.GLOWING, new EffectCap(0));
            put(StatusEffects.LEVITATION, new EffectCap(126));
            put(StatusEffects.LUCK, new EffectCap());
            put(StatusEffects.UNLUCK, new EffectCap());
            put(StatusEffects.SLOW_FALLING, new EffectCap(0));
            put(StatusEffects.CONDUIT_POWER, new EffectCap());
            put(StatusEffects.DOLPHINS_GRACE, new EffectCap());
            put(StatusEffects.BAD_OMEN, new EffectCap());
            put(StatusEffects.HERO_OF_THE_VILLAGE, new EffectCap(11));
            put(StatusEffects.DARKNESS, new EffectCap(0));
        }};

        public static boolean isGood(StatusEffect effect) {
            for (List<StatusEffect> good_effects : map.keySet()) {
                if (good_effects.contains(effect))
                    return true;
            }
            return false;
        }

        public static StatusEffect corrupt(StatusEffect effect) {
            for (Entry<List<StatusEffect>, List<StatusEffect>> entry : map.entrySet()) {
                if (!entry.getValue().isEmpty() && entry.getKey().contains(effect))
                    return entry.getValue().get(0);
                if (!entry.getKey().isEmpty() && entry.getValue().contains(effect))
                    return entry.getKey().get(0);
            }
            return effect;
        }
        public static StatusEffect alter(StatusEffect effect) {
            for (Entry<List<StatusEffect>, List<StatusEffect>> entry : map.entrySet()) {
                int i = entry.getKey().indexOf(effect);
                if (i != -1)
                    return entry.getKey().get((i + 1) % entry.getKey().size());

                i = entry.getValue().indexOf(effect);
                if (i != -1)
                    return entry.getValue().get((i + 1) % entry.getValue().size());
            }
            return effect;
        }

        public static StatusEffectInstance capEffect(StatusEffectInstance instance) {
            return cap_map.getOrDefault(instance.getEffectType(), new EffectCap()).apply(instance);
        }
    }

    public static int getDuration(float time) {
        return Math.round(time * 20);
    }

    public static boolean matchesBottle(ItemStack stack) {
        return stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)
                || stack.isOf(Items.GLASS_BOTTLE);
    }

    public static boolean matchesFilledBottle(ItemStack stack) {
        return stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION);
    }

    public static final int MAX_INGREDIENT_COUNT = 10;

    public static String getNbtId(int index) {
        return "ingredient_" + index;
    }

    public static int getNbIngredient(ItemStack stack) {
        if (!stack.hasNbt()) return 0;

        NbtCompound nbt = stack.getOrCreateNbt();
        for (int i = 1; i <= MAX_INGREDIENT_COUNT; i++) {
            if (!nbt.contains(getNbtId(i)))
                return i - 1;
        }

        return MAX_INGREDIENT_COUNT;
    }

    public static ItemStack pushIngredient(ItemStack stack, Item ingredient) {
        int nb_ingredient = getNbIngredient(stack);
        if (nb_ingredient == MAX_INGREDIENT_COUNT)
            return stack;
        stack.getOrCreateNbt().putString(getNbtId(nb_ingredient + 1), Registries.ITEM.getId(ingredient).toString());
        return stack;
    }

    public static boolean hasIngredient(ItemStack stack, Item ingredient) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getOrCreateNbt();

        for (int i = 1; i <= MAX_INGREDIENT_COUNT; i++) {
            if (!nbt.contains(getNbtId(i)))
                return false;
            if (nbt.getString(getNbtId(i)).equals(Registries.ITEM.getId(ingredient).toString()))
                return true;
        }
        ;
        return false;
    }

    public static List<Item> getIngredients(ItemStack stack) {
        List<Item> result = new ArrayList<>();
        if (!stack.hasNbt()) return result;
        NbtCompound nbt = stack.getOrCreateNbt();

        for (int i = 1; i <= MAX_INGREDIENT_COUNT; i++) {
            if (!nbt.contains(getNbtId(i))) return result;
            if (!Identifier.isValid(nbt.getString(getNbtId(i)))) return result;
            result.add(Registries.ITEM.get(new Identifier(nbt.getString(getNbtId(i)))));
        }
        return result;
    }

    public static void printIngredients(ItemStack stack) {
        Brewery.LOGGER.info("potion (" + Registries.ITEM.getId(stack.getItem()) + ", " + PotionUtil.getPotion(stack).finishTranslationKey("") + "):");
        
        if (!stack.hasNbt()) return;
        NbtCompound nbt = stack.getOrCreateNbt();

        for (int i = 1; i <= MAX_INGREDIENT_COUNT; i++) {
            if (!nbt.contains(getNbtId(i)))
                break;
            Brewery.LOGGER.info("\tingredient " + i + ": " + nbt.getString(getNbtId(i)));
        }
        ;
    }

    public static ItemStack makeFailed(ItemStack input) {
        input.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
        input.getOrCreateNbt().putInt(PotionUtil.CUSTOM_POTION_COLOR_KEY,
                ModPotionUtils.PotionBases.getCustomColor(ModPotionUtils.PotionBases.FAILED_POTION));
        return PotionUtil.setPotion(input, ModPotionUtils.PotionBases.FAILED_POTION);
    }

    public static void registerPotions() {
        // needed even if empty to register potion
        PotionBases.registerPotions();
        Ingredients.registerIngredients();
    }
}
