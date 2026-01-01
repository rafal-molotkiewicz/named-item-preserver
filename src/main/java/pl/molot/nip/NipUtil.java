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
    public static String getBlockPos(ItemEntity entity) {
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
                // Replace underscores and capitalize words
                String[] parts = path.split("_");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].isEmpty()) continue;
                    String p = parts[i];
                    sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
                    if (i < parts.length - 1) sb.append(' ');
                }
                return sb.toString();
        }
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
        return "by " + kind + " " + picker.getName().getString();
    }

    /**
     * Format an item for log output: "<name> at x, y, z in Dimension" using logger placeholders.
     */
    public static LogMessage itemLogMessage(ItemEntity entity) {
        if (entity == null) return new LogMessage("unknown");
        return new LogMessage("{} at {} in {}", new Object[]{getDisplayName(entity), getBlockPos(entity), getDimensionName(entity)});
    }

    public static LogMessage pickedUpMessage(ItemEntity entity, Entity picker) {
        if (entity == null) return new LogMessage("unknown");
        return new LogMessage("{} {} at {} in {}", new Object[]{getDisplayName(entity), getPickerDescriptor(picker), getBlockPos(entity), getDimensionName(entity)});
    }

    public static LogMessage destroyedMessage(ItemEntity entity) {
        if (entity == null) return new LogMessage("unknown");
        return new LogMessage("{} at {} in {}", new Object[]{getDisplayName(entity), getBlockPos(entity), getDimensionName(entity)});
    }

    /** Backwards-compatible textual formatter returning single string */
    public static String formatItemLog(ItemEntity entity) {
        if (entity == null) return "unknown";
        return getDisplayName(entity) + " at " + getBlockPos(entity) + " in " + getDimensionName(entity);
    }

    public static final class LogMessage {
        public final String format;
        public final Object[] args;

        public LogMessage(String format, Object[] args) {
            this.format = format;
            this.args = args;
        }

        public LogMessage(String single) {
            this.format = single;
            this.args = new Object[0];
        }

        @Override
        public String toString() {
            return format;
        }
    }
}
