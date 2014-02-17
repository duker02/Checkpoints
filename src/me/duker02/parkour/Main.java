package me.duker02.parkour;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	public HashMap<Player, Location> checkpoints = new HashMap<Player, Location>();

	public HashMap<Player, Integer> plate = new HashMap<Player, Integer>();

	public ArrayList<Player> plate_delete = new ArrayList<Player>();

	public String prefix;
	public String cp_disappeared;
	public String cp_teleport;
	public String cp_added;
	public String cp_deleted;
	public String no_cp;
	public String cp_already_exists;
	public String no_permission;
	public String no_drop;
	public String no_place;
	public String cp_doesnt_exist;
	public String reload;
	public String cleared;
	public String plate_added;
	public String plate_exists;
	public String plate_deleted;

	public boolean cp_item;
	public String cp_itemID;
	public String cp_item_name;
	public String cp_item_lore;

	@Override
	public void onDisable() {
		System.out.println("Parkour disabled.");
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		System.out.println("Parkour enabled.");
		Bukkit.getPluginManager().registerEvents(this, this);

		prefix = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.prefix"));
		cp_disappeared = ChatColor.translateAlternateColorCodes('&',
				getConfig().getString("messages.error.disappeared"));
		cp_teleport = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.success.teleport"));
		cp_added = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.admin.added"));
		cp_deleted = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.admin.deleted"));
		no_cp = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.error.no-checkpoint"));
		cp_already_exists = ChatColor.translateAlternateColorCodes('&',
				getConfig().getString("messages.admin.already-exists"));
		no_permission = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.error.no-permission"));
		no_drop = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.error.no-drop"));
		no_place = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.error.no-place"));
		cp_doesnt_exist = ChatColor.translateAlternateColorCodes('&',
				getConfig().getString("messages.admin.doesnt-exist"));
		reload = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.admin.config-reload"));
		cleared = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.admin.clear"));
		plate_added = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.admin.plate-added"));
		plate_exists = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.admin.plate-exists"));
		plate_deleted = ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString("messages.admin.plate-deleted"));

		cp_item = getConfig().getBoolean("checkpoints.item-enabled");
		cp_itemID = getConfig().getString("checkpoints.item");
		cp_item_name = getConfig().getString("checkpoints.item-name");
		cp_item_lore = getConfig().getString("checkpoints.item-lore");
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = (Player) e.getPlayer();
		if (plate.containsKey(p)) e.setCancelled(true);
		if (plate_delete.contains(p)) e.setCancelled(true);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent ev) {
		Player p1 = (Player) ev.getPlayer();
		if (p1 instanceof Player) {
			Player p = (Player) p1;
			if (ev.getAction().equals(Action.PHYSICAL)) {
				if (ev.getClickedBlock().getTypeId() == 70) {
					if (isPlate(ev.getClickedBlock().getLocation())) {
						try {
							checkpoints.put(p, getPlate(ev.getClickedBlock().getLocation()));
						} catch (Exception e) {
							System.out.println("Something went wrong with saving checkpoint.");
						}
						checkpoints.put(p, getPlate(ev.getClickedBlock().getLocation()));
						System.out.println("PHYSICAL PLATE ACTION: New checkpoint saved for " + p.getName());
					}
				}
			}

			if (ev.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				if (ev.getClickedBlock().getType() == Material.STONE_PLATE) {
					if (plate.containsKey(p)) {
						ev.setCancelled(true);
						if (!isPlate(ev.getClickedBlock().getLocation())) {
							savePlate(plate.get(p), ev.getClickedBlock()
									.getLocation());
							plate.remove(p);
							if (!plate.containsKey(p))
								p.sendMessage(prefix + plate_added);
						} else {
							plate.remove(p);
							if (!plate.containsKey(p))
								p.sendMessage(prefix + plate_exists);
						}
					}
					if (plate_delete.contains(p)) {
						ev.setCancelled(true);
						if (isPlate(ev.getClickedBlock().getLocation())) {
							deletePlate(ev.getClickedBlock().getLocation());
							plate_delete.remove(p);
							if (!plate_delete.contains(p)) p.sendMessage(prefix + plate_deleted);
						} else {
							plate_delete.remove(p);
							if (!plate_delete.contains(p)) p.sendMessage(prefix + ChatColor.RED + "That plate is not in the config!");
						}
					}
				}
			}

			if (cp_item) {
				if ((ev.getAction().equals(Action.RIGHT_CLICK_AIR) || ev
						.getAction().equals(Action.RIGHT_CLICK_BLOCK))
						&& p.getItemInHand().getType() == Material
								.matchMaterial(cp_itemID)) {
					if (checkpoints.containsKey(p)) {
						if (getCheckpoint(checkpoints.get(p)) != null) {
							p.teleport(getCheckpoint(checkpoints.get(p)));
						} else {
							p.sendMessage(prefix + cp_disappeared);
						}
					} else {
						p.sendMessage(prefix + no_cp);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		Player p = (Player) e.getPlayer();
		if (e.getItemDrop()
				.getItemStack()
				.getItemMeta()
				.getDisplayName()
				.equals(ChatColor.translateAlternateColorCodes('&',
						cp_item_name))) {
			e.setCancelled(true);
			p.sendMessage(prefix + no_drop);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = (Player) e.getPlayer();
		if (e.getPlayer()
				.getItemInHand()
				.getItemMeta()
				.getDisplayName()
				.equals(ChatColor.translateAlternateColorCodes('&',
						cp_item_name))) {
			e.setCancelled(true);
			p.sendMessage(prefix + no_place);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = (Player) e.getPlayer();
		if (p.hasPermission("checkpoints.allow")) {
			if (cp_item) {
				giveItem(p);
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player p = (Player) sender;
		if (label.equalsIgnoreCase("checkpoint")
				|| label.equalsIgnoreCase("cp")) {
			if (p.hasPermission("checkpoints.allow")) {
				if (args.length == 0) {
					p.sendMessage(prefix
							+ ChatColor.GOLD
							+ getDescription().getVersion()
							+ " by "
							+ getDescription().getAuthors().toString()
									.replace("[", "").replace("]", "") + ":");
					p.sendMessage(ChatColor.DARK_GRAY
							+ "/checkpoint teleport | /cp tp" + ChatColor.GRAY
							+ " - Go to last checkpoint.");
					if (p.hasPermission("checkpoints.admin"))
						p.sendMessage(ChatColor.DARK_GRAY
								+ "/checkpoint teleport # | /cp tp #"
								+ ChatColor.GRAY + " - TP to CP from config.");
					if (p.hasPermission("checkpoints.admin"))
						p.sendMessage(ChatColor.DARK_GRAY
								+ "/checkpoint add | /cp add" + ChatColor.GRAY
								+ " - Add a checkpoint.");
					if (p.hasPermission("checkpoints.admin"))
						p.sendMessage(ChatColor.DARK_GRAY
								+ "/checkpoint list | /cp list"
								+ ChatColor.GRAY
								+ " - List checkpoints from config.");
					if (p.hasPermission("checkpoints.admin"))
						p.sendMessage(ChatColor.DARK_GRAY
								+ "/checkpoint delete | /cp delete"
								+ ChatColor.GRAY
								+ " - Delete a CP at your location.");
					if (p.hasPermission("checkpoints.admin"))
						p.sendMessage(ChatColor.DARK_GRAY
								+ "/checkpoint delete # | /cp delete #"
								+ ChatColor.GRAY + " - Delete CP from config.");
					if (p.hasPermission("checkpoints.admin"))
						p.sendMessage(ChatColor.DARK_GRAY
								+ "/checkpoint clear | /cp clear"
								+ ChatColor.GRAY + " - Delete all checkpoints.");
					if (p.hasPermission("checkpoints.admin"))
						p.sendMessage(ChatColor.DARK_GRAY
								+ "/checkpoint reload | /cp reload"
								+ ChatColor.GRAY + " - Reload the config file.");
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("tp")
							|| args[0].equalsIgnoreCase("teleport")) {
						if (checkpoints.containsKey(p)) {
							if (getCheckpoint(checkpoints.get(p)) != null) {
								p.teleport(getCheckpoint(checkpoints.get(p)));
								p.sendMessage(prefix + cp_teleport);
							} else {
								p.sendMessage(prefix + cp_doesnt_exist);
							}
						} else {
							p.sendMessage(prefix + no_cp);
						}
					} else if (args[0].equalsIgnoreCase("add")) {
						if (p.hasPermission("checkpoints.admin")) {
							if (getCheckpoint(p.getLocation()) == null) {
								saveCheckpoint(p.getLocation());
								p.sendMessage(prefix + cp_added);
							} else {
								p.sendMessage(prefix + cp_already_exists);
							}
						} else {
							p.sendMessage(prefix + no_permission);
						}
					} else if (args[0].equalsIgnoreCase("delete")) {
						if (p.hasPermission("checkpoints.admin")) {
							if (getCheckpoint(p.getLocation()) != null) {
								deleteCheckpoint(p.getLocation());
								p.sendMessage(prefix + cp_deleted);
							} else {
								p.sendMessage(prefix + cp_doesnt_exist);
							}
						} else {
							p.sendMessage(prefix + no_permission);
						}
					} else if (args[0].equalsIgnoreCase("reload")) {
						if (p.hasPermission("checkpoints.admin")) {
							reloadConfig();
							p.sendMessage(prefix + reload);
						} else {
							p.sendMessage(prefix + no_permission);
						}
					} else if (args[0].equalsIgnoreCase("clear")) {
						if (p.hasPermission("checkpoints.admin")) {
							clearCheckpoints();
							p.sendMessage(prefix + cleared);
						} else {
							p.sendMessage(prefix + no_permission);
						}
					} else if (args[0].equalsIgnoreCase("list")) {
						if (p.hasPermission("checkpoints.admin")) {
							p.sendMessage(ChatColor.DARK_GRAY
									+ ""
									+ ChatColor.STRIKETHROUGH
									+ "----------------------------------------------------");
							p.sendMessage(ChatColor.RED
									+ "[#]: [world], [x], [y], [z], [yaw], [pitch]");
							for (String s : getConfig().getStringList(
									"checkpoints.locations")) {
								p.sendMessage(ChatColor.GOLD
										+ Integer
												.toString(getConfig()
														.getStringList(
																"checkpoints.locations")
														.indexOf(s))
										+ ChatColor.GRAY + ": "
										+ ChatColor.GRAY + s);
							}
							p.sendMessage(ChatColor.DARK_GRAY
									+ ""
									+ ChatColor.STRIKETHROUGH
									+ "----------------------------------------------------");
						} else {
							p.sendMessage(prefix + no_permission);
						}
					}
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("plate")) {
						if (p.hasPermission("checkpoints.admin")) {
							if (args[1].equalsIgnoreCase("delete")) {
								plate_delete.add(p);
								if (plate_delete.contains(p))
									p.sendMessage(prefix
											+ ChatColor.YELLOW
											+ "Punch a stone plate to remove it from config.");
							} else if (args[1].equalsIgnoreCase("clear")) {
								clearPlates();
								p.sendMessage(prefix + ChatColor.YELLOW
										+ "Plates were cleared from config.");
							}
						} else {
							p.sendMessage(prefix + no_permission);
						}
					} else if (args[0].equalsIgnoreCase("delete")) {
						if (p.hasPermission("checkpoints.admin")) {
							int i = Integer.parseInt(args[1]);
							if (i <= getConfig().getStringList(
									"checkpoints.locations").size() - 1) {
								if (getConfig().getStringList(
										"checkpoints.locations").get(i) != null) {
									deleteCheckpoint(getCheckpoint(stringToLocation(getConfig()
											.getStringList(
													"checkpoints.locations")
											.get(i))));
									p.sendMessage(prefix + cp_deleted);
								} else {
									p.sendMessage(prefix + cp_doesnt_exist);
								}
							} else {
								p.sendMessage(prefix
										+ ChatColor.RED
										+ "Out of bounds! Provide a number less/equal to "
										+ getConfig().getStringList(
												"checkpoints.locations").size());
							}
						} else {
							p.sendMessage(prefix + no_permission);
						}
					} else if (args[0].equalsIgnoreCase("teleport")
							|| args[0].equalsIgnoreCase("tp")) {
						if (p.hasPermission("checkpoints.admin")) {
							int loc = Integer.parseInt(args[1]);
							if (loc <= getConfig().getStringList(
									"checkpoints.locations").size() - 1) {
								if (getCheckpoint(stringToLocation(getConfig()
										.getStringList("checkpoints.locations")
										.get(loc))) != null) {
									p.teleport(stringToLocation(getConfig()
											.getStringList(
													"checkpoints.locations")
											.get(loc)));
								} else {
									p.sendMessage(prefix + cp_doesnt_exist);
								}
							} else {
								p.sendMessage(prefix
										+ ChatColor.RED
										+ "Out of bounds! Provide a number less/equal to "
										+ getConfig().getStringList(
												"checkpoints.locations").size());
							}
						} else {
							p.sendMessage(prefix + no_permission);
						}
					}
				} else if (args.length == 3) {
					if (args[0].equalsIgnoreCase("plate") && args[1].equalsIgnoreCase("add")) {
						if (p.hasPermission("checkpoints.admin")) {
							int i = Integer.parseInt(args[2]);
							if (i <= getConfig().getStringList(
									"checkpoints.locations").size() - 1) {
								plate.put(p, i);
								if (plate.containsKey(p)) {
									p.sendMessage(prefix
											+ ChatColor.YELLOW
											+ "Punch a stone plate to add it for checkpoint "
											+ i + ".");
								}
							} else {
								p.sendMessage(prefix
										+ ChatColor.RED
										+ "Out of bounds! Provide a number less/equal to "
										+ getConfig().getStringList(
												"checkpoints.locations")
												.size());
							}
						} else {
							p.sendMessage(prefix + no_permission);
						}
					}
				}
			} else {
				p.sendMessage(prefix + no_permission);
			}
		}
		return false;
	}

	public Double fiveDec(double val) {
		DecimalFormat df5 = new DecimalFormat("#####.#####");
		return Double.valueOf(df5.format(val));
	}

	public void saveCheckpoint(Location loc) {
		String location = loc.getWorld().getName() + ", " + fiveDec(loc.getX())
				+ ", " + loc.getY() + ", " + fiveDec(loc.getZ()) + ", "
				+ loc.getYaw() + ", " + loc.getPitch();
		List<String> configList = getConfig().getStringList(
				"checkpoints.locations");
		configList.add(location);
		getConfig().set("checkpoints.locations", configList);
		saveConfig();
	}

	public void deleteCheckpoint(Location loc) {
		String location = loc.getWorld().getName() + ", " + fiveDec(loc.getX())
				+ ", " + loc.getY() + ", " + fiveDec(loc.getZ()) + ", "
				+ loc.getYaw() + ", " + loc.getPitch();
		List<String> configList = getConfig().getStringList(
				"checkpoints.locations");
		configList.remove(location);
		getConfig().set("checkpoints.locations", configList);
		saveConfig();
	}

	public void clearCheckpoints() {
		List<String> configList = getConfig().getStringList(
				"checkpoints.locations");
		configList.clear();
		getConfig().set("checkpoints.locations", configList);
		saveConfig();
	}

	public Location getCheckpoint(Location loc) {
		String location = loc.getWorld().getName() + ", " + loc.getX() + ", "
				+ loc.getY() + ", " + loc.getZ() + ", " + loc.getYaw() + ", "
				+ loc.getPitch();
		for (String locs : getConfig().getStringList("checkpoints.locations")) {
			String[] locs1 = locs.split(", ");
			if (locs.equals(location)) {
				World w = Bukkit.getWorld(locs1[0]);
				Double x = Double.parseDouble(locs1[1]);
				Double y = Double.parseDouble(locs1[2]);
				Double z = Double.parseDouble(locs1[3]);
				float yaw = Float.parseFloat(locs1[4]);
				float pitch = Float.parseFloat(locs1[5]);
				Location location1 = new Location(w, fiveDec(x), y, fiveDec(z),
						yaw, pitch);
				return location1;
			}
		}
		return null;
	}

	public void savePlate(int cp, Location loc) {
		String location = Integer.toString(cp) + "-" + loc.getWorld().getName()
				+ ", " + fiveDec(loc.getX()) + ", " + loc.getY() + ", "
				+ fiveDec(loc.getZ());
		List<String> configList = getConfig().getStringList(
				"checkpoints.plates");
		configList.add(location);
		getConfig().set("checkpoints.plates", configList);
		saveConfig();
	}

	public void deletePlate(Location loc) {
		String location = "[0-9]+" + "-" + loc.getWorld().getName()
				+ ", " + fiveDec(loc.getX()) + ", " + loc.getY() + ", "
				+ fiveDec(loc.getZ());
		List<String> configList = getConfig().getStringList(
				"checkpoints.plates");
		configList.remove(location);
		getConfig().set("checkpoints.plates", configList);
		saveConfig();
	}

	public void clearPlates() {
		List<String> configList = getConfig().getStringList(
				"checkpoints.plates");
		configList.clear();
		getConfig().set("checkpoints.plates", configList);
		saveConfig();
	}

	public boolean isPlate(Location loc) {
		String location = 0 + "-" + loc.getWorld().getName() + ", "
				+ fiveDec(loc.getX()) + ", " + loc.getY() + ", "
				+ fiveDec(loc.getZ());
		for (String locs : getConfig().getStringList("checkpoints.plates")) {
			String [] locs1 = locs.split("-");
			String [] locs2 = location.split("-");
			if (locs1[1].equals(locs2[1])) {
				return true;
			}
		}
		return false;
	}
	
	public Location getPlate(Location loc) {
		String location = 0 + "-" + loc.getWorld().getName() + ", "
				+ fiveDec(loc.getX()) + ", " + loc.getY() + ", "
				+ fiveDec(loc.getZ()) + ", " + loc.getYaw() + ", "
				+ loc.getPitch();
		for (String locs : getConfig().getStringList("checkpoints.plates")) {
			String [] locs1 = locs.split("-");
			String [] locs2 = location.split("-");
			if (locs1[1].equals(locs2[1])) {
				int i = Integer.parseInt(locs1[0]);
				String loc1 = getConfig().getStringList("checkpoints.locations").get(i);
				String[] loc2 = loc1.split(", ");
				World world = Bukkit.getWorld(loc2[0]);
				Double x = Double.parseDouble(loc2[1]);
				Double y = Double.parseDouble(loc2[2]);
				Double z = Double.parseDouble(loc2[3]);
				float yaw = Float.parseFloat(loc2[4]);
				float pitch = Float.parseFloat(loc2[5]);
				Location location1 = new Location(world, x, y, z, yaw, pitch);
				return location1;
			}
		}
		return null;
	}

	public Location stringToLocation(String string) {
		String[] multipleStrings = string.split(", ");
		World world = Bukkit.getWorld(multipleStrings[0]);
		Double x = Double.parseDouble(multipleStrings[1]);
		Double y = Double.parseDouble(multipleStrings[2]);
		Double z = Double.parseDouble(multipleStrings[3]);
		float yaw = Float.parseFloat(multipleStrings[4]);
		float pitch = Float.parseFloat(multipleStrings[5]);
		Location loc = new Location(world, fiveDec(x), y, fiveDec(z), yaw,
				pitch);
		return loc;
	}

	public void giveItem(Player p) {
		p.getInventory().clear();
		ItemStack item = new ItemStack(Material.matchMaterial(cp_itemID), 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				cp_item_name));
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.translateAlternateColorCodes('&', cp_item_lore));
		meta.setLore(lore);
		item.setItemMeta(meta);
		p.getInventory().addItem(item);
	}
	
	public void printLocationSystem(Location loc) {
		
	}
}
