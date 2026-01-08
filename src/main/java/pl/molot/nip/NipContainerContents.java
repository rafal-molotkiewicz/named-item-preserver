// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

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
        ContainerComponent container = containerStack.get(DataComponentTypes.CONTAINER);
        if (container != null && container != ContainerComponent.DEFAULT) {
            List<ItemStack> all = container.stream().map(s -> s == null ? ItemStack.EMPTY : s.copy()).toList();
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
                containerStack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(mutated));
            }
        }

        // Bundle component.
        BundleContentsComponent bundle = containerStack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundle != null && !bundle.isEmpty()) {
            List<ItemStack> kept = new ArrayList<>();
            boolean bundleChanged = false;
            for (ItemStack nested : bundle.iterateCopy()) {
                if (nested == null || nested.isEmpty()) continue;
                if (NipUtil.isNamedItem(nested)) {
                    dropper.accept(nested);
                    bundleChanged = true;
                } else {
                    kept.add(nested);
                }
            }
            if (bundleChanged) {
                containerStack.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(kept));
            }
        }
    }

    /**
     * Returns copies of all named (custom-name) item stacks contained within the provided container stack.
     * Supports the generic {@link DataComponentTypes#CONTAINER} and {@link DataComponentTypes#BUNDLE_CONTENTS}
     * components.
     */
    public static Iterable<ItemStack> iterateNamedContentsCopy(ItemStack containerStack) {
        if (containerStack == null || containerStack.isEmpty()) return List.of();

        List<ItemStack> result = new ArrayList<>();

        ContainerComponent container = containerStack.get(DataComponentTypes.CONTAINER);
        if (container != null && container != ContainerComponent.DEFAULT) {
            for (ItemStack nested : container.iterateNonEmptyCopy()) {
                if (NipUtil.isNamedItem(nested)) {
                    result.add(nested);
                }
            }
        }

        BundleContentsComponent bundle = containerStack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundle != null && !bundle.isEmpty()) {
            for (ItemStack nested : bundle.iterateCopy()) {
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
    public static void dropNamedContents(ServerWorld world, ItemEntity containerEntity) {
        if (world == null || containerEntity == null) return;

        ItemStack containerStack = containerEntity.getStack();
        if (containerStack == null || containerStack.isEmpty()) return;

        spillAndStripNamedContents(
            containerStack,
            named -> {
                if (named == null || named.isEmpty()) return;
                containerEntity.dropStack(world, named.copy());
            }
        );
    }
}
