# AstraAH

Packet-based Auction House plugin for Paper/Folia servers.

This repository is currently an alpha/work-in-progress branch: the packet GUI framework, YAML-backed storage, configuration model, and core auction domain classes already exist, but several player-facing flows are still only partially wired or are mid-refactor.

## Current Implementation Status

| Area | Status | Notes |
|------|--------|-------|
| Packet-based inventory framework | Implemented | Custom `ClickableInventory` uses PacketEvents only, without Bukkit inventory GUIs |
| `/market` command | Implemented | Registered in `plugin.yml` and handled by `AuctionHouseCommand` |
| Main market browsing | Partial | Listing region, sorting, category filtering and navigation code exist |
| Purchase backend | Partial | `AuctionHouse#onAttemptPurchase(...)` exists, current UI click handler is still placeholder logic |
| Listing details view | Scaffold only | Config model exists, UI implementation is still a TODO |
| Create listing flow | Scaffold only | GUI config/classes exist for 3 steps, but the flow is not finished |
| My Listings page | Scaffold only | Config exists, complete page flow is not wired yet |
| Settings / preference UI | Partial | Preferences, action overrides and config parsing exist, UI is not finished |
| Transaction history storage | Implemented | Already persisted in `transactions.yml` |
| Quick history filter sidebar | Planned | Mentioned in TODO comments, not implemented yet |
| Economy integration | Partial | Vault provider exists, but runtime setup currently falls back to the unfinished file provider |
| Permissions integration | Partial | Permission nodes and provider abstractions exist, but provider setup is incomplete |
| JDBC storage | Stub | Class exists, methods are empty |
| Language / i18n | Partial | `settings.language` exists, active bundled language resources are not finished |

## What Is Already Built Into The Repository

- Packet-only GUI foundation powered by PacketEvents.
- Core auction domain service with cache refresh, listing lookup, purchase handling, listing removal, per-player preferences, allowed actions, and transaction history handling.
- YAML storage providers for:
  - listings in `data.yml`
  - player preferences in `preferences.yml`
  - per-player action overrides in `actions.yml`
  - transaction history in `transactions.yml`
- Periodic asynchronous YAML saving with backup files under `backup-<name>/backup-<name>.yml`.
- Sort modes for listings:
  - name A-Z
  - name Z-A
  - price high-low
  - price low-high
  - price-per-piece high-low
  - price-per-piece low-high
- Category filters based on wildcard material matching such as `*STONE*`, prefix-only, suffix-only, or `*` / `**` for all materials.
- Config-driven GUI models for:
  - main page
  - create listing step 1
  - create listing step 2
  - create listing step 3
  - my listings
  - settings
  - listing info
- MiniMessage support for configured titles, item names, and lore.
- Configurable GUI item parsing with support for:
  - `material`
  - `amount`
  - `display-name`
  - `lore`
  - `item-model`
  - `unbreakable`
  - `hide-flags`
  - `enchantments`
- Built-in provider abstractions for default permissions, LuckPerms permissions, Vault permissions, Vault economy, file economy, AstraEconomy, and CoinsEngine.

## Built In But Not Fully Wired Yet

- `FileEconomyProvider` exists but currently returns placeholder values and does not provide a working standalone economy yet.
- `VaultEconomyProvider`, `AstraEconomyProvider`, and `CoinsEngineEconomyProvider` exist as provider classes, but provider selection from `config.yml` is not wired up.
- Permission provider abstractions exist, but `AstraAH` does not currently initialize a provider.
- `balCommand`, `balTopCommand`, and `payCommand` exist only as empty shells and are not registered in `plugin.yml`.
- `JDBCDatabaseStorageProvider` exists only as a stub.
- A second page/navigation architecture (`Page`, `PageController`, `MainPage`) has been started and is still unfinished.

## Commands

| Command | Status | Description |
|---------|--------|-------------|
| `/market` | Implemented | Opens the Auction House UI |
| `/bal` | Not registered | Placeholder command class only |
| `/baltop` | Not registered | Placeholder command class only |
| `/pay <player> <amount>` | Not registered | Placeholder command class only |

## Permissions

The current `plugin.yml` already declares a permission tree with default `true` values for these nodes:

- `astraah.actions.categories`
- `astraah.actions.settings`
- `astraah.actions.my_listings`
- `astraah.actions.refresh`
- `astraah.actions.sort`
- `astraah.actions.search`
- `astraah.actions.history`
- `astraah.actions.sub_setting.showadvancedcategories`
- `astraah.actions.sub_setting.showadvancedhistory`
- `astraah.actions.sub_setting.reloadonopen`
- `astraah.actions.sub_setting.defaultfilter`
- `astraah.actions.sub_setting.defaultsort`

Important: permission nodes, action-state storage and permission provider classes exist, but end-to-end runtime permission setup is still incomplete.

## Configuration

The plugin is primarily configured through `src/main/resources/config.yml`.

Important sections:

- `guis.main_page`: title, listing templates, sort option labels, navigation arrows, settings/search/sort/listing buttons.
- `guis.create_listing_1`, `guis.create_listing_2`, `guis.create_listing_3`: prepared configuration for the create-listing flow.
- `guis.my_listings`: prepared configuration for the player listings page.
- `guis.settings`: prepared configuration for the settings page.
- `guis.listing_info`: prepared configuration for the listing details screen.
- `categories.*`: category preview items and wildcard material rules.
- `settings.actions.*`: default availability of categories, settings, my listings, refresh, sort, search, history, and preference controls.
- `settings.defaults.*`: default player layout behavior such as reload-on-open and advanced sidebar modes.
- `settings.economy.*`: provider and tax settings parsed from config, but not fully enforced yet.
- `settings.storage.*`: storage provider settings. `file` is currently the only real implementation.
- `settings.language`: language key setting exists, but the full language resource system is not active yet.
- `settings.categories`: toggle for category support.
- `economy.default_balance`: default starting value intended for the file-based economy.

## Storage

Current YAML storage files:

- `plugins/AstraAH/data.yml`: active listings
- `plugins/AstraAH/preferences.yml`: player UI preferences and custom category entries
- `plugins/AstraAH/actions.yml`: per-player action overrides
- `plugins/AstraAH/transactions.yml`: purchase history

Listings are stored using Base64-serialized Bukkit `ItemStack`s.

## Planned Features From TODO Comments

These features are explicitly mentioned in source-code TODO comments and are not yet complete:

- Exclude specific items from being listed, including name-based matching.
- Finish the listing info screen.
- Add a quick filter section on the right side based on the last 4 purchased items.
- Clicking a quick-history entry should auto-filter by material and sort by the lowest price.
- Create custom filters per player.
- Store additional per-player defaults such as preferred currency, category, and sort type.
- Add configurable default sort type per player.
- Initialize default custom filters for new players and let them edit those filters later.
- Add multi-currency support.
- Add configurable fee/tax models such as base fee plus percentage tax, including optional upfront charging.
- Expose an API.
- Allow other plugins to block opening the Auction House GUI.
- Register AstraAH as an economy provider through Vault when no external economy is present.
- Implement additional economy providers properly and wire provider selection from config.
- Build an admin UI for transaction history, per-player views, and statistics such as total money made.
- Add alerting / threshold features for suspicious or notable money flow.
- Add full placeholder support for listing display templates.

## Technical TODO / Known Gaps

The codebase also contains non-user-facing TODOs that are still open:

- Clean up the single-listing cache update path.
- Extract button-layout helper methods in the auction layout logic.
- Improve the internal item parser implementation.
- Finish the ongoing page/UI refactor.

## Dependencies

| Dependency | Required | Purpose |
|------------|----------|---------|
| Paper API `1.21.11-R0.1-SNAPSHOT` | Yes | Server API target |
| PacketEvents `2.11.2` | Yes | Packet-based GUI rendering |
| Vault API | Soft dependency | Economy integration groundwork |
| LuckPerms API | Optional groundwork | Future permissions integration |

`plugin.yml` marks the plugin as `folia-supported: true`.

## Building

```bash
mvn clean package
```

Requires Java 21+.
