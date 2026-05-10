// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Best-effort extraction of all ItemStacks an entity can "hold" in vanilla:
 * - LivingEntity equipment (hands + armor)
 * - Any entity implementing {@link Container}
 * - Entities exposing a no-arg getInventory() returning {@link Container}
 *
 * Intended for despawn-time preservation logic (before discard()).
 */
public final class NipEntityHeldItems {
    private NipEntityHeldItems() {}

    private static final Map<Class<?>, Method> GET_INVENTORY_CACHE = new ConcurrentHashMap<>();
    
    // Sentinel value for classes that don't have getInventory() - ConcurrentHashMap doesn't allow null values
    private static Method NO_INVENTORY_METHOD;
    
    static {
        try {
            NO_INVENTORY_METHOD = Object.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void dropPreservedItemsOnDespawn(ServerLevel world, Entity entity) {
        if (world == null || entity == null) return;

        var dropper = (java.util.function.Consumer<ItemStack>) (named -> {
            if (named == null || named.isEmpty()) return;
            entity.spawnAtLocation(world, named.copy());
        });

        // 1) LivingEntity equipment (covers hands, armor; includes fox mouth item, helmets, etc.).
        if (entity instanceof LivingEntity living) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = living.getItemBySlot(slot);
                if (stack == null || stack.isEmpty()) continue;

                if (NipUtil.isNamedItem(stack)) {
                    entity.spawnAtLocation(world, stack.copy());
                    living.setItemSlot(slot, ItemStack.EMPTY);
                    continue;
                }

                // Spill-and-strip to avoid duplication if other mods later also drop the same container stack.
                NipContainerContents.spillAndStripNamedContents(stack, dropper);
            }
        }

        // 2) Direct Inventory implementation (covers many storage entities).
        if (entity instanceof Container inv) {
            dropFromInventory(world, entity, inv, dropper);
            return;
        }

        // 3) getInventory() via reflection (covers villager/allay/horses-like cases depending on mapping).
        Container reflected = tryGetInventory(entity);
        if (reflected != null) {
            dropFromInventory(world, entity, reflected, dropper);
        }
    }

    private static void dropFromInventory(ServerLevel world, Entity entity, Container inv, java.util.function.Consumer<ItemStack> dropper) {
        int size;
        try {
            size = inv.getContainerSize();
        } catch (Throwable t) {
            return;
        }

        for (int i = 0; i < size; i++) {
            ItemStack stack;
            try {
                stack = inv.getItem(i);
            } catch (Throwable t) {
                continue;
            }

            if (stack == null || stack.isEmpty()) continue;

            if (NipUtil.isNamedItem(stack)) {
                entity.spawnAtLocation(world, stack.copy());
                try {
                    inv.setItem(i, ItemStack.EMPTY);
                } catch (Throwable ignored) {
                    // Best-effort.
                }
                continue;
            }

            NipContainerContents.spillAndStripNamedContents(stack, dropper);
        }
    }

    private static Container tryGetInventory(Entity entity) {
        if (entity == null) return null;
        
        Class<?> cls = entity.getClass();
        
        Method cached = GET_INVENTORY_CACHE.get(cls);
        if (cached == null) {
            Method found = findGetInventory(cls);
            cached = (found != null) ? found : NO_INVENTORY_METHOD;
            GET_INVENTORY_CACHE.put(cls, cached);
        }

        if (cached == NO_INVENTORY_METHOD) return null;

        try {
            Object result = cached.invoke(entity);
            return result instanceof Container inv ? inv : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static Method findGetInventory(Class<?> cls) {
        try {
            Method m = cls.getMethod("getInventory");
            if (!Container.class.isAssignableFrom(m.getReturnType())) return null;
            return m;
        } catch (NoSuchMethodException ignored) {
            // Try declared method too.
        } catch (SecurityException ignored) {
            return null;
        }

        try {
            Method m = cls.getDeclaredMethod("getInventory");
            if (!Container.class.isAssignableFrom(m.getReturnType())) return null;
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (SecurityException ignored) {
            return null;
        }
    }
}
