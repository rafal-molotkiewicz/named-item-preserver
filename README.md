# Named Item Preserver

This mod changes how **named (custom-name/anvil-name) items** behave and adds **logging/broadcasting** around named item lifecycle events.

## What it does

### Prevents despawn of named ItemEntities
- If an item entity is about to despawn in `ItemEntity#tick()`, and its `ItemStack` has a custom name, the mod prevents the despawn by resetting the internal despawn age.

### Spills named contents from container-like items when the container would disappear
The mod inspects container-like item stacks using data components:
- `DataComponentTypes.CONTAINER` (e.g. shulker-box-like container items)
- `DataComponentTypes.BUNDLE_CONTENTS` (bundles)

When an **unnamed** container item entity is about to despawn, the mod spawns (drops) any **named items inside it** as separate item entities before the container despawns.

When an item entity is **destroyed via the damage path** (`ItemEntity#damage(...)`), the same “spill named contents” behavior happens **only if enabled in config** (`spillNamedContentsFromDestroyedContainerEntities`).

Important: for **unnamed** containers, the mod removes (strips) the named items from the container stack after dropping them, to avoid duplication if other mods also process that container later.

### Drops named items from mobs that despawn
When a mob is removed by vanilla despawn logic (`MobEntity#checkDespawn()`), the mod drops:
- Any **named equipped items** (hands + armor) and clears those slots.
- Any **named stacks** found in an entity inventory (if the entity implements `Inventory`, or exposes a `getInventory()` method returning `Inventory`) and clears those slots.

Additionally, for non-named stacks in those equipment/inventory sources, the mod spawns any **named items contained inside** container-like items (container/bundle components).

For **unnamed** containers found in mob equipment/inventories, the mod also strips those named contents out of the container stack after dropping them (to reduce duplication if other mods also drop that inventory later).

## Logging / broadcasting
The mod emits messages for **named items**:
- When spawned via the `ItemEntity(World, x, y, z, stack)` constructor (typical “dropped item” creation).
- When picked up by a player (`ItemEntity#onPlayerCollision`).
- When picked up by a mob (`MobEntity#loot(...)`).
- When destroyed via damage (`ItemEntity#damage(...)`).
- When removed for other removal reasons (`Entity#setRemoved(...)`), excluding unload-to-chunk/unload-with-player/changed-dimension.
- When an item entity changes dimension (`Entity#teleportCrossDimension(...)`) (“transcended from … to …”).

Messages include the item type + custom name, coordinates, and dimension name.

Depending on config, these messages can also be broadcast into in-game chat:
- `broadcastTo = None | OP | All`

## Optional item nameplates
If enabled (`displayItemName = true`), when an `ItemEntity` stack is set:
- If the stack is named, the item entity is forced to show the custom name above it.
- On cross-dimension teleport, the destination item entity is also forced to show the name.

## Configuration
Config is loaded from Fabric’s config directory as `named-item-preserver.json`.

Fields (as implemented):
- `verbosity`: `IMPORTANT` / `NORMAL` / `DEBUG` (controls what the mod logs)
- `displayItemName`: `true`/`false`
- `broadcastTo`: `None` / `OP` / `All`
- `spillNamedContentsFromDestroyedContainerEntities`: `true`/`false` (gates spilling named contents when an item entity is destroyed via damage)

## Client-side code
The project includes a client entrypoint, but it currently do not implement any client behavior.
