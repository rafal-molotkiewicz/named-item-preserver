# Versioning and Release Strategy

## Version Format

This project uses semantic versioning with Minecraft version suffix:

```
v{MOD_VERSION}+mc{MINECRAFT_VERSION}
```

Example: `v0.1.1+mc1.21.11`

## Tagging Strategy

- **Mod Version** (e.g., `0.1.1`): Standard semantic versioning for the mod itself
  - Major: Breaking changes
  - Minor: New features, backward compatible
  - Patch: Bug fixes, minor improvements

- **Minecraft Version Suffix** (e.g., `+mc1.21.11`): Indicates target Minecraft version
  - Allows multiple releases of the same mod version for different Minecraft versions
  - Example: `v0.1.1+mc1.21.11` and `v0.1.1+mc1.21.10` can coexist

## Release Process

1. Update `mod_version` in `gradle.properties`
2. Update version references in `fabric.mod.json` description
3. Commit changes with descriptive message
4. Create annotated tag: `git tag -a v{VERSION}+mc{MC_VERSION} -m "Release message"`
5. Push: `git push origin main && git push origin v{VERSION}+mc{MC_VERSION}`

## Backporting

To support an older Minecraft version:
1. Create a branch: `git checkout -b mc1.21.10`
2. Make necessary API adjustments
3. Update versions in gradle.properties and fabric.mod.json
4. Tag with appropriate suffix: `v0.1.1+mc1.21.10`

## Current Versions

- **Mod Version**: 0.1.1
- **Minecraft**: 1.21.11
- **Fabric API**: 0.141.1+1.21.11
- **Yarn Mappings**: 1.21.11+build.1
- **Fabric Loader**: 0.18.4

---

**Note for AI Agent**: This file documents the project's versioning strategy. Always refer to this when creating releases or managing versions. The `+mc{VERSION}` suffix is critical for supporting multiple Minecraft versions.
