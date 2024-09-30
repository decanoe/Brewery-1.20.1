package decanoe.brewery.brewing_utils;

import net.minecraft.util.Colors;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStack.TooltipSection;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.effect.StatusEffect;

public class IngredientType {
    public static class IngredientEffect extends IngredientType {
        public StatusEffect effect;
        public int amplifier;
        public int duration;
        
        public IngredientEffect(StatusEffect effect, int duration, int amplifier) {
            this.effect = effect;
            this.amplifier = amplifier;
            this.duration = duration;
        }
        public IngredientEffect(StatusEffect effect, int duration) {
            this(effect, duration, 0);
        }
        public IngredientEffect(StatusEffect effect) {
            this(effect, ModPotionUtils.getDuration(5));
        }

        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(potion);
            for (int i = 0; i < effects.size(); i++) {
                if (effects.get(i).getEffectType() == this.effect) {

                    effects.set(i, combineEffects(effects.get(i), new StatusEffectInstance(this.effect, this.duration, this.amplifier)));
                    potion.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
                    return PotionUtil.setCustomPotionEffects(potion, effects);
                }
            }

            effects.add(ModPotionUtils.Effects.capEffect(new StatusEffectInstance(this.effect, this.duration, this.amplifier)));
            potion.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
            return PotionUtil.setCustomPotionEffects(potion, effects);
        }
    }
    public static class IngredientColor extends IngredientType {
        public int color; // 0xAARRGGBB
        
        public IngredientColor(int r, int g, int b) {
            this.color = b + (g + r * 256)* 256;
        }
        public IngredientColor(int color) {
            this.color = color;
        }
        public IngredientColor() {
            this(Colors.RED);
        }

        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            potion.getOrCreateNbt().putInt(PotionUtil.CUSTOM_POTION_COLOR_KEY, this.color);
            return potion;
        }
    }
    public static class IngredientDuration extends IngredientType {
        public float mult;
        
        public IngredientDuration(float mult) {
            this.mult = mult;
        }
        public IngredientDuration() {
            this(1.25f);
        }

        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(potion);

            for (int i = 0; i < effects.size(); i++) {
                if (effects.get(i).getDuration() == StatusEffectInstance.INFINITE) continue;

                effects.set(i, ModPotionUtils.Effects.capEffect(new StatusEffectInstance(
                    effects.get(i).getEffectType(),
                    Math.round(effects.get(i).getDuration() * this.mult),
                    effects.get(i).getAmplifier(),
                    effects.get(i).isAmbient(),
                    effects.get(i).shouldShowParticles(),
                    effects.get(i).shouldShowIcon()
                )));
            }
            
            potion.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
            return PotionUtil.setCustomPotionEffects(potion, effects);
        }
    }
    public static class IngredientInfinite extends IngredientType {
        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(potion);

            for (int i = 0; i < effects.size(); i++) {
                effects.set(i, ModPotionUtils.Effects.capEffect(new StatusEffectInstance(
                    effects.get(i).getEffectType(),
                    StatusEffectInstance.INFINITE,
                    effects.get(i).getAmplifier(),
                    effects.get(i).isAmbient(),
                    effects.get(i).shouldShowParticles(),
                    effects.get(i).shouldShowIcon()
                )));
            }
            
            potion.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
            return PotionUtil.setCustomPotionEffects(potion, effects);
        }
    }
    public static class IngredientAmplifier extends IngredientType {
        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(potion);

            for (int i = 0; i < effects.size(); i++) {
                effects.set(i, ModPotionUtils.Effects.capEffect(new StatusEffectInstance(
                    effects.get(i).getEffectType(),
                    effects.get(i).getDuration(),
                    effects.get(i).getAmplifier() + 1,
                    effects.get(i).isAmbient(),
                    effects.get(i).shouldShowParticles(),
                    effects.get(i).shouldShowIcon()
                )));
            }
            
            potion.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
            return PotionUtil.setCustomPotionEffects(potion, effects);
        }
    }
    public static class IngredientCorruption extends IngredientType {
        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(potion);

            for (int i = 0; i < effects.size(); i++) {
                effects.set(i, ModPotionUtils.Effects.capEffect(new StatusEffectInstance(
                    ModPotionUtils.Effects.corrupt(effects.get(i).getEffectType()),
                    effects.get(i).getDuration(),
                    effects.get(i).getAmplifier(),
                    effects.get(i).isAmbient(),
                    effects.get(i).shouldShowParticles(),
                    effects.get(i).shouldShowIcon()
                )));
            }
            
            potion.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
            return PotionUtil.setCustomPotionEffects(potion, effects);
        }
    }
    public static class IngredientAlteration extends IngredientType {
        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(potion);

            for (int i = 0; i < effects.size(); i++) {
                effects.set(i, ModPotionUtils.Effects.capEffect(new StatusEffectInstance(
                    ModPotionUtils.Effects.alter(effects.get(i).getEffectType()),
                    effects.get(i).getDuration(),
                    effects.get(i).getAmplifier(),
                    effects.get(i).isAmbient(),
                    effects.get(i).shouldShowParticles(),
                    effects.get(i).shouldShowIcon()
                )));
            }
            
            potion.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
            return PotionUtil.setCustomPotionEffects(potion, effects);
        }
    }
    public static class IngredientCure extends IngredientType {
        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(potion);

            int i = 0;
            while (i < effects.size()) {
                if (!ModPotionUtils.Effects.isGood(effects.get(i).getEffectType())) effects.remove(i);
                else i++;
            }
            
            potion.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
            return PotionUtil.setCustomPotionEffects(potion, effects);
        }
    }
    public static class IngredientInvertCure extends IngredientType {
        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(potion);

            int i = 0;
            while (i < effects.size()) {
                if (ModPotionUtils.Effects.isGood(effects.get(i).getEffectType())) effects.remove(i);
                else i++;
            }
            
            potion.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
            return PotionUtil.setCustomPotionEffects(potion, effects);
        }
    }
    public static class IngredientHideEffect extends IngredientType {
        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(potion);

            for (int i = 0; i < effects.size(); i++) {
                effects.set(i, new StatusEffectInstance(
                    effects.get(i).getEffectType(),
                    effects.get(i).getDuration(),
                    effects.get(i).getAmplifier(),
                    effects.get(i).isAmbient(),
                    false,
                    false
                ));
            }
            
            potion.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
            potion.addHideFlag(TooltipSection.ADDITIONAL);
            return PotionUtil.setCustomPotionEffects(potion, effects);
        }
    }
    public static class IngredientShowRecipe extends IngredientType {
        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            potion.getOrCreateNbt().putBoolean("show_recipe", true);
            return potion;
        }
    }
    public static class IngredientGlint extends IngredientType {
        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            potion.getOrCreateNbt().putBoolean("ench_glint", true);
            return potion;
        }
    }
    public static class IngredientTranfer extends IngredientType {
        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(potion);
            List<StatusEffectInstance> added_effects = PotionUtil.getPotionEffects(ingredient);

            for (int j = 0; j < added_effects.size(); j++) {
                boolean found = false;
                for (int i = 0; i < effects.size(); i++) {
                    if (effects.get(i).getEffectType() == added_effects.get(j).getEffectType()) {
                        effects.set(i, combineEffects(effects.get(i), added_effects.get(j)));
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    effects.add(ModPotionUtils.Effects.capEffect(added_effects.get(j)));
                }
            }
            
            potion.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
            return PotionUtil.setCustomPotionEffects(potion, effects);
        }
    }
    public static class IngredientDurationTranfer extends IngredientType {
        float mult;
        
        public IngredientDurationTranfer(float duration_mult) {
            this.mult = duration_mult;
        }

        @Override
        public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(potion);
            List<StatusEffectInstance> added_effects = PotionUtil.getPotionEffects(ingredient);

            for (int j = 0; j < added_effects.size(); j++) {
                added_effects.set(j, ModPotionUtils.Effects.capEffect(new StatusEffectInstance(
                    added_effects.get(j).getEffectType(),
                    Math.round(added_effects.get(j).getDuration() * this.mult),
                    added_effects.get(j).getAmplifier(),
                    added_effects.get(j).isAmbient(),
                    added_effects.get(j).shouldShowParticles(),
                    added_effects.get(j).shouldShowIcon()
                )));

                boolean found = false;
                for (int i = 0; i < effects.size(); i++) {
                    if (effects.get(i).getEffectType() == added_effects.get(j).getEffectType()) {
                        effects.set(i, combineEffects(effects.get(i), added_effects.get(j)));
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    effects.add(ModPotionUtils.Effects.capEffect(added_effects.get(j)));
                }
            }
            
            potion.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
            return PotionUtil.setCustomPotionEffects(potion, effects);
        }
    }

    public static class PotionIngredientMap {
        HashMap<Potion, List<IngredientType>> map;
        List<IngredientType> default_effects;

        PotionIngredientMap(List<IngredientType> default_effect) {
            this.default_effects = default_effect;
            this.map = new HashMap<>();
        };
        PotionIngredientMap put(Potion potion, List<IngredientType> effects) {
            if (map.containsKey(potion)) {
                map.get(potion).addAll(effects);
            }
            else {
                map.put(potion, effects);
            }
            return this;
        }
        PotionIngredientMap putDefault(List<IngredientType> effects) {
            default_effects.addAll(effects);
            return this;
        }

        
        public List<IngredientType> getIngredientEffects(ItemStack potion) {
            return map.getOrDefault(ModPotionUtils.PotionBases.getBase(PotionUtil.getPotion(potion)), default_effects);
        }
    }

    static StatusEffectInstance combineEffects(StatusEffectInstance base_effect, StatusEffectInstance added_effect) {
        if (base_effect.getAmplifier() == added_effect.getAmplifier()) {
            return ModPotionUtils.Effects.capEffect(new StatusEffectInstance(
                base_effect.getEffectType(),
                base_effect.getDuration() + added_effect.getDuration(),
                base_effect.getAmplifier(),
                base_effect.isAmbient(),
                base_effect.shouldShowParticles(),
                base_effect.shouldShowIcon()
            ));
        }
        else if (base_effect.getAmplifier() > added_effect.getAmplifier()) {
            int diff = base_effect.getAmplifier() - added_effect.getAmplifier();
            return ModPotionUtils.Effects.capEffect(new StatusEffectInstance(
                base_effect.getEffectType(),
                (int)Math.round(added_effect.getDuration() * Math.pow(0.75, diff)) + base_effect.getDuration(),
                base_effect.getAmplifier(),
                base_effect.isAmbient(),
                base_effect.shouldShowParticles(),
                base_effect.shouldShowIcon()
            ));
        }
        else {
            int diff = added_effect.getAmplifier() - base_effect.getAmplifier();
            return ModPotionUtils.Effects.capEffect(new StatusEffectInstance(
                base_effect.getEffectType(),
                added_effect.getDuration() + (int)Math.round(base_effect.getDuration() * Math.pow(0.75, diff)),
                added_effect.getAmplifier(),
                base_effect.isAmbient(),
                base_effect.shouldShowParticles(),
                base_effect.shouldShowIcon()
            ));
        }
    }

    IngredientType() {}
    public ItemStack applyEffect(ItemStack potion, ItemStack ingredient) {
        return potion;
    }
}