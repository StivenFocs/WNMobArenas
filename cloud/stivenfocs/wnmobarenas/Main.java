package cloud.stivenfocs.wnmobarenas;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;

public class Main extends JavaPlugin implements Listener {
	
	public static Main plugin;
	
	public static final Random random = new Random();
	public Integer mainTaskId;
	
	public static Boolean spawnToggle = true;
	public static LinkedHashMap<String, ItemData> items = new LinkedHashMap<>();
	public static HashMap<String, Mob> mobs = new HashMap<>();
	public static HashMap<String, ArenaRegion> arenaRegions = new HashMap<>();
	public static HashMap<UUID, AliveMob> mobEntities = new HashMap<>();

	public void onEnable() {
		plugin = this;
		
		getCommand("wnmobarenas").setExecutor(this);
		getCommand("wnmobarenas").setTabCompleter(this);
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		reload();
	}
	
	public void onDisable() {
		for (AliveMob aliveMob : mobEntities.values()) {
			aliveMob.livingEntity.remove();
		}
	}
	
	public boolean reload() {
		try {
			reloadConfig();
			
			getConfig().options().header("Developed with LOV by StivenFocs for WitherNetwork");
			getConfig().options().copyDefaults(true);
			
			if (getConfig().get("options.items") == null) getConfig().createSection("options.items");
			if (getConfig().get("options.mobs") == null) {
				getConfig().set("options.mobs.common_zombie.displayname", "&a&lCommon Zombie");
				getConfig().set("options.mobs.common_zombie.visible_displayname", true);
				getConfig().set("options.mobs.common_zombie.type", "ZOMBIE");
				getConfig().set("options.mobs.common_zombie.health", 20.0);
				getConfig().set("options.mobs.common_zombie.is_boss", false);
				getConfig().set("options.mobs.common_zombie.inventory.helmet", "common_zombie_helmet");
				getConfig().set("options.mobs.common_zombie.inventory.chestplate", "common_zombie_chestplate");
				getConfig().set("options.mobs.common_zombie.inventory.leggings", "common_zombie_leggings");
				getConfig().set("options.mobs.common_zombie.inventory.boots", "common_zombie_boots");
				getConfig().set("options.mobs.common_zombie.inventory.hand_item", "common_zombie_sword");
				getConfig().set("options.mobs.common_zombie.drops", new ArrayList<>());
				getConfig().set("options.mobs.common_zombie.earn_per_hit", 5);
				getConfig().set("options.mobs.common_zombie.earn_per_kill", 200);
				getConfig().set("options.mobs.common_zombie.parameters.IsBaby", true);
			}
			if (getConfig().get("options.regions") == null) getConfig().createSection("options.regions");
			getConfig().addDefault("options.mob_spawn_delay", 400);
			getConfig().addDefault("options.boss_spawn_delay", 1000);
			
			getConfig().addDefault("messages.not_permitted", "&cYou are not allowed to do this.");
			getConfig().addDefault("messages.configuration_reloaded", "&2The WNMobArenas configuration file has been reloaded");
			getConfig().addDefault("messages.an_error_occurred", "&4Oh No! &cAn error occurred while trying to execute this task! The plugin could've stopped working.");
			getConfig().addDefault("messages.only_players", "&cSorry but only a player can execute this command!");
			getConfig().addDefault("messages.incomplete_command", "&4Something is missing! &cSomething is missing, you can see the subcommands list by using /wnmobarenas");
			getConfig().addDefault("messages.need_an_item", "&eYou have to hold and item in hand to set it.");
			getConfig().addDefault("messages.item_not_found", "&cItem with name &f%item% &cnot found");
			getConfig().addDefault("messages.item_saved", "&aItem successfully set in the configuration file.");
			getConfig().addDefault("messages.you_got_wand", "&aYou got the &5&lRegionWand");
			getConfig().addDefault("messages.invalid_region", "&cInvalid region selection");
			getConfig().addDefault("messages.region_locations_set", "&f%region%&a's corners successfully set");
			getConfig().addDefault("messages.unknown_region", "&4Unknown region! &ccan't find this region in the configuration");
			getConfig().addDefault("messages.spawner_set", "%name% &7set at this location.");
			getConfig().addDefault("messages.unknown_subcommand", "&4Unknown subcomand! &cyou can see the subcommands list by using /wnmobarenas");
			getConfig().addDefault("messages.integer_needed", "&cAn integer number is needed!");
			getConfig().addDefault("messages.spawner_not_found", "&cSpawner with name &f%spawner% &cnot found in region &f%region%");
			getConfig().addDefault("messages.mob_not_found", "&cMob with name &f%mob% &cnot found.");
			getConfig().addDefault("messages.mob_spawned", "&2Mob spawned");
			getConfig().addDefault("messages.added_to_inventory", "&aItem added to your inventory");
			getConfig().addDefault("messages.spawners_toggled", "&eSpawners status toggled");
			getConfig().addDefault("messages.item_removed", "&cItem removed from the list.");
			List<String> newHelp = new ArrayList<>();
			newHelp.add("&8&m========================");
			newHelp.add("&7&l* &b&lW&e&lN&d&lMobArenas");
			newHelp.add(" ");
			newHelp.add("&7&l* &7/wnmobarenas reload");
			newHelp.add("&7&l* &7/wnmobarenas toggle");
			newHelp.add("&7&l* &7/wnmobarenas getwand");
			newHelp.add("&7&l* &7/wnmobarenas setregion <regionName>");
			newHelp.add("&7&l* &7/wnmobarenas setspawner <regionName> <spawnerName>");
			newHelp.add("&7&l* &7/wnmobarenas spawn <regionName> <spawnerName> <mobName>");
			newHelp.add("&7&l* &7/wnmobarenas setitem <itemName>");
			newHelp.add("&7&l* &7/wnmobarenas getitem <itemName>");
			newHelp.add("&7&l* &7/wnmobarenas removeitem <itemName>");
			newHelp.add("&7&l* &7/wnmobarenas viewitems [index offset]");
			newHelp.add("&7&l* &7/wnmobarenas pos1");
			newHelp.add("&7&l* &7/wnmobarenas pos2");
			newHelp.add(" ");
			newHelp.add("&8&m========================");
			getConfig().addDefault("messages.help", newHelp);
			
			saveConfig();
			reloadConfig();
			
			if (mainTaskId != null) Bukkit.getScheduler().cancelTask(mainTaskId);
			for (AliveMob aliveMob : mobEntities.values()) {
				aliveMob.livingEntity.remove();
			}
			mobEntities.clear();
			
			items.clear();
			for (String itemName : getConfig().getConfigurationSection("options.items").getKeys(false)) {
				try {
					Boolean customDisplayNameVisible = getConfig().getBoolean("options.items." + itemName + ".custom_displayname_visible", false);
					String customDisplayName = getConfig().getString("options.items." + itemName + ".custom_displayname", itemName);
					items.put(itemName, new ItemData(((ItemStack) getConfig().get("options.items." + itemName + ".item")), customDisplayNameVisible, customDisplayName));
					//getLogger().info("Item loaded: " + itemName);
				} catch (Exception ex) {
					getLogger().warning("Unable to get the item: " + itemName);
				}
			}
			mobs.clear();
			for (String mobName : getConfig().getConfigurationSection("options.mobs").getKeys(false)) {
				try {
					String displayname = getConfig().getString("options.mobs." + mobName + ".displayname", mobName);
					Boolean visibleDisplayName = getConfig().getBoolean("options.mobs." + mobName + ".visible_displayname");
					EntityType type;
					try {
						type = EntityType.valueOf(getConfig().getString("options.mobs." + mobName + ".type"));
					} catch (Exception ex) {
						getLogger().info("Unrecognized EntityType of the mob \"" + mobName + "\", due to this, the mob has not been registered.");
						continue;
					}
					Double health = getConfig().getDouble("options.mobs." + mobName + ".health", 20.0);
					Boolean isBoss = getConfig().getBoolean("options.mobs." + mobName + ".is_boss");
					ItemStack helmet = null, chestplate = null, leggings = null, boots = null, handItem = null;
					if (items.containsKey(getConfig().getString("options.mobs." + mobName + ".inventory.helmet"))) helmet = getItemByName(getConfig().getString("options.mobs." + mobName + ".inventory.helmet"));
					if (items.containsKey(getConfig().getString("options.mobs." + mobName + ".inventory.chestplate"))) chestplate = getItemByName(getConfig().getString("options.mobs." + mobName + ".inventory.chestplate"));
					if (items.containsKey(getConfig().getString("options.mobs." + mobName + ".inventory.leggings"))) leggings = getItemByName(getConfig().getString("options.mobs." + mobName + ".inventory.leggings"));
					if (items.containsKey(getConfig().getString("options.mobs." + mobName + ".inventory.boots"))) boots = getItemByName(getConfig().getString("options.mobs." + mobName + ".inventory.boots"));
					if (items.containsKey(getConfig().getString("options.mobs." + mobName + ".inventory.hand_item"))) handItem = getItemByName(getConfig().getString("options.mobs." + mobName + ".inventory.hand_item"));
					HashMap<ItemData, Integer> new_drops = new HashMap<>();
					for (String rawDropItemName : getConfig().getStringList("options.mobs." + mobName + ".drops")) {
						try {
							String[] drop = rawDropItemName.split(",");
							if (drop.length > 1 && isDigit(drop[1])) {
								if (items.containsKey(drop[0])) new_drops.put(items.get(drop[0]), Integer.parseInt(drop[1]));
								else getLogger().warning("No item called \"" + drop[0] + "\" from the drop \"" + rawDropItemName + "\" of the mob \"" + mobName + "\"");
							} else getLogger().warning("Unable to parse the mob \"" + mobName + "\" drop \"" + rawDropItemName + "\" for: invalid structure (Example: common_sword,50)");
						} catch (Exception ex) {
							getLogger().warning("Unable to parse the mob \"" + mobName + "\" drop \"" + rawDropItemName + "\" for: " + ex.getMessage());
							ex.printStackTrace();
						}
					}
					Double earnPerHit = getConfig().getDouble("options.mobs." + mobName + ".earn_per_hit", 0.00D);
					Double earnPerKill = getConfig().getDouble("options.mobs." + mobName + ".earn_per_kill", 0.00D);
					HashMap<String, Object> parameters = new HashMap<>();
					if (getConfig().get("options.mobs." + mobName + ".parameters") != null) {
						for (String field : getConfig().getConfigurationSection("options.mobs." + mobName + ".parameters").getKeys(false)) {
							parameters.put(field, getConfig().get("options.mobs." + mobName + ".parameters." + field));
						}
					}
					Mob passengerMob = null;
					if (getConfig().getString("options.mobs." + mobName + ".passenger") != null && mobs.containsKey(getConfig().getString("options.mobs." + mobName + ".passenger"))) passengerMob = mobs.get(getConfig().getString("options.mobs." + mobName + ".passenger")); 
					mobs.put(mobName, new Mob(mobName, displayname, visibleDisplayName, type, health, isBoss, helmet, chestplate, leggings, boots, handItem, new_drops, earnPerHit, earnPerKill, parameters, passengerMob));
					//getLogger().info("Mob loaded: " + mobName);
				} catch (Exception ex) {
					getLogger().warning("An exception occurred while trying to parse the mob : " + ex.getMessage());
					ex.printStackTrace();
				}
			}
			arenaRegions.clear();
			for (String arenaRegionName : getConfig().getConfigurationSection("options.regions").getKeys(false)) {
				try {
					String displayname = getConfig().getString("options.regions." + arenaRegionName + ".displayname", arenaRegionName);
					HashMap<String, RegionSpawner> newRegionSpawners = new HashMap<>();
					
					for (String spawnerName : getConfig().getConfigurationSection("options.regions." + arenaRegionName + ".spawners").getKeys(false)) {
						String spawnerDisplayName = getConfig().getString("options.regions." + arenaRegionName + ".spawners." + spawnerName + ".displayname", spawnerName);
						Integer maxSpawnedMobs = getConfig().getInt("options.regions." + arenaRegionName + ".spawners." + spawnerName + ".max_spawned_mobs", 15);
						Integer maxSpawnedBosses = getConfig().getInt("options.regions." + arenaRegionName + ".spawners." + spawnerName + ".max_spawned_bosses", maxSpawnedMobs);
						
						Location spawnerLocation = null;
						try {
							spawnerLocation = stringToLocation(getConfig().getString("options.regions." + arenaRegionName + ".spawners." + spawnerName + ".location"));
						} catch (Exception ex) {
							getLogger().warning("Unable to parse the location of the spawner \"" + spawnerName + "\" of the region \"" + getName() + "\" for: " + ex.getMessage());
							continue;
						}
						
						HashMap<Mob, Integer> newMobs = new HashMap<>();
						HashMap<Mob, Integer> newBosses = new HashMap<>();
						for (String rawSpawnerMob : getConfig().getStringList("options.regions." + arenaRegionName + ".spawners." + spawnerName + ".mobs")) {
							try {
								String[] spawnerMob = rawSpawnerMob.split(",");
								if (spawnerMob.length > 1 && isDigit(spawnerMob[1])) {
									if (mobs.containsKey(spawnerMob[0])) {
										if (mobs.get(spawnerMob[0]).isBoss()) newBosses.put(mobs.get(spawnerMob[0]), Integer.parseInt(spawnerMob[1]));
										else newMobs.put(mobs.get(spawnerMob[0]), Integer.parseInt(spawnerMob[1]));
									} else getLogger().warning("No mob called \"" + spawnerMob[0] + "\" from the mob \"" + rawSpawnerMob + "\" of the spawner \"" + spawnerName +  "\" from the region \"" + arenaRegionName + "\"");
								} else getLogger().warning("Unable to parse the mob \"" + rawSpawnerMob + "\" of the spawner \"" + spawnerName + "\" of the region \"" + arenaRegionName + "\" for: invalid structure (Example: common_zombie,50)");
							} catch (Exception ex) {
								getLogger().warning("Unable to parse the mob \"" + rawSpawnerMob + "\" of the spawner \"" + spawnerName + "\" of the region \"" + arenaRegionName + "\" for: " + ex.getMessage());
							}
						}
						LinkedHashMap<Mob, Integer> sortedMobsRarity = new LinkedHashMap<>();
						ArrayList<Integer> numbersList = new ArrayList<>();
						numbersList.addAll(newMobs.values());
						Collections.sort(numbersList);
						for (Integer number : numbersList) {
							for (Mob mob : newMobs.keySet()) {
								if (newMobs.get(mob).equals(number)) {
									sortedMobsRarity.put(mob, number);
								}
							}
						}
						LinkedHashMap<Mob, Integer> sortedBossesRarity = new LinkedHashMap<>();
						ArrayList<Integer> numbersList1 = new ArrayList<>();
						numbersList1.addAll(newBosses.values());
						Collections.sort(numbersList1);
						for (Integer number : numbersList) {
							for (Mob mob : newBosses.keySet()) {
								if (newBosses.get(mob).equals(number)) {
									sortedBossesRarity.put(mob, number);
								}
							}
						}
						
						Integer mobSpawnDelay = getConfig().getInt("options.regions." + arenaRegionName + ".spawners." + spawnerName + ".mob_spawn_delay", getConfig().getInt("options.mob_spawn_delay"));
						Integer bossSpawnDelay = getConfig().getInt("options.regions." + arenaRegionName + ".spawners." + spawnerName + ".boss_spawn_delay", getConfig().getInt("options.boss_spawn_delay"));
						
						newRegionSpawners.put(spawnerName, new RegionSpawner(arenaRegionName, spawnerName, spawnerDisplayName, maxSpawnedMobs, maxSpawnedBosses, spawnerLocation, sortedMobsRarity, sortedBossesRarity, mobSpawnDelay, bossSpawnDelay));
					}
					
					arenaRegions.put(arenaRegionName, new ArenaRegion(arenaRegionName, displayname, newRegionSpawners));
				} catch (Exception ex) {
					getLogger().warning("Unable to parse the ArenaRegion \"" + arenaRegionName + "\" due to: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
			
			mainTaskId = Bukkit.getScheduler().runTaskTimer(this, () -> {
				if (spawnToggle) {
					for (ArenaRegion arenaRegion : arenaRegions.values()) {
						for (RegionSpawner regionSpawner : arenaRegion.getSpawners().values()) {
							if (regionSpawner.getActualMobSpawnDelay() <= 0) {
								regionSpawner.resetActualMobSpawnDelay();
								
								if (getSpawnerMobs(regionSpawner).size() < regionSpawner.getMaxSpawnedMobs()) {
									if (regionSpawner.getMobs().size() > 0) {
										Integer luck = (random.nextInt(100) + 1);
										for (Mob mob : regionSpawner.getMobs().keySet()) {
											if (luck <= regionSpawner.getMobs().get(mob)) {
												if (random.nextInt(3) == 1) {
													mob.spawn(regionSpawner, regionSpawner.getLocation());
													break;
												}
											}
										}
									}
								}
							} else regionSpawner.setActualMobSpawnDelay(regionSpawner.getActualMobSpawnDelay() - 1);
							
							if (regionSpawner.getActualBossSpawnDelay() <= 0) {
								regionSpawner.resetActualBossSpawnDelay();
								
								if (getSpawnerBosses(regionSpawner).size() < regionSpawner.getMaxSpawnedBosses()) {
									if (regionSpawner.getBosses().size() > 0) {
										Integer luck = (random.nextInt(100) + 1);
										for (Mob boss : regionSpawner.getBosses().keySet()) {
											if (luck <= regionSpawner.getBosses().get(boss)) {
												if (random.nextInt(3) == 1) {
													boss.spawn(regionSpawner, regionSpawner.getLocation());
													break;
												}
											}
										}
									}
								}
							} else regionSpawner.setActualBossSpawnDelay(regionSpawner.getActualBossSpawnDelay() - 1);
						}
					}
				}
			}, 0L, 0L).getTaskId();
			
			//getLogger().info("Configuration successfully reloaded");
			return true;
		} catch (Exception ex) {
			getLogger().severe("Couldn't load the whole configuration! disabling the plugin...");
			ex.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return false;
		}
	}
	
	public ItemStack getWand() {
		ItemStack wandItem = new ItemStack(Material.BLAZE_ROD);
		ItemMeta wandItemMeta = wandItem.getItemMeta();
		wandItemMeta.setDisplayName("§5§lWNMobArenas RegionWand");
		wandItem.setItemMeta(wandItemMeta);
		return wandItem;
	}
	
	public static ItemStack getItemByName(String itemName) {
		if (items.containsKey(itemName)) return items.get(itemName).itemStack;
		
		return new ItemStack(Material.DIRT);
	}
	
	public static String locationToString(Location location, Boolean isBlock) {
		if (isBlock) return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
		return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
	}
	
	public static Location stringToLocation(String location_string) {
		String[] location_split = location_string.split(",");
		if (location_split.length > 4) return new Location(Bukkit.getWorld(location_split[0]), Double.parseDouble(location_split[1]), Double.parseDouble(location_split[2]), Double.parseDouble(location_split[3]), Float.parseFloat(location_split[4]), Float.parseFloat(location_split[5]));
		return new Location(Bukkit.getWorld(location_split[0]), Double.parseDouble(location_split[1]), Double.parseDouble(location_split[2]), Double.parseDouble(location_split[3]));
	}
	
	public static boolean isInArea(Location location, Location pos1, Location pos2) {
		if (location.getWorld().getName().equals(pos1.getWorld().getName())) {
			if (location.getBlockX() >= Math.min(pos1.getBlockX(), pos2.getBlockX()) && location.getBlockX() <= Math.max(pos1.getBlockX(), pos2.getBlockX())) {
				if (location.getBlockY() >= Math.min(pos1.getBlockY(), pos2.getBlockY()) && location.getBlockY() <= Math.max(pos1.getBlockY(), pos2.getBlockY())) {
					if (location.getBlockZ() >= Math.min(pos1.getBlockZ(), pos2.getBlockZ()) && location.getBlockZ() <= Math.max(pos1.getBlockZ(), pos2.getBlockZ())) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public static boolean isDigit(String digit) {
		try {
			Integer.parseInt(digit);
			return true;
		} catch (Exception ignored) {}
		return false;
	}
	
	public void sendString(String text, CommandSender sender) {
		if (text.length() > 0 && getConfig().getString(text) != null) text = getConfig().getString(text);
		if (text.length() > 0) {
			if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
				if (sender instanceof Player) me.clip.placeholderapi.PlaceholderAPI.setPlaceholders((org.bukkit.OfflinePlayer) sender, text);
				else me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(null, text);
			}
			
			for (String line : text.split("\\n")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
			}
		}
	}
	
	public static List<AliveMob> getSpawnerMobs(RegionSpawner regionSpawner) {
		List<AliveMob> spawnerMobs = new ArrayList<>();
		for (AliveMob aliveMob : mobEntities.values()) {
			if (aliveMob.ownerSpawner != null && !aliveMob.mob.isBoss() && aliveMob.ownerSpawner.equals(regionSpawner)) spawnerMobs.add(aliveMob);
		}
		
		return spawnerMobs;
	}
	public static List<AliveMob> getSpawnerBosses(RegionSpawner regionSpawner) {
		List<AliveMob> spawnerBosses = new ArrayList<>();
		for (AliveMob aliveMob : mobEntities.values()) {
			if (aliveMob.ownerSpawner != null && aliveMob.mob.isBoss() && aliveMob.ownerSpawner.equals(regionSpawner)) spawnerBosses.add(aliveMob);
		}
		
		return spawnerBosses;
	}
	
	public static ItemStack setStringData(ItemStack item, String field, String value) {
        net.minecraft.server.v1_8_R3.ItemStack item_ = org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(item);
		if (item_.getTag() == null) item_.setTag(new net.minecraft.server.v1_8_R3.NBTTagCompound());
		item_.getTag().setString(field, value);
        return org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asCraftMirror(item_);
    }
    public static String getStringData(ItemStack item, String field) {
        net.minecraft.server.v1_8_R3.ItemStack item_ = org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(item);
        if (item_.getTag() == null) item_.setTag(new net.minecraft.server.v1_8_R3.NBTTagCompound());
		try {
            return item_.getTag().getString(field);
        } catch (NullPointerException ex) {
            return null;
        }
    }
    public static Boolean hasKey(ItemStack item, String field) {
        net.minecraft.server.v1_8_R3.ItemStack item_ = org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(item);
        if (item_.getTag() == null) item_.setTag(new net.minecraft.server.v1_8_R3.NBTTagCompound());
		return item_.getTag().hasKey(field);
    }
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("wnmobarenas.admin")) {
			if (args.length == 0) {
				for (String line : getConfig().getStringList("messages.help")) {
					sendString(line, sender);
				}
			} else {
				if (args[0].equalsIgnoreCase("reload")) {
					if (reload()) sendString("messages.configuration_reloaded", sender);
					else sendString("messages.an_error_occurred", sender);
				} else if (args[0].equalsIgnoreCase("toggle")) {
					spawnToggle = !spawnToggle;
					sendString("messages.spawners_toggled", sender);
				} else if (args[0].equalsIgnoreCase("getwand")) {
					if (sender instanceof Player) {
						((Player) sender).getInventory().addItem(getWand());
						sendString("messages.you_got_wand", sender);
					} else sendString("messages.only_players", sender);
				} else if (args[0].equalsIgnoreCase("setregion")) {
					if (sender instanceof Player) {
						Player p = (Player) sender;
						
						if (args.length > 1) {
							if (pos1.containsKey(p.getUniqueId()) && pos2.containsKey(p.getUniqueId()) && pos1.get(p.getUniqueId()).getWorld().equals(pos2.get(p.getUniqueId()).getWorld())) {
								if (getConfig().get("options.regions." + args[1] + ".displayname") == null) getConfig().set("options.regions." + args[1] + ".displayname", args[1]);
								getConfig().set("options.regions." + args[1] + ".pos1", locationToString(pos1.get(p.getUniqueId()), true));
								getConfig().set("options.regions." + args[1] + ".pos2", locationToString(pos2.get(p.getUniqueId()), true));
								if (getConfig().get("options.regions." + args[1] + ".spawners") == null) getConfig().createSection("options.regions." + args[1] + ".spawners");
								saveConfig();
								reload();
								
								sendString(getConfig().getString("messages.region_locations_set").replace("%region%", args[1]), sender);
							} else sendString("messages.invalid_region", sender);
						} else sendString("messages.incomplete_command", sender);
					} else sendString("messages.only_players", sender);
				} else if (args[0].equalsIgnoreCase("setspawner")) {
					if (sender instanceof Player) {
						Player p = (Player) sender;
						
						if (args.length > 2) {
							if (arenaRegions.containsKey(args[1])) {
								getConfig().set("options.regions." + args[1] + ".spawners." + args[2] + ".location", locationToString(p.getLocation(), false));
								if (getConfig().get("options.regions." + args[1] + ".spawners." + args[2] + ".mobs") == null) getConfig().set("options.regions." + args[1] + ".spawners." + args[2] + ".mobs", new ArrayList<>());
								saveConfig();
								reload();
								
								sendString(getConfig().getString("messages.spawner_set").replace("%name%", args[2]), sender);
							} else sendString("messages.unknown_region", sender);
						} else sendString("messages.incomplete_command", sender);
					} else sendString("messages.only_players", sender);
				} else if (args[0].equalsIgnoreCase("spawn")) {
					if (args.length > 3) {
						if (arenaRegions.containsKey(args[1])) {
							ArenaRegion arenaRegion = arenaRegions.get(args[1]);
							if (arenaRegion.getSpawners().containsKey(args[2])) {
								RegionSpawner regionSpawner = arenaRegion.getSpawners().get(args[2]);
								if (mobs.containsKey(args[3])) {
									mobs.get(args[3]).spawn(null, regionSpawner.getLocation());
									sendString("messages.mob_spawned", sender);
								} else sendString(getConfig().getString("messages.mob_not_found").replace("%mob%", args[3]), sender);
							} else sendString(getConfig().getString("messages.spawner_not_found").replace("%spawner%", args[2]).replace("%region%", args[1]), sender);
						} else sendString("messages.unknown_region", sender);
					} else sendString("messages.incomplete_command", sender);
				} else if (args[0].equalsIgnoreCase("setitem")) {
					if (sender instanceof Player) {
						Player p = (Player) sender;
						
						if (args.length > 1) {
							ItemStack itemInHand = p.getInventory().getItemInHand();
							if (itemInHand != null && !itemInHand.getType().equals(Material.AIR)) {
								getConfig().set("options.items." + args[1] + ".item", itemInHand.clone());
								if (getConfig().get("options.items." + args[1] + ".custom_displayname_visible") == null) getConfig().set("options.items." + args[1] + ".custom_displayname_visible", false);
								if (getConfig().get("options.items." + args[1] + ".custom_displayname") == null) getConfig().set("options.items." + args[1] + ".custom_displayname", args[1]);
								saveConfig();
								reload();
								
								sendString("messages.item_saved", sender);
							} else sendString("messages.need_an_item", sender);
						} else sendString("messages.incomplete_command", sender);
					} else sendString("messages.only_players", sender);
				} else if (args[0].equalsIgnoreCase("getitem")) {
					if (sender instanceof Player) {
						Player p = (Player) sender;
						
						if (args.length > 1) {
							if (items.containsKey(args[1])) {
								p.getInventory().addItem(getItemByName(args[1]));
								sendString("messages.added_to_inventory", sender);
							} else sendString(getConfig().getString("messages.item_not_found").replace("%item%", args[1]), sender);
						} else sendString("messages.incomplete_command", sender);
					} else sendString("messages.only_players", sender);
				} else if (args[0].equalsIgnoreCase("removeitem")) {
					if (sender instanceof Player) {
						Player p = (Player) sender;
						
						if (args.length > 1) {
							if (getConfig().get("options.items." + args[1]) != null) {
								getConfig().set("options.items." + args[1], null);
								saveConfig();
								reload();
								
								sendString("messages.item_removed", sender);
							} else sendString(getConfig().getString("messages.item_not_found").replace("%item%", args[1]), sender);
						} else sendString("messages.incomplete_command", sender);
					} else sendString("messages.only_players", sender);
				} else if (args[0].equalsIgnoreCase("viewitems")) {
					if (sender instanceof Player) {
						Player p = (Player) sender;
						org.bukkit.inventory.Inventory inv = Bukkit.createInventory(null, 54, "All saved items"); 
						
						int startIndex = 0;
						
						if (args.length > 1) {
							if (isDigit(args[1])) {
								startIndex = Integer.parseInt(args[1]);
								if (startIndex < 0) startIndex = 0;
							} else {
								sendString("messages.integer_needed", sender);
								return true;
							}
						}
						
						List<String> items = new ArrayList<>(this.items.keySet());
						for (int i = startIndex; i < items.size(); i++) {
							inv.addItem(getItemByName(items.get(i)));
						}
						
						p.openInventory(inv);
					} else sendString("messages.only_players", sender);
				} else if (args[0].equalsIgnoreCase("pos1")) {
					if (sender instanceof Player) {
						Player p = (Player) sender;
						pos1.put(p.getUniqueId(), p.getLocation());
						sendString("&aPos1 set to &f" + p.getLocation().getBlockX() + " " + p.getLocation().getBlockY() + " " + p.getLocation().getBlockZ() + " (" + p.getLocation().getWorld().getName() + ")", p);
					} else sendString("messages.only_players", sender);
				} else if (args[0].equalsIgnoreCase("pos2")) {
					if (sender instanceof Player) {
						Player p = (Player) sender;
						pos2.put(p.getUniqueId(), p.getLocation());
						sendString("&cPos2 set to &f" + p.getLocation().getBlockX() + " " + p.getLocation().getBlockY() + " " + p.getLocation().getBlockZ() + " (" + p.getLocation().getWorld().getName() + ")", p);
					} else sendString("messages.only_players", sender);
				} else sendString("messages.unknown_subcommand", sender);
			}
		} else sendString("messages.not_permitted", sender);
		return true;
	}
	
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> su = new ArrayList<>();
		
		if (sender.hasPermission("wnmobarenas.admin")) {
			if (args.length == 1) {
				if (args[0].equals("")) {
					su.add("reload");
					su.add("toggle");
					su.add("getwand");
					su.add("setregion");
					su.add("setspawner");
					su.add("spawn");
					su.add("setitem");
					su.add("getitem");
					su.add("removeitem");
					su.add("viewitems");
					su.add("pos1");
					su.add("pos2");
				} else {
					if ("reload".startsWith(args[0].toLowerCase())) su.add("reload");
					if ("toggle".startsWith(args[0].toLowerCase())) su.add("toggle");
					if ("getwand".startsWith(args[0].toLowerCase())) su.add("getwand");
					if ("setregion".startsWith(args[0].toLowerCase())) su.add("setregion");
					if ("setspawner".startsWith(args[0].toLowerCase())) su.add("setspawner");
					if ("spawn".startsWith(args[0].toLowerCase())) su.add("spawn");
					if ("setitem".startsWith(args[0].toLowerCase())) su.add("setitem");
					if ("getitem".startsWith(args[0].toLowerCase())) su.add("getitem");
					if ("removeitem".startsWith(args[0].toLowerCase())) su.add("removeitem");
					if ("viewitems".startsWith(args[0].toLowerCase())) su.add("viewitems");
					if ("pos1".startsWith(args[0].toLowerCase())) su.add("pos1");
					if ("pos2".startsWith(args[0].toLowerCase())) su.add("pos2");
				}
			} else if (args.length == 2) {
				if (args[1].equals("")) {
					if (args[0].equalsIgnoreCase("setregion")) {
						su.addAll(arenaRegions.keySet());
					} else if (args[0].equalsIgnoreCase("spawn")) {
						su.addAll(arenaRegions.keySet());
					} else if (args[0].equalsIgnoreCase("setitem") || args[0].equalsIgnoreCase("getitem")) {
						su.addAll(items.keySet());
					} else if (args[0].equalsIgnoreCase("setspawner")) {
						su.addAll(arenaRegions.keySet());
					}
				} else {
					if (args[0].equalsIgnoreCase("setregion")) {
						for (String arenaRegionName : arenaRegions.keySet()) {
							if (arenaRegionName.toLowerCase().startsWith(args[1].toLowerCase())) {
								su.add(arenaRegionName);
							}
						}
					} else if (args[0].equalsIgnoreCase("spawn")) {
						for (String arenaRegionName : arenaRegions.keySet()) {
							if (arenaRegionName.toLowerCase().startsWith(args[1].toLowerCase())) {
								su.add(arenaRegionName);
							}
						}
					} else if (args[0].equalsIgnoreCase("setitem") || args[0].equalsIgnoreCase("getitem")) {
						for (String itemName : items.keySet()) {
							if (itemName.toLowerCase().startsWith(args[1].toLowerCase())) {
								su.add(itemName);
							}
						}
					} else if (args[0].equalsIgnoreCase("setspawner")) {
						for (String arenaRegionName : arenaRegions.keySet()) {
							if (arenaRegionName.toLowerCase().startsWith(args[1].toLowerCase())) {
								su.add(arenaRegionName);
							}
						}
					}
				}
			} else if (args.length == 3) {
				if (args[2].equals("")) {
					if (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("setspawner")) {
						if (arenaRegions.containsKey(args[1])) {
							ArenaRegion arenaRegion = arenaRegions.get(args[1]);
							su.addAll(arenaRegion.getSpawners().keySet());
						}
					}
				} else {
					if (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("setspawner")) {
						if (arenaRegions.containsKey(args[1])) {
							ArenaRegion arenaRegion = arenaRegions.get(args[1]);
							for (String spawnerName : arenaRegion.getSpawners().keySet()) {
								if (spawnerName.toLowerCase().startsWith(args[2].toLowerCase())) {
									su.add(spawnerName);
								}
							}
						}
					}
				}
			} else if (args.length == 4) {
				if (args[3].equals("")) {
					if (args[0].equalsIgnoreCase("spawn")) {
						if (arenaRegions.containsKey(args[1])) {
							if (arenaRegions.get(args[1]).getSpawners().containsKey(args[2])) {
								su.addAll(mobs.keySet());
							}
						}
					}
				} else {
					if (args[0].equalsIgnoreCase("spawn")) {
						if (arenaRegions.containsKey(args[1])) {
							if (arenaRegions.get(args[1]).getSpawners().containsKey(args[2])) {
								for (String mobName : mobs.keySet()) {
									if (mobName.toLowerCase().startsWith(args[3].toLowerCase())) {
										su.add(mobName);
									}
								}
							}
						}
					}
				}
			}
		}
		
		return su;
	}
	
	public HashMap<UUID, Location> pos1 = new HashMap<>();
	public HashMap<UUID, Location> pos2 = new HashMap<>();

	@EventHandler
	public void onInteract(org.bukkit.event.player.PlayerInteractEvent event) {
		ItemStack itemInHand = event.getPlayer().getInventory().getItemInHand();
		if (itemInHand != null && !itemInHand.getType().equals(Material.AIR) && itemInHand.isSimilar(getWand())) {
			if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				event.setCancelled(true);
				pos1.put(event.getPlayer().getUniqueId(), event.getClickedBlock().getLocation());
				sendString("&aPos1 set to &f" + event.getClickedBlock().getLocation().getBlockX() + " " + event.getClickedBlock().getLocation().getBlockY() + " " + event.getClickedBlock().getLocation().getBlockZ() + " (" + event.getClickedBlock().getLocation().getWorld().getName() + ")", event.getPlayer());
			} else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				event.setCancelled(true);
				pos2.put(event.getPlayer().getUniqueId(), event.getClickedBlock().getLocation());
				sendString("&cPos2 set to &f" + event.getClickedBlock().getLocation().getBlockX() + " " + event.getClickedBlock().getLocation().getBlockY() + " " + event.getClickedBlock().getLocation().getBlockZ() + " (" + event.getClickedBlock().getLocation().getWorld().getName() + ")", event.getPlayer());
			}
		}
	}
	
	@EventHandler
	public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
		if (mobEntities.containsKey(event.getEntity().getUniqueId())) {
			AliveMob aliveMob = mobEntities.get(event.getEntity().getUniqueId());
			event.getDrops().clear();
			for (ItemData dropItemData : aliveMob.mob.getDrops().keySet()) {
				ItemStack dropItem = setStringData(dropItemData.itemStack, "customDisplayName", dropItemData.customDisplayName);
				dropItem = setStringData(dropItem, "customDisplayNameVisible", String.valueOf(dropItemData.customDisplayNameVisible));
				if ((random.nextInt(100) + 1) <= aliveMob.mob.getDrops().get(dropItemData)) event.getDrops().add(dropItem);
			}
			
			mobEntities.remove(event.getEntity().getUniqueId());
		}
	}
	
	@EventHandler
	public void onItemSpawn(org.bukkit.event.entity.ItemSpawnEvent event) {
		ItemStack droppedItem = event.getEntity().getItemStack();
		if (hasKey(droppedItem, "customDisplayName") && hasKey(droppedItem, "customDisplayNameVisible")) {
			if (getStringData(droppedItem, "customDisplayNameVisible") == "true") event.getEntity().setCustomNameVisible(true);
			event.getEntity().setCustomName(ChatColor.translateAlternateColorCodes('&', getStringData(droppedItem, "customDisplayName")));
		}
	}

}

class ArenaRegion {
	
	final String name;
	final String displayname;
	final HashMap<String, RegionSpawner> spawners;
	
	public ArenaRegion(String name, String displayname, HashMap<String, RegionSpawner> spawners) {
		this.name = name;
		this.displayname = displayname;
		this.spawners = spawners;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayname;
	}
	
	public HashMap<String, RegionSpawner> getSpawners() {
		return new HashMap<>(spawners);
	}
	
	public String toString() {
		return name;
	}

}

class RegionSpawner {

	final String ownerRegionName;
	final String name;
	final String displayname;
	final Integer maxSpawnedMobs;
	final Integer maxSpawnedBosses;
	final Location location;
	final LinkedHashMap<Mob, Integer> mobs; // Integer number is probabilty from 1 to 100
	final LinkedHashMap<Mob, Integer> bosses;
	final Integer mobSpawnDelay;
	final Integer bossSpawnDelay;
	Integer actualMobSpawnDelay = 0;
	Integer actualBossSpawnDelay = 0;

	public RegionSpawner(String ownerRegionName, String name, String displayname, Integer maxSpawnedMobs, Integer maxSpawnedBosses, Location location, LinkedHashMap<Mob, Integer> mobs, LinkedHashMap<Mob, Integer> bosses, Integer mobSpawnDelay, Integer bossSpawnDelay) {
		this.ownerRegionName = ownerRegionName;
		this.name = name;
		this.displayname = displayname;
		this.maxSpawnedMobs = maxSpawnedMobs;
		this.maxSpawnedBosses = maxSpawnedBosses;
		this.location = location;
		this.mobs = mobs;
		this.bosses = bosses;
		this.mobSpawnDelay = mobSpawnDelay;
		this.bossSpawnDelay = bossSpawnDelay;
	}
	
	public String getOwnerRegionName() {
		return ownerRegionName;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayname;
	}
	
	public Integer getMaxSpawnedMobs() {
		return maxSpawnedMobs;
	}
	
	public Integer getMaxSpawnedBosses() {
		return maxSpawnedBosses;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public LinkedHashMap<Mob, Integer> getMobs() {
		return new LinkedHashMap<>(mobs);
	}
	
	public LinkedHashMap<Mob, Integer> getBosses() {
		return new LinkedHashMap<>(bosses);
	}
	
	public Integer getMobSpawnDelay() {
		return mobSpawnDelay;
	}
	public Integer getBossSpawnDelay() {
		return bossSpawnDelay;
	}
	public Integer getActualMobSpawnDelay() {
		return actualMobSpawnDelay;
	}
	public Integer getActualBossSpawnDelay() {
		return actualBossSpawnDelay;
	}
	public void setActualMobSpawnDelay(Integer newActualMobSpawnDelay) {
		actualMobSpawnDelay = newActualMobSpawnDelay;
	}
	public void setActualBossSpawnDelay(Integer newActualBossSpawnDelay) {
		actualBossSpawnDelay = newActualBossSpawnDelay;
	}
	public void resetActualMobSpawnDelay() {
		actualMobSpawnDelay = getMobSpawnDelay();
	}
	public void resetActualBossSpawnDelay() {
		actualBossSpawnDelay = getBossSpawnDelay();
	}
	
	public String toString() {
		return "\"" + getName() + "\" Owned by \"" + getOwnerRegionName() + "\"";
	}

}

class Mob {

	final String name;
	final String displayname;
	final Boolean visibleDisplayName;
	final EntityType type;
	final Double health;
	final Boolean isBoss;
	final ItemStack helmet;
	final ItemStack chestplate;
	final ItemStack leggings;
	final ItemStack boots;
	final ItemStack handItem;
	final HashMap<ItemData, Integer> drops;
	final Double earnPerHit;
	final Double earnPerKill;
	final HashMap<String, Object> parameters;
	final Mob passengerMob;
	
	public Mob(String name, String displayname, Boolean visibleDisplayName, EntityType type, Double health, Boolean isBoss, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack handItem, HashMap<ItemData, Integer> drops, Double earnPerHit, Double earnPerKill, HashMap<String, Object> parameters, Mob passengerMob) {
		this.name = name;
		this.displayname = displayname;
		this.visibleDisplayName = visibleDisplayName;
		this.type = type;
		this.health = health;
		this.isBoss = isBoss;
		this.helmet = helmet;
		this.chestplate = chestplate;
		this.leggings = leggings;
		this.boots = boots;
		this.handItem = handItem;
		this.drops = drops;
		this.earnPerHit = earnPerHit;
		this.earnPerKill = earnPerKill;
		this.parameters = parameters;
		this.passengerMob = passengerMob;
	}
	
	public Entity spawn(RegionSpawner ownerSpawner, Location location) {
		LivingEntity spawnedEntity = null;
		
		org.bukkit.craftbukkit.v1_8_R3.CraftWorld craftWorld = (org.bukkit.craftbukkit.v1_8_R3.CraftWorld) location.getWorld();
		net.minecraft.server.v1_8_R3.Entity nmsEntity = craftWorld.createEntity(location, getEntityType().getEntityClass());
		spawnedEntity = (LivingEntity) nmsEntity.getBukkitEntity();
		
		spawnedEntity.setCanPickupItems(false);
		spawnedEntity.setRemoveWhenFarAway(true);
		
		spawnedEntity.setCustomName(ChatColor.translateAlternateColorCodes('&', getDisplayName()));
		spawnedEntity.setCustomNameVisible(getVisibleDisplayName());
		
		spawnedEntity.getEquipment().setHelmetDropChance(0F);
		spawnedEntity.getEquipment().setChestplateDropChance(0F);
		spawnedEntity.getEquipment().setLeggingsDropChance(0F);
		spawnedEntity.getEquipment().setBootsDropChance(0F);
		spawnedEntity.getEquipment().setItemInHandDropChance(0F);
		
		spawnedEntity.getEquipment().setHelmet(getHelmet());
		spawnedEntity.getEquipment().setChestplate(getChestplate());
		spawnedEntity.getEquipment().setLeggings(getLeggings());
		spawnedEntity.getEquipment().setBoots(getBoots());
		spawnedEntity.getEquipment().setItemInHand(getHandItem());
		
		spawnedEntity.setMaxHealth(health);
		spawnedEntity.setHealth(health);
		
		net.minecraft.server.v1_8_R3.NBTTagCompound parameters = new net.minecraft.server.v1_8_R3.NBTTagCompound();
        nmsEntity.e(parameters);
		for (String nbt_parameter : getParameters().keySet()) {
			Object value = getParameters().get(nbt_parameter);
			
			if (value instanceof Boolean) {
				parameters.setBoolean(nbt_parameter, (Boolean) value);
			} else if (value instanceof String) {
				parameters.setString(nbt_parameter, (String) value);
			} else if (value instanceof Integer) {
				parameters.setInt(nbt_parameter, (Integer) value);
			} else if (value instanceof Double) {
				parameters.setDouble(nbt_parameter, (Double) value);
			}
		}
        nmsEntity.f(parameters);
		
		craftWorld.getHandle().addEntity(nmsEntity);
		
		Main.mobEntities.put(spawnedEntity.getUniqueId(), new AliveMob(this, spawnedEntity, ownerSpawner));
		return spawnedEntity;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayname;
	}
	
	public Boolean getVisibleDisplayName() {
		return visibleDisplayName;
	}
	
	public EntityType getEntityType() {
		return type;
	}
	
	public Double getHealth() {
		return health;
	}
	
	public Boolean isBoss() {
		return isBoss;
	}
	
	public ItemStack getHelmet() {
		return helmet;
	}
	
	public ItemStack getChestplate() {
		return chestplate;
	}
	
	public ItemStack getLeggings() {
		return leggings;
	}
	
	public ItemStack getBoots() {
		return boots;
	}
	
	public ItemStack getHandItem() {
		return handItem;
	}
	
	public HashMap<ItemData, Integer> getDrops() {
		return new HashMap<>(drops);
	}
	
	public Double getEarnPerHit() {
		return earnPerHit;
	}
	
	public Double getEarnPerKill() {
		return earnPerKill;
	}
	
	public HashMap<String, Object> getParameters() {
		return new HashMap<>(parameters);
	}
	
	public String toString() {
		return name;
	}

}

class AliveMob {

	public final Mob mob;
	public final LivingEntity livingEntity;
	public final RegionSpawner ownerSpawner;
	
	public AliveMob(Mob mob, LivingEntity livingEntity, RegionSpawner ownerSpawner) {
		this.mob = mob;
		this.livingEntity = livingEntity;
		this.ownerSpawner = ownerSpawner;
	}

}

class ItemData {

	public ItemStack itemStack;
	public Boolean customDisplayNameVisible;
	public String customDisplayName;
	
	public ItemData(ItemStack itemStack, Boolean customDisplayNameVisible, String customDisplayName) {
		this.itemStack = itemStack;
		this.customDisplayNameVisible = customDisplayNameVisible;
		this.customDisplayName = customDisplayName;
	}

}