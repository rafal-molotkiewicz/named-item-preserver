// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.molot.nip.NamedItemPreserver;
import pl.molot.nip.NipUtil;

@Mixin(ItemEntity.class)
public abstract class ItemEntityPickupMixin {
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
        if (NipUtil.isNamedItem(self) && self.isRemoved()) {
            NipUtil.LogMessage msg = NipUtil.pickedUpMessage(self, player);
            NamedItemPreserver.LOGGER.info(msg.format, msg.args);
        }
    }
}
