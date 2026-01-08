// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Best-effort extraction of all ItemStacks an entity can "hold" in vanilla:
 * - LivingEntity equipment (hands + armor)
 * - Any entity implementing {@link Inventory}
 * - Entities exposing a no-arg getInventory() returning {@link Inventory}
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

    public static void dropPreservedItemsOnDespawn(ServerWorld world, Entity entity) {
        if (world == null || entity == null) return;

        var dropper = (java.util.function.Consumer<ItemStack>) (named -> {
            if (named == null || named.isEmpty()) return;
            entity.dropStack(world, named.copy());
        });

        // 1) LivingEntity equipment (covers hands, armor; includes fox mouth item, helmets, etc.).
        if (entity instanceof LivingEntity living) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = living.getEquippedStack(slot);
                if (stack == null || stack.isEmpty()) continue;

                if (NipUtil.isNamedItem(stack)) {
                    entity.dropStack(world, stack.copy());
                    living.equipStack(slot, ItemStack.EMPTY);
                    continue;
                }

                // Spill-and-strip to avoid duplication if other mods later also drop the same container stack.
                NipContainerContents.spillAndStripNamedContents(stack, dropper);
            }
        }

        // 2) Direct Inventory implementation (covers many storage entities).
        if (entity instanceof Inventory inv) {
            dropFromInventory(world, entity, inv, dropper);
            return;
        }

        // 3) getInventory() via reflection (covers villager/allay/horses-like cases depending on mapping).
        Inventory reflected = tryGetInventory(entity);
        if (reflected != null) {
            dropFromInventory(world, entity, reflected, dropper);
        }
    }

    private static void dropFromInventory(ServerWorld world, Entity entity, Inventory inv, java.util.function.Consumer<ItemStack> dropper) {
        int size;
        try {
            size = inv.size();
        } catch (Throwable t) {
            return;
        }

        for (int i = 0; i < size; i++) {
            ItemStack stack;
            try {
                stack = inv.getStack(i);
            } catch (Throwable t) {
                continue;
            }

            if (stack == null || stack.isEmpty()) continue;

            if (NipUtil.isNamedItem(stack)) {
                entity.dropStack(world, stack.copy());
                try {
                    inv.setStack(i, ItemStack.EMPTY);
                } catch (Throwable ignored) {
                    // Best-effort.
                }
                continue;
            }

            NipContainerContents.spillAndStripNamedContents(stack, dropper);
        }
    }

    private static Inventory tryGetInventory(Entity entity) {
        if (entity == null) return null;
        
        Class<?> cls = entity.getClass();
        if (cls == null) return null;
        
        Method cached = GET_INVENTORY_CACHE.get(cls);
        if (cached == null) {
            Method found = findGetInventory(cls);
            cached = (found != null) ? found : NO_INVENTORY_METHOD;
            GET_INVENTORY_CACHE.put(cls, cached);
        }

        if (cached == NO_INVENTORY_METHOD) return null;

        try {
            Object result = cached.invoke(entity);
            return result instanceof Inventory inv ? inv : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static Method findGetInventory(Class<?> cls) {
        try {
            Method m = cls.getMethod("getInventory");
            if (!Inventory.class.isAssignableFrom(m.getReturnType())) return null;
            return m;
        } catch (NoSuchMethodException ignored) {
            // Try declared method too.
        } catch (SecurityException ignored) {
            return null;
        }

        try {
            Method m = cls.getDeclaredMethod("getInventory");
            if (!Inventory.class.isAssignableFrom(m.getReturnType())) return null;
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (SecurityException ignored) {
            return null;
        }
    }
}
