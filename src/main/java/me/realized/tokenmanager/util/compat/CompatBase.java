package me.realized.tokenmanager.util.compat;

import java.lang.reflect.Method;
import org.bukkit.inventory.ItemStack;

class CompatBase {

    static final Method AS_NMS_COPY;
    static final Method AS_BUKKIT_COPY;

    static final Class<?> TAG_COMPOUND;
    static final Method GET_TAG;
    static final Method SET_TAG;
    static final Method SET;
    static final Method GET_STRING;
    static final Method SET_STRING;

    static {
        final Class<?> CB_ITEMSTACK = ReflectionUtil.getCBClass("inventory.CraftItemStack");
        final Class<?> NMS_ITEMSTACK = ReflectionUtil.getNMSClass("ItemStack");
        AS_NMS_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asNMSCopy", ItemStack.class);
        AS_BUKKIT_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asBukkitCopy", NMS_ITEMSTACK);
        TAG_COMPOUND = ReflectionUtil.getNMSClass("NBTTagCompound");

        final Class<?> TAG_BASE = ReflectionUtil.getNMSClass("NBTBase");
        GET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "getTag");
        SET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "setTag", TAG_COMPOUND);
        SET = ReflectionUtil.getMethod(TAG_COMPOUND, "set", String.class, TAG_BASE);
        GET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "getString", String.class);
        SET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "setString", String.class, String.class);
    }
}
