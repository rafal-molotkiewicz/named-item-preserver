// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.entity.ItemEntity;
import pl.molot.nip.NamedItemPreserver;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEntity.class)
public abstract class ItemEntityDespawnMixin {
    /**
     * Vanilla despawns item entities by calling discard() inside ItemEntity#tick().
     * We redirect that call and skip it if the ItemStack has a custom name.
     */
    @Redirect(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;discard()V")
    )
    private void nip$preventNamedItemDespawn(ItemEntity self) {
        // ItemStack custom name is what you care about.
        if (self.getStack().getCustomName() != null) {
            // Make it "fresh" again so vanilla won't try every tick.
            ((ItemEntityAgeAccessor)(Object) self).nip$setItemAge(0);
            NamedItemPreserver.LOGGER.info("Prevented despawn of {}", self.getStack().getName().getString());
            return; // prevent despawn
        }
        self.discard(); // normal behavior
    }
}
