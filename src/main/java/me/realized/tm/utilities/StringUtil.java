package me.realized.tm.utilities;

import org.bukkit.ChatColor;

import java.util.List;

public class StringUtil {

    public static String join(String joiner, List<TMShop> shops) {
        if (shops.isEmpty()) return "none";

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < shops.size() - 1; i++) {
            builder.append(shops.get(i).getName()).append(joiner);
        }
        builder.append(shops.get(shops.size() - 1).getName());
        return builder.toString();
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String translateEnchantment(String input) {
        input = input.replaceAll("power", "ARROW_DAMAGE");
        input = input.replaceAll("flame", "ARROW_FIRE");
        input = input.replaceAll("infinity", "ARROW_INFINITE");
        input = input.replaceAll("punch", "ARROW_KNOCKBACK");
        input = input.replaceAll("sharpness", "DAMAGE_ALL");
        input = input.replaceAll("baneofarthopods", "DAMAGE_ARTHROPODS");
        input = input.replaceAll("smite", "DAMAGE_UNDEAD");
        input = input.replaceAll("efficiency", "DIG_SPEED");
        input = input.replaceAll("unbreaking", "DURABILITY");
        input = input.replaceAll("thorns", "THORNS");
        input = input.replaceAll("fireaspect", "FIRE_ASPECT");
        input = input.replaceAll("knockback", "KNOCKBACK");
        input = input.replaceAll("fortune", "LOOT_BONUS_BLOCKS");
        input = input.replaceAll("looting", "LOOT_BONUS_MOBS");
        input = input.replaceAll("respiration", "OXYGEN");
        input = input.replaceAll("blastprotection", "PROTECTION_EXPLOSIONS");
        input = input.replaceAll("featherfalling", "PROTECTION_FALL");
        input = input.replaceAll("fireprotection", "PROTECTION_FIRE");
        input = input.replaceAll("projectileprotection", "PROTECTION_PROJECTILE");
        input = input.replaceAll("protection", "PROTECTION_ENVIRONMENTAL");
        input = input.replaceAll("silktouch", "SILK_TOUCH");
        input = input.replaceAll("aquaaffinity", "WATER_WORKER");
        input = input.replaceAll("luck", "LUCK");
        input = input.replaceAll("lure", "LURE");
        return input;
    }

    public static String reverseTranslateEnchantment(String input) {
        input = input.replaceAll("ARROW_DAMAGE", "power");
        input = input.replaceAll("ARROW_FIRE", "flame");
        input = input.replaceAll("ARROW_INFINITE", "infinity");
        input = input.replaceAll("ARROW_KNOCKBACK", "punch");
        input = input.replaceAll("DAMAGE_ALL", "sharpness");
        input = input.replaceAll("DAMAGE_ARTHROPODS", "baneofarthopods");
        input = input.replaceAll("DAMAGE_UNDEAD", "smite");
        input = input.replaceAll("DIG_SPEED", "efficiency");
        input = input.replaceAll("DURABILITY", "unbreaking");
        input = input.replaceAll("THORNS", "thorns");
        input = input.replaceAll("FIRE_ASPECT", "fireaspect");
        input = input.replaceAll("KNOCKBACK", "knockback");
        input = input.replaceAll("LOOT_BONUS_BLOCKS", "fortune");
        input = input.replaceAll("LOOT_BONUS_MOBS", "looting");
        input = input.replaceAll("OXYGEN", "respiration");
        input = input.replaceAll("PROTECTION_EXPLOSIONS", "blastprotection");
        input = input.replaceAll("PROTECTION_FALL", "featherfalling");
        input = input.replaceAll("PROTECTION_FIRE", "fireprotection");
        input = input.replaceAll("PROTECTION_PROJECTILE", "projectileprotection");
        input = input.replaceAll("PROTECTION_ENVIRONMENTAL", "protection");
        input = input.replaceAll("SILK_TOUCH", "silktouch");
        input = input.replaceAll("WATER_WORKER", "aquaaffinity");
        input = input.replaceAll("LUCK", "luck");
        input = input.replaceAll("LURE", "lure");
        return input;
    }

    public static String translatePotionEffect(String input) {
        input = input.replaceAll("speed", "SPEED");
        input = input.replaceAll("slowness", "SLOW");
        input = input.replaceAll("haste", "FAST_DIGGING");
        input = input.replaceAll("fatigue", "SLOW_DIGGING");
        input = input.replaceAll("strength", "INCREASE_DAMAGE");
        input = input.replaceAll("heal", "HEAL");
        input = input.replaceAll("harm", "HARM");
        input = input.replaceAll("jump", "JUMP");
        input = input.replaceAll("nausea", "CONFUSION");
        input = input.replaceAll("regeneration", "REGENERATION");
        input = input.replaceAll("resistance", "DAMAGE_RESISTANCE");
        input = input.replaceAll("fireresistance", "FIRE_RESISTANCE");
        input = input.replaceAll("waterbreathing", "WATER_BREATHING");
        input = input.replaceAll("invisibility", "INVISIBILITY");
        input = input.replaceAll("blindness", "BLINDNESS");
        input = input.replaceAll("nightvision", "NIGHT_VISION");
        input = input.replaceAll("hunger", "HUNGER");
        input = input.replaceAll("weakness", "WEAKNESS");
        input = input.replaceAll("poison", "POISON");
        input = input.replaceAll("wither", "WITHER");
        input = input.replaceAll("healthboost", "HEALTH_BOOST");
        input = input.replaceAll("absorption", "ABSORPTION");
        input = input.replaceAll("saturation", "SATURATION");
        return input;
    }

    public static String reverseTranslatePotionEffect(String input) {
        input = input.replaceAll("SPEED", "speed");
        input = input.replaceAll("SLOW", "slowness");
        input = input.replaceAll("FAST_DIGGING", "haste");
        input = input.replaceAll("SLOW_DIGGING", "fatigue");
        input = input.replaceAll("INCREASE_DAMAGE", "strength");
        input = input.replaceAll("HEAL", "instantheal");
        input = input.replaceAll("HARM", "instantdamage");
        input = input.replaceAll("JUMP", "jumpboost");
        input = input.replaceAll("NAUSEA", "confusion");
        input = input.replaceAll("REGENERATION", "regeneration");
        input = input.replaceAll("DAMAGE_RESISTANCE", "resistance");
        input = input.replaceAll("FIRE_RESISTANCE", "fireresistance");
        input = input.replaceAll("WATER_BREATHING", "waterbreathing");
        input = input.replaceAll("INVISIBILITY", "invisibility");
        input = input.replaceAll("BLINDNESS", "blindness");
        input = input.replaceAll("NIGHT_VISION", "nightvision");
        input = input.replaceAll("HUNGER", "hunger");
        input = input.replaceAll("WEAKNESS", "weakness");
        input = input.replaceAll("POISON", "poison");
        input = input.replaceAll("WITHER", "wither");
        input = input.replaceAll("HEALTH_BOOST", "healthboost");
        input = input.replaceAll("ABSORPTION", "absorption");
        input = input.replaceAll("SATURATION", "saturation");
        return input;
    }

    public static String format(long seconds) {
        if (seconds <= 0) return "updating...";

        long years = seconds / 31556952;
        seconds -= years * 31556952;
        long months = seconds / 2592000;
        seconds -= months * 2592000;
        long weeks = seconds / 604800;
        seconds -= weeks * 604800;
        long days = seconds / 86400;
        seconds -= days * 86400;
        long hours = seconds / 3600;
        seconds -= hours * 3600;
        long minutes = seconds / 60;
        seconds -= minutes * 60;

        StringBuilder sb = new StringBuilder();

        if (years > 0) {
            sb.append(years).append("yr");
        }

        if (months > 0) {
            sb.append(months).append("mo");
        }

        if (weeks > 0) {
            sb.append(weeks).append("w");
        }

        if (days > 0) {
            sb.append(days).append("d");
        }

        if (hours > 0) {
            sb.append(hours).append("h");
        }

        if (minutes > 0) {
            sb.append(minutes).append("m");
        }

        if (seconds > 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString();
    }
}
