package net.mrgregorix.plugins.cases;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class GiveCaseCommandExecutor implements CommandExecutor
{
    private final CasesPlugin plugin;

    public GiveCaseCommandExecutor(final CasesPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args)
    {
        if (!sender.hasPermission("givecase"))
        {
            sender.sendMessage(ChatColor.RED + "Nope");
            return true;
        }

        if ((args.length < 1) || ((args.length < 3) && !(sender instanceof Player)))
        {
            sender.sendMessage(ChatColor.RED + "Usage: /givecase <name of the case> [how many] [player's name]");
            return true;
        }

        final CaseData caseData = this.plugin.getCaseData(args[0]);
        if (caseData == null)
        {
            sender.sendMessage(ChatColor.RED + "Couldn't find case named: " + ChatColor.GOLD + args[0]);
            sender.sendMessage(ChatColor.RED + "Available cases are: " + ChatColor.GOLD + StringUtils.join(this.plugin.getCases(), ChatColor.RED + ", " + ChatColor.GOLD));
            return true;
        }

        final int amount;
        try
        {
            amount = (args.length < 2) ? 1 : Integer.parseInt(args[1]);
        }
        catch (final NumberFormatException e)
        {
            sender.sendMessage(ChatColor.GOLD + args[1] + ChatColor.RED + " is not a number"");
            return true;
        }

        final Player target;
        boolean all = false;

        if (args.length < 3)
        {
            target = (Player) sender;
        }
        else
        {
            target = this.plugin.getServer().getPlayer(args[2]);
            if("*".equals(args[2]))
            {
                all = true;
            }
            else if (target == null)
            {
                sender.sendMessage(ChatColor.RED + "Player " + ChatColor.GOLD + args[2] + ChatColor.RED + " is not online");
                return true;
            }
        }

        final ItemStack item = caseData.getItem().clone();
        item.setAmount(amount);

        if(all)
        {
            for(final Player player : this.plugin.getServer().getOnlinePlayers())
            {
                player.getInventory().addItem(item);
                player.updateInventory();
            }
            sender.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.GOLD + amount + ChatColor.GREEN + " cases " + ChatColor.GOLD + args[0] + ChatColor.GREEN + ChatColor.GOLD + " to all player");
        }
        else
        {
            target.getInventory().addItem(item);
            target.updateInventory();
            sender.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.GOLD + amount + ChatColor.GREEN + " cases " + ChatColor.GOLD + args[0] + ChatColor.GREEN + " to " + ChatColor.GOLD + target.getName());
        }

        return true;
    }
}
