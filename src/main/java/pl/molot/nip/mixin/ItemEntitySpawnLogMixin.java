// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.molot.nip.NamedItemPreserver;
import pl.molot.nip.ItemEntityRemovalLogState;
import pl.molot.nip.NipUtil;

/**
 * Logs when an ItemEntity is created with an initial stack (typical "drop" path).
 * This avoids logging on chunk load, because chunk-loaded ItemEntity instances are
 * created via the (EntityType, World) constructor and populated from NBT.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntitySpawnLogMixin {

    @Inject(
        method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V",
        at = @At("TAIL")
    )
    private void nip$logNamedItemSpawn(World world, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        if (NipUtil.isNamedItem(stack)) {
            ItemEntity self = (ItemEntity) (Object) this;
            NamedItemPreserver.LOGGER.info(NipUtil.droppedMessage(stack, self));
            if ((Object) self instanceof ItemEntityRemovalLogState logState) {
                // Prevent immediate duplicates if something removes it the same tick.
                // (pickup/damage hooks will override with more specific logs if needed)
                // We do NOT mark removal logged here, as it isn't a removal.
                logState.nip$setSpecificRemovalPending(false);
            }
        }
    }
}
