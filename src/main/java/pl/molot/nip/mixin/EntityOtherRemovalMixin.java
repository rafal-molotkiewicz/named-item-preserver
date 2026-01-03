// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.molot.nip.NamedItemPreserver;
import pl.molot.nip.ItemEntityRemovalLogState;
import pl.molot.nip.NipUtil;

@Mixin(Entity.class)
public abstract class EntityOtherRemovalMixin {

    @Unique
    private ItemStack nip$removeStackSnapshot;

    @Unique
    private boolean nip$removeWasNamed;

    @Inject(
        method = "setRemoved(Lnet/minecraft/entity/Entity$RemovalReason;)V",
        at = @At("HEAD")
    )
    private void nip$snapshotBeforeRemoval(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof ItemEntity itemEntity)) {
            return;
        }

        if (!((Object) itemEntity instanceof ItemEntityRemovalLogState logState)) {
            return;
        }

        if (logState.nip$wasRemovalLogged() || logState.nip$hasSpecificRemovalPending()) {
            this.nip$removeStackSnapshot = null;
            this.nip$removeWasNamed = false;
            return;
        }

        ItemStack current = itemEntity.getStack();
        this.nip$removeStackSnapshot = current == null ? null : current.copy();
        this.nip$removeWasNamed = this.nip$removeStackSnapshot != null && NipUtil.isNamedItem(this.nip$removeStackSnapshot);
    }

    @Inject(
        method = "setRemoved(Lnet/minecraft/entity/Entity$RemovalReason;)V",
        at = @At("TAIL")
    )
    private void nip$logOtherRemoval(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof ItemEntity itemEntity)) {
            return;
        }

        if (reason == Entity.RemovalReason.UNLOADED_TO_CHUNK
            || reason == Entity.RemovalReason.UNLOADED_WITH_PLAYER
            || reason == Entity.RemovalReason.CHANGED_DIMENSION) {
            return;
        }

        if (!((Object) itemEntity instanceof ItemEntityRemovalLogState logState)) {
            return;
        }

        if (!this.nip$removeWasNamed || logState.nip$wasRemovalLogged() || logState.nip$hasSpecificRemovalPending()) {
            this.nip$removeStackSnapshot = null;
            this.nip$removeWasNamed = false;
            return;
        }

        NamedItemPreserver.LOGGER.info(NipUtil.removedMessage(this.nip$removeStackSnapshot, reason, itemEntity));
        logState.nip$markRemovalLogged();

        this.nip$removeStackSnapshot = null;
        this.nip$removeWasNamed = false;
    }
}
