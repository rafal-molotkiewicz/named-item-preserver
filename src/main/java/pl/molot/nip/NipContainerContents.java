// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utilities for container-like items (e.g. shulker boxes, bundles).
 */
public final class NipContainerContents {
    private NipContainerContents() {}

    /** True if this stack contains any named (custom-name) items in supported container components. */
    public static boolean hasNamedContents(ItemStack containerStack) {
        if (containerStack == null || containerStack.isEmpty()) return false;
        for (ItemStack nested : iterateNamedContentsCopy(containerStack)) {
            if (nested != null && !nested.isEmpty()) return true;
        }
        return false;
    }

    /**
     * Spill named contents out of an unnamed container stack and strip those named items from the container
     * so that later dropping the container does not duplicate them.
     *
     * If the container stack itself is named, does nothing (caller should preserve the container).
     */
    public static void spillAndStripNamedContents(ItemStack containerStack, Consumer<ItemStack> dropper) {
        if (containerStack == null || containerStack.isEmpty()) return;
        if (dropper == null) return;

        // Preserve named containers as-is.
        if (NipUtil.isNamedItem(containerStack)) return;

        boolean changed = false;

        // Generic container component.
        ItemContainerContents container = containerStack.get(DataComponents.CONTAINER);
        if (container != null && !container.equals(ItemContainerContents.EMPTY)) {
            List<ItemStack> all = container.allItemsCopyStream().toList();
            List<ItemStack> mutated = new ArrayList<>(all.size());
            for (ItemStack nested : all) {
                if (NipUtil.isNamedItem(nested)) {
                    dropper.accept(nested);
                    mutated.add(ItemStack.EMPTY);
                    changed = true;
                } else {
                    mutated.add(nested);
                }
            }
            if (changed) {
                containerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(mutated));
            }
        }

        // Bundle component.
        BundleContents bundle = containerStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null && !bundle.isEmpty()) {
            List<ItemStackTemplate> kept = new ArrayList<>();
            boolean bundleChanged = false;
            for (ItemStackTemplate template : bundle.items()) {
                ItemStack nested = template.create();
                if (nested == null || nested.isEmpty()) continue;
                if (NipUtil.isNamedItem(nested)) {
                    dropper.accept(nested.copy());
                    bundleChanged = true;
                } else {
                    kept.add(template);
                }
            }
            if (bundleChanged) {
                containerStack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(kept));
            }
        }
    }

    /**
     * Returns copies of all named (custom-name) item stacks contained within the provided container stack.
     * Supports the generic {@link DataComponents#CONTAINER} and {@link DataComponents#BUNDLE_CONTENTS}
     * components.
     */
    public static Iterable<ItemStack> iterateNamedContentsCopy(ItemStack containerStack) {
        if (containerStack == null || containerStack.isEmpty()) return List.of();

        List<ItemStack> result = new ArrayList<>();

        ItemContainerContents container = containerStack.get(DataComponents.CONTAINER);
        if (container != null && !container.equals(ItemContainerContents.EMPTY)) {
            container.nonEmptyItemCopyStream().forEach(nested -> {
                if (NipUtil.isNamedItem(nested)) {
                    result.add(nested);
                }
            });
        }

        BundleContents bundle = containerStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null && !bundle.isEmpty()) {
            for (ItemStackTemplate template : bundle.items()) {
                ItemStack nested = template.create();
                if (nested == null || nested.isEmpty()) continue;
                if (NipUtil.isNamedItem(nested)) {
                    result.add(nested);
                }
            }
        }

        return result;
    }

    /**
     * Drops all named (custom-name) item stacks contained within the provided item entity's stack.
     *
     * This strips the named stacks from the container (for unnamed containers) to avoid duplication
     * if some other code later also drops/reads the container contents.
     */
    public static void dropNamedContents(ServerLevel world, ItemEntity containerEntity) {
        if (world == null || containerEntity == null) return;

        ItemStack containerStack = containerEntity.getItem();
        if (containerStack == null || containerStack.isEmpty()) return;

        spillAndStripNamedContents(
            containerStack,
            named -> {
                if (named == null || named.isEmpty()) return;
                containerEntity.spawnAtLocation(world, named.copy());
            }
        );
    }
}
