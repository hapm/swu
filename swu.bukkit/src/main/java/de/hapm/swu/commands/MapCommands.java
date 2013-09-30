package de.hapm.swu.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import de.hapm.swu.SmoothWorldUpdaterPlugin;

public class MapCommands implements CommandExecutor {

	private SmoothWorldUpdaterPlugin plugin;

	public MapCommands(SmoothWorldUpdaterPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String cmdAlias,
			String[] args) {
		if (cmd.getName().equals("swumap")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be called by a player.");
				return true;
			}
			
			Player p = (Player)sender;
			final ItemStack item = p.getItemInHand();
			if (!item.getType().equals(Material.MAP)) {
				// TODO create a map and add it to the inventory instead of this
				p.sendMessage("You don't have a written map in your hand, that can be converted");
				return true;
			}
			
			MapView map = plugin.getServer().getMap(item.getDurability());
			if (map == null) {
				p.sendMessage("The map you're using couldn't be find by the server.");
				return true;
			}
			
			plugin.changeToSwuMap(map);
		}
		return false;
	}

}
