// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import pl.molot.nip.ItemEntityRemovalLogState;

@Mixin(ItemEntity.class)
public abstract class ItemEntityRemovalStateMixin implements ItemEntityRemovalLogState {

    @Unique
    private boolean nip$removalLogged;

    @Unique
    private boolean nip$specificRemovalPending;

    @Override
    public boolean nip$wasRemovalLogged() {
        return this.nip$removalLogged;
    }

    @Override
    public void nip$markRemovalLogged() {
        this.nip$removalLogged = true;
    }

    @Override
    public boolean nip$hasSpecificRemovalPending() {
        return this.nip$specificRemovalPending;
    }

    @Override
    public void nip$setSpecificRemovalPending(boolean pending) {
        this.nip$specificRemovalPending = pending;
    }
}
