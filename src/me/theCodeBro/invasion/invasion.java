package me.theCodeBro.invasion;






import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.kitteh.tag.PlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

public class invasion extends JavaPlugin implements Listener{
	public boolean running = false;
	public boolean midRunning = false;
	
	
	public List<String> red = new ArrayList<String>();
	public List<String> blue = new ArrayList<String>();
	Random rand = new Random();

	int broken = 0;

	int countDownSpawn = 60;
	int time = 1200;
	
	int limit = 3;
	
	public boolean midJoin = false;
	

	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event){
		Player player = event.getPlayer();
		event.setCancelled(true);
		if(event.isCancelled()){
			for(Player p : Bukkit.getOnlinePlayers()){
				if(getTeam(player) == getTeam(p)){
					p.sendMessage("<" + player.getDisplayName() + ">  " + event.getMessage());
				}
			}
		}
	}
	
	
	@EventHandler
	public void tag(PlayerReceiveNameTagEvent event) {
		Player target = event.getNamedPlayer();
		Player player = event.getPlayer();
		if (getTeam(target) == 1) {
			event.setTag(ChatColor.RED + target.getDisplayName());
			TagAPI.refreshPlayer(target, player);
		} else if (getTeam(target) == 2) {
			event.setTag(ChatColor.BLUE + target.getDisplayName());
			TagAPI.refreshPlayer(target, player);
		}
	}

	@EventHandler
	public void damgeEvent(EntityDamageEvent e) {
		if(e.getCause() == DamageCause.ENTITY_ATTACK){
		EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
		Entity attacker = event.getDamager();
		Entity target = event.getEntity();
		if (attacker instanceof Player && target instanceof Player) {
			Player player = (Player) attacker;
			Player def = (Player) target;
			if (getTeam(player) == getTeam(def)) {
				event.setCancelled(true);
			}
		}
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
            if(getTeam(player) == 1){
            	player.teleport(loc[0]);
            	event.setRespawnLocation(loc[0]);
            }else if(getTeam(player) == 2){
            	player.teleport(loc[4]);
            	event.setRespawnLocation(loc[4]);
            }
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (getTeam(player) != 2 && event.getBlock().getType() == Material.SPONGE) {
			broken++;

			if (broken == 1) {
				Bukkit.broadcastMessage(ChatColor.GOLD + "Only One "+ChatColor.YELLOW+ "Sponge " + ChatColor.GOLD +  "Left!");

			}
		}else if(getTeam(player) == 2){
			event.setCancelled(true);
		}

	}
	
	
	public void clear(Player player){
		
		PlayerInventory pi = player.getInventory();
		pi.clear();
		pi.setBoots(null);
		pi.setChestplate(null);
		pi.setHelmet(null);
		pi.setLeggings(null);
		
	}
	

	

	public void reset() {
		red.clear();
		blue.clear();
		broken = 0;
		countDownSpawn = 60;
		time = 1200;
		running = false;
		midRunning = false;
		
		for(Player player : Bukkit.getOnlinePlayers()){
			if(getTeam(player) != 0){
			clear(player);
			}
		}

	}

	@Override
	public void onDisable() {
		getLogger().info("Invasion disabled!");
	}
	//Red = Invaders
	//Blue = Defenders

 
	@Override
	public void onEnable() {
		loadConfiguration();
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
					
					@Override
					public void run() {
						if (!running && countDownSpawn == 60
								&& red.size() >= limit && blue.size() >= limit) {
							midRunning = true;
							for (Player p : Bukkit.getOnlinePlayers()) {
								if (getTeam(p) == 2) {
									p.teleport(loc[1]);
								}
								
							}
							Bukkit.broadcastMessage(ChatColor.GOLD
									+ "The Game Has Started! The DEFENDERS Have A 60 Second Head Start!");
						}
						if (midRunning) {
							countDownSpawn--;
							if (countDownSpawn <= 10) {
								Bukkit.broadcastMessage(ChatColor.GOLD + ""
										+ countDownSpawn);
							}
						}
						
						
						
						if (running) {
							time -= 1;
							if (time == 900) {
								Bukkit.broadcastMessage(ChatColor.GOLD
										+ "15 Minutes Remaining!");
							} else if (time == 600) {
								Bukkit.broadcastMessage(ChatColor.GOLD
										+ "10 Minutes Remaining!");
							} else if (time == 300) {
								Bukkit.broadcastMessage(ChatColor.GOLD
										+ "5 Minutes Remaining!");
							}
						}
						if (countDownSpawn == 0) {
							midRunning = false;
							running = true;
                            countDownSpawn = 1;
							
							for (Player p : Bukkit.getOnlinePlayers()) {
								if (getTeam(p) == 1) {
									p.teleport();
								}
							}
							Bukkit.broadcastMessage(ChatColor.GOLD
									+ "The INVADERS Have Been Released!");
						}
                                if(time == 0 && broken != 2){
                                	for (Player p : Bukkit.getOnlinePlayers()) {
        								if (getTeam(p) != 0) {
        									p.teleport(p.getWorld().getSpawnLocation());

        								}
        								Bukkit.broadcastMessage(ChatColor.BLUE
        										+ "The Game Has Ended! DEFENDERS Are Victorious!");
        								reset();
        							}
                                }
						if (running && broken == 2) {
							for (Player p : Bukkit.getOnlinePlayers()) {
								if (getTeam(p) != 0) {
									p.teleport(p.getWorld().getSpawnLocation());

								}
								
							}
							Bukkit.broadcastMessage(ChatColor.RED
									+ "The Game Has Ended! INVADERS Are Victorious!");
							reset();
						}
						
						for(Player player : Bukkit.getOnlinePlayers()){
							if(player.getLocation() == player.getWorld().getSpawnLocation()){
							   if(getTeam(player) == 1){
					            	player.teleport(loc[0]);
					            }else if(getTeam(player) == 2){
					            	player.teleport(loc[4]);
					            }
							}
					}
					}
				}, 0L, 20L);

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		getLogger().info("Invasion enabled!");
		

	}
	
	
	
	
	
	
	
	
	

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		Player player = (Player) sender;
		if (commandLabel.equalsIgnoreCase("join")) {
			if (red.size() > blue.size()) {//if red team has more members add the player to the blue team.
		        blue.add(player.getName());
		        player.sendMessage(ChatColor.GREEN+"Welcome to the " +ChatColor.DARK_BLUE+"DEFENDERS "+ChatColor.GREEN+"team!");
		    } else if (red.size() < blue.size()) {//if blue team has more members add the player to the red team.
		        red.add(player.getName());
		        player.sendMessage(ChatColor.GREEN+"Welcome to the " +ChatColor.DARK_RED+"INVAIDERS "+ChatColor.GREEN+"team!");
		    }else if(red.contains(player.getName())){
		    	player.sendMessage(ChatColor.RED+"Sorry! You are already in the"+ChatColor.DARK_RED+" RED "+ChatColor.RED+"team!");
		    }else if(blue.contains(player.getName())){
		    	player.sendMessage(ChatColor.RED+"Sorry! You are already in the"+ChatColor.DARK_BLUE+" BLUE "+ChatColor.RED+"team!");
		    }else if(blue.size() == limit){
		    	player.sendMessage(ChatColor.RED+"Sorry! But the"+ChatColor.DARK_BLUE+" INVAIDERS"+ChatColor.RED+" team is full. Try again soon!");
		    	return false;
		    }else if(red.size() == limit){
		    	player.sendMessage(ChatColor.RED+"Sorry! But the"+ChatColor.DARK_RED+" DEFENDERS"+ChatColor.RED+" team is full. Try again soon!");
		    	return false;
		    }else if(midJoin && red.size() != limit && red.size() < blue.size()){
		    	red.add(player.getName());
		    	player.sendMessage(ChatColor.GREEN+"Welcome to the " +ChatColor.DARK_RED+"INVAIDERS "+ChatColor.GREEN+"team!");
		    	midJoin = false;
		    	return true;
		    }else if(midJoin && blue.size() != limit && blue.size() < red.size()){
		    	red.add(player.getName());
		    	player.sendMessage(ChatColor.RED+"Sorry! You are already in the"+ChatColor.DARK_BLUE+" BLUE "+ChatColor.RED+"team!");
		    	midJoin = false;
		    	return true;
		    } else {
		        Integer team = rand.nextInt(2);
		        if (team == 1) {;//if the teams are equal randomly select which team.
		            red.add(player.getName());
		        } else {
		            blue.add(player.getName());
		} else if (commandLabel.equalsIgnoreCase("leave")) {
			
			team.remove(player.getName());
			player.teleport(player.getWorld().getSpawnLocation());
			clear(player);
			if(getTeam(player) == 1){
				red--;
				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.GOLD
						+ " left the " + ChatColor.RED + "red" + ChatColor.GOLD + " team!, now someone can join!");
			}else if(getTeam(player) == 2){
				blue--;
			}if(running || midRunning){
				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.GOLD
						+ " left the " + ChatColor.BLUE + "blue" + ChatColor.GOLD + " team!, now someone can join!");
				}
			}
			if(running || midRunning){
			midJoin = true;
			player.sendMessage(ChatColor.GREEN + "You Left The Game");
		} else if (commandLabel.equalsIgnoreCase("setLimit")) {
			if(args.length == 1){
				if(player.isOp()){
				int max = Integer.parseInt(args[0]);
			   limit = max;  
			   player.sendMessage(ChatColor.GREEN + "Team Limit Set To " + String.valueOf(limit));
				}else{
					player.sendMessage(ChatColor.RED + "You Need To Be An OP For That command");
				}
			}
		}else if (commandLabel.equalsIgnoreCase("start")) {
			if(!midRunning && !running){
			if(player.isOp()){
				midRunning = true;
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (getTeam(p) == 2) {
						p.teleport(loc[1]);
					}
					
				}
				Bukkit.broadcastMessage(ChatColor.GOLD
						+ "The Game Has Started! The DEFENDERS Have A 60 Second Head Start!");
			}else{
				player.sendMessage(ChatColor.RED + "You Need To Be An OP For That command");
			}
			}else{
				player.sendMessage(ChatColor.RED + "The Game Is Already Running");
			}
		}
		return false;
	}
   
