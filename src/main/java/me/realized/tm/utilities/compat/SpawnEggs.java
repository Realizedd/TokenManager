package me.realized.tm.utilities.compat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class SpawnEggs {

    private Class<?> itemStack;
    private Class<?> nbtTagCompound;
    private Class<?> nbtBase;
    private Class<?> craftItemStack;

    private final EntityType type;

    public SpawnEggs(EntityType type) {
        this.type = type;

        final String version = Bukkit.getServer().getClass().getName().split("\\.")[3];

        try {
            itemStack = Class.forName("net.minecraft.server." + version + ".ItemStack");
            nbtTagCompound = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
            nbtBase = Class.forName("net.minecraft.server." + version + ".NBTBase");
            craftItemStack = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public ItemStack toItemStack(int amount) {
        try {
            ItemStack item = new ItemStack(Material.MONSTER_EGG, amount);
            Object stack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(itemStack, item);
            Object tagCompound = itemStack.getMethod("getTag").invoke(stack);

            if (tagCompound == null) {
                tagCompound = nbtTagCompound.newInstance();
            }

            Object id = nbtTagCompound.newInstance();
            nbtTagCompound.getMethod("setString", String.class, String.class).invoke(id, "id", type.getName());
            nbtTagCompound.getMethod("set", String.class, nbtBase).invoke(tagCompound, "EntityTag", id);
            itemStack.getMethod("setTag", nbtTagCompound).invoke(stack, tagCompound);
            return (ItemStack) craftItemStack.getMethod("asBukkitCopy", itemStack).invoke(null, stack);
        } catch (Exception e) {
            return null;
        }
    }

    /* UNUSED
    @SuppressWarnings("deprecation")
    public static SpawnEggs fromItemStack(ItemStack item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        } else if (item.getType() != Material.MONSTER_EGG) {
            throw new IllegalArgumentException("item is not a monster egg");
        }

        try {
            Object stack = craftItemStack.getMethod("asNMSCopy", itemStack).invoke(null, item);
            Object tagCompound = itemStack.getMethod("getTag").invoke(stack);

            if (tagCompound != null) {
                Object entityTagCompound = nbtTagCompound.getMethod("getCompound", String.class).invoke(tagCompound, "EntityTag");
                String name = (String) nbtTagCompound.getMethod("getString").invoke(entityTagCompound, "id");
                EntityType type = EntityType.fromName(name);

                if (type != null) {
                    return new SpawnEggs(type);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    */
}
