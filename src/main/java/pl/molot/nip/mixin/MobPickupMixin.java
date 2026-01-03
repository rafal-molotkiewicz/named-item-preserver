// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import pl.molot.nip.NamedItemPreserver;
import pl.molot.nip.ItemEntityRemovalLogState;
import pl.molot.nip.NipUtil;

@Mixin(MobEntity.class)
public abstract class MobPickupMixin {

    @WrapOperation(
        method = "tickMovement",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/mob/MobEntity;loot(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/ItemEntity;)V"
        )
    )
    private void nip$wrapMobLootCall(MobEntity self, ServerWorld world, ItemEntity itemEntity, Operation<Void> original) {

        ItemStack before = itemEntity.getStack().copy();
        boolean wasNamed = NipUtil.isNamedItem(before);

        ItemEntityRemovalLogState logState = (Object) itemEntity instanceof ItemEntityRemovalLogState s ? s : null;
        if (wasNamed && logState != null) {
            logState.nip$setSpecificRemovalPending(true);
        }

        original.call(self, world, itemEntity);

        if (wasNamed && itemEntity.isRemoved() && !(logState != null && logState.nip$wasRemovalLogged())) {
            NamedItemPreserver.LOGGER.info(NipUtil.pickedUpMessage(before, self, self));
            if (logState != null) logState.nip$markRemovalLogged();
        }

        if (logState != null) logState.nip$setSpecificRemovalPending(false);
    }
}
