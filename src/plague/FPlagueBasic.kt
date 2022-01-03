package plague

import arc.*
import arc.struct.*
import arc.util.*
import mindustry.*
import mindustry.content.*
import mindustry.game.*
import mindustry.game.EventType.*
import mindustry.gen.*
import mindustry.maps.Map
import mindustry.mod.*
import mindustry.net.Administration.*
import mindustry.type.*
import mindustry.world.*
import mindustry.world.blocks.defense.turrets.*

class FPlagueBasic : Plugin() {
    var mapvotes = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    private val rules = Rules()
    private var survivorBanned = Rules()
    var relogTeam = ObjectMap<String, Team>()
    var gameovervotes = ArrayList<String>()
    var totalplayers = 0 // for some reason i need it here cus java
    var playersThatVoted = ArrayList<String>()
    var plagueCores = ArrayList<Tile>()
    var playerCores = HashMap<String, Tile>()
    var leaders = ArrayList<String>()


    companion object {
        @JvmField
        var Have120SecondsPassed = false
        var gameTime = System.currentTimeMillis()
        @JvmField
        var selectedMap: Map? = null
        @JvmField
        var plagueBanned = Rules()
        fun floatToShort(x: Float): Short {
            if (x < Short.MIN_VALUE) {
                return Short.MIN_VALUE
            }
            return if (x > Short.MAX_VALUE) {
                Short.MAX_VALUE
            } else Math.round(x).toShort()
        }
    }

    // I need this here cus java
    var kickedPlayer: Player? = null
    var teamProximityCore: Team? = null
    override fun init() {
        // Start all plague unit multipliers in x seconds and time before all teamless are turned to plague
        PlagueTime(120)
        rules.canGameOver = false //I have my own way to game over
        rules.reactorExplosions = false // I wonder,nah plague op
        rules.buildSpeedMultiplier = 5f // game goes faster brr
        rules.fire = false // Obvious
        rules.logicUnitBuild = false // You know why
        rules.damageExplosions = false // NO NO NO
        rules.unitCap = 100 // lag prevention
        rules.unitCapVariable = false // so cap wont change if core is upgraded
        mapvotes = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        init_rules()
        //load events
        Events.on(ServerLoadEvent::class.java) { event: ServerLoadEvent? -> println("events loaded!") }

        // Prevents destroying of power source 
        Vars.netServer.admins.addActionFilter { action: PlayerAction ->
            if (action.block != null && action.block === Blocks.powerSource && action.type == ActionType.breakBlock) {
                return@addActionFilter false
            }
            true
        }
        // No placing near crux core and banned blocks incase of bug
        Vars.netServer.admins.addActionFilter { action: PlayerAction ->
            val boulders = ArrayList<Block>()
            boulders.add(Blocks.boulder)
            boulders.add(Blocks.daciteBoulder)
            boulders.add(Blocks.basaltBoulder)
            boulders.add(Blocks.sandBoulder)
            boulders.add(Blocks.shaleBoulder)
            boulders.add(Blocks.snowBoulder)
            if (plagueCores.isEmpty() && Vars.state.gameOver == false) {
                for (x in 0 until Vars.world.width()) {
                    for (y in 0 until Vars.world.height()) {
                        if (Vars.world.tile(x, y).block() === Blocks.coreFoundation || Vars.world.tile(x, y)
                                .block() === Blocks.coreShard || Vars.world.tile(x, y).block() === Blocks.coreNucleus
                        ) {
                            if (Vars.world.tile(x, y).team() === Team.purple && Vars.world.tile(x, y).isCenter) {
                                plagueCores.add(Vars.world.tile(x, y))
                            }
                        }
                    }
                }
            }
            if (action.block != null && action.type == ActionType.placeBlock && action.tile.block() === Blocks.powerSource) {
                return@addActionFilter false
            }
            if (action.block != null && action.type == ActionType.placeBlock && action.player.team() !== Team.purple && survivorBanned.bannedBlocks.contains(
                    action.block
                )
            ) {
                action.player.sendMessage("[red]NO PERMISSION TO PLACE BANNED BLOCKS")
                return@addActionFilter false
            }
            if (action.block != null && action.type == ActionType.placeBlock && action.player.team() === Team.purple && plagueBanned.bannedBlocks.contains(
                    action.block
                )
            ) {
                action.player.sendMessage("[red]NO PERMISSION TO PLACE BANNED BLOCKS")
                return@addActionFilter false
            }
            if (action.block != null && action.type == ActionType.breakBlock && boulders.contains(action.tile.block())) {
                action.tile.setNet(Blocks.air)
                return@addActionFilter false
            }
            if (action.block != null && action.type == ActionType.placeBlock) {
                for (b in plagueCores) {
                    if (cartesianDistance(
                            action.tile.x.toFloat(),
                            action.tile.y.toFloat(),
                            b.x.toFloat(),
                            b.y.toFloat()
                        ) < 90 && action.player.team() !== Team.purple
                    ) {
                        return@addActionFilter false
                    }
                }
            }
            true
        }


        // No unit control until 20 mins have passed
        Vars.netServer.admins.addActionFilter { action: PlayerAction ->
            if (action.type == ActionType.control && (System.currentTimeMillis() - gameTime) / 1000 < 1200) {
                return@addActionFilter false
            }
            true
        }

        // Make core from vault using 1k thorium 
        Events.on(TapEvent::class.java) { event: TapEvent ->
            if (event.tile.block() === Blocks.vault) {
                if (event.tile.build.items().has(
                        Items.thorium,
                        1000
                    ) && event.tile.build.team() !== Team.purple && event.tile.build.team === event.player.team()
                ) {
                    Vars.world.tile(event.tile.build.tileX(), event.tile.build.tileY())
                        .setNet(Blocks.coreShard, event.player.team(), 0)
                }
            }
        }

        // Makes you plague if you join too late also if rejoin put back on team
        Events.on(PlayerJoin::class.java) { event: PlayerJoin ->
            if (event.player.info.timesJoined <= 1) {
                //System.out.println("player has not joined before");
                event.player.sendMessage("[yellow]Welcome to plague!")
                event.player.sendMessage("[blue]Discord Link: [yellow]https://discord.gg/rfzm5xgJSC")
                event.player.sendMessage("[purple]Plague is an attack like mode where the infected (purple) must build units to take out the survivors")
                event.player.sendMessage("[purple]Survivors can't build units, their focus is surviving purple's units, to become a survivor place a block on the outside of the map")
                event.player.sendMessage("[purple]Plague has infinite power and must attack and kill survivors (20 minutes for unit control)")
                //event.player.sendMessage("For more information, use /info");
            }
            if (relogTeam.containsKey(event.player.uuid())) {
                event.player.team(relogTeam[event.player.uuid()])
                Call.setRules(event.player.con, survivorBanned)
            } else if (Have120SecondsPassed == true) {
                event.player.team(Team.purple)
                Call.setRules(event.player.con, plagueBanned)
            } else if (event.player.team() === Team.sharded) {
                val spawnunit = UnitTypes.gamma.spawn(
                    Team.purple,
                    (Vars.world.width() * 4).toFloat(),
                    (Vars.world.height() * 4).toFloat()
                )
                event.player.unit(spawnunit)
                Call.setRules(event.player.con, survivorBanned)
            }
        }
        Events.on(PlayerLeave::class.java) { event: PlayerLeave ->
            if (event.player.team() !== Team.purple && event.player.team() !== Team.sharded) {
                relogTeam.put(event.player.uuid(), event.player.team())
            }
            if (!event.player.unit().isNull) {
                if (event.player.unit().type === UnitTypes.gamma) {
                    event.player.unit().kill()
                }
            }
            if (gameovervotes.contains(event.player.name)) {
                gameovervotes.remove(event.player.name)
            }
        }

        // Create core with block placement
        Events.on(BuildSelectEvent::class.java) { event: BuildSelectEvent ->
            if (event.builder.team === Team.sharded && !Have120SecondsPassed) {
                val randomTeamNumber = Math.floor(Math.random() * 200 + 6).toInt()
                val chosenteam = Team.all[randomTeamNumber]
                val teamcores = ArrayList<String>()
                val closestcores = ArrayList<Float>()
                val distanceaway = 80f
                for (x in 0 until Vars.world.width()) {
                    for (y in 0 until Vars.world.height()) {
                        if (Vars.world.tile(x, y).block() === Blocks.coreFoundation && Vars.world.tile(
                                x,
                                y
                            ).isCenter && Vars.world.tile(x, y).build.team === chosenteam
                        ) {
                            teamcores.add("hascorelol")
                        }
                    }
                }
                if (teamcores.isEmpty() == true) {
                    for (t in Team.all) {
                        if (t !== chosenteam) {
                            val nearestEnemyCore =
                                Vars.state.teams.closestCore(event.builder.player.x / 8, event.builder.player.y / 8, t)
                            if (nearestEnemyCore != null) {
                                if (cartesianDistance(
                                        event.builder.player.x / 8,
                                        event.builder.player.y / 8,
                                        nearestEnemyCore.tileX().toFloat(),
                                        nearestEnemyCore.tileY().toFloat()
                                    ) < distanceaway
                                ) {
                                    closestcores.add(
                                        cartesianDistance(
                                            event.builder.player.x,
                                            event.builder.player.y,
                                            nearestEnemyCore.tileX().toFloat(),
                                            nearestEnemyCore.tileY().toFloat()
                                        )
                                    )
                                }
                            }
                        }
                    }
                    if (closestcores.isEmpty() == true) {
                        playerCores.forEach { (p: String?, core: Tile) ->
                            //System.out.println(cartesianDistance(event.tile.x, event.tile.y, core.x, core.y));
                            if (cartesianDistance(
                                    event.tile.x.toFloat(),
                                    event.tile.y.toFloat(),
                                    core.x.toFloat(),
                                    core.y.toFloat()
                                ) < distanceaway + 80
                            ) {
                                //System.out.println(cartesianDistance(event.tile.x, event.tile.y, core.x, core.y));
                                teamProximityCore = Vars.world.tile(core.x.toInt(), core.y.toInt()).build.team
                            }
                        }
                        if (teamProximityCore != null) {
                            event.builder.player.team(teamProximityCore)
                            Call.setRules(event.builder.player.con, survivorBanned)
                            event.tile.setNet(Blocks.coreFoundation, Team.all[teamProximityCore!!.id], 0)
                            for (stack in rules.loadout) {
                                Call.setItem(event.tile.build, stack.item, stack.amount)
                            }
                            playerCores[event.builder.player.uuid()] = event.tile
                        } else {
                            event.builder.player.team(chosenteam)
                            Call.setRules(event.builder.player.con, survivorBanned)
                            event.tile.setNet(Blocks.coreFoundation, Team.all[randomTeamNumber], 0)
                            for (stack in rules.loadout) {
                                Call.setItem(event.tile.build, stack.item, stack.amount)
                            }
                            playerCores[event.builder.player.uuid()] = event.tile
                            leaders.add(event.builder.player.uuid())
                        }
                    }
                }
                teamcores.clear()
                closestcores.clear()
                teamProximityCore = null
            }
        }

        // Some maps have power infs outside of no build oh well invincibility time
        Events.on(BlockDestroyEvent::class.java) { event: BlockDestroyEvent ->
            if (event.tile.block() === Blocks.powerSource) {
                event.tile.setNet(Blocks.powerSource, event.tile.team(), 0)
            }
        }


        // Hell no no ono
        Events.on(GameOverEvent::class.java) { event: GameOverEvent? ->
            gameTime = System.currentTimeMillis()
            relogTeam.clear()
            gameovervotes.clear()
            plagueCores.clear()
            playerCores.clear()
            leaders.clear()
            PlagueTime.timer.cancel()
            PlagueTime.multiplier1.cancel()
            PlagueTime.gameover.cancel()
            PlagueTime.resetToDefaults()
            Have120SecondsPassed = false
            PlagueTime(120) // amount of seconds until everything actually starts

            // Why,why would you even do more timers Fitmo.. WHY?! Cursed existence
            mostVotedMap()
            if (selectedMap != null) {
                Groups.player.each { p: Player -> p.sendMessage("[yellow]Voted map is: " + selectedMap!!.name()) }
                MapChangerThings()
            }
        }
        Events.on(UnitCreateEvent::class.java) { event: UnitCreateEvent ->
            if (event.unit.type === UnitTypes.mono) {
                event.unit.team.items().add(Items.lead, 800)
                event.unit.team.items().add(Items.copper, 750)
                event.unit.kill()
            }
        }
    }

    // Turns you infected
    override fun registerClientCommands(handler: CommandHandler) {
        handler.register("infect", "You become [purple]INFECTED") { args: Array<String>, player: Player ->
            if (player.team() == Team.purple) return@register
            if (Groups.player.count { it.team() == player.team() } == 1) player.team().cores().each { it.tile.removeNet()}
            Call.setRules(player.con, plagueBanned)
            player.team(Team.purple)
        }
        handler.register("rules", "All the rules on the server") { args: Array<String?>?, player: Player ->
            player.sendMessage("1. Don't grief")
            player.sendMessage("2. Griefing includes wasting resources, you know who you are.")
            player.sendMessage("3. Shooting someone's core with a core unit is punishable by a kick, as it blocks visibility of resource display with a warning message.")
            player.sendMessage("4. Don't be toxic.")
            player.sendMessage("5. No blast bombing")
            player.sendMessage("6. Surv vs surv combat is a kickable offense")
            player.sendMessage("7. Don't build within a survivor's territory as plague. You know what they have conquered, use your head.")
        }
        handler.register(
            "info",
            "provides information about the plague game mode"
        ) { args: Array<String?>?, player: Player ->
            player.sendMessage("Plague is an attack like mode where the infected (purple) must build units to take out the survivors")
            player.sendMessage("Survivors can't build units, their focus is surviving purple's units, to become a survivor place a block on the outside of the map")
            player.sendMessage("Plague has infinite power and must attack and kill survivors (20 minutes for unit control)")
        }

        // Kick a player from the team
        handler.register(
            "teamkick",
            "<player>",
            "Kick a player from your team"
        ) { args: Array<String?>, player: Player ->
            Groups.player.each { p: Player ->
                if (Strings.stripColors(p.name).equals(
                        args[0], ignoreCase = true
                    )
                ) {
                    kickedPlayer = p
                }
            }
            if (kickedPlayer == null) {
                player.sendMessage("No player with such name")
            } else {
                if (leaders.contains(player.uuid()) && player.team() === kickedPlayer!!.team() && player !== kickedPlayer) {
                    if (Have120SecondsPassed == true) {
                        kickedPlayer!!.team(Team.purple)
                    } else {
                        kickedPlayer!!.team(Team.sharded)
                    }
                    val playerCore = playerCores[kickedPlayer!!.uuid()]
                    playerCore!!.setNet(Blocks.air)
                    playerCores.remove(kickedPlayer!!.uuid())
                } else {
                    player.sendMessage("[red]You aren't a leader/team creator or target player is in another team")
                }
            }
            kickedPlayer = null
        }

        // It shows how long a round has lasted duh
        handler.register(
            "time",
            "Check how long game has lasted"
        ) { args: Array<String?>?, player: Player -> player.sendMessage("[purple]Game has lasted [green]" + (System.currentTimeMillis() - gameTime) / 60000 + " [purple]minutes") }

        // Kills currently controlled unit
        handler.register(
            "kill",
            "Kills currently controlled unit"
        ) { args: Array<String?>?, player: Player -> player.unit().kill() }

        // Respawns unit
        handler.register(
            "respawn",
            "Respawn your gamma if bugged as sharded"
        ) { args: Array<String?>?, player: Player ->
            if (player.team() === Team.sharded) {
                if (!player.unit().isNull) {
                    player.unit().kill()
                }
                val spawnunit = UnitTypes.gamma.spawn(
                    Team.purple,
                    (Vars.world.width() * 4).toFloat(),
                    (Vars.world.height() * 4).toFloat()
                )
                player.unit(spawnunit)
            }
        }

        // Start voting to select next map or see all maps
        handler.register(
            "rtv",
            "[MapNumber]",
            "Vote for next map or see all maps"
        ) { args: Array<String>, player: Player ->
            if (args.size == 1) {
                val allcustommaps = Vars.maps.customMaps()
                if (!playersThatVoted.contains(player.name)) {
                    if (args[0].matches("[0-9]+".toRegex())) {
                        try {
                            val votednumber = args[0].toInt() - 1
                            mapvotes[votednumber]++
                            player.sendMessage("You voted for map " + allcustommaps[votednumber].name())
                            playersThatVoted.add(player.name)
                        } catch (e: Exception) {
                            player.sendMessage("Vote failed")
                        }
                    }
                } else {
                    player.sendMessage("You already voted")
                }
            } else {
                var mapnumber = 0
                player.sendMessage("All maps:")
                val list = Vars.maps.customMaps()
                for (map in list) {
                    mapnumber++
                    player.sendMessage(mapnumber.toString() + " " + map.name())
                }
            }
        }

        // How do I explain.. if 4/5th of players voted then game ends
        handler.register("endgame", "[purple] Vote for the game to end") { args: Array<String?>?, player: Player ->
            if (Have120SecondsPassed) {
                if (gameovervotes.contains(player.name)) {
                    gameovervotes.remove(player.name)
                } else {
                    gameovervotes.add(player.name)
                }
                totalplayers = 0
                Groups.player.each { p: Player? -> totalplayers++ }
                Groups.player.each { p: Player ->
                    if (totalplayers <= 5) {
                        p.sendMessage("[yellow] There are currently [purple]" + gameovervotes.size + "[yellow] of [purple]" + 2 + "[yellow] votes to end the game. Vote with /endgame")
                    } else {
                        p.sendMessage("[yellow] There are currently [purple]" + gameovervotes.size + "[yellow] of [purple]" + totalplayers / 5 * 4 + "[yellow] votes to end the game. Vote with /endgame")
                    }
                }
                if (totalplayers <= 5) {
                    if (gameovervotes.size >= 2) {
                        Events.fire(GameOverEvent(Team.purple))
                    }
                } else if (totalplayers / 5 * 4 <= gameovervotes.size) {
                    Events.fire(GameOverEvent(Team.purple))
                }
            }
        }


        // Gameover command for admins
        handler.register("gameover", "Ends round,Admin only") { args: Array<String?>?, player: Player ->
            if (player.admin() == true) {
                Events.fire(GameOverEvent(Team.purple))
            }
        }
        handler.register(
            "gameover2",
            "Ends round,only used if game didn't end"
        ) { args: Array<String?>?, player: Player? ->
            val cores = ArrayList<String>()
            for (x in 0 until Vars.world.width()) {
                for (y in 0 until Vars.world.height()) {
                    if (Vars.world.tile(x, y).block() === Blocks.coreShard || Vars.world.tile(x, y)
                            .block() === Blocks.coreFoundation || Vars.world.tile(x, y).block() === Blocks.coreNucleus
                    ) {
                        if (Vars.world.tile(x, y).build.team !== Team.purple) {
                            cores.add("There is a surv core")
                        }
                    }
                }
            }
            if (cores.isEmpty() && !Vars.state.gameOver && Have120SecondsPassed) {
                Events.fire(GameOverEvent(Team.purple))
            }
            cores.clear()
        }
        handler.register("playerlist", "Shows all players and their team") { args: Array<String?>?, player: Player ->
            Groups.player.each { p: Player ->
                if (p.team() !== Team.crux) {
                    if (leaders.contains(p.uuid())) {
                        player.sendMessage("[red]" + p.name + "(Leader)" + " [yellow]Team ID:" + p.team().id)
                    } else {
                        player.sendMessage("[red]" + p.name + " [yellow]Team ID:" + p.team().id)
                    }
                }
            }
        }
    }

    //this thing has more than rules as you can see lmao
    fun init_rules() {
        Vars.state.rules.canGameOver = false //I have my own way to game over
        Vars.state.rules.reactorExplosions = false // I wonder,nah plague op
        Vars.state.rules.buildSpeedMultiplier = 5f // game goes faster brr
        Vars.state.rules.fire = false // Obvious
        Vars.state.rules.logicUnitBuild = false // You know why
        Vars.state.rules.damageExplosions = false // NO NO NO
        Vars.state.rules.unitCap = 100 // lag prevention
        Vars.state.rules.unitCapVariable = false // so cap wont change if core is upgraded
        survivorBanned = rules.copy()
        survivorBanned.bannedBlocks.addAll(
            Blocks.groundFactory,
            Blocks.navalFactory,
            Blocks.commandCenter,
            Blocks.multiplicativeReconstructor
        )
        plagueBanned = rules.copy()
        plagueBanned.bannedBlocks.addAll(
            Blocks.battery,
            Blocks.batteryLarge,
            Blocks.steamGenerator,
            Blocks.combustionGenerator,
            Blocks.differentialGenerator,
            Blocks.rtgGenerator,
            Blocks.thermalGenerator,
            Blocks.impactReactor,
            Blocks.duo,
            Blocks.scatter,
            Blocks.scorch,
            Blocks.hail,
            Blocks.lancer,
            Blocks.arc,
            Blocks.parallax,
            Blocks.swarmer,
            Blocks.salvo,
            Blocks.segment,
            Blocks.tsunami,
            Blocks.fuse,
            Blocks.ripple,
            Blocks.cyclone,
            Blocks.foreshadow,
            Blocks.spectre,
            Blocks.meltdown,
            Blocks.navalFactory,
            Blocks.copperWall,
            Blocks.copperWallLarge,
            Blocks.titaniumWall,
            Blocks.titaniumWallLarge,
            Blocks.plastaniumWall,
            Blocks.plastaniumWallLarge,
            Blocks.thoriumWall,
            Blocks.thoriumWallLarge,
            Blocks.phaseWall,
            Blocks.phaseWallLarge,
            Blocks.surgeWall,
            Blocks.surgeWallLarge,
            Blocks.door,
            Blocks.doorLarge,
            Blocks.thoriumReactor,
            Blocks.solarPanel,
            Blocks.largeSolarPanel
        ) // Can't be trusted
        Blocks.powerSource.health = Int.MAX_VALUE

        // Disable default unit damage
        UnitTypes.alpha.weapons.clear()
        UnitTypes.beta.weapons.clear()
        UnitTypes.gamma.weapons.clear()
        UnitTypes.flare.weapons.clear()
        UnitTypes.horizon.weapons.clear()
        UnitTypes.zenith.weapons.clear()
        UnitTypes.antumbra.weapons.clear()
        UnitTypes.eclipse.weapons.clear()
        UnitTypes.horizon.abilities.clear()
        UnitTypes.alpha.health = 1f
        UnitTypes.beta.health = 1f
        UnitTypes.gamma.health = 1f
        UnitTypes.flare.health = 1f

        //Disable pickup,interesting variable
        UnitTypes.oct.payloadCapacity = 0f
        UnitTypes.mega.payloadCapacity = 0f
        UnitTypes.quad.payloadCapacity = 0f

        // Easily changable starting items for survivors
        rules.loadout.clear()
        rules.loadout.add(ItemStack(Items.copper, 4000))
        rules.loadout.add(ItemStack(Items.lead, 4000))
        rules.loadout.add(ItemStack(Items.graphite, 2000))
        rules.loadout.add(ItemStack(Items.silicon, 1000))
        rules.loadout.add(ItemStack(Items.titanium, 2000))
        rules.loadout.add(ItemStack(Items.metaglass, 1000))

        //foreshadow build damage removed cus yes
        (Blocks.foreshadow as ItemTurret).ammoTypes[Items.surgeAlloy].buildingDamageMultiplier = 0f
    }

    fun findmap(mapname: String?): Map? {
        val findMap: Map?
        findMap = Vars.maps.all().find { map: Map ->
            Strings.stripColors(map.name().replace('_', ' ')).equals(Strings.stripColors(mapname), ignoreCase = true)
        }
        return findMap
    }

    fun mostVotedMap(): Map? {
        playersThatVoted.clear()
        val allmaps = Vars.maps.customMaps()
        var max = -1
        for (i in 0..9) {
            if (mapvotes[i] > (if (max == -1) 0 else mapvotes[max])) {
                max = i
            }
        }
        return if (max != -1) {
            mapvotes = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            selectedMap = allmaps[max]
            allmaps[max]
        } else {
            null
        }
    }

    fun closestCore(playerx: Int, playery: Int, team: Team?): Float {
        val nearestCoreTeam0 = Vars.state.teams.closestCore((playerx / 8).toFloat(), (playery / 8).toFloat(), team)
        return if (nearestCoreTeam0 != null) {
            cartesianDistance(
                floatToShort((playerx / 8).toFloat()).toFloat(),
                floatToShort((playery / 8).toFloat()).toFloat(),
                nearestCoreTeam0.tileX().toFloat(),
                nearestCoreTeam0.tileY().toFloat()
            )
        } else {
            1000F
        }
    }

    fun cartesianDistance(x: Float, y: Float, cx: Float, cy: Float): Float { // TODO: .dst?
        return Math.sqrt(Math.pow((x - cx).toDouble(), 2.0) + Math.pow((y - cy).toDouble(), 2.0)).toFloat()
    }
}