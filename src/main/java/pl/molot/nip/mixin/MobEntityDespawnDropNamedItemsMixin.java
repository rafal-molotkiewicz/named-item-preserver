// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import pl.molot.nip.NipEntityHeldItems;

/**
 * When a mob is despawned by vanilla despawn logic, drop any named equipment it carries.
 * This is intentionally scoped to the despawn path (MobEntity#checkDespawn), so it does
 * not trigger for CHANGED_DIMENSION, chunk unload, etc.
 */
@Mixin(MobEntity.class)
public abstract class MobEntityDespawnDropNamedItemsMixin {

    @WrapOperation(
        method = "checkDespawn",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;discard()V")
    )
    private void nip$dropNamedItemsOnDespawn(MobEntity self, Operation<Void> original) {
        if (self.getEntityWorld() instanceof ServerWorld serverWorld) {
            NipEntityHeldItems.dropPreservedItemsOnDespawn(serverWorld, self);
        }
        original.call(self);
    }
}
