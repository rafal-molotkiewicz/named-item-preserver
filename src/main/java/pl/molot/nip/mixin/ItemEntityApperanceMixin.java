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
            self.setCustomName(stack.getCustomName());
            self.setCustomNameVisible(true);
            NamedItemPreserver.LOGGER.debug("Enabled name display for {}", NipUtil.getDisplayName(stack));
        }
        
        // Log at NORMAL level when a named item appears in the world
        if (NipUtil.isNamedItem(stack)) {
            NamedItemPreserver.LOGGER.info(NipUtil.droppedMessage(stack, self));
        }
    }
}
