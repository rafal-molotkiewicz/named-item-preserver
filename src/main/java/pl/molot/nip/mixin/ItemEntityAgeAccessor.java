// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
public interface ItemEntityAgeAccessor {
    @Accessor(value = "age", remap = true)
    void nip$setItemAge(int value);

    @Accessor(value = "age", remap = true)
    int nip$getItemAge();
}
