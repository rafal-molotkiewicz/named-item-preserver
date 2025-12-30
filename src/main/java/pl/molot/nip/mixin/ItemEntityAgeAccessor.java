// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
public interface ItemEntityAgeAccessor {
    @Accessor(value = "itemAge", remap = true) 
    void nip$setItemAge(int value);

    @Accessor(value = "itemAge", remap = true) // optional, helps logging
    int nip$getItemAge();
}
