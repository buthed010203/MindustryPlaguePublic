package plague

import arc.*
import arc.util.*
import mindustry.*
import mindustry.content.*
import mindustry.content.UnitTypes.*
import mindustry.game.*
import mindustry.game.EventType.*
import mindustry.gen.*
import kotlin.math.*

class PlagueTime(seconds: Int) {
    var modifier = 1.0
    var modifiermessage = 1.0

    internal inner class PlagueyTime : TimerTask() {
        override fun run() {
            FPlagueBasic.Have120SecondsPassed = true
            Groups.player.each { p: Player ->
                if (p.team() === Team.sharded) {
                    p.team(Team.purple)
                    Call.setRules(p.con, FPlagueBasic.plagueBanned)
                    //FPlagueBasic.randomGen(Vars.world.width(), Vars.world.height());
                }
            }
        }
    }

    internal inner class gameOvering : TimerTask() {
        override fun run() {
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
            if (cores.isEmpty() && !Vars.state.gameOver) {
                Events.fire(GameOverEvent(Team.purple))
                gameover.schedule(gameOvering(), (130 * 1000).toLong())
            } else if (!Vars.state.gameOver) {
                gameover.schedule(gameOvering(), (5 * 1000).toLong())
            } else {
                gameover.schedule(gameOvering(), (50 * 1000).toLong())
            }
            cores.clear()
            cancel()
        }
    }

    init {

        val plagueyTime = Timer.schedule(PlagueyTime(), 125 * 1000F)

        val unitMultiplier = Timer.schedule({
            modifier *= 1.05
            modifier += 0.05
            modifiermessage = (modifier * 100.0).roundToInt() / 100.0 // TODO: What the hell is this?

            // ^^^ You read it you monster
            Groups.player.each {
                it.sendMessage("[yellow]Units now deal increased [red]damage[yellow] and have more [green]health[yellow] for a total multiplier of [red] ${modifiermessage}x")
            }

            Vars.content.units().each { // TODO: Merge with for loop below
                it.health *= modifier.toFloat()
                it.weapons.each { w -> w.bullet.damage *= modifier.toFloat() }
            }

            for (unit in listOf(mega, quad, poly, flare, horizon, zenith, antumbra, eclipse)) { // I don't know why this is a thing but it was here before so whatever
                unit.weapons.each {
                    it.bullet.damage = 0f
                    it.bullet.splashDamage = 0f
                }
            }
            Groups.unit.each {
                if (it.type == mono) {
                    it.team.items().add(Items.copper, 750)
                    it.team.items().add(Items.lead, 750)
                    it.kill()
                }
            }
        }, 600F, 600F)
        val gameOver = Timer.schedule(gameOvering(), (132 * 1000).toLong())
    }

    companion object { // I don't know why theres only one static method but here we are..
        private val defaults = buildMap { // TODO: Horribly janky way to store default unit stats: Map<Unit, Pair<Health, Weapons>>, Seq of a data class instead?
            Vars.content.units().each {
                put(it, it.health to it.weapons.copy())
            }
        }

        /** Resets all damage and health to default */
        fun resetToDefaults() {
            Vars.content.units().each {
                val default = defaults[it] ?: return@each // This should always be in the map but just in case... (Honestly why am I even bothering with a map when .each() is always the same on this immutable ordered seq?)
                it.health = default.first
                it.weapons = default.second
            }

            // Idk man, this was just here before
            flare.health = 1f
            gamma.health = 1f
            beta.health = 1f
            alpha.health = 1f
        }
    }
}