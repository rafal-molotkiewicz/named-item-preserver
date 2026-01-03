// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip;

/**
 * Per-ItemEntity state used to avoid duplicate disappearance logs.
 *
 * Implemented via mixin onto ItemEntity.
 */
public interface ItemEntityRemovalLogState {
    boolean nip$wasRemovalLogged();

    void nip$markRemovalLogged();

    boolean nip$hasSpecificRemovalPending();

    void nip$setSpecificRemovalPending(boolean pending);
}
