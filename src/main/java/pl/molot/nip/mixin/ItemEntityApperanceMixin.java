// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.molot.nip.NamedItemPreserver;
import pl.molot.nip.NipUtil;
import pl.molot.nip.config.ConfigManager;

import java.util.Objects;

@Mixin(ItemEntity.class)
public abstract class ItemEntityApperanceMixin {
    /**
     * When an ItemEntity's stack is set, if it has a custom name and displayItemName is enabled,
     * make the name visible above the item.
     */
    @Inject(
        method = "setStack",
        at = @At("TAIL")
    )
    private void nip$showNameIfEnabled(ItemStack stack, CallbackInfo ci) {
        ItemEntity self = (ItemEntity)(Object)this;
        
        // Show custom name above item if enabled in config        
        if (ConfigManager.get().displayItemName && NipUtil.isNamedItem(stack)) {
            boolean changed = false;

            if (!Objects.equals(self.getCustomName(), stack.getCustomName())) {
                self.setCustomName(stack.getCustomName());
                changed = true;
            }

            if (!self.isCustomNameVisible()) {
                self.setCustomNameVisible(true);
                changed = true;
            }

            if (changed) {
                NamedItemPreserver.LOGGER.debug("Enabled name display for {}", NipUtil.getDisplayName(stack));
            }
        }
    }
}
