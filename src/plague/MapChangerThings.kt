package plague

import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.net.WorldReloader
import mindustry.Vars
import mindustry.game.Gamemode
import java.util.*

class MapChangerThings {
    init {
        mapchangetimer = Timer()
        mapchangetimer.schedule(MapChange(), (14 * 1000).toLong())
    }

    internal inner class MapChange : TimerTask() {
        override fun run() {
            Groups.player.each { p: Player -> p.kick("A voted map is being loaded, sorry!", 0) }

            // Why would anyone create this,you may ask? not even i know
            val reloader = WorldReloader()
            reloader.begin()
            Vars.world.loadMap(FPlagueBasic.selectedMap, FPlagueBasic.selectedMap!!.applyRules(Gamemode.survival))
            Vars.state.rules = Vars.state.map.applyRules(Gamemode.survival)
            Vars.logic.play()
            reloader.end()
            FPlagueBasic.selectedMap = null
            mapchangetimer.cancel()
        }
    }

    companion object {
        var mapchangetimer: Timer
    }
}