# Changelog

## v1.0.0

Initial release!

1. The whole Tinkers' smeltery now runs on Create: Metallurgy machines
    - Melting, ore melting, material melting and damagable melting → Foundry lid
    - Alloying → Foundry mixer (catalysts are re-emitted as outputs so they aren't eaten)
    - Entity melting → Industrial crucible
    - Casting and molding → Casting table and basin, all ~22 Tinkers' casting serialisers
    - Conversion runs at datapack reload, after tags are bound and the material registry is filled, so recipes another addon added to the smeltery are converted too
    - Every conversion is individually toggleable under `[convert]`
2. Casting is covered without reimplementing 22 recipe types
    - Conversion reads the display view every Tinkers' casting recipe already hands to JEI, so material-driven recipes expand into one resolved entry per material, NBT and all
    - No mixin needed and the results show up in JEI on their own
3. Graphite molds are gone
    - Every Create: Metallurgy casting recipe is repointed at the matching Tinkers' cast
    - The recipes that made a mold are deleted and all six mold items are hidden
    - The swap runs before the dedupe, so plates, rods, gears and the metals Tinkers' has no recipe for all survive
4. Tinkers' fluids are prioritized
    - 18 shared metals fold into the Tinkers' fluid; Create: Metallurgy recipes are rewritten onto it rather than deleted
    - Duplicate molten buckets hidden from creative and JEI
    - Same units on both sides (ingot 90 mB, nugget 10, block 810), so nothing is rescaled
5. Obdurium, void steel and necromium are now Tinkers' tool materials
    - Generated at runtime, on tic3nh's harvest tiers when installed and vanilla tiers otherwise
    - Each gets a `material_fluid`, a `material_melting` and a `tconstruct:material` for every solid form the metal actually has, so it can be cast, melted and repaired. Only obdurium has an ingot and block, so void steel and necromium are cast-only
    - Judged by their molten fluid, not by `forge:ingots/<metal>`, since Create: Metallurgy ships no ingot for two of the three
    - Traits follow each metal's alloy recipe, one per ingredient, at level 1 the way Tinkers writes its own
    - Names in a lang file plus a Mantle colour file, so the tool station reads Obdurium in the metal's colour instead of `material.tinkersmetallurgy.obdurium`
    - Colours sampled from Create: Metallurgy's own texture palettes: obdurium cream gold, void steel teal, necromium sage grey
    - Lithium is excluded on purpose, it's a soft alkali intermediate, not tool metal
6. Recipe viewer cleanup, `[jei] categories` picks a side
    - `TINKERS` (default) keeps Tinkers' compact cycling categories and catalyses them with the Create: Metallurgy machines, `CREATE_METALLURGY` does the opposite, `BOTH` shows everything twice
    - Converted recipes are filtered out as the categories collect them, through the one Create method every addon category uses, so no recipe-viewer API is needed (Too Many Recipe Viewers runs JEI plugins against EMI and can't enumerate them)
    - Create: Metallurgy's casting categories are dropped and its graphite mold recipes read across into the Tinkers' ones, so casting is one thing instead of two tabs; melting and grinding are left alone
    - Molding always keeps its Tinkers' category, it's the only place cast creation is shown
    - Alloying and mob boiling stay on the Create: Metallurgy side even under `TINKERS`, since Tinkers' categories can't draw a heat requirement
    - Category icons swapped to the machine that runs them, since the seared blocks can't be built any more
    - `hideTicSmelteryBlocks` also pulls the smeltery blocks out of the catalyst row under a recipe
7. Mob boiling or whatever u wanna call it works
    - Min crucible heat lands on 6 and 9, the values Create: Metallurgy uses for piglins and iron golems, instead of a number no build reaches
    - Heat is measured against Tinkers' melting points, not a Forge fluid temperature (two scales 300 apart)
