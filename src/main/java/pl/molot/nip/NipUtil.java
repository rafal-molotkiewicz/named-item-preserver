// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.Identifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registries;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.util.math.BlockPos;

public final class NipUtil {
    private NipUtil() {}

    //region Named Item Checks

    /** "Named item" means: item stack has a custom name (anvil name). */
    public static boolean isNamedItem(ItemStack stack) {
        return stack != null && stack.getCustomName() != null;
    }

    //endregion

    //region Position & Dimension

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

    //endregion

    //region Display Names

    /**
     * Return a human-friendly name for an entity.
     * Uses the translated display name when available, otherwise falls back to the registry path.
     */
    public static String getDisplayName(Entity entity) {
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

    /** Return safe display name for an ItemStack. */
    public static String getDisplayName(ItemStack stack) {
        if (stack == null) return "unknown";
        return stack.getCustomName() != null ? stack.getCustomName().getString() : stack.getName().getString();
    }

    /** Return the translated base item type name (e.g. "Diamond Sword"), ignoring custom names. */
    public static String getItemTypeName(ItemStack stack) {
        if (stack == null) return "unknown";
        try {
            String name = stack.getItem().getName().getString();
            return (name == null || name.isBlank()) ? "unknown" : name;
        } catch (Throwable ignored) {
            // Fallback: might include custom name in some cases, but better than nothing.
            return getDisplayName(stack);
        }
    }

    /**
     * Human-friendly item descriptor, e.g. "Diamond Sword named Test 5".
     * If the item has no custom name, returns the base type name.
     */
    public static String describeItem(ItemStack stack) {
        if (stack == null) return "unknown";
        String typeName = getItemTypeName(stack);
        if (stack.getCustomName() == null) {
            return typeName;
        }
        String custom = stack.getCustomName().getString();
        if (custom == null || custom.isBlank()) {
            return typeName;
        }
        return typeName + " named " + custom;
    }

    //endregion

    //region Picker Descriptors

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
        return "by " + kind + " " + getDisplayName(picker);
    }

    //endregion

    //region Info Messages

    /**
     * Standard pickup message.
     * - `stack` is the item being picked up (pass a pre-pickup copy if needed)
     * - `picker` is who picked it up
     * - `location` provides position + dimension context (picker or item entity)
     */
    public static String pickedUpMessage(ItemStack stack, Entity picker, Entity location) {
        String itemDesc = describeItem(stack);
        return itemDesc + " was taken " + getPickerDescriptor(picker)
            + " at " + getBlockPos(location)
            + " in " + getDimensionName(location == null ? null : location.getEntityWorld());
    }

    public static String droppedMessage(ItemStack stack, Entity location) {
        String itemDesc = describeItem(stack);
        return itemDesc + " was dropped at " + getBlockPos(location)
            + " in " + getDimensionName(location == null ? null : location.getEntityWorld());
    }

    public static String destroyedMessage(ItemStack stack, DamageSource source, Entity location) {
        String itemDesc = describeItem(stack);
        return itemDesc + " was destroyed by " + getDamageCause(source)
            + " at " + getBlockPos(location)
            + " in " + getDimensionName(location == null ? null : location.getEntityWorld());
    }

    public static String removedMessage(ItemStack stack, RemovalReason reason, Entity location) {
        String itemDesc = describeItem(stack);
        return itemDesc + " was removed (" + formatRemovalReason(reason) + ")"
            + " at " + getBlockPos(location)
            + " in " + getDimensionName(location == null ? null : location.getEntityWorld());
    }

    public static String transcendedMessage(ItemStack stack, World fromWorld, BlockPos fromPos, World toWorld, BlockPos toPos) {
        String itemDesc = describeItem(stack);
        return itemDesc + " transcended from " + getDimensionName(fromWorld) + " " + formatBlockPos(fromPos)
            + " to " + getDimensionName(toWorld) + " " + formatBlockPos(toPos);
    }

    //endregion

    //region Internal Helpers

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

    private static String getDamageCause(DamageSource source) {
        if (source == null) return "Unknown";
        // Try to return a stable, readable identifier (e.g. lava, cactus).
        try {
            Object maybeName = source.getClass().getMethod("getName").invoke(source);
            if (maybeName instanceof String name && !name.isBlank()) {
                return humanizePath(name);
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall through
        } catch (RuntimeException ignored) {
            // Fall through
        }
        return source.toString();
    }

    private static String formatRemovalReason(RemovalReason reason) {
        if (reason == null) return "unknown";
        return humanizePath(reason.name().toLowerCase());
    }

    private static String formatBlockPos(BlockPos pos) {
        if (pos == null) return "unknown";
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    //endregion
}
