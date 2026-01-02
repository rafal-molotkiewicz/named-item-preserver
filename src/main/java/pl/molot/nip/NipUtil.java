// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.Identifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import pl.molot.nip.mixin.EntityWorldAccessor;

public final class NipUtil {
    private NipUtil() {}

    /** "Named item" means: item stack has a custom name (anvil name). */
    public static boolean isNamedItem(ItemEntity e) {
        return e.getStack().getCustomName() != null;
    }

    /** "Named item" means: item stack has a custom name (anvil name). */
    public static boolean isNamedItem(ItemStack stack) {
        return stack.getCustomName() != null;
    }

    /** Return formatted "x, y, z" for the entity's position. */
    public static String getBlockPos(Entity entity) {
        if (entity == null) return "unknown";
        BlockPos pos = entity.getBlockPos();
        if (pos == null) return "unknown";
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    /**
     * Return a human-friendly dimension name for the provided world. Examples:
     * - minecraft:overworld -> Overworld
     * - minecraft:the_nether -> Nether
     * - minecraft:the_end -> End
     */
    public static String getDimensionName(World world) {
        if (world == null) return "Unknown";
        Identifier id = world.getRegistryKey().getValue();
        if (id == null) return "Unknown";
        String path = id.getPath();
        switch (path) {
            case "overworld": return "Overworld";
            case "the_nether": return "Nether";
            case "the_end": return "End";
            default:
                return humanizePath(path);
        }
    }

    /**
     * Return a human-friendly name for an entity.
     * Uses the translated display name when available, otherwise falls back to the registry path.
     */
    public static String getEntityDisplayName(Entity entity) {
        if (entity == null) return "Unknown";

        String display = entity.getDisplayName().getString();
        if (display != null && !display.isBlank() && !display.contains(".")) {
            return display;
        }

        Identifier id = Registries.ENTITY_TYPE.getId(entity.getType());
        if (id != null) {
            return humanizePath(id.getPath());
        }

        return (display == null || display.isBlank()) ? "Unknown" : display;
    }

    private static String humanizePath(String path) {
        if (path == null || path.isBlank()) return "Unknown";
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.length() == 0 ? "Unknown" : sb.toString();
    }

    /** Return dimension name for an entity by using EntityWorldAccessor internally. */
    public static String getDimensionName(ItemEntity entity) {
        World world = ((EntityWorldAccessor)(Object)entity).nip$getWorld();
        return getDimensionName(world);
    }

    /** Return safe display name for an ItemStack. */

    public static String getDisplayName(ItemStack stack) {
        if (stack == null) return "unknown";
        return stack.getCustomName() != null ? stack.getCustomName().getString() : stack.getName().getString();
    }

    /** Return safe display name for an ItemEntity. */
    public static String getDisplayName(ItemEntity entity) {
        if (entity == null) return "unknown";
        return getDisplayName(entity.getStack());
    }

    /** Return descriptor for the picker entity, e.g. "by player Foo" or "by mob Zombie". */
    public static String getPickerDescriptor(Entity picker) {
        if (picker == null) return "by unknown";
        String kind;
        if (picker instanceof PlayerEntity) {
            kind = "player";
        } else if (picker instanceof MobEntity) {
            kind = "mob";
        } else {
            kind = "entity";
        }
        return "by " + kind + " " + getEntityDisplayName(picker);
    }

    /**
     * Standard pickup message.
     * - `stack` is the item being picked up (pass a pre-pickup copy if needed)
     * - `picker` is who picked it up
     * - `location` provides position + dimension context (picker or item entity)
     */
    public static String pickedUpMessage(ItemStack stack, Entity picker, Entity location) {
        String itemName = getDisplayName(stack);
        return "Named item " + itemName + " taken " + getPickerDescriptor(picker)
            + " at " + getBlockPos(location)
            + " in " + getDimensionName(location == null ? null : location.getEntityWorld());
    }

    /** Backwards-compatible textual formatter returning single string */
    public static String formatItemLog(ItemEntity entity) {
        if (entity == null) return "unknown";
        return getDisplayName(entity) + " at " + getBlockPos(entity) + " in " + getDimensionName(entity);
    }
}
