package net.mrgregorix.plugins.cases;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class CaseData
{
    private final String         name;
    private final ItemStack      item;
    private final List<String>   broadcastMessage;
    private final List<LootData> lootData;
    private final String         nothingMessage;
    private final boolean        allowEmptyDrops;

    @SuppressWarnings("deprecation")
    public CaseData(final ConfigurationSection section)
    {
        this.name = section.getName();
        this.item = new ItemStack(Material.CHEST, 1);

        final ItemMeta meta = this.item.getItemMeta();
        this.applyMeta(section, meta);
        this.item.setItemMeta(meta);

        this.broadcastMessage = section.contains("message") ? this.colorStringList(section.getStringList("message")) : null;
        this.allowEmptyDrops = section.getBoolean("empty_drops");
        this.nothingMessage = section.getString("nothing").isEmpty() ? null : ChatColor.translateAlternateColorCodes('&', section.getString("nothing"));

        final ImmutableList.Builder<LootData> lootsBuilder = ImmutableList.builder();

        final ConfigurationSection lootsSection = section.getConfigurationSection("loots");
        for (final String key : lootsSection.getKeys(false))
        {
            Integer id = null;
            try
            {
                id = Integer.parseInt(key);
            }
            catch (final NumberFormatException ignored)
            {
            }

            final Material material = (id == null) ? Material.getMaterial(key.replaceAll("\\s+", "_").replaceAll("\\W", "").toUpperCase()) : Material.getMaterial(id);
            if (material == null)
            {
                throw new IllegalArgumentException("Unknown material: " + key);
            }

            final ConfigurationSection currentSection = lootsSection.getConfigurationSection(key);
            final int amount = currentSection.getInt("amount", 1);
            final int percent = currentSection.getInt("percent", 0);
            final short data = (short) currentSection.getInt("data", 0);

            final ItemStack itemStack = new ItemStack(material, amount, data);
            final ItemMeta itemMeta = itemStack.getItemMeta();
            this.applyMeta(currentSection, itemMeta);
            itemStack.setItemMeta(itemMeta);

            lootsBuilder.add(new LootData(itemStack, percent, ChatColor.translateAlternateColorCodes('&', currentSection.getString("message"))));
        }

        this.lootData = lootsBuilder.build();
    }

    private List<String> colorStringList(final Collection<String> message)
    {
        return message.stream().map(part -> ChatColor.translateAlternateColorCodes('&', part)).collect(Collectors.toList());
    }

    public String getName()
    {
        return this.name;
    }

    public ItemStack getItem()
    {
        return this.item;
    }

    public List<String> getBroadcastMessage()
    {
        return this.broadcastMessage;
    }

    public List<LootData> getLootData()
    {
        return this.lootData;
    }

    public boolean isAllowEmptyDrops()
    {
        return this.allowEmptyDrops;
    }

    public String getNothingMessage()
    {
        return this.nothingMessage;
    }

    private void applyMeta(final ConfigurationSection section, final ItemMeta meta)
    {
        if (section.contains("name"))
        {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name")));
        }
        if (section.contains("description"))
        {
            meta.setLore(section.getStringList("description").stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
        }
        if (section.contains("enchants"))
        {
            for (final String enchantString : section.getStringList("enchants"))
            {
                final Enchantment enchant;
                final int level;

                if (enchantString.contains(":"))
                {
                    final String[] split = enchantString.split(":");
                    enchant = Enchantment.getByName(split[0]);
                    level = Integer.parseInt(split[1]);
                }
                else
                {
                    enchant = Enchantment.getByName(enchantString);
                    level = 1;
                }
                if (enchant == null)
                {
                    throw new IllegalArgumentException("Unknown enchant: " + enchantString);
                }

                meta.addEnchant(enchant, level, true);
            }
        }
    }

    public static final class LootData
    {
        private final ItemStack item;
        private final int       chance;
        private final String    message;

        private LootData(final ItemStack item, final int chance, final String message)
        {
            this.item = item;
            this.chance = chance;
            this.message = message;
        }

        public ItemStack getItem()
        {
            return this.item;
        }

        public int getChance()
        {
            return this.chance;
        }

        public String getMessage()
        {
            return this.message;
        }
    }
}
