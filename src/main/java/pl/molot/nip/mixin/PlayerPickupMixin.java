// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.molot.nip.NamedItemPreserver;
import pl.molot.nip.ItemEntityRemovalLogState;
import pl.molot.nip.NipUtil;

@Mixin(ItemEntity.class)
public abstract class PlayerPickupMixin {

    @Unique
    private ItemStack nip$pickupStackSnapshot;

    @Unique
    private boolean nip$pickupWasNamed;

    @Inject(
        method = "onPlayerCollision",
        at = @At("HEAD")
    )
    private void nip$snapshotBeforePlayerPickup(PlayerEntity player, CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack current = self.getStack();
        this.nip$pickupStackSnapshot = current == null ? null : current.copy();
        this.nip$pickupWasNamed = this.nip$pickupStackSnapshot != null && NipUtil.isNamedItem(this.nip$pickupStackSnapshot);

        if (this.nip$pickupWasNamed && (Object) self instanceof ItemEntityRemovalLogState logState) {
            logState.nip$setSpecificRemovalPending(true);
        }
    }

    /**
     * Log when a player picks up a named item.
     */
    @Inject(
        method = "onPlayerCollision",
        at = @At("TAIL"),
        cancellable = false
    )
    private void nip$logNamedItemPickup(PlayerEntity player, CallbackInfo ci) {
        ItemEntity self = (ItemEntity)(Object)this;
        
        // Only log when the item was actually removed (picked up) to avoid duplicate messages
        if (this.nip$pickupWasNamed && self.isRemoved()
            && !((Object) self instanceof ItemEntityRemovalLogState logState && logState.nip$wasRemovalLogged())) {
            NamedItemPreserver.LOGGER.info(NipUtil.pickedUpMessage(this.nip$pickupStackSnapshot, player, self));
            if ((Object) self instanceof ItemEntityRemovalLogState logState) {
                logState.nip$markRemovalLogged();
            }
        }

        if ((Object) self instanceof ItemEntityRemovalLogState logState) {
            logState.nip$setSpecificRemovalPending(false);
        }

        this.nip$pickupStackSnapshot = null;
        this.nip$pickupWasNamed = false;
    }
}
