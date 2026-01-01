// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip;

import net.minecraft.entity.ItemEntity;
import pl.molot.nip.mixin.ItemEntityAgeAccessor;

public final class NipItemEntity {
    private NipItemEntity() {}

    public static int getDespawnAge(ItemEntity e) {
        return ((ItemEntityAgeAccessor)(Object)e).nip$getItemAge();
    }

    public static void resetDespawnAge(ItemEntity e) {
        ((ItemEntityAgeAccessor)(Object)e).nip$setItemAge(0);
    }
}
