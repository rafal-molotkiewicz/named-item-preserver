// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import pl.molot.nip.NamedItemPreserver;
import pl.molot.nip.ItemEntityRemovalLogState;
import pl.molot.nip.NipUtil;

@Mixin(Mob.class)
public abstract class MobPickupMixin {

    @WrapOperation(
        method = "aiStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Mob;pickUpItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/item/ItemEntity;)V"
        )
    )
    private void nip$wrapMobLootCall(Mob self, ServerLevel world, ItemEntity itemEntity, Operation<Void> original) {

        ItemStack before = itemEntity.getItem().copy();
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
