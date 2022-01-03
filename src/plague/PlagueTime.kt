package plague;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import arc.Events;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.type.Weapon;


public class PlagueTime {
	static Timer timer;
	static Timer multiplier1;
	static Timer gameover;
	double modifier = 1.0;
	double modifiermessage = 1;
	
	   
	
	public PlagueTime(int seconds) {
        timer = new Timer();
        timer.schedule(new PlagueyTime(), 125 * 1000);
        multiplier1 = new Timer();
        multiplier1.schedule(new UnitMultiplier(), 600 * 1000);
        gameover = new Timer();
        gameover.schedule(new gameOvering(), 132 * 1000);
	}

    class PlagueyTime extends TimerTask {
        public void run() {
        	FPlagueBasic.Have120SecondsPassed = true; 
        	Groups.player.each(p -> {
        		if(p.team() == Team.sharded) {
        			p.team(Team.purple);
        			Call.setRules(p.con, FPlagueBasic.plagueBanned);
        			//FPlagueBasic.randomGen(Vars.world.width(), Vars.world.height());
        		}
        	});
        }
    }
    
    class gameOvering extends TimerTask {
        public void run() {
        	ArrayList<String> cores = new ArrayList<String>();
        	for(int x = 0; x < Vars.world.width(); x++) {
				for(int y = 0; y < Vars.world.height(); y++) {
					if(Vars.world.tile(x, y).block() == Blocks.coreShard || Vars.world.tile(x, y).block() == Blocks.coreFoundation || Vars.world.tile(x, y).block() == Blocks.coreNucleus) {
						if(Vars.world.tile(x, y).build.team != Team.purple) {
							cores.add("There is a surv core");
						}
					}
				}
			}
        	
        	if(cores.isEmpty() && !Vars.state.gameOver) {
				Events.fire(new GameOverEvent(Team.purple));	
				gameover.schedule(new gameOvering(), 130 * 1000);
			} else if (!Vars.state.gameOver){
				gameover.schedule(new gameOvering(), 5 * 1000);		
			} else {
				gameover.schedule(new gameOvering(), 50 * 1000);
			}
			
			cores.clear();
			
			this.cancel();
        }
    }
    
    class UnitMultiplier extends TimerTask {
        public void run() {
        	modifier = modifier * 1.05;
        	modifier = modifier + 0.05;
     	    modifiermessage = Math.round(modifier * 100.0) / 100.0;
     	    UnitTypes.antumbra.health = defaultantumbrahealth * (float) modifier;
     	    UnitTypes.arkyid.health = defaultarkyidhealth * (float) modifier;
     	    UnitTypes.atrax.health = defaultatraxhealth * (float) modifier;
     	    UnitTypes.bryde.health = defaultbrydehealth * (float) modifier;
     	    UnitTypes.corvus.health = defaultcorvushealth * (float) modifier;
     	    UnitTypes.crawler.health = defaultcrawlerhealth * (float) modifier;
     	    UnitTypes.dagger.health = defaultdaggerhealth * (float) modifier;
     	    UnitTypes.eclipse.health = defaulteclipsehealth * (float) modifier;
     	    UnitTypes.fortress.health = defaultfortresshealth * (float) modifier;
     	    UnitTypes.horizon.health = defaulthorizonhealth * (float) modifier;
     	    UnitTypes.mace.health = defaultmacehealth * (float) modifier;
     	    UnitTypes.mega.health = defaultmegahealth * (float) modifier;
    	   	UnitTypes.minke.health = defaultminkehealth * (float) modifier;
    	   	UnitTypes.nova.health = defaultnovahealth * (float) modifier;
    	   	UnitTypes.oct.health = defaultocthealth * (float) modifier;
    	   	UnitTypes.omura.health = defaultomurahealth * (float) modifier;
    	   	UnitTypes.poly.health = defaultpolyhealth * (float) modifier;
    	   	UnitTypes.pulsar.health = defaultpulsarhealth * (float) modifier;
    	   	UnitTypes.quad.health = defaultquadhealth * (float) modifier;
    	   	UnitTypes.quasar.health = defaultquasarhealth * (float) modifier;
    	   	UnitTypes.reign.health = defaultreignhealth * (float) modifier;
    	   	UnitTypes.risso.health = defaultrissohealth * (float) modifier;
    	   	UnitTypes.scepter.health = defaultscepterhealth * (float) modifier;
    	   	UnitTypes.sei.health = defaultseihealth * (float) modifier;
    	   	UnitTypes.spiroct.health = defaultspirocthealth * (float) modifier;
    	   	UnitTypes.toxopid.health = defaulttoxopidhealth * (float) modifier;
    	   	UnitTypes.vela.health = defaultvelahealth * (float) modifier;
    	   	UnitTypes.zenith.health = defaultzenithhealth * (float) modifier;

    	   	UnitTypes.antumbra.weapons.get(0).bullet.damage = defaultantumbradamage0 * (float) modifier;
    	   	UnitTypes.antumbra.weapons.get(1).bullet.damage = defaultantumbradamage1 * (float) modifier;
    	   	UnitTypes.antumbra.weapons.get(2).bullet.damage = defaultantumbradamage2 * (float) modifier;
    	   	UnitTypes.antumbra.weapons.get(3).bullet.damage = defaultantumbradamage3 * (float) modifier;
    	   	UnitTypes.antumbra.weapons.get(4).bullet.damage = defaultantumbradamage4 * (float) modifier;
    	   	UnitTypes.antumbra.weapons.get(5).bullet.damage = defaultantumbradamage5 * (float) modifier;

    	    UnitTypes.arkyid.weapons.get(0).bullet.damage = defaultarkyiddamage0 * (float) modifier;
    	    UnitTypes.arkyid.weapons.get(1).bullet.damage = defaultarkyiddamage1 * (float) modifier;
    	    UnitTypes.arkyid.weapons.get(2).bullet.damage = defaultarkyiddamage2 * (float) modifier;
    	    UnitTypes.arkyid.weapons.get(3).bullet.damage = defaultarkyiddamage3 * (float) modifier;
    	    UnitTypes.arkyid.weapons.get(4).bullet.damage = defaultarkyiddamage4 * (float) modifier;
    	    UnitTypes.arkyid.weapons.get(5).bullet.damage = defaultarkyiddamage5 * (float) modifier;
    	    UnitTypes.arkyid.weapons.get(6).bullet.damage = defaultarkyiddamage6 * (float) modifier;
    	    UnitTypes.arkyid.weapons.get(7).bullet.damage = defaultarkyiddamage7 * (float) modifier;
    	    
    	    UnitTypes.atrax.weapons.get(0).bullet.damage = defaultatraxdamage0 * (float) modifier;
    	    UnitTypes.atrax.weapons.get(1).bullet.damage = defaultatraxdamage1 * (float) modifier;
    	   
    	    UnitTypes.bryde.weapons.get(0).bullet.damage = defaultbrydedamage0 * (float) modifier;
    	    UnitTypes.bryde.weapons.get(1).bullet.damage = defaultbrydedamage1 * (float) modifier;
    	    UnitTypes.bryde.weapons.get(2).bullet.damage = defaultbrydedamage2 * (float) modifier;
    	   
    	    UnitTypes.corvus.weapons.get(0).bullet.damage = defaultcorvusdamage0 * (float) modifier;
    	   
    	    UnitTypes.crawler.weapons.get(0).bullet.splashDamage = defaultcrawlerdamage0 * (float) modifier;
      	    UnitTypes.crawler.weapons.get(0).bullet.splashDamageRadius = defaultcrawlerdamageradius0 * (float) modifier * 10;
    	   
    	    UnitTypes.dagger.weapons.get(0).bullet.damage = defaultdaggerdamage0 * (float) modifier;
    	    UnitTypes.dagger.weapons.get(1).bullet.damage = defaultdaggerdamage1 * (float) modifier;
    	   
    	    UnitTypes.eclipse.weapons.get(0).bullet.damage = defaulteclipsedamage0 * (float) modifier;
    	    UnitTypes.eclipse.weapons.get(1).bullet.damage = defaulteclipsedamage1 * (float) modifier;
    	    UnitTypes.eclipse.weapons.get(2).bullet.damage = defaulteclipsedamage2 * (float) modifier;
    	    UnitTypes.eclipse.weapons.get(3).bullet.damage = defaulteclipsedamage3 * (float) modifier;
    	    UnitTypes.eclipse.weapons.get(4).bullet.damage = defaulteclipsedamage4 * (float) modifier;
    	    UnitTypes.eclipse.weapons.get(5).bullet.damage = defaulteclipsedamage5 * (float) modifier;
    	   
    	    UnitTypes.fortress.weapons.get(0).bullet.damage = defaultfortressdamage0 * (float) modifier;
    	    UnitTypes.fortress.weapons.get(1).bullet.damage = defaultfortressdamage1 * (float) modifier;
    	   
    	   	// Horizon has uneditable ability
    	   
    	    UnitTypes.mace.weapons.get(0).bullet.damage = defaultmacedamage0 * (float) modifier;
    	    UnitTypes.mace.weapons.get(1).bullet.damage = defaultmacedamage1 * (float) modifier;
    	   
    	   
    	    UnitTypes.mega.weapons.get(0).bullet.damage = defaultmegadamage0 * (float) modifier;
    	    UnitTypes.mega.weapons.get(1).bullet.damage = defaultmegadamage1 * (float) modifier;
    	    UnitTypes.mega.weapons.get(2).bullet.damage = defaultmegadamage2 * (float) modifier;
    	    UnitTypes.mega.weapons.get(3).bullet.damage = defaultmegadamage3 * (float) modifier;
    	   
    	    UnitTypes.minke.weapons.get(0).bullet.damage = defaultminkedamage0 * (float) modifier;
    	    UnitTypes.minke.weapons.get(1).bullet.damage = defaultminkedamage1 * (float) modifier;
    	    UnitTypes.minke.weapons.get(2).bullet.damage = defaultminkedamage2 * (float) modifier;
    	    UnitTypes.minke.weapons.get(3).bullet.damage = defaultminkedamage3 * (float) modifier;
    	   
    	    UnitTypes.nova.weapons.get(0).bullet.damage = defaultnovadamage0 * (float) modifier;
    	    UnitTypes.nova.weapons.get(1).bullet.damage = defaultnovadamage1 * (float) modifier;
    	   
    	   // Oct is weaponless kekw
    	   
    	    UnitTypes.omura.weapons.get(0).bullet.damage = defaultomuradamage0 * (float) modifier;

    	    UnitTypes.poly.weapons.get(0).bullet.damage = defaultpolydamage0 * (float) modifier;
    	    UnitTypes.poly.weapons.get(1).bullet.damage = defaultpolydamage1 * (float) modifier;
    	   
    	    UnitTypes.pulsar.weapons.get(0).bullet.damage = defaultpulsardamage0 * (float) modifier;
    	    UnitTypes.pulsar.weapons.get(1).bullet.damage = defaultpulsardamage1 * (float) modifier;
    	   
    	    UnitTypes.quad.weapons.get(0).bullet.damage = defaultquaddamage0 * (float) modifier;

    	    UnitTypes.quasar.weapons.get(0).bullet.damage = defaultquasardamage0 * (float) modifier;
    	    UnitTypes.quasar.weapons.get(1).bullet.damage = defaultquasardamage1 * (float) modifier;
    	   
    	    UnitTypes.reign.weapons.get(0).bullet.damage = defaultreigndamage0 * (float) modifier;
    	    UnitTypes.reign.weapons.get(1).bullet.damage = defaultreigndamage1 * (float) modifier;
    	   
    	    UnitTypes.risso.weapons.get(0).bullet.damage = defaultrissodamage0 * (float) modifier;
    	    UnitTypes.risso.weapons.get(1).bullet.damage = defaultrissodamage1 * (float) modifier;
    	    UnitTypes.risso.weapons.get(2).bullet.damage = defaultrissodamage2 * (float) modifier;
    	   
    	    UnitTypes.scepter.weapons.get(0).bullet.damage = defaultscepterdamage0 * (float) modifier;
    	    UnitTypes.scepter.weapons.get(1).bullet.damage = defaultscepterdamage1 * (float) modifier;
    	    UnitTypes.scepter.weapons.get(2).bullet.damage = defaultscepterdamage2 * (float) modifier;
    	    UnitTypes.scepter.weapons.get(3).bullet.damage = defaultscepterdamage3 * (float) modifier;
    	    UnitTypes.scepter.weapons.get(4).bullet.damage = defaultscepterdamage4 * (float) modifier;
    	    UnitTypes.scepter.weapons.get(5).bullet.damage = defaultscepterdamage5 * (float) modifier;
    	   
    	    UnitTypes.sei.weapons.get(0).bullet.damage = defaultseidamage0 * (float) modifier;
    	    UnitTypes.sei.weapons.get(1).bullet.damage = defaultseidamage1 * (float) modifier;
    	    UnitTypes.sei.weapons.get(2).bullet.damage = defaultseidamage2 * (float) modifier;
    	   
    	    UnitTypes.spiroct.weapons.get(0).bullet.damage = defaultspiroctdamage0 * (float) modifier;
    	    UnitTypes.spiroct.weapons.get(1).bullet.damage = defaultspiroctdamage1 * (float) modifier;
    	    UnitTypes.spiroct.weapons.get(2).bullet.damage = defaultspiroctdamage2 * (float) modifier;
    	    UnitTypes.spiroct.weapons.get(3).bullet.damage = defaultspiroctdamage3 * (float) modifier;
    	   
    	    UnitTypes.toxopid.weapons.get(0).bullet.damage = defaulttoxopiddamage0 * (float) modifier;
    	    UnitTypes.toxopid.weapons.get(1).bullet.damage = defaulttoxopiddamage1 * (float) modifier;
    	    UnitTypes.toxopid.weapons.get(2).bullet.damage = defaulttoxopiddamage2 * (float) modifier;
    	   
    	    UnitTypes.vela.weapons.get(0).bullet.damage = defaultveladamage0 * (float) modifier;
    	   
    	    UnitTypes.zenith.weapons.get(0).bullet.damage =	defaultzenithdamage0 * (float) modifier;
    	    UnitTypes.zenith.weapons.get(1).bullet.damage = defaultzenithdamage1 * (float) modifier;
    	   
    	    
    	    
     	    // ^^^ You read it you monster
     	    
     	    
     	       	   
     	  	Groups.player.each(p -> {
      			p.sendMessage("[yellow]Units now deal increased [red]damage[yellow] and have more [green]health[yellow] for a total multiplier of [red]" + modifiermessage + "x");
      		});
     	 	multiplier1.schedule(new UnitMultiplier(), 600 * 1000);
     	 
			 for(Weapon w : UnitTypes.mega.weapons) {
				 w.bullet.damage = 0;
				 w.bullet.splashDamage = 0;
			 }
			 for(Weapon w : UnitTypes.quad.weapons) {
				 w.bullet.damage = 0;
				 w.bullet.splashDamage = 0;
			 }
			 for(Weapon w : UnitTypes.poly.weapons) {
				 w.bullet.damage = 0;
				 w.bullet.splashDamage = 0;
			 }
			 for(Weapon w : UnitTypes.flare.weapons) {
				w.bullet.damage = 0;
				w.bullet.splashDamage = 0;
			 }
			 for(Weapon w : UnitTypes.horizon.weapons) {
				w.bullet.damage = 0;
				w.bullet.splashDamage = 0;
			 }
			 for(Weapon w : UnitTypes.zenith.weapons) {
				w.bullet.damage = 0;
				w.bullet.splashDamage = 0;
			 }
			 for(Weapon w : UnitTypes.antumbra.weapons) {
				w.bullet.damage = 0;
				w.bullet.splashDamage = 0;
			 }
			 for(Weapon w : UnitTypes.eclipse.weapons) {
				w.bullet.damage = 0;
				w.bullet.splashDamage = 0;
			 }
			 Groups.unit.each(u -> {
				 if(u.type == UnitTypes.mono) {
					 u.team.items().add(Items.copper, 750);
					 u.team.items().add(Items.lead, 750);
					 u.kill();
				 }
			 });
            this.cancel();
        }
        // Below will be disabled damage but weapons aren't deleted like on other units
    }

    // NIGHTMARES BELOW DO NOT CHECK
    // Resets all damage and health to default
    
   	public static void resetToDefaults() {
	   
	   //DEFAULT UNITS ARE GAY
	   	UnitTypes.flare.health = 1;
	   	UnitTypes.gamma.health = 1;
	   	UnitTypes.beta.health = 1;
	   	UnitTypes.alpha.health = 1;


	   	UnitTypes.antumbra.health = defaultantumbrahealth;
	   	UnitTypes.arkyid.health = defaultarkyidhealth;
	   	UnitTypes.atrax.health = defaultatraxhealth;
	   	UnitTypes.bryde.health = defaultbrydehealth;
	   	UnitTypes.corvus.health = defaultcorvushealth;
	   	UnitTypes.crawler.health = defaultcrawlerhealth;
	   	UnitTypes.dagger.health = defaultdaggerhealth;
	   	UnitTypes.eclipse.health = defaulteclipsehealth;
	   	UnitTypes.fortress.health = defaultfortresshealth;
	   	UnitTypes.horizon.health = defaulthorizonhealth;
	   	UnitTypes.mace.health = defaultmacehealth;
	   	UnitTypes.mega.health = defaultmegahealth;
	   	UnitTypes.minke.health = defaultminkehealth;
	   	UnitTypes.nova.health = defaultnovahealth;
	   	UnitTypes.oct.health = defaultocthealth;
	   	UnitTypes.omura.health = defaultomurahealth;
	   	UnitTypes.poly.health = defaultpolyhealth;
	   	UnitTypes.pulsar.health = defaultpulsarhealth;
	   	UnitTypes.quad.health = defaultquadhealth;
	   	UnitTypes.quasar.health = defaultquasarhealth;
	   	UnitTypes.reign.health = defaultreignhealth;
	   	UnitTypes.risso.health = defaultrissohealth;
	   	UnitTypes.scepter.health = defaultscepterhealth;
	   	UnitTypes.sei.health = defaultseihealth;
	   	UnitTypes.spiroct.health = defaultspirocthealth;
	   	UnitTypes.toxopid.health = defaulttoxopidhealth;
	   	UnitTypes.vela.health = defaultvelahealth;
	   	UnitTypes.zenith.health = defaultzenithhealth;

	   
	   
	   	UnitTypes.antumbra.weapons.get(0).bullet.damage = defaultantumbradamage0;
	   	UnitTypes.antumbra.weapons.get(1).bullet.damage = defaultantumbradamage1;
	   	UnitTypes.antumbra.weapons.get(2).bullet.damage = defaultantumbradamage2;
	   	UnitTypes.antumbra.weapons.get(3).bullet.damage = defaultantumbradamage3;
	   	UnitTypes.antumbra.weapons.get(4).bullet.damage = defaultantumbradamage4;
	   	UnitTypes.antumbra.weapons.get(5).bullet.damage = defaultantumbradamage5;
   
	    UnitTypes.arkyid.weapons.get(0).bullet.damage = defaultarkyiddamage0;
	    UnitTypes.arkyid.weapons.get(1).bullet.damage = defaultarkyiddamage1;
	    UnitTypes.arkyid.weapons.get(2).bullet.damage = defaultarkyiddamage2;
	    UnitTypes.arkyid.weapons.get(3).bullet.damage = defaultarkyiddamage3;
	    UnitTypes.arkyid.weapons.get(4).bullet.damage = defaultarkyiddamage4;
	    UnitTypes.arkyid.weapons.get(5).bullet.damage = defaultarkyiddamage5;
	    UnitTypes.arkyid.weapons.get(6).bullet.damage = defaultarkyiddamage6;
	    UnitTypes.arkyid.weapons.get(7).bullet.damage = defaultarkyiddamage7;
	    
	    UnitTypes.atrax.weapons.get(0).bullet.damage = defaultatraxdamage0;
	    UnitTypes.atrax.weapons.get(1).bullet.damage = defaultatraxdamage1;
	   
	    UnitTypes.bryde.weapons.get(0).bullet.damage = defaultbrydedamage0;
	    UnitTypes.bryde.weapons.get(1).bullet.damage = defaultbrydedamage1;
	    UnitTypes.bryde.weapons.get(2).bullet.damage = defaultbrydedamage2;
	   
	    UnitTypes.corvus.weapons.get(0).bullet.damage = defaultcorvusdamage0;
	   
	    UnitTypes.crawler.weapons.get(0).bullet.splashDamage = defaultcrawlerdamage0;
  	    UnitTypes.crawler.weapons.get(0).bullet.splashDamageRadius = defaultcrawlerdamageradius0;
	   
	    UnitTypes.dagger.weapons.get(0).bullet.damage = defaultdaggerdamage0;
	    UnitTypes.dagger.weapons.get(1).bullet.damage = defaultdaggerdamage1;
	   
	    UnitTypes.eclipse.weapons.get(0).bullet.damage = defaulteclipsedamage0;
	    UnitTypes.eclipse.weapons.get(1).bullet.damage = defaulteclipsedamage1;
	    UnitTypes.eclipse.weapons.get(2).bullet.damage = defaulteclipsedamage2;
	    UnitTypes.eclipse.weapons.get(3).bullet.damage = defaulteclipsedamage3;
	    UnitTypes.eclipse.weapons.get(4).bullet.damage = defaulteclipsedamage4;
	    UnitTypes.eclipse.weapons.get(5).bullet.damage = defaulteclipsedamage5;
	   
	    UnitTypes.fortress.weapons.get(0).bullet.damage = defaultfortressdamage0;
	    UnitTypes.fortress.weapons.get(1).bullet.damage = defaultfortressdamage1;
	   
	   	// Horizon has uneditable ability
	   
	    UnitTypes.mace.weapons.get(0).bullet.damage = defaultmacedamage0;
	    UnitTypes.mace.weapons.get(1).bullet.damage = defaultmacedamage1;
	   
	   
	    UnitTypes.mega.weapons.get(0).bullet.damage = defaultmegadamage0;
	    UnitTypes.mega.weapons.get(1).bullet.damage = defaultmegadamage1;
	    UnitTypes.mega.weapons.get(2).bullet.damage = defaultmegadamage2;
	    UnitTypes.mega.weapons.get(3).bullet.damage = defaultmegadamage3;
	   
	    UnitTypes.minke.weapons.get(0).bullet.damage = defaultminkedamage0;
	    UnitTypes.minke.weapons.get(1).bullet.damage = defaultminkedamage1;
	    UnitTypes.minke.weapons.get(2).bullet.damage = defaultminkedamage2;
	    UnitTypes.minke.weapons.get(3).bullet.damage = defaultminkedamage3;
	   
	    UnitTypes.nova.weapons.get(0).bullet.damage = defaultnovadamage0;
	    UnitTypes.nova.weapons.get(1).bullet.damage = defaultnovadamage1;
	   
	   // Oct is weaponless kekw
	   
	    UnitTypes.omura.weapons.get(0).bullet.damage = defaultomuradamage0;

	    UnitTypes.poly.weapons.get(0).bullet.damage = defaultpolydamage0;
	    UnitTypes.poly.weapons.get(1).bullet.damage = defaultpolydamage1;
	   
	    UnitTypes.pulsar.weapons.get(0).bullet.damage = defaultpulsardamage0;
	    UnitTypes.pulsar.weapons.get(1).bullet.damage = defaultpulsardamage1;
	   
	    UnitTypes.quad.weapons.get(0).bullet.damage = defaultquaddamage0;

	    UnitTypes.quasar.weapons.get(0).bullet.damage = defaultquasardamage0;
	    UnitTypes.quasar.weapons.get(1).bullet.damage = defaultquasardamage1;
	   
	    UnitTypes.reign.weapons.get(0).bullet.damage = defaultreigndamage0;
	    UnitTypes.reign.weapons.get(1).bullet.damage = defaultreigndamage1;
	   
	    UnitTypes.risso.weapons.get(0).bullet.damage = defaultrissodamage0;
	    UnitTypes.risso.weapons.get(1).bullet.damage = defaultrissodamage1;
	    UnitTypes.risso.weapons.get(2).bullet.damage = defaultrissodamage2;
	   
	    UnitTypes.scepter.weapons.get(0).bullet.damage = defaultscepterdamage0;
	    UnitTypes.scepter.weapons.get(1).bullet.damage = defaultscepterdamage1;
	    UnitTypes.scepter.weapons.get(2).bullet.damage = defaultscepterdamage2;
	    UnitTypes.scepter.weapons.get(3).bullet.damage = defaultscepterdamage3;
	    UnitTypes.scepter.weapons.get(4).bullet.damage = defaultscepterdamage4;
	    UnitTypes.scepter.weapons.get(5).bullet.damage = defaultscepterdamage5;
	   
	    UnitTypes.sei.weapons.get(0).bullet.damage = defaultseidamage0;
	    UnitTypes.sei.weapons.get(1).bullet.damage = defaultseidamage1;
	    UnitTypes.sei.weapons.get(2).bullet.damage = defaultseidamage2;
	   
	    UnitTypes.spiroct.weapons.get(0).bullet.damage = defaultspiroctdamage0;
	    UnitTypes.spiroct.weapons.get(1).bullet.damage = defaultspiroctdamage1;
	    UnitTypes.spiroct.weapons.get(2).bullet.damage = defaultspiroctdamage2;
	    UnitTypes.spiroct.weapons.get(3).bullet.damage = defaultspiroctdamage3;
	   
	    UnitTypes.toxopid.weapons.get(0).bullet.damage = defaulttoxopiddamage0;
	    UnitTypes.toxopid.weapons.get(1).bullet.damage = defaulttoxopiddamage1;
	    UnitTypes.toxopid.weapons.get(2).bullet.damage = defaulttoxopiddamage2;
	   
	    UnitTypes.vela.weapons.get(0).bullet.damage = defaultveladamage0;
	   
	    UnitTypes.zenith.weapons.get(0).bullet.damage =	defaultzenithdamage0;
	    UnitTypes.zenith.weapons.get(1).bullet.damage = defaultzenithdamage1;
   	}

	// Default units cause trouble SMH
	public static float flarehp = UnitTypes.flare.health;
	public static float gammahp = UnitTypes.gamma.health;
	public static float betahp = UnitTypes.beta.health;
	public static float alphahp = UnitTypes.alpha.health;


	// All default stats
	public static float defaultantumbrahealth = UnitTypes.antumbra.health;
	public static float defaultarkyidhealth = UnitTypes.arkyid.health;
	public static float defaultatraxhealth = UnitTypes.atrax.health;
	public static float defaultbrydehealth = UnitTypes.bryde.health;
	public static float defaultcorvushealth = UnitTypes.corvus.health;
	public static float defaultcrawlerhealth = UnitTypes.crawler.health;
	public static float defaultdaggerhealth = UnitTypes.dagger.health;
	public static float defaulteclipsehealth = UnitTypes.eclipse.health;
	public static float defaultfortresshealth = UnitTypes.fortress.health;
	public static float defaulthorizonhealth = UnitTypes.horizon.health;
	public static float defaultmacehealth = UnitTypes.mace.health;
	public static float defaultmegahealth = UnitTypes.mega.health;
	public static float defaultminkehealth = UnitTypes.minke.health;
	public static float defaultnovahealth = UnitTypes.nova.health;
	public static float defaultocthealth = UnitTypes.oct.health;
	public static float defaultomurahealth = UnitTypes.omura.health;
	public static float defaultpolyhealth = UnitTypes.poly.health;
	public static float defaultpulsarhealth = UnitTypes.pulsar.health;
	public static float defaultquadhealth = UnitTypes.quad.health;
	public static float defaultquasarhealth = UnitTypes.quasar.health;
	public static float defaultreignhealth = UnitTypes.reign.health;
	public static float defaultrissohealth = UnitTypes.risso.health;
	public static float defaultscepterhealth = UnitTypes.scepter.health;
	public static float defaultseihealth = UnitTypes.sei.health;
	public static float defaultspirocthealth = UnitTypes.spiroct.health;
	public static float defaulttoxopidhealth = UnitTypes.toxopid.health;
	public static float defaultvelahealth = UnitTypes.vela.health;
	public static float defaultzenithhealth = UnitTypes.zenith.health;

	public static float defaultantumbradamage0 = UnitTypes.antumbra.weapons.get(0).bullet.damage;
	public static float defaultantumbradamage1 = UnitTypes.antumbra.weapons.get(1).bullet.damage;
	public static float defaultantumbradamage2 = UnitTypes.antumbra.weapons.get(2).bullet.damage;
	public static float defaultantumbradamage3 = UnitTypes.antumbra.weapons.get(3).bullet.damage;
	public static float defaultantumbradamage4 = UnitTypes.antumbra.weapons.get(4).bullet.damage;
	public static float defaultantumbradamage5 = UnitTypes.antumbra.weapons.get(5).bullet.damage;

	public static float defaultarkyiddamage0 = UnitTypes.arkyid.weapons.get(0).bullet.damage;
	public static float defaultarkyiddamage1 = UnitTypes.arkyid.weapons.get(1).bullet.damage;
	public static float defaultarkyiddamage2 = UnitTypes.arkyid.weapons.get(2).bullet.damage;
	public static float defaultarkyiddamage3 = UnitTypes.arkyid.weapons.get(3).bullet.damage;
	public static float defaultarkyiddamage4 = UnitTypes.arkyid.weapons.get(4).bullet.damage;
	public static float defaultarkyiddamage5 = UnitTypes.arkyid.weapons.get(5).bullet.damage;
	public static float defaultarkyiddamage6 = UnitTypes.arkyid.weapons.get(6).bullet.damage;
	public static float defaultarkyiddamage7 = UnitTypes.arkyid.weapons.get(7).bullet.damage;

	public static float defaultatraxdamage0 = UnitTypes.atrax.weapons.get(0).bullet.damage;
	public static float defaultatraxdamage1 = UnitTypes.atrax.weapons.get(1).bullet.damage;

	public static float defaultbrydedamage0 = UnitTypes.bryde.weapons.get(0).bullet.damage;
	public static float defaultbrydedamage1 = UnitTypes.bryde.weapons.get(1).bullet.damage;
	public static float defaultbrydedamage2 = UnitTypes.bryde.weapons.get(2).bullet.damage;

	public static float defaultcorvusdamage0 = UnitTypes.corvus.weapons.get(0).bullet.damage;

	public static float defaultcrawlerdamage0 = UnitTypes.crawler.weapons.get(0).bullet.splashDamage;
	public static float defaultcrawlerdamageradius0 = UnitTypes.crawler.weapons.get(0).bullet.splashDamageRadius;

	public static float defaultdaggerdamage0 = UnitTypes.dagger.weapons.get(0).bullet.damage;
	public static float defaultdaggerdamage1 = UnitTypes.dagger.weapons.get(1).bullet.damage;

	public static float defaulteclipsedamage0 = UnitTypes.eclipse.weapons.get(0).bullet.damage;
	public static float defaulteclipsedamage1 = UnitTypes.eclipse.weapons.get(1).bullet.damage;
	public static float defaulteclipsedamage2 = UnitTypes.eclipse.weapons.get(2).bullet.damage;
	public static float defaulteclipsedamage3 = UnitTypes.eclipse.weapons.get(3).bullet.damage;
	public static float defaulteclipsedamage4 = UnitTypes.eclipse.weapons.get(4).bullet.damage;
	public static float defaulteclipsedamage5 = UnitTypes.eclipse.weapons.get(5).bullet.damage;

	public static float defaultfortressdamage0 = UnitTypes.fortress.weapons.get(0).bullet.damage;
	public static float defaultfortressdamage1 = UnitTypes.fortress.weapons.get(1).bullet.damage;

	// Horizon has an ability i think

	public static float defaultmacedamage0 = UnitTypes.mace.weapons.get(0).bullet.damage;
	public static float defaultmacedamage1 = UnitTypes.mace.weapons.get(1).bullet.damage;


	public static float defaultmegadamage0 = UnitTypes.mega.weapons.get(0).bullet.damage;
	public static float defaultmegadamage1 = UnitTypes.mega.weapons.get(1).bullet.damage;
	public static float defaultmegadamage2 = UnitTypes.mega.weapons.get(2).bullet.damage;
	public static float defaultmegadamage3 = UnitTypes.mega.weapons.get(3).bullet.damage;

	public static float defaultminkedamage0 = UnitTypes.minke.weapons.get(0).bullet.damage;
	public static float defaultminkedamage1 = UnitTypes.minke.weapons.get(1).bullet.damage;
	public static float defaultminkedamage2 = UnitTypes.minke.weapons.get(2).bullet.damage;
	public static float defaultminkedamage3 = UnitTypes.minke.weapons.get(3).bullet.damage;

	public static float defaultnovadamage0 = UnitTypes.nova.weapons.get(0).bullet.damage;
	public static float defaultnovadamage1 = UnitTypes.nova.weapons.get(1).bullet.damage;

	// Oct is weaponless kekw

	public static float defaultomuradamage0 = UnitTypes.omura.weapons.get(0).bullet.damage;

	public static float defaultpolydamage0 = UnitTypes.poly.weapons.get(0).bullet.damage;
	public static float defaultpolydamage1 = UnitTypes.poly.weapons.get(1).bullet.damage;

	public static float defaultpulsardamage0 = UnitTypes.pulsar.weapons.get(0).bullet.damage;
	public static float defaultpulsardamage1 = UnitTypes.pulsar.weapons.get(1).bullet.damage;

	public static float defaultquaddamage0 = UnitTypes.quad.weapons.get(0).bullet.damage;

	public static float defaultquasardamage0 = UnitTypes.quasar.weapons.get(0).bullet.damage;
	public static float defaultquasardamage1 = UnitTypes.quasar.weapons.get(1).bullet.damage;

	public static float defaultreigndamage0 = UnitTypes.reign.weapons.get(0).bullet.damage;
	public static float defaultreigndamage1 = UnitTypes.reign.weapons.get(1).bullet.damage;

	public static float defaultrissodamage0 = UnitTypes.risso.weapons.get(0).bullet.damage;
	public static float defaultrissodamage1 = UnitTypes.risso.weapons.get(1).bullet.damage;
	public static float defaultrissodamage2 = UnitTypes.risso.weapons.get(2).bullet.damage;

	public static float defaultscepterdamage0 = UnitTypes.scepter.weapons.get(0).bullet.damage;
	public static float defaultscepterdamage1 = UnitTypes.scepter.weapons.get(1).bullet.damage;
	public static float defaultscepterdamage2 = UnitTypes.scepter.weapons.get(2).bullet.damage;
	public static float defaultscepterdamage3 = UnitTypes.scepter.weapons.get(3).bullet.damage;
	public static float defaultscepterdamage4 = UnitTypes.scepter.weapons.get(4).bullet.damage;
	public static float defaultscepterdamage5 = UnitTypes.scepter.weapons.get(5).bullet.damage;

	public static float defaultseidamage0 = UnitTypes.sei.weapons.get(0).bullet.damage;
	public static float defaultseidamage1 = UnitTypes.sei.weapons.get(1).bullet.damage;
	public static float defaultseidamage2 = UnitTypes.sei.weapons.get(2).bullet.damage;

	public static float defaultspiroctdamage0 = UnitTypes.spiroct.weapons.get(0).bullet.damage;
	public static float defaultspiroctdamage1 = UnitTypes.spiroct.weapons.get(1).bullet.damage;
	public static float defaultspiroctdamage2 = UnitTypes.spiroct.weapons.get(2).bullet.damage;
	public static float defaultspiroctdamage3 = UnitTypes.spiroct.weapons.get(3).bullet.damage;

	public static float defaulttoxopiddamage0 = UnitTypes.toxopid.weapons.get(0).bullet.damage;
	public static float defaulttoxopiddamage1 = UnitTypes.toxopid.weapons.get(1).bullet.damage;
	public static float defaulttoxopiddamage2 = UnitTypes.toxopid.weapons.get(2).bullet.damage;

	public static float defaultveladamage0 = UnitTypes.vela.weapons.get(0).bullet.damage;

	public static float defaultzenithdamage0 = UnitTypes.zenith.weapons.get(0).bullet.damage;
	public static float defaultzenithdamage1 = UnitTypes.zenith.weapons.get(1).bullet.damage;
   
}
