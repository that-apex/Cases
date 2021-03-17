package net.mrgregorix.plugins.cases;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class CasesPlugin extends JavaPlugin
{
    private final Map<String, CaseData> caseData = new HashMap<>();

    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();

        this.caseData.clear();
        final ConfigurationSection section = this.getConfig().getConfigurationSection("drops");

        for (final String caseName : section.getKeys(false))
        {
            this.caseData.put(caseName, new CaseData(section.getConfigurationSection(caseName)));
        }

        this.getCommand("givecase").setExecutor(new GiveCaseCommandExecutor(this));

        this.getServer().getPluginManager().registerEvents(new CasePlacedListener(this), this);
    }

    public CaseData getCaseData(final String name)
    {
        return this.caseData.get(name);
    }

    public Set<String> getCases()
    {
        return this.caseData.keySet();
    }

    public CaseData getCaseOfItem(final ItemStack item)
    {
        for (final CaseData data : this.caseData.values())
        {
            if (data.getItem().isSimilar(item))
            {
                return data;
            }
        }

        return null;
    }

    public Collection<CaseData.LootData> getLoot(final CaseData caseData)
    {
        final Collection<CaseData.LootData> items = new LinkedList<>();

        while (true)
        {
            for (final CaseData.LootData data : caseData.getLootData())
            {
                final int random = (int)(Math.random() * 100.0);
                if (random < data.getChance())
                {
                    items.add(data);
                }
            }

            if(caseData.isAllowEmptyDrops() || !items.isEmpty())
            {
                break;
            }
        }

        return items;
    }
}
