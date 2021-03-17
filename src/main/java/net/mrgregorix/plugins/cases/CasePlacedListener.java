package net.mrgregorix.plugins.cases;

import java.util.Collection;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class CasePlacedListener implements Listener
{
    private final CasesPlugin plugin;

    public CasePlacedListener(final CasesPlugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCasePlaced(final BlockPlaceEvent event)
    {
        final ItemStack item = event.getItemInHand();
        final CaseData caseData = this.plugin.getCaseOfItem(item);

        if (caseData == null)
        {
            return;
        }

        event.setCancelled(true);

        if(item.getAmount() > 1)
        {
            item.setAmount(item.getAmount() - 1);
        }
        else
        {
            event.getPlayer().getInventory().setItemInHand(null);
        }

        final Collection<CaseData.LootData> loot = this.plugin.getLoot(caseData);

        for (final CaseData.LootData data : loot)
        {
            event.getBlockPlaced().getWorld().dropItemNaturally(event.getBlockPlaced().getLocation(), data.getItem());
        }

        if(loot.isEmpty() && (caseData.getNothingMessage() == null))
        {
            return;
        }

        if(caseData.getBroadcastMessage() == null)
        {
            return;
        }

        for (String line : caseData.getBroadcastMessage())
        {
            line = this.replaceInLine(line, event, caseData);

            if (line.contains("%drop%"))
            {
                if (loot.isEmpty())
                {
                    this.plugin.getServer().broadcastMessage(line.replace("%drop%", caseData.getNothingMessage()));
                }
                else
                {
                    for (final CaseData.LootData drop : loot)
                    {
                        this.plugin.getServer().broadcastMessage(line.replace("%drop%", drop.getMessage()));
                    }
                }
            }
            else
            {
                this.plugin.getServer().broadcastMessage(line);
            }
        }
    }

    private String replaceInLine(final String line, final BlockPlaceEvent event, final CaseData caseData)
    {
        return line
                .replace("%player%", event.getPlayer().getName())
                .replace("%case%", caseData.getName());
    }
}
