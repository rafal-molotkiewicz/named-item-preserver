// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip;

import net.minecraft.entity.ItemEntity;

public final class NipUtil {
    private NipUtil() {}

    /** "Named item" means: item stack has a custom name (anvil name). */
    public static boolean isNamedItem(ItemEntity e) {
        return e.getStack().getCustomName() != null;
    }
}
