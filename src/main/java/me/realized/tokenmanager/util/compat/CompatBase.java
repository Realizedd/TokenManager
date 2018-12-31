package me.realized.tokenmanager.util.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

class CompatBase {

    static final Method AS_NMS_COPY, AS_BUKKIT_COPY;

    static final Class<?> TAG_COMPOUND;
    static final Method GET_TAG, SET_TAG, SET, SET_STRING;

    static final Constructor<?> GAME_PROFILE_CONST, PROPERTY_CONST;
    static final Method GET_PROPERTIES, PUT;
    static final Field PROFILE;

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
        SET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "setString", String.class, String.class);

        final Class<?> GAME_PROFILE = ReflectionUtil.getALClass("GameProfile");
        GAME_PROFILE_CONST = ReflectionUtil.getConstructor(GAME_PROFILE, UUID.class, String.class);
        GET_PROPERTIES = ReflectionUtil.getMethod(GAME_PROFILE, "getProperties");
        final Class<?> PROPERTY = ReflectionUtil.getALClass("properties.Property");
        PROPERTY_CONST = ReflectionUtil.getConstructor(PROPERTY, String.class, String.class);
        PUT = ReflectionUtil.getMethod(ReflectionUtil.getALClass("properties.PropertyMap"), "put", Object.class, Object.class);
        PROFILE = ReflectionUtil.getDeclaredField(ReflectionUtil.getCBClass("inventory.CraftMetaSkull"), "profile");
    }
}
