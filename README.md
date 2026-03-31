# AstraAH

A feature-rich, packet-based Auction House plugin for Paper/Folia servers. Players can list items for sale, browse the marketplace through a custom GUI, and purchase items from other players. Supports Vault or integrated Economy, more planned.

## Implementation Progress

| Version | Tested Versions | Folia | Paper | Database | Vault Economy |
|---------|-----------------|-------|-------|----------|---------------|
| [1.0.0-SNAPSHOT](https://github.com/veyno/AstraAH) | 1.21.4 - 1.21.11 | ✅ | ✅ | ❌ | ✅ |

## Features (Completed / Planned)

- [x] Packet-based custom GUI (no Bukkit inventory flicker)
- [x] Region-based inventory layout (categories, listings, navigation)
- [x] Item listing & purchasing
- [x] Category sidebar with wildcard material filters
- [x] Listing sorting (A–Z, Z–A, price high–low, low–high, price-per-piece)
- [x] Pagination for listings and category filters
- [x] Vault economy integration
- [x] File-based YAML storage for listings
- [x] Transaction history records (data model ready)
- [x] MiniMessage formatting support for all labels
- [x] Multi-language support (en_us, de_de)
- [x] Configurable GUI items and labels via `config.yml`
- [ ] Listing info view (right-click on listing)
- [ ] Create listing menu (item selection → price → confirm)
- [ ] My Listings view
- [ ] Transaction history sidebar (last 4 purchased items)
- [ ] Database storage (JDBC / SQLite / MySQL)
- [ ] AstraEconomy provider integration
- [ ] CoinsEngine provider integration
- [ ] Admin mode: view / delete listings of other players
- [ ] Permission system (open, create, admin)
- [ ] Exclude specific items from being listed (by name/material patterns)
- [ ] PAPI Placeholders
- [ ] Economy commands: `/bal`, `/baltop`, `/pay`
- [ ] Economy tax fully functional

## Commands

| Name | Permission | Description |
|------|------------|-------------|
| `/market` | *(none yet)* | Opens the main Auction House GUI |
| `/bal` | *(planned)* | Check your balance |
| `/baltop` | *(planned)* | View the top player balances |
| `/pay <player> <amount>` | *(planned)* | Send money to another player |

## Permissions

> **Note:** No permission system is implemented yet. All features are currently accessible to all players.

| Name | Default | Description |
|------|---------|-------------|
| `astraah.use` | `true` | Allows the player to open the auction house and purchase items |
| `astraah.sell` | `true` | Allows the player to create listings |
| `astraah.admin` | `op` | Allows access to admin features (view/delete any listing) |

*(Planned — not yet enforced in code)*

## Admin Mode *(Planned)*

The admin mode will allow server operators and permitted staff to manage all player listings:

- View all active listings including the seller's name and listing ID
- Delete any listing regardless of ownership
- Access via a dedicated admin GUI or command flag (e.g. `/market admin`)
- Guarded by the `astraah.admin` permission node

This feature is currently tracked as a TODO and has no implementation yet.

## Configuration

The plugin is configured via `config.yml`. Key sections:

| Section | Description |
|---------|-------------|
| `settings.economy.provider` | Economy backend: `auto`, `vault`, `astraeconomy`, `coinsengine`, `file` |
| `settings.economy.tax_percentage` | Percentage cut taken on each sale (default: `10`) |
| `settings.economy.language` | GUI language: `en_us` or `de_de` |
| `settings.categories` | Enable/disable the category sidebar |
| `categories` | Define category filters with wildcard material patterns (e.g. `*STONE*`) |
| `messages` | All GUI labels, item names, and lore lines (MiniMessage format) |
| `guis` | GUI layout items for each screen |
| `economy.default_balance` | Starting balance for file-based economy (testing only) |

## Dependencies

| Dependency | Required | Purpose |
|------------|----------|---------|
| [Paper](https://papermc.io) 1.21.4+ | ✅ | Server API |
| [PacketEvents](https://github.com/retrooper/packetevents) 2.11+ | ✅ | Packet-based GUI rendering |
| [Vault](https://www.spigotmc.org/resources/vault.34315/) | ❌ (soft) | Economy integration |

## Storage

Currently the plugin stores listings in `plugins/AstraAH/data.yml` using Base64-serialized ItemStacks. JDBC/database support is planned but not yet implemented.

## Building

```bash
mvn clean package
```

The shaded jar will be output to `target/`.

Requires Java 21+.
