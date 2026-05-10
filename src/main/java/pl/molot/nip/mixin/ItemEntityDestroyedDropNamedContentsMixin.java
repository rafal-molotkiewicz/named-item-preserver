// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import pl.molot.nip.NipContainerContents;
import pl.molot.nip.config.ConfigManager;

/**
 * When an ItemEntity is destroyed (damage path) and vanilla is about to discard it,
 * spill any *named* contents (shulker boxes, bundles, and other container-component items).
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityDestroyedDropNamedContentsMixin {

    @WrapOperation(
        method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V")
    )
    private void nip$dropNamedContentsBeforeDiscard(ItemEntity self, Operation<Void> original, ServerLevel world, DamageSource source, float amount) {
        if (world != null && ConfigManager.get().spillNamedContentsFromDestroyedContainerEntities) {
            NipContainerContents.dropNamedContents(world, self);
        }
        original.call(self);
    }
}
