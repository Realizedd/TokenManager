package me.realized.tm.utilities;

import me.realized.tm.Core;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemUtil {

    private static final List<String> ENCHANTMENTS = new ArrayList<>();
    private static final List<String> EFFECTS = new ArrayList<>();
    private static final Core instance = Core.getInstance();
    private static final String INVALID_ITEM = "Failed to create item: ";
    private static final String INVALID_META = "Failed to apply item meta: ";

    static {
        for (Enchantment enchantment : Enchantment.values()) {
            if (isDefaultEnchantment(enchantment)) {
                ENCHANTMENTS.add(StringUtil.reverseTranslateEnchantment(enchantment.getName()));
            }
        }

        for (PotionEffectType type : PotionEffectType.values()) {
            if (isDefaultPotionEffectType(type)) {
                EFFECTS.add(StringUtil.reverseTranslatePotionEffect(type.getName()));
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static ItemStack toItemStack(String data) {
        try {
            ItemStack item = null;
            String[] args = data.split(" +");

            if (args.length < 2 || args[0].split(":").length == 0) {
                instance.warn(INVALID_ITEM + data + " does not have a type and amount.");
                return null;
            }

            String[] type = args[0].split(":");
            int amount = Integer.parseInt(args[1]);
            Material material = Material.matchMaterial(type[0]);
            short durability = 0;

            if (type.length > 1) {
                if (Bukkit.getVersion().contains("1.9")) {
                    if (material == Material.MONSTER_EGG) {
                        if (EntityType.fromName(type[1]) == null) {
                            instance.warn(INVALID_ITEM + type[1] + " is not a valid entity type.");
                        } else {
                            SpawnEgg1_9 spawnEgg1_9 = new SpawnEgg1_9(EntityType.fromName(type[1]));
                            item = spawnEgg1_9.toItemStack(amount);
                        }
                    } else if (material == Material.POTION) {
                        List<String> values = Arrays.asList(type[1].split("-"));

                        if (!Potion1_9.PotionType.isType(values.get(0).toUpperCase())) {
                            instance.warn(INVALID_ITEM + values.get(0) + " is not a valid potion type. Available: " + Potion1_9.PotionType.names());
                        } else {
                            item = new Potion1_9(Potion1_9.PotionType.valueOf(values.get(0).toUpperCase()), values.contains("strong"), values.contains("extended"), values.contains("linger"), values.contains("splash")).toItemStack(amount);
                        }
                    } else {
                        durability = Short.parseShort(type[1]);
                    }
                } else {
                    durability = Short.parseShort(type[1]);
                }
            }

            if (item == null) {
                item = new ItemStack(material, amount, durability);
            }

            if (args.length > 2) {
                for (int i = 2; i < args.length; i++) {
                    addItemMeta(item, args[i]);
                }
            }

            return item;
        } catch (Exception e) {
            instance.warn(INVALID_ITEM + e.getMessage());
            return null;
        }
    }

    private static void addItemMeta(ItemStack item, String meta) {
        try {
            String[] args = meta.split(":", 2);
            ItemMeta itemMeta = item.getItemMeta();

            if (args.length < 1) {
                instance.warn(INVALID_META + meta + " does not contain any meta data.");
                return;
            }

            if (args[0].equalsIgnoreCase("name")) {
                itemMeta.setDisplayName(StringUtil.color(args[1].replace("_", " ")));
                item.setItemMeta(itemMeta);
                return;
            }

            if (args[0].equalsIgnoreCase("lore")) {
                List<String> lore = new ArrayList<>();

                for (String st : args[1].split("\\|")) {
                    lore.add(StringUtil.color(st.replace("_", " ")));
                }

                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
                return;
            }

            if (ENCHANTMENTS.contains(args[0])) {
                item.addUnsafeEnchantment(Enchantment.getByName(StringUtil.translateEnchantment(args[0])), Integer.parseInt(args[1]));
                return;
            }

            if (EFFECTS.contains(args[0]) && item.getType() == Material.POTION) {
                PotionMeta potionMeta = (PotionMeta) itemMeta;
                PotionEffectType type = PotionEffectType.getByName(StringUtil.translatePotionEffect(args[0]));
                int power = Integer.parseInt(args[1].split(":")[0]);
                int duration = Integer.parseInt(args[1].split(":")[1]);
                potionMeta.addCustomEffect(new PotionEffect(type, duration, power), true);
                item.setItemMeta(potionMeta);
                return;
            }

            if ((args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("owner")) && item.getType() == Material.SKULL_ITEM) {
                if (item.getDurability() == 3) {
                    SkullMeta skullMeta = (SkullMeta) itemMeta;
                    skullMeta.setOwner(args[1]);
                    item.setItemMeta(skullMeta);
                }
            }
        } catch (Exception e) {
            instance.warn(INVALID_META + e.getMessage());
        }
    }

    private static boolean isDefaultEnchantment(Enchantment enchantment) {
        if (enchantment == null) {
            return false;
        }

        switch (enchantment.getName()) {
            case "ARROW_DAMAGE":
            case "ARROW_FIRE":
            case "ARROW_INFINITE":
            case "ARROW_KNOCKBACK":
            case "DAMAGE_ALL":
            case "DAMAGE_ARTHROPODS":
            case "DAMAGE_UNDEAD":
            case "DIG_SPEED":
            case "DURABILITY":
            case "THORNS":
            case "FIRE_ASPECT":
            case "KNOCKBACK":
            case "LOOT_BONUS_BLOCKS":
            case "LOOT_BONUS_MOBS":
            case "OXYGEN":
            case "PROTECTION_EXPLOSIONS":
            case "PROTECTION_FALL":
            case "PROTECTION_FIRE":
            case "PROTECTION_PROJECTILE":
            case "PROTECTION_ENVIRONMENTAL":
            case "SILK_TOUCH":
            case "WATER_WORKER":
            case "LUCK":
            case "LURE":
                return true;
            default:
                return false;
        }
    }

    private static boolean isDefaultPotionEffectType(PotionEffectType type) {
        if (type == null) {
            return false;
        }

        switch (type.getName()) {
            case "SPEED":
            case "SLOW":
            case "FAST_DIGGING":
            case "SLOW_DIGGING":
            case "INCREASE_DAMAGE":
            case "HEAL":
            case "HARM":
            case "JUMP":
            case "NAUSEA":
            case "REGENERATION":
            case "DAMAGE_RESISTANCE":
            case "FIRE_RESISTANCE":
            case "WATER_BREATHING":
            case "INVISIBILITY":
            case "BLINDNESS":
            case "NIGHT_VISION":
            case "HUNGER":
            case "WEAKNESS":
            case "POISON":
            case "WITHER":
            case "HEALTH_BOOST":
            case "ABSORPTION":
            case "SATURATION":
                return true;
            default:
                return false;
        }
    }
}
