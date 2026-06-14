# Shared Sources

This directory contains Java sources and resources shared by
the Forge 1.20.1 and NeoForge 1.21.1 targets.

It is not an independent Gradle subproject.

The shared sources are compiled separately by each target
against that target's Minecraft and loader APIs.

Version-sensitive classes and resources must be placed in
the corresponding target directory.

Examples of version-sensitive content include:

- Minecraft methods whose signatures differ between versions
- Forge or NeoForge loader APIs
- mixins
- mod metadata
- recipes whose directory names or JSON formats differ
- tags whose directory names differ
- data components and NBT implementations
- client rendering APIs that changed between Minecraft versions

Do not claim that common can be built independently.
