// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.molot.nip.NamedItemPreserver;
import pl.molot.nip.ItemEntityRemovalLogState;
import pl.molot.nip.NipUtil;

@Mixin(ItemEntity.class)
public abstract class ItemEntityDestroyMixin {

    @Unique
    private ItemStack nip$damageStackSnapshot;

    @Unique
    private boolean nip$damageWasNamed;

    @Inject(
        method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At("HEAD")
    )
    private void nip$snapshotBeforeDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack current = self.getItem();
        this.nip$damageStackSnapshot = current == null ? null : current.copy();
        this.nip$damageWasNamed = this.nip$damageStackSnapshot != null && NipUtil.isNamedItem(this.nip$damageStackSnapshot);

        if (this.nip$damageWasNamed && (Object) self instanceof ItemEntityRemovalLogState logState) {
            logState.nip$setSpecificRemovalPending(true);
        }
    }

    @Inject(
        method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At("TAIL")
    )
    private void nip$logNamedItemDestruction(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity) (Object) this;

        if (this.nip$damageWasNamed && self.isRemoved()
            && !((Object) self instanceof ItemEntityRemovalLogState logState && logState.nip$wasRemovalLogged())) {
            NamedItemPreserver.LOGGER.info(NipUtil.destroyedMessage(this.nip$damageStackSnapshot, source, self));
            if ((Object) self instanceof ItemEntityRemovalLogState logState) {
                logState.nip$markRemovalLogged();
            }
        }

        if ((Object) self instanceof ItemEntityRemovalLogState logState) {
            logState.nip$setSpecificRemovalPending(false);
        }

        this.nip$damageStackSnapshot = null;
        this.nip$damageWasNamed = false;
    }
}
