# Granite Tracker

A RuneLite plugin for tracking granite ore distribution and mining statistics granite mining, with the intention of uncovering all the RNG layers of competitive 3T4G.

---

## Features

- Tracks attempts, successes, and failures
- Tracks bonus ore procs from Varrock Armour, Celestial Ring, and Mining Cape
- Tracks granite distribution (500g / 2kg / 5kg) with percentages
- Two in-game overlays, individually toggleable from the panel or config
- Profile system — save, load, and delete sessions for comparison
- Reset button to clear current session statistics

---

## Overlays

### General Statistics
Displays attempts, successes, failures, and bonus ore proc counts (Varrock Armour, Celestial Ring, Mining Cape) as percentages of total successes.

### Granite Distribution
Displays how many of each granite type (500g / 2kg / 5kg) you have mined, with percentages of total ores.
Tracks via XP gained + your accounts total mined ore count, to make it work accurately with Infernal Pickaxe.

---

## Panel

Accessible via the sidebar icon. From here you can:

- **Save** the current session under a named profile
- **Load** a previously saved profile
- **Delete** a saved profile
- **Toggle** each overlay on/off
- **Reset** all statistics for the current session

---

## Technical Notes

- Ore counts are tracked via **VarPlayer 4526** rather than inventory, which means the Infernal Pickaxe is fully supported (ores consumed before entering inventory are still counted).
- Granite XP resolution uses a range-based algorithm to handle OSRS's internal fractional XP rounding. Each ore type has a valid XP range: 500g = 50–52, 2kg = 60–62, 5kg = 75–77. (The low-end is assuming no Prospector's)
- In rare edge cases (3 ores mined in the same tick with specific XP values), the distribution is ambiguous and the tick goes unresolved. This is negligibly rare in practice.

---

## Known Limitations

- [ ] No screenshot / export of statistics
- [ ] Replace the generated panel icon with a proper sprite

---

## Author

<!-- OSRS: CrownedFoxxo / Discord: CrownedFoxxy / GitHub: Outlashed -->
