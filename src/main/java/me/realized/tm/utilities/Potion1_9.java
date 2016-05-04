package me.realized.tm.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Potion1_9 {

    public enum PotionType {

        FIRE_RESISTANCE, INSTANT_DAMAGE, INSTANT_HEAL,
        INVISIBILITY, JUMP, LUCK,
        NIGHT_VISION, POISON, REGEN,
        SLOWNESS, SPEED, STRENGTH,
        WATER, WATER_BREATHING, WEAKNESS,
        EMPTY, MUNDANE, THICK, AWKWARD;

        public static boolean isType(String input) {
            for (PotionType type : values()) {
                if (type.name().equals(input)) {
                    return true;
                }
            }

            return false;
        }

        public static List<String> names() {
            List<String> result = new ArrayList<>();

            for (PotionType type : values()) {
                result.add(type.name());
            }

            return result;
        }
    }

    private Class<?> itemStack;
    private Class<?> nbtTagCompound;
    private Class<?> craftItemStack;

    private final PotionType type;
    private final boolean strong, extended, linger, splash;

    public Potion1_9(PotionType type, boolean strong, boolean extended, boolean linger, boolean splash) {
        this.type = type;
        this.strong = strong;
        this.extended = extended;
        this.linger = linger;
        this.splash = splash;

        final String version = Bukkit.getServer().getClass().getName().split("\\.")[3];

        try {
            itemStack = Class.forName("net.minecraft.server." + version + ".ItemStack");
            nbtTagCompound = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
            craftItemStack = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (ClassNotFoundException ignored) {}
    }

    public ItemStack toItemStack(int amount) {
        ItemStack item = new ItemStack(Material.POTION, amount);
        if (splash) {
            item = new ItemStack(Material.SPLASH_POTION, amount);
        } else if (linger) {
            item = new ItemStack(Material.LINGERING_POTION, amount);
        }

        try {
            Object stack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(itemStack, item);
            Object tagCompound = itemStack.getMethod("getTag").invoke(stack);

            if (tagCompound == null) {
                tagCompound = nbtTagCompound.newInstance();
            }

            String tag;

            if (type.equals(PotionType.FIRE_RESISTANCE)) {
                if (extended) {
                    tag = "long_fire_resistance";
                } else {
                    tag = "fire_resistance";
                }
            } else if (type.equals(PotionType.INSTANT_DAMAGE)) {
                if (strong) {
                    tag = "strong_harming";
                } else {
                    tag = "harming";
                }
            } else if (type.equals(PotionType.INSTANT_HEAL)) {
                if (strong) {
                    tag = "strong_healing";
                } else {
                    tag = "healing";
                }
            } else if (type.equals(PotionType.INVISIBILITY)) {
                if (extended) {
                    tag = "long_invisibility";
                } else {
                    tag = "invisibility";
                }
            } else if (type.equals(PotionType.JUMP)) {
                if (extended) {
                    tag = "long_leaping";
                } else if (strong) {
                    tag = "strong_leaping";
                } else {
                    tag = "leaping";
                }
            } else if (type.equals(PotionType.LUCK)) {
                tag = "luck";
            } else if (type.equals(PotionType.NIGHT_VISION)) {
                if (extended) {
                    tag = "long_night_vision";
                } else {
                    tag = "night_vision";
                }
            } else if (type.equals(PotionType.POISON)) {
                if (extended) {
                    tag = "long_poison";
                } else if (strong) {
                    tag = "strong_poison";
                } else {
                    tag = "poison";
                }
            } else if (type.equals(PotionType.REGEN)) {
                if (extended) {
                    tag = "long_regeneration";
                } else if (strong) {
                    tag = "strong_regeneration";
                } else {
                    tag = "regeneration";
                }
            } else if (type.equals(PotionType.SLOWNESS)) {
                if (extended) {
                    tag = "long_slowness";
                } else {
                    tag = "slowness";
                }
            } else if (type.equals(PotionType.SPEED)) {
                if (extended) {
                    tag = "long_swiftness";
                } else if (strong) {
                    tag = "strong_swiftness";
                } else {
                    tag = "swiftness";
                }
            } else if (type.equals(PotionType.STRENGTH)) {
                if (extended) {
                    tag = "long_strength";
                } else if (strong) {
                    tag = "strong_strength";
                } else {
                    tag = "strength";
                }
            } else if (type.equals(PotionType.WATER_BREATHING)) {
                if (extended) {
                    tag = "long_water_breathing";
                } else {
                    tag = "water_breathing";
                }
            } else if (type.equals(PotionType.WATER)) {
                tag = "water";
            } else if (type.equals(PotionType.WEAKNESS)) {
                if (extended) {
                    tag = "long_weakness";
                } else {
                    tag = "weakness";
                }
            } else if (type.equals(PotionType.EMPTY)) {
                tag = "empty";
            } else if (type.equals(PotionType.MUNDANE)) {
                tag = "mundane";
            } else if (type.equals(PotionType.THICK)) {
                tag = "thick";
            } else if (type.equals(PotionType.AWKWARD)) {
                tag = "awkward";
            } else {
                return null;
            }

            nbtTagCompound.getMethod("setString", String.class, String.class).invoke(tagCompound, "Potion", "minecraft:" + tag);
            itemStack.getMethod("setTag", nbtTagCompound).invoke(stack, tagCompound);
            return (ItemStack) craftItemStack.getMethod("asBukkitCopy", itemStack).invoke(null, stack);
        } catch (Exception e) {
            return null;
        }
    }
}
