package me.realized.tokenmanager.util.compat;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import org.bukkit.inventory.meta.SkullMeta;

public final class Skulls extends CompatBase {

    public static void setSkull(final SkullMeta meta, final String value) {
        try {
            final Object profile = GAME_PROFILE_CONST.newInstance(UUID.randomUUID(), null);
            final Object propertyMap = GET_PROPERTIES.invoke(profile);

            if (propertyMap == null) {
                throw new IllegalStateException("Profile doesn't contain a property map");
            }

            PUT.invoke(propertyMap, "textures", PROPERTY_CONST.newInstance("textures", value));
            PROFILE.set(meta, profile);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private Skulls() {}
}
