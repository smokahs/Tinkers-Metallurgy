# Changelog

## v1.2.0

Create Low-Heated absorbed, MIT, from [zehmaria/createlowheated](https://github.com/zehmaria/createlowheated). Do not install both

1. A fourth heat level, below the blaze burner
    - Create's `HeatCondition` gains `lowheated` and its `HeatLevel` gains `LOW`, so `"heatRequirement": "lowheated"` works in any basin recipe
    - The Basic Burner is the block that reaches it: one slot of solid fuel, lit by hand with flint and steel, three andesite alloy to craft
    - Feed it by hand, by funnel, by deployer, by mechanical arm, by dropping items on it, or by dispenser
    - A max-RPM encased fan pointed at one empowers it to a kindled blaze burner's heat, at 32x the fuel
2. The Tinkers' smeltery uses it
    - `[heat] lowheatedThreshold` puts converted melting and alloying recipes on the new rung
    - `[heat] heatedThreshold` moves from 1 to **500**, splitting the metals where Tinkers' own temperatures already do: tin (225), lead (330), zinc (420) and aluminum (425) melt on a basic burner, copper (500) up to gold (700) need a blaze, iron (800) up needs blaze cake. Set it back to 1 for the old behaviour
    - Mob boiling gets `[heat] crucibleLowheatedHeat`, at 0: a burning basic burner reads 0 to the crucible the way a passive heater did, and bare blocks read -1 each, so the requirement is a full footprint rather than a hotter one
3. Passive heating is off
    - `#create:passive_boiler_heaters` (campfires, lava, magma) no longer heats boilers or basins, and neither does an unfed blaze burner
    - A charcoal-fed Basic Burner gives exactly what they used to, so steam costs fuel now
    - `[burner] passiveBoilerHeaters` puts them back; `[burner] basicBurnerBoiler` instead keeps the burner out of the boiler entirely, which puts them back as a side effect
    - `#tinkersmetallurgy:lowheat_recipe_heaters` is empty and yours: blocks in it count as low heat for recipes without ever heating a boiler
4. 38 low-heated cooking recipes come with it
    - Chocolate, builder's tea, the three vanilla soups and ~33 Farmer's Delight dishes, all guarded by `forge:mod_loaded`
    - Empty `#tinkersmetallurgy:lowheated_cooking` in a datapack to drop the lot
5. Recipe viewer
    - Low-heated basin and mixing recipes draw the Basic Burner, not a blaze burner, and no blaze cake
    - Tinkers' foundry category prints the new requirement alongside the other two
6. Optional integrations, all compile-only, none required
    - KubeJS: `.lowheated()` alongside `.heated()` and `.superheated()`, both on Create's processing schema and on Create: Metallurgy's, so foundry lid and mixer recipes can be written at the new tier
    - Jade: a burner's heat requirement and how long its fuel has left
7. Differences from Create Low-Heated
    - No creative tab of its own; the burner sits in Create's, next to the blaze burner it stands in for
    - Comparator output is 0 unlit, 1 lit, 2 empowered. Upstream read the heat level's ordinal, which stopped ordering correctly once `LOW` was appended to the end of the enum
    - The burner crackles while lit. Upstream's `animateTick` had a pre-1.20 signature, so it never bound
    - A second, slower fan aimed at a burner no longer clears the empowered flag a faster one set, and burner placement honours `fanHorizontalOnly` the way the fan itself already did
    - Low-heated flames in the recipe viewer scroll at the speed of the tier below a lit blaze, not the fastest of the lot, which is what `LOW`'s ordinal was giving them
    - Dispenser support reads the dispenser's inventory at the top of `dispenseFrom` instead of capturing its locals partway through
    - Corrupt `FuelLevel` NBT falls back to unlit rather than throwing on chunk load
    - Create: Crafts & Additions and The One Probe support is not carried over; neither is a dependency here

## v1.1.0

1. Tinkers' melting category dropped
    - It listed the same 336 pages as the foundry category, one tab for the melter and one for the foundry
    - The foundry tab is the one conversion matches: byproducts, foundry ore rate, no solid fuel
    - Takes the melter, the melting pan and the melting modifier out of the catalyst row with it, none of which melt anything once the smeltery is gone
2. The foundry category draws Create heat instead of Tinkers'
    - The melting point in °C becomes the blaze burner level the converted recipe needs
    - `[jei] createHeat` turns it off

## v1.0.0

Initial release!

**Requires** Mantle, Tinkers' Construct, Create (6.0.8–6.1.0) and Create: Metallurgy (1.0.1+). JEI is optional.

1. The whole Tinkers' smeltery now runs on Create: Metallurgy machines
    - Melting, ore melting, material melting and damagable melting → Foundry lid
    - Alloying → Foundry mixer
    - Entity melting → Industrial crucible
    - Casting and molding → Casting table and basin, all ~22 Tinkers' casting serialisers
    - Conversion runs at datapack reload, after tags are bound and the material registry is filled, so recipes another addon added to the smeltery are converted too
    - Every conversion is individually toggleable under `[convert]`
2. Casting is covered without reimplementing 22 recipe types
    - Conversion reads the display view every Tinkers' casting recipe already hands to JEI, so material-driven recipes expand into one resolved entry per material, NBT and all
3. Graphite molds are gone
    - Every Create: Metallurgy casting recipe is converted to the matching Tinkers' cast
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
