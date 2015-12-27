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
            ENCHANTMENTS.add(StringUtil.reverseTranslateEnchantment(enchantment.getName()));
        }

        for (PotionEffectType type : PotionEffectType.values()) {
            if (type != null) {
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
}
