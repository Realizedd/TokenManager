/*
 *
 *   This file is part of TokenManager, licensed under the MIT License.
 *
 *   Copyright (c) Realized
 *   Copyright (c) contributors
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 *
 */

package me.realized.tokenmanager.util.inventory;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.Consumer;
import me.realized.tokenmanager.util.EnumUtil;
import me.realized.tokenmanager.util.NumberUtil;
import me.realized.tokenmanager.util.StringUtil;
import me.realized.tokenmanager.util.compat.CompatUtil;
import me.realized.tokenmanager.util.compat.Items;
import me.realized.tokenmanager.util.compat.Potions;
import me.realized.tokenmanager.util.compat.Potions.PotionType;
import me.realized.tokenmanager.util.compat.SpawnEggs;
import me.realized.tokenmanager.util.compat.Terracottas;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class ItemUtil {

    private static final Map<String, Enchantment> ENCHANTMENTS;
    private static final Map<String, PotionEffectType> EFFECTS;

    static {
        final Map<String, Enchantment> enchantments = new HashMap<>();
        Arrays.stream(Enchantment.values()).forEach(enchantment -> {
            enchantments.put(enchantment.getName(), enchantment);

            if (!CompatUtil.isPre1_13()) {
                enchantments.put(enchantment.getKey().getKey(), enchantment);
            }
        });
        enchantments.put("power", Enchantment.ARROW_DAMAGE);
        enchantments.put("flame", Enchantment.ARROW_FIRE);
        enchantments.put("infinity", Enchantment.ARROW_INFINITE);
        enchantments.put("punch", Enchantment.ARROW_KNOCKBACK);
        enchantments.put("sharpness", Enchantment.DAMAGE_ALL);
        enchantments.put("baneofarthopods", Enchantment.DAMAGE_ARTHROPODS);
        enchantments.put("smite", Enchantment.DAMAGE_UNDEAD);
        enchantments.put("efficiency", Enchantment.DIG_SPEED);
        enchantments.put("unbreaking", Enchantment.DURABILITY);
        enchantments.put("thorns", Enchantment.THORNS);
        enchantments.put("fireaspect", Enchantment.FIRE_ASPECT);
        enchantments.put("knockback", Enchantment.KNOCKBACK);
        enchantments.put("fortune", Enchantment.LOOT_BONUS_BLOCKS);
        enchantments.put("looting", Enchantment.LOOT_BONUS_MOBS);
        enchantments.put("respiration", Enchantment.OXYGEN);
        enchantments.put("blastprotection", Enchantment.PROTECTION_EXPLOSIONS);
        enchantments.put("featherfalling", Enchantment.PROTECTION_FALL);
        enchantments.put("fireprotection", Enchantment.PROTECTION_FIRE);
        enchantments.put("projectileprotection", Enchantment.PROTECTION_PROJECTILE);
        enchantments.put("protection", Enchantment.PROTECTION_ENVIRONMENTAL);
        enchantments.put("silktouch", Enchantment.SILK_TOUCH);
        enchantments.put("aquaaffinity", Enchantment.WATER_WORKER);
        enchantments.put("luck", Enchantment.LUCK);
        ENCHANTMENTS = Collections.unmodifiableMap(enchantments);

        final Map<String, PotionEffectType> effects = new HashMap<>();
        Arrays.stream(PotionEffectType.values()).forEach(type -> {
            if (type == null) {
                return;
            }

            effects.put(type.getName(), type);
        });
        effects.put("speed", PotionEffectType.SPEED);
        effects.put("slowness", PotionEffectType.SLOW);
        effects.put("haste", PotionEffectType.FAST_DIGGING);
        effects.put("fatigue", PotionEffectType.SLOW_DIGGING);
        effects.put("strength", PotionEffectType.INCREASE_DAMAGE);
        effects.put("heal", PotionEffectType.HEAL);
        effects.put("harm", PotionEffectType.HARM);
        effects.put("jump", PotionEffectType.JUMP);
        effects.put("nausea", PotionEffectType.CONFUSION);
        effects.put("regeneration", PotionEffectType.REGENERATION);
        effects.put("resistance", PotionEffectType.DAMAGE_RESISTANCE);
        effects.put("fireresistance", PotionEffectType.FIRE_RESISTANCE);
        effects.put("waterbreathing", PotionEffectType.WATER_BREATHING);
        effects.put("invisibility", PotionEffectType.INVISIBILITY);
        effects.put("blindness", PotionEffectType.BLINDNESS);
        effects.put("nightvision", PotionEffectType.NIGHT_VISION);
        effects.put("hunger", PotionEffectType.HUNGER);
        effects.put("weakness", PotionEffectType.WEAKNESS);
        effects.put("poison", PotionEffectType.POISON);
        effects.put("wither", PotionEffectType.WITHER);
        effects.put("healthboost", PotionEffectType.HEALTH_BOOST);
        effects.put("absorption", PotionEffectType.ABSORPTION);
        effects.put("saturation", PotionEffectType.SATURATION);
        EFFECTS = Collections.unmodifiableMap(effects);
    }

    private ItemUtil() {}

    public static ItemStack replace(final ItemStack item, final long value, final String... placeholders) {
        if (!item.hasItemMeta()) {
            return item;
        }

        final ItemMeta meta = item.getItemMeta();

        if (meta.hasLore()) {
            final List<String> lore = meta.getLore();
            lore.replaceAll(line -> {
                for (final String placeholder : placeholders) {
                    line = line
                        .replace("%" + placeholder + "%", NumberUtil.withCommas(value))
                        .replace("%" + placeholder + "_formatted%", NumberUtil.withSuffix(value))
                        .replace("%" + placeholder + "_raw%", String.valueOf(value));
                }

                return line;
            });
            meta.setLore(lore);
        }

        if (meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();

            for (final String placeholder : placeholders) {
                displayName = displayName
                    .replace("%" + placeholder + "%", NumberUtil.withCommas(value))
                    .replace("%" + placeholder + "_formatted%", NumberUtil.withSuffix(value))
                    .replace("%" + placeholder + "_raw%", String.valueOf(value));
            }

            meta.setDisplayName(displayName);
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack loadFromString(final String line) {
        if (line == null || line.isEmpty()) {
            throw new IllegalArgumentException("Line is empty or null");
        }

        final String[] args = line.split(" +");
        String[] materialData = args[0].split(":");
        Material material = Material.matchMaterial(materialData[0]);

        // TEMP: Allow confirm button item loading in 1.13
        if (!CompatUtil.isPre1_13()) {
            if (materialData[0].equalsIgnoreCase("STAINED_CLAY")) {
                material = Material.TERRACOTTA;

                if (materialData.length > 1) {
                    material = Terracottas.from((short) NumberUtil.parseLong(materialData[1]).orElse(0));
                }
            }
        }

        if (material == null) {
            throw new IllegalArgumentException("'" + args[0] + "' is not a valid material");
        }

        ItemStack result = new ItemStack(material, 1);

        if (materialData.length > 1) {
            // Handle potions and spawn eggs switching to NBT in 1.9+
            if (!CompatUtil.isPre1_9()) {
                if (material.name().contains("POTION")) {
                    final String[] values = materialData[1].split("-");
                    final PotionType type;

                    if ((type = EnumUtil.getByName(values[0], PotionType.class)) == null) {
                        throw new IllegalArgumentException("'" + values[0] + "' is not a valid PotionType. Available: " + EnumUtil.getNames(PotionType.class));
                    }

                    result = new Potions(type, Arrays.asList(values)).toItemStack();
                } else if (CompatUtil.isPre1_13() && material.name().equals("MONSTER_EGG")) {
                    final EntityType type;

                    if ((type = EnumUtil.getByName(materialData[1], EntityType.class)) == null) {
                        throw new IllegalArgumentException("'" + materialData[0] + "' is not a valid EntityType. Available: " + EnumUtil.getNames(EntityType.class));
                    }

                    result = new SpawnEggs(type).toItemStack();
                }
            }

            final OptionalLong value;

            if ((value = NumberUtil.parseLong(materialData[1])).isPresent()) {
                result.setDurability((short) value.getAsLong());
            }
        }

        if (args.length < 2) {
            return result;
        }

        result.setAmount(Integer.parseInt(args[1]));

        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                final String argument = args[i];
                final String[] pair = argument.split(":", 2);

                if (pair.length < 2) {
                    continue;
                }

                applyMeta(result, pair[0], pair[1]);
            }
        }

        return result;
    }


    public static ItemStack loadFromString(final String line, final Consumer<String> errorHandler) {
        ItemStack result;

        try {
            result = loadFromString(line);
        } catch (Exception ex) {
            result = ItemBuilder
                .of(Material.REDSTONE_BLOCK)
                .name("&4&m------------------")
                .lore(
                    "&cThere was an error",
                    "&cwhile loading this",
                    "&citem, please contact",
                    "&can administrator.",
                    "&4&m------------------"
                )
                .build();
            errorHandler.accept(ex.getMessage());
        }

        return result;
    }

    private static void applyMeta(final ItemStack item, final String key, final String value) {
        final ItemMeta meta = item.getItemMeta();

        if (key.equalsIgnoreCase("name")) {
            meta.setDisplayName(StringUtil.color(value.replace("_", " ")));
            item.setItemMeta(meta);
            return;
        }

        if (key.equalsIgnoreCase("lore")) {
            meta.setLore(StringUtil.color(Lists.newArrayList(value.split("\\|")), s -> s.replace("_", " ")));
            item.setItemMeta(meta);
            return;
        }

        if (key.equalsIgnoreCase("unbreakable") && value.equalsIgnoreCase("true")) {
            if (CompatUtil.isPre1_12()) {
                meta.spigot().setUnbreakable(true);
            } else {
                meta.setUnbreakable(true);
            }

            item.setItemMeta(meta);
            return;
        }

        if (key.equalsIgnoreCase("flags")) {
            if (!CompatUtil.isPre1_8()) {
                final String[] flags = value.split(",");

                for (final String flag : flags) {
                    final ItemFlag itemFlag = EnumUtil.getByName(flag, ItemFlag.class);

                    if (itemFlag == null) {
                        continue;
                    }

                    meta.addItemFlags(itemFlag);
                }
            }

            item.setItemMeta(meta);
            return;
        }

        final Enchantment enchantment = ENCHANTMENTS.get(key);

        if (enchantment != null) {
            item.addUnsafeEnchantment(enchantment, Integer.parseInt(value));
            return;
        }

        if (item.getType().name().contains("POTION")) {
            final PotionEffectType effectType = EFFECTS.get(key);

            if (effectType != null) {
                final String[] values = value.split(":");
                final PotionMeta potionMeta = (PotionMeta) meta;
                potionMeta.addCustomEffect(new PotionEffect(effectType, Integer.parseInt(values[1]), Integer.parseInt(values[0])), true);
                item.setItemMeta(potionMeta);
                return;
            }
        }

        if (Items.equals(Items.HEAD, item) && (key.equalsIgnoreCase("player") || key.equalsIgnoreCase("owner"))) {
            final SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwner(value);
            item.setItemMeta(skullMeta);
        }

        if (item.getType().name().contains("LEATHER_") && key.equalsIgnoreCase("color")) {
            final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;
            final String[] values = value.split(",");
            leatherArmorMeta.setColor(Color.fromRGB(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2])));
            item.setItemMeta(leatherArmorMeta);
        }
    }
}
