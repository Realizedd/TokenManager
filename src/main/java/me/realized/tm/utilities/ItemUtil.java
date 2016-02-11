package me.realized.tm.utilities;

import me.realized.tm.Core;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

    private static final List<String> ENCHANTMENTS = new ArrayList<>();
    private static final List<String> EFFECTS = new ArrayList<>();

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
            ItemStack item;
            String[] args = data.split(" +");

            if (args.length < 2) {
                return null;
            }

            if (args[0].split(":").length == 0) {
                return null;
            }

            String[] type = args[0].split(":");
            Material material = Material.getMaterial(Integer.parseInt(type[0]));
            short durability = 0;

            if (type.length > 1) {
                durability = Short.parseShort(type[1]);
            }

            int amount = Integer.parseInt(args[1]);

            item = new ItemStack(material, amount, durability);


            if (args.length > 2) {
                for (int i = 2; i < args.length; i++) {
                    addMeta(item, args[i]);
                }
            }

            return item;

        } catch (Exception e) {
            Core.getInstance().warn("An error occurred while trying to parse '" + data + "' to item: " + e.getMessage());
        }

        return null;
    }

    private static void addMeta(ItemStack item, String meta) {
        try {
            String[] args = meta.split(":", 2);
            ItemMeta itemMeta = item.getItemMeta();

            if (args.length < 1) {
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
            Core.getInstance().warn("An error occurred while trying to apply meta '" + meta + "' to item " + item.getType() + ": " + e.getMessage());
        }
    }

    private static boolean isDefaultEnchantment(Enchantment enchantment) {
        if (enchantment == null) {
            return false;
        }

        switch (enchantment.getName()) {
            case "ARROW_DAMAGE":
                return true;
            case "ARROW_FIRE":
                return true;
            case "ARROW_INFINITE":
                return true;
            case "ARROW_KNOCKBACK":
                return true;
            case "DAMAGE_ALL":
                return true;
            case "DAMAGE_ARTHROPODS":
                return true;
            case "DAMAGE_UNDEAD":
                return true;
            case "DIG_SPEED":
                return true;
            case "DURABILITY":
                return true;
            case "THORNS":
                return true;
            case "FIRE_ASPECT":
                return true;
            case "KNOCKBACK":
                return true;
            case "LOOT_BONUS_BLOCKS":
                return true;
            case "LOOT_BONUS_MOBS":
                return true;
            case "OXYGEN":
                return true;
            case "PROTECTION_EXPLOSIONS":
                return true;
            case "PROTECTION_FALL":
                return true;
            case "PROTECTION_FIRE":
                return true;
            case "PROTECTION_PROJECTILE":
                return true;
            case "PROTECTION_ENVIRONMENTAL":
                return true;
            case "SILK_TOUCH":
                return true;
            case "WATER_WORKER":
                return true;
            case "LUCK":
                return true;
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
                return true;
            case "SLOW":
                return true;
            case "FAST_DIGGING":
                return true;
            case "SLOW_DIGGING":
                return true;
            case "INCREASE_DAMAGE":
                return true;
            case "HEAL":
                return true;
            case "HARM":
                return true;
            case "JUMP":
                return true;
            case "NAUSEA":
                return true;
            case "REGENERATION":
                return true;
            case "DAMAGE_RESISTANCE":
                return true;
            case "FIRE_RESISTANCE":
                return true;
            case "WATER_BREATHING":
                return true;
            case "INVISIBILITY":
                return true;
            case "BLINDNESS":
                return true;
            case "NIGHT_VISION":
                return true;
            case "HUNGER":
                return true;
            case "WEAKNESS":
                return true;
            case "POISON":
                return true;
            case "WITHER":
                return true;
            case "HEALTH_BOOST":
                return true;
            case "ABSORPTION":
                return true;
            case "SATURATION":
                return true;
            default:
                return false;
        }
    }
}
