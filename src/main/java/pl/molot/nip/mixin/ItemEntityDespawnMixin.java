// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.entity.ItemEntity;
import pl.molot.nip.NamedItemPreserver;
import pl.molot.nip.NipUtil;
import pl.molot.nip.NipItemEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(ItemEntity.class)
public abstract class ItemEntityDespawnMixin {
    /**
     * Vanilla despawns item entities by calling discard() inside ItemEntity#tick().
     * We redirect that call and skip it if the ItemStack has a custom name.
     */
    @WrapOperation(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;discard()V")
    )
    private void nip$preventNamedItemDespawn(ItemEntity self, Operation<Void> original) {
        // ItemStack custom name is what you care about.
        if (NipUtil.isNamedItem(self.getStack())) {
            // Reset age so vanilla won't try every tick.
            NipItemEntity.resetDespawnAge(self);
            NamedItemPreserver.LOGGER.debug("Prevented despawn of {}", NipUtil.getDisplayName(self.getStack()));
            return; // prevent despawn
        }
        
        original.call(self); // normal behavior
    }
}
