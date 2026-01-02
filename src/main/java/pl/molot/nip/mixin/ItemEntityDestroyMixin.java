// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.molot.nip.NamedItemPreserver;
import pl.molot.nip.NipUtil;

@Mixin(ItemEntity.class)
public abstract class ItemEntityDestroyMixin {

    @Unique
    private ItemStack nip$damageStackSnapshot;

    @Unique
    private boolean nip$damageWasNamed;

    @Inject(
        method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
        at = @At("HEAD")
    )
    private void nip$snapshotBeforeDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack current = self.getStack();
        this.nip$damageStackSnapshot = current == null ? null : current.copy();
        this.nip$damageWasNamed = this.nip$damageStackSnapshot != null && NipUtil.isNamedItem(this.nip$damageStackSnapshot);
    }

    @Inject(
        method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
        at = @At("TAIL")
    )
    private void nip$logNamedItemDestruction(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity) (Object) this;

        if (this.nip$damageWasNamed && self.isRemoved()) {
            NamedItemPreserver.LOGGER.info(NipUtil.destroyedMessage(this.nip$damageStackSnapshot, source, self));
        }

        this.nip$damageStackSnapshot = null;
        this.nip$damageWasNamed = false;
    }
}
