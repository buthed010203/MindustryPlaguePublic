package plague;


import static mindustry.Vars.netServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arc.Events;
import arc.struct.Seq;

import static mindustry.Vars.maps;

import arc.util.CommandHandler;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.content.Blocks;

import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.EventType.BlockDestroyEvent;
import mindustry.game.EventType.BuildSelectEvent;
import mindustry.game.EventType.GameOverEvent;
import mindustry.game.EventType.PlayerJoin;
import mindustry.game.EventType.PlayerLeave;
import mindustry.game.EventType.TapEvent;
import mindustry.game.EventType.UnitCreateEvent;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.mod.Plugin;
import mindustry.net.Administration.ActionType;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.storage.CoreBlock;

public class FPlagueBasic extends Plugin {
	int useless = 0;
	static boolean Have120SecondsPassed = false;
	static long gameTime = System.currentTimeMillis();
	int[] mapvotes = new int[]{0,0,0,0,0,0,0,0,0,0};

	public static mindustry.maps.Map selectedMap;
	private final Rules rules = new Rules();
	private Rules survivorBanned = new Rules();
	public static Rules plagueBanned = new Rules();
	Map<String, Team> relogTeam = new HashMap<String, Team>();
	ArrayList<String> gameovervotes = new ArrayList<String>();
	int totalplayers = 0; // for some reason i need it here cus java
	ArrayList<String> playersThatVoted = new ArrayList<String>();
	ArrayList<Tile> plagueCores = new ArrayList<Tile>();
	HashMap<String, Tile> playerCores = new HashMap<String, Tile>();
	ArrayList<String> leaders = new ArrayList<String>();
	// I need this here cus java
	Player kickedPlayer = null;
	Team teamProximityCore = null;

	@Override
    public void init(){
		// Start all plague unit multipliers in x seconds and time before all teamless are turned to plague
		new PlagueTime(120);
		
		rules.canGameOver = false; //I have my own way to game over
		rules.reactorExplosions = false; // I wonder,nah plague op
		rules.buildSpeedMultiplier = 5; // game goes faster brr
		rules.fire = false; // Obvious
		rules.logicUnitBuild = false; // You know why
		rules.damageExplosions = false; // NO NO NO
		rules.unitCap = 100; // lag prevention
		rules.unitCapVariable = false; // so cap wont change if core is upgraded
		mapvotes = new int[]{0,0,0,0,0,0,0,0,0,0};
		init_rules();
		//load events
		Events.on(EventType.ServerLoadEvent.class, event -> System.out.println("events loaded!"));
		
		// Prevents destroying of power source 
		netServer.admins.addActionFilter((action) ->{
			if(action.block != null && action.block == Blocks.powerSource && action.type == ActionType.breakBlock){
                return false;
            }
			return true;
		});
		// No placing near crux core and banned blocks incase of bug
		netServer.admins.addActionFilter((action) ->{ 
			ArrayList<Block> boulders = new ArrayList<Block>();
			boulders.add(Blocks.boulder);
			boulders.add(Blocks.daciteBoulder);
			boulders.add(Blocks.basaltBoulder);
			boulders.add(Blocks.sandBoulder);
			boulders.add(Blocks.shaleBoulder);
			boulders.add(Blocks.snowBoulder);
			if(plagueCores.isEmpty() && Vars.state.gameOver == false) {
				for(int x = 0; x < Vars.world.width(); x++) {
					for(int y = 0; y < Vars.world.height(); y++) {
						if(Vars.world.tile(x, y).block() == Blocks.coreFoundation || Vars.world.tile(x, y).block() == Blocks.coreShard || Vars.world.tile(x, y).block() == Blocks.coreNucleus) {
							if(Vars.world.tile(x, y).team() == Team.purple && Vars.world.tile(x, y).isCenter()) {
								plagueCores.add(Vars.world.tile(x, y));
							}
						}	
					}
				}
			}
			
			if(action.block != null && action.type == ActionType.placeBlock && action.tile.block() == Blocks.powerSource) {
				return false;
			}
			
			if(action.block != null && action.type == ActionType.placeBlock && action.player.team() != Team.purple && survivorBanned.bannedBlocks.contains(action.block)) {
				action.player.sendMessage("[red]NO PERMISSION TO PLACE BANNED BLOCKS");
				return false;
			}
			
			if(action.block != null && action.type == ActionType.placeBlock && action.player.team() == Team.purple && plagueBanned.bannedBlocks.contains(action.block)) {
				action.player.sendMessage("[red]NO PERMISSION TO PLACE BANNED BLOCKS");
				return false;
			}
			
			if(action.block != null && action.type == ActionType.breakBlock && boulders.contains(action.tile.block())) {
				action.tile.setNet(Blocks.air);
				return false;
			}
			
			if(action.block != null && action.type == ActionType.placeBlock){
				for(Tile b : plagueCores) {
					if(cartesianDistance(action.tile.x, action.tile.y, b.x, b.y) < 90 && action.player.team() != Team.purple) {
					return false;
					}
				}
			}
			return true;
		});
		
		
		
		// No unit control until 20 mins have passed
		netServer.admins.addActionFilter((action) ->{
			if(action.type == ActionType.control && ((System.currentTimeMillis() - gameTime) / 1000) < 1200){
				return false;
            }
			return true;
		});

		// Make core from vault using 1k thorium 
		Events.on(TapEvent.class, event -> {
			if(event.tile.block() == Blocks.vault) {
				if(event.tile.build.items().has(Items.thorium, 1000) && event.tile.build.team() != Team.purple && event.tile.build.team == event.player.team()) {
					Vars.world.tile(event.tile.build.tileX(), event.tile.build.tileY()).setNet(Blocks.coreShard, event.player.team(), 0);
				}
			}
		});
		
		// Makes you plague if you join too late also if rejoin put back on team
		Events.on(PlayerJoin.class, event -> {
			if(event.player.getInfo().timesJoined <= 1) {
				//System.out.println("player has not joined before");
				event.player.sendMessage("[yellow]Welcome to plague!");
				event.player.sendMessage("[blue]Discord Link: [yellow]https://discord.gg/rfzm5xgJSC");
				event.player.sendMessage("[purple]Plague is an attack like mode where the infected (purple) must build units to take out the survivors");
				event.player.sendMessage("[purple]Survivors can't build units, their focus is surviving purple's units, to become a survivor place a block on the outside of the map");
				event.player.sendMessage("[purple]Plague has infinite power and must attack and kill survivors (20 minutes for unit control)");
				//event.player.sendMessage("For more information, use /info");
			}

			if(relogTeam.containsKey(event.player.uuid())) {
				event.player.team(relogTeam.get(event.player.uuid()));
				Call.setRules(event.player.con, survivorBanned);
			} else if (Have120SecondsPassed == true) {
				event.player.team(Team.purple);
				Call.setRules(event.player.con, plagueBanned);
			}	else if (event.player.team() == Team.sharded) {
				Unit spawnunit = UnitTypes.gamma.spawn(Team.purple, Vars.world.width() * 4, Vars.world.height() * 4);
				event.player.unit(spawnunit);
				Call.setRules(event.player.con, survivorBanned);
			}
		}); 

		Events.on(PlayerLeave.class, event -> {
			if(event.player.team() != Team.purple && event.player.team() != Team.sharded) {
			relogTeam.put(event.player.uuid(), event.player.team());
			}
			
			if(!event.player.unit().isNull()) {
				if(event.player.unit().type == UnitTypes.gamma) {
					event.player.unit().kill();
				}
			}
			
			if(gameovervotes.contains(event.player.name)) {
				gameovervotes.remove(event.player.name);
			}
		});
		
		// Create core with block placement
		Events.on(BuildSelectEvent.class, event -> {
        	if(event.builder.team == Team.sharded && !Have120SecondsPassed) {
				int randomTeamNumber = (int) Math.floor((Math.random() * 200) + 6);
				Team chosenteam = Team.all[randomTeamNumber];
				ArrayList<String> teamcores = new ArrayList<String>();
				ArrayList<Float> closestcores = new ArrayList<Float>();
				float distanceaway = 80;


				for(int x = 0; x < Vars.world.width(); x++){
					for(int y = 0; y < Vars.world.height(); y++){

						if(Vars.world.tile(x, y).block() == Blocks.coreFoundation && Vars.world.tile(x, y).isCenter() && Vars.world.tile(x, y).build.team == chosenteam){
							teamcores.add("hascorelol");
						}
					}
				}
        	
				if(teamcores.isEmpty() == true) {
					for(Team t : Team.all) {
						if (t != chosenteam) {
							CoreBlock.CoreBuild nearestEnemyCore = Vars.state.teams.closestCore(event.builder.getPlayer().x / 8, event.builder.getPlayer().y / 8, t);
							if(nearestEnemyCore != null) {
								if(cartesianDistance(event.builder.getPlayer().x / 8, event.builder.getPlayer().y / 8, nearestEnemyCore.tileX(), nearestEnemyCore.tileY()) < distanceaway) {
									closestcores.add(cartesianDistance(event.builder.getPlayer().x, event.builder.getPlayer().y, nearestEnemyCore.tileX(), nearestEnemyCore.tileY()));
								}
							}
						}
					}
            	
            
					if(closestcores.isEmpty() == true) {
						playerCores.forEach((p, core) -> {
							//System.out.println(cartesianDistance(event.tile.x, event.tile.y, core.x, core.y));
							if(cartesianDistance(event.tile.x, event.tile.y, core.x, core.y) < distanceaway + 80) {
								//System.out.println(cartesianDistance(event.tile.x, event.tile.y, core.x, core.y));
								teamProximityCore = Vars.world.tile(core.x, core.y).build.team;
							}
						});

						if(teamProximityCore != null) {
							event.builder.getPlayer().team(teamProximityCore);
							Call.setRules(event.builder.getPlayer().con, survivorBanned);
							event.tile.setNet(Blocks.coreFoundation, Team.all[teamProximityCore.id], 0);
							for(ItemStack stack : rules.loadout) {
								Call.setItem(event.tile.build, stack.item, stack.amount);
							}
								playerCores.put(event.builder.getPlayer().uuid(), event.tile);

						} else {
							event.builder.getPlayer().team(chosenteam);
							Call.setRules(event.builder.getPlayer().con, survivorBanned);
							event.tile.setNet(Blocks.coreFoundation, Team.all[randomTeamNumber], 0);
							for(ItemStack stack : rules.loadout) {
								Call.setItem(event.tile.build, stack.item, stack.amount);
							}
							playerCores.put(event.builder.getPlayer().uuid(), event.tile);
							leaders.add(event.builder.getPlayer().uuid());
						}
					}
				}
				teamcores.clear();
				closestcores.clear();
				teamProximityCore = null;
        	}
        });

		// Some maps have power infs outside of no build oh well invincibility time
		Events.on(BlockDestroyEvent.class, event -> {
	          if(event.tile.block() == Blocks.powerSource) {
	        	 event.tile.setNet(Blocks.powerSource, event.tile.team(), 0);
	          }
		});
		
		
		// Hell no no ono
		Events.on(GameOverEvent.class, event -> {
			gameTime = System.currentTimeMillis();
			relogTeam.clear();
			gameovervotes.clear();	
			plagueCores.clear();
			playerCores.clear();
			leaders.clear();
			PlagueTime.timer.cancel();
			PlagueTime.multiplier1.cancel();
			PlagueTime.gameover.cancel();
			PlagueTime.resetToDefaults();
			Have120SecondsPassed = false;
			new PlagueTime(120); // amount of seconds until everything actually starts
			
			// Why,why would you even do more timers Fitmo.. WHY?! Cursed existence
			mostVotedMap();

			if(selectedMap != null) {
				Groups.player.each(p -> {
					p.sendMessage("[yellow]Voted map is: " + selectedMap.name());
				});
				new MapChangerThings();
			}
		});
		
		Events.on(UnitCreateEvent.class, event -> {
			if(event.unit.type == UnitTypes.mono) {
				event.unit.team.items().add(Items.lead, 800);
				event.unit.team.items().add(Items.copper, 750);
				event.unit.kill();
			}
		});
	}
	
	
	
	
	// Turns you infected
	@Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("infect", "You become [purple]INFECTED", (args, player) -> {
        	if(player.team() != Team.purple) {
				useless = 0;
				Team playerteam = player.team();
				Groups.player.each(p -> {
					if(p.team() == player.team()){
						useless++;
					}
				});
				if(useless == 1){
					for(int x = 0; x < Vars.world.width(); x++) {
						for(int y = 0; y < Vars.world.height(); y++) {
							if(Vars.world.tile(x, y).block() == Blocks.coreFoundation && Vars.world.tile(x, y).isCenter() && Vars.world.tile(x, y).build.team == playerteam){
								Vars.world.tile(x, y).setNet(Blocks.air);
							}
						}
					}
				}
        	}

        	Call.setRules(player.con, plagueBanned);
        	player.team(Team.purple);  	
        	useless = 0;
        });

		handler.<Player>register("rules", "All the rules on the server", (args, player) -> {
			player.sendMessage("1. Don't grief");
			player.sendMessage("2. Griefing includes wasting resources, you know who you are.");
			player.sendMessage("3. Shooting someone's core with a core unit is punishable by a kick, as it blocks visibility of resource display with a warning message.");
			player.sendMessage("4. Don't be toxic.");
			player.sendMessage("5. No blast bombing");
			player.sendMessage("6. Surv vs surv combat is a kickable offense");
			player.sendMessage("7. Don't build within a survivor's territory as plague. You know what they have conquered, use your head.");
		});

		handler.<Player>register("info" ,"provides information about the plague game mode", (args, player) -> {
			player.sendMessage("Plague is an attack like mode where the infected (purple) must build units to take out the survivors");
			player.sendMessage("Survivors can't build units, their focus is surviving purple's units, to become a survivor place a block on the outside of the map");
			player.sendMessage("Plague has infinite power and must attack and kill survivors (20 minutes for unit control)");
		});

		// Kick a player from the team
        handler.<Player>register("teamkick", "<player>", "Kick a player from your team", (args, player) -> {
        
        Groups.player.each(p ->{
        	if(Strings.stripColors(p.name).equalsIgnoreCase(args[0])) {
        		kickedPlayer = p;
        	}
        });
        	if(kickedPlayer == null) {
        		player.sendMessage("No player with such name");
        	} else {
        		if(leaders.contains(player.uuid()) && player.team() == kickedPlayer.team() && player != kickedPlayer) {
        			if(Have120SecondsPassed == true){
        				kickedPlayer.team(Team.purple);
        			} else {
        				kickedPlayer.team(Team.sharded);
        			}
        			Tile playerCore = playerCores.get(kickedPlayer.uuid());
        			playerCore.setNet(Blocks.air);
        		    playerCores.remove(kickedPlayer.uuid());
        		    
        		} else {
        			player.sendMessage("[red]You aren't a leader/team creator or target player is in another team");
        		}
        		
        	}
        	kickedPlayer = null;	
        });

        // It shows how long a round has lasted duh
        handler.<Player>register("time", "Check how long game has lasted", (args, player) -> {
        	player.sendMessage("[purple]Game has lasted [green]" + ((System.currentTimeMillis() - gameTime) / 60000) + " [purple]minutes");
        	
        });
        
        // Kills currently controlled unit
        handler.<Player>register("kill", "Kills currently controlled unit", (args, player) -> {
        	player.unit().kill();
        });

        // Respawns unit
        handler.<Player>register("respawn", "Respawn your gamma if bugged as sharded", (args, player) -> {
			if(player.team() == Team.sharded) {
				if(!player.unit().isNull()) {
					player.unit().kill();
				}
				Unit spawnunit = UnitTypes.gamma.spawn(Team.purple, Vars.world.width() * 4, Vars.world.height() * 4);
				player.unit(spawnunit);
			}
        });
        
        // Start voting to select next map or see all maps
        handler.<Player>register("rtv", "[MapNumber]" ,"Vote for next map or see all maps", (args, player) -> {
			if(args.length == 1) {

				Seq<mindustry.maps.Map> allcustommaps = mindustry.Vars.maps.customMaps();
				if(!playersThatVoted.contains(player.name)) {
					if(args[0].matches("[0-9]+")) {
						try {
							int votednumber = Integer.parseInt(args[0]) - 1;
							mapvotes[votednumber]++;
							player.sendMessage("You voted for map " + allcustommaps.get(votednumber).name());
							playersThatVoted.add(player.name);
						} catch(Exception e) {
							player.sendMessage("Vote failed");
						}
					}
				} else {
					player.sendMessage("You already voted");
				}
			} else {
				int mapnumber = 0;
				player.sendMessage("All maps:");
				Seq<mindustry.maps.Map> list = mindustry.Vars.maps.customMaps();

				for(mindustry.maps.Map map : list) {
					mapnumber++;
					player.sendMessage(mapnumber + " " + map.name());
				}
			}
		});
        
        // How do I explain.. if 4/5th of players voted then game ends
        handler.<Player>register("endgame", "[purple] Vote for the game to end", (args, player) -> { 	
        	
        	if(Have120SecondsPassed) {
				if(gameovervotes.contains(player.name)) {
						gameovervotes.remove(player.name);
				} else {
					gameovervotes.add(player.name);
				}
				totalplayers = 0;
				Groups.player.each(p -> {
					totalplayers++;
				});

				Groups.player.each(p -> {
					if(totalplayers <= 5) {
						p.sendMessage("[yellow] There are currently [purple]" + gameovervotes.size() + "[yellow] of [purple]" + 2 + "[yellow] votes to end the game. Vote with /endgame");
					} else {
						p.sendMessage("[yellow] There are currently [purple]" + gameovervotes.size() + "[yellow] of [purple]" + (totalplayers / 5 * 4) + "[yellow] votes to end the game. Vote with /endgame");
					}
				});

				if(totalplayers <= 5) {
					if(gameovervotes.size() >= 2) {
						Events.fire(new GameOverEvent(Team.purple));
					}
				} else if((totalplayers / 5 * 4) <= gameovervotes.size()) {
					Events.fire(new GameOverEvent(Team.purple));
				}
			}
        });
        
        
        
        // Gameover command for admins
        handler.<Player>register("gameover", "Ends round,Admin only", (args, player) -> {
        	if(player.admin() == true) {
        		Events.fire(new GameOverEvent(Team.purple));
        	}
        });
        
        
        handler.<Player>register("gameover2", "Ends round,only used if game didn't end", (args, player) -> {
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
        	
        	if(cores.isEmpty() && !Vars.state.gameOver && Have120SecondsPassed) {
				Events.fire(new GameOverEvent(Team.purple));
			}
			cores.clear();
        	
        	
        });

		handler.<Player>register("playerlist", "Shows all players and their team", (args, player) -> {
			Groups.player.each(p -> {
				if(p.team() != Team.crux) {
					if(leaders.contains(p.uuid())) {
						player.sendMessage("[red]" + p.name + "(Leader)" + " [yellow]Team ID:" + p.team().id);
					} else {
						player.sendMessage("[red]" + p.name + " [yellow]Team ID:" + p.team().id);
					}
				}
			});
		});

	}

	//this thing has more than rules as you can see lmao
	void init_rules(){
		
		Vars.state.rules.canGameOver = false; //I have my own way to game over
		Vars.state.rules.reactorExplosions = false; // I wonder,nah plague op
		Vars.state.rules.buildSpeedMultiplier = 5; // game goes faster brr
		Vars.state.rules.fire = false; // Obvious
		Vars.state.rules.logicUnitBuild = false; // You know why
		Vars.state.rules.damageExplosions = false; // NO NO NO
		Vars.state.rules.unitCap = 100; // lag prevention
		Vars.state.rules.unitCapVariable = false; // so cap wont change if core is upgraded
		
				
        survivorBanned = rules.copy();
        survivorBanned.bannedBlocks.addAll(Blocks.groundFactory, Blocks.navalFactory, Blocks.commandCenter, Blocks.multiplicativeReconstructor);
        
        plagueBanned = rules.copy();
        plagueBanned.bannedBlocks.addAll(Blocks.battery, Blocks.batteryLarge, Blocks.steamGenerator, Blocks.combustionGenerator, Blocks.differentialGenerator, Blocks.rtgGenerator, Blocks.thermalGenerator, Blocks.impactReactor, Blocks.duo, Blocks.scatter, Blocks.scorch, Blocks.hail, Blocks.lancer, Blocks.arc, Blocks.parallax, Blocks.swarmer, Blocks.salvo, Blocks.segment, Blocks.tsunami, Blocks.fuse, Blocks.ripple, Blocks.cyclone, Blocks.foreshadow, Blocks.spectre, Blocks.meltdown, Blocks.navalFactory, Blocks.copperWall, Blocks.copperWallLarge, Blocks.titaniumWall, Blocks.titaniumWallLarge, Blocks.plastaniumWall, Blocks.plastaniumWallLarge, Blocks.thoriumWall, Blocks.thoriumWallLarge, Blocks.phaseWall, Blocks.phaseWallLarge, Blocks.surgeWall, Blocks.surgeWallLarge, Blocks.door, Blocks.doorLarge, Blocks.thoriumReactor, Blocks.solarPanel, Blocks.largeSolarPanel); // Can't be trusted
        
        Blocks.powerSource.health = Integer.MAX_VALUE;
        
        // Disable default unit damage
        UnitTypes.alpha.weapons.clear();
        UnitTypes.beta.weapons.clear();
        UnitTypes.gamma.weapons.clear();
        UnitTypes.flare.weapons.clear();
        UnitTypes.horizon.weapons.clear();
        UnitTypes.zenith.weapons.clear();
        UnitTypes.antumbra.weapons.clear();
        UnitTypes.eclipse.weapons.clear();
        UnitTypes.horizon.abilities.clear();
        UnitTypes.alpha.health = 1;
        UnitTypes.beta.health = 1;
        UnitTypes.gamma.health = 1;
        UnitTypes.flare.health = 1;
        
        //Disable pickup,interesting variable
        UnitTypes.oct.payloadCapacity = 0;
        UnitTypes.mega.payloadCapacity = 0;
        UnitTypes.quad.payloadCapacity = 0;
        
        // Easily changable starting items for survivors
        rules.loadout.clear();
        rules.loadout.add(new ItemStack(Items.copper, 4000));
        rules.loadout.add(new ItemStack(Items.lead, 4000));
        rules.loadout.add(new ItemStack(Items.graphite, 2000));
        rules.loadout.add(new ItemStack(Items.silicon, 1000));
        rules.loadout.add(new ItemStack(Items.titanium, 2000));
        rules.loadout.add(new ItemStack(Items.metaglass, 1000));
        
        //foreshadow build damage removed cus yes
        ((ItemTurret) Blocks.foreshadow).ammoTypes.get(Items.surgeAlloy).buildingDamageMultiplier = 0;
    }

	public mindustry.maps.Map findmap(String mapname) {
		mindustry.maps.Map findMap;
		findMap = maps.all().find(map -> Strings.stripColors(map.name().replace('_', ' ')).equalsIgnoreCase(Strings.stripColors(mapname)));
    	if(findMap != null) {
    		return findMap;
    	}
    	return null;
	}
	
	public mindustry.maps.Map mostVotedMap(){
		playersThatVoted.clear();
		Seq<mindustry.maps.Map> allmaps = mindustry.Vars.maps.customMaps();
		int max = -1;
		for(int i = 0; i < 10; i++){
			if(mapvotes[i]>(max == -1 ? 0 : mapvotes[max])){
				max=i;
			}
		}
		        
		if(max != -1){
			mapvotes = new int[]{0,0,0,0,0,0,0,0,0,0};
			selectedMap = allmaps.get(max);
			return allmaps.get(max);
		} else {
			return null;
		}
	}
	
	public float closestCore(int playerx, int playery, Team team) {
		CoreBlock.CoreBuild nearestCoreTeam0 = Vars.state.teams.closestCore(playerx / 8, playery / 8, team);
		if(nearestCoreTeam0 != null) {
			float nearestCoreCartTeam0 = cartesianDistance(floatToShort(playerx / 8), floatToShort(playery / 8), nearestCoreTeam0.tileX(), nearestCoreTeam0.tileY());
			return nearestCoreCartTeam0;
		} else {
			return 1000;
		}
	}
	
	public static short floatToShort(float x) {
	    if (x < Short.MIN_VALUE) {
	        return Short.MIN_VALUE;
	    }
	    if (x > Short.MAX_VALUE) {
	        return Short.MAX_VALUE;
	    }
	    return (short) Math.round(x);
	}
	
	public float cartesianDistance(float x, float y, float cx, float cy){
        return (float) Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2) );
    }
}
