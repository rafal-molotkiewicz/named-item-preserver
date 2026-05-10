// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.molot.nip.NamedItemPreserver;
import pl.molot.nip.ItemEntityRemovalLogState;
import pl.molot.nip.NipUtil;
import pl.molot.nip.config.ConfigManager;

import java.util.Objects;

@Mixin(Entity.class)
public abstract class EntityDimensionChangeMixin {

    @Unique
    private ItemStack nip$dimStackSnapshot;

    @Unique
    private boolean nip$dimWasNamed;

    @Unique
    private ServerLevel nip$fromWorldSnapshot;

    @Unique
    private BlockPos nip$fromPosSnapshot;

    @Inject(
        method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
        at = @At("HEAD")
    )
    private void nip$snapshotBeforeCrossDimension(ServerLevel fromWorld, ServerLevel toWorld, TeleportTransition target, CallbackInfoReturnable<Entity> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof ItemEntity itemEntity)) {
            return;
        }

        ItemStack current = itemEntity.getItem();
        this.nip$dimStackSnapshot = current == null ? null : current.copy();
        this.nip$dimWasNamed = this.nip$dimStackSnapshot != null && NipUtil.isNamedItem(this.nip$dimStackSnapshot);

        if (this.nip$dimWasNamed) {
            this.nip$fromWorldSnapshot = fromWorld;
            this.nip$fromPosSnapshot = itemEntity.blockPosition();
        } else {
            this.nip$fromWorldSnapshot = null;
            this.nip$fromPosSnapshot = null;
        }
    }

    @Inject(
        method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
        at = @At("RETURN")
    )
    private void nip$logNamedItemCrossDimension(ServerLevel fromWorld, ServerLevel toWorld, TeleportTransition target, CallbackInfoReturnable<Entity> cir) {
        if (!this.nip$dimWasNamed) {
            this.nip$dimStackSnapshot = null;
            return;
        }

        Entity self = (Entity) (Object) this;

        Entity result = cir.getReturnValue();

        BlockPos toPos = result != null ? result.blockPosition() : null;

        if (result instanceof ItemEntity toItem && ConfigManager.get().displayItemName) {
            // Ensure the destination entity keeps showing the name if that setting is enabled.
            // This is idempotent and only re-applies if required.
            if (NipUtil.isNamedItem(this.nip$dimStackSnapshot) && !Objects.equals(toItem.getCustomName(), this.nip$dimStackSnapshot.getCustomName())) {
                toItem.setCustomName(this.nip$dimStackSnapshot.getCustomName());
            }
            if (NipUtil.isNamedItem(this.nip$dimStackSnapshot) && !toItem.isCustomNameVisible()) {
                toItem.setCustomNameVisible(true);
            }
        }

        NamedItemPreserver.LOGGER.info(
            NipUtil.transcendedMessage(
                this.nip$dimStackSnapshot,
                this.nip$fromWorldSnapshot != null ? this.nip$fromWorldSnapshot : fromWorld,
                this.nip$fromPosSnapshot,
                result != null ? result.level() : toWorld,
                toPos
            )
        );

        this.nip$dimStackSnapshot = null;
        this.nip$dimWasNamed = false;
        this.nip$fromWorldSnapshot = null;
        this.nip$fromPosSnapshot = null;
        
        if (self instanceof ItemEntity fromItem && (Object) fromItem instanceof ItemEntityRemovalLogState logState) {
            // The source entity is removed with CHANGED_DIMENSION; keep our removal-state consistent.
            // This does not affect the destination entity's logging.
            logState.nip$markRemovalLogged();
        }
    }
}
