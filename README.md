# title2025

<!-- Plugin description -->
Reflects OSC 0/1/2 window titles in terminal tab names for IntelliJ IDEA's Reworked terminal.
<!-- Plugin description end -->

This is a workaround for [IJPL-197056](https://youtrack.jetbrains.com/issue/IJPL-197056) and [IJPL-221050](https://youtrack.jetbrains.com/issue/IJPL-221050).

> [!NOTE]
> This plugin relies on internal implementation details of the IntelliJ Terminal due to a lack of public APIs.
> They may change without notice, so this plugin can break even in minor IDE updates.

## Requirements

- IntelliJ IDEA 2025.3.1 or later (or compatible IDE)

## Installation

1. Download ZIP from [Releases](https://github.com/hirofumi/title2025/releases)
2. Settings → Plugins → ⚙️ → Install Plugin from Disk...

## Usage

The plugin works automatically when:

- Terminal engine is set to "Reworked 2025"
- `terminal.show.application.title` is enabled in Advanced Settings

Any terminal program that sets the window title will update the tab name.

## Development

### Requirements

- JDK 21

### Commands

```bash
./gradlew build        # Build
./gradlew buildPlugin  # Build plugin ZIP (build/distributions/)
./gradlew runIde       # Run in sandbox IDE
./gradlew test         # Test
```
