package codechicken.nei.init;

import codechicken.nei.*;
import codechicken.nei.api.API;
import codechicken.nei.api.GuiInfo;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.api.NEIPlugin;
import codechicken.nei.jei.JEIIntegrationManager;
import codechicken.nei.util.ItemInfo;
import codechicken.nei.util.ItemList;
import codechicken.nei.util.ItemStackSet;
import codechicken.nei.util.LogHelper;
import codechicken.nei.util.helper.potion.IPotionRecipe;
import codechicken.nei.util.helper.potion.PotionRecipeHelper;
import codechicken.nei.widget.dumps.FluidRegistryDumper;
import codechicken.nei.widget.dumps.ForgeRegistryDumper;
import codechicken.nei.widget.dumps.ItemPanelDumper;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * Created by covers1624 on 30/03/2017.
 * Main initialization class for NEI.
 */
public class NEIInitialization {

    public static ImmutableList<IConfigureNEI> plugins;

    /**
     * Called to do first initialization of NEI's core components.
     * Including default item filters, Plugin initialization. ect.
     */
    public static void bootNEI() {
        long start = System.nanoTime();
        LogHelper.info("Loading NEI.");
        NEIClientConfig.loadStates();
        PotionRecipeHelper.init();
        hideVanillaItems();
        addBaseSubsets();
        loadDefaultSubsets();
        loadPotionSubsets();
        loadModSubsets();
        loadRegistryDumps();
        API.addItemFilter(() -> item -> !ItemInfo.hiddenItems.contains(item));
        API.addItemFilter(() -> item -> !JEIIntegrationManager.isBlacklisted(item));
        ItemList.registerLoadCallback(ItemInfo.itemSearchNames::clear);
        GuiInfo.load();
        LayoutManager.load();
        NEIController.load();

        for (IConfigureNEI plugin : plugins) {
            try {
                plugin.loadConfig();
                LogHelper.debug("Loaded Plugin: %s[%s]", plugin.getName(), plugin.getClass().getName());
            } catch (Exception e) {
                LogHelper.fatalError("Caught fatal exception from an NEI plugin! Class: ", e, plugin.getClass().getName());
            }
        }

        replaceMetadata();

        ItemSorter.loadConfig();
        LogHelper.info("Finished NEI Initialization after %s ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));

    }

    private static void replaceMetadata() {

        StringBuilder builder = new StringBuilder("\n\n");

        if (plugins.isEmpty()) {
            builder.append(TextFormatting.RED).append("No installed plugins.");
        } else {
            builder.append(TextFormatting.GREEN).append("Installed plugins: ");
            for (IConfigureNEI plugin : plugins) {
                builder.append("\n");
                builder.append("      ").append(TextFormatting.GREEN);
                builder.append(plugin.getName()).append(", Version: ").append(plugin.getVersion());

            }
        }
        builder.append("\n\n");
        //String desc = ModDescriptionEnhancer.enhanceDesc(NotEnoughItems.metadata.description);
        NotEnoughItems.metadata.description = NotEnoughItems.metadata.description.replace("<plugins>", builder.toString());
    }

    public static void scrapeData(ASMDataTable dataTable) {
        ImmutableList.Builder<IConfigureNEI> plugins = ImmutableList.builder();
        for (ASMDataTable.ASMData data : dataTable.getAll(NEIPlugin.class.getName())) {
            try {
                Class<?> pluginClass = Class.forName(data.getClassName());
                if (IConfigureNEI.class.isAssignableFrom(pluginClass)) {
                    IConfigureNEI pluginInstance = (IConfigureNEI) pluginClass.newInstance();
                    plugins.add(pluginInstance);
                } else {
                    LogHelper.error("Found class with annotation @NEIPlugin but class does not implement IConfigureNEI.. Class: " + data.getClassName());
                }

            } catch (Exception e) {
                LogHelper.fatalError("Fatal exception occurred whilst loading a plugin! Class: %s", e, data.getClassName());
            }
        }
        NEIInitialization.plugins = plugins.build();

    }

    private static void hideVanillaItems() {
        API.hideItem(new ItemStack(Blocks.FARMLAND));
        API.hideItem(new ItemStack(Blocks.LIT_FURNACE));
    }

    private static void addBaseSubsets() {
        API.addSubset("Items", item -> Block.getBlockFromItem(item.getItem()) == Blocks.AIR);
        API.addSubset("Blocks", item -> Block.getBlockFromItem(item.getItem()) != Blocks.AIR);
        //API.addSubset("Blocks.MobSpawners", ItemStackSet.of(Blocks.MOB_SPAWNER));TODO
    }

    private static void loadDefaultSubsets() {

        ProgressBar bar = ProgressManager.push("Subset calculation", Item.REGISTRY.getKeys().size());

        ItemStackSet tools = new ItemStackSet();
        ItemStackSet picks = new ItemStackSet();
        ItemStackSet shovels = new ItemStackSet();
        ItemStackSet axes = new ItemStackSet();
        ItemStackSet hoes = new ItemStackSet();
        ItemStackSet swords = new ItemStackSet();
        ItemStackSet chest = new ItemStackSet();
        ItemStackSet helmets = new ItemStackSet();
        ItemStackSet legs = new ItemStackSet();
        ItemStackSet boots = new ItemStackSet();
        ItemStackSet other = new ItemStackSet();
        ItemStackSet ranged = new ItemStackSet();
        ItemStackSet food = new ItemStackSet();
        ItemStackSet potioningredients = new ItemStackSet();

        ArrayList<ItemStackSet> creativeTabRanges = new ArrayList<>(CreativeTabs.CREATIVE_TAB_ARRAY.length);
        List<ItemStack> stackList = new LinkedList<>();
        NonNullList<ItemStack> nonNullStackList = new NonNullList<>(stackList, null);

        for (Item item : Item.REGISTRY) {
            bar.step(item == null ? "null item" : item.toString());
            if (item == null) {
                continue;
            }

            for (CreativeTabs itemTab : item.getCreativeTabs()) {
                if (itemTab != null) {
                    while (itemTab.getTabIndex() >= creativeTabRanges.size()) {
                        creativeTabRanges.add(null);
                    }
                    ItemStackSet set = creativeTabRanges.get(itemTab.getTabIndex());
                    if (set == null) {
                        creativeTabRanges.set(itemTab.getTabIndex(), set = new ItemStackSet());
                    }
                    stackList.clear();
                    item.getSubItems(itemTab, nonNullStackList);
                    for (ItemStack stack : stackList) {
                        set.add(stack);
                    }
                }
            }

            //TODO EntityEquipmentSlot
            if (item.isDamageable()) {
                tools.with(item);
                if (item instanceof ItemPickaxe) {
                    picks.with(item);
                } else if (item instanceof ItemSpade) {
                    shovels.with(item);
                } else if (item instanceof ItemAxe) {
                    axes.with(item);
                } else if (item instanceof ItemHoe) {
                    hoes.with(item);
                } else if (item instanceof ItemSword) {
                    swords.with(item);
                } else if (item instanceof ItemArmor) {
                    switch (((ItemArmor) item).armorType) {
                        case HEAD:
                            helmets.with(item);
                            break;
                        case CHEST:
                            chest.with(item);
                            break;
                        case LEGS:
                            legs.with(item);
                            break;
                        case FEET:
                            boots.with(item);
                            break;
                    }
                } else if (item == Items.ARROW || item == Items.BOW) {
                    ranged.with(item);
                } else if (item == Items.FISHING_ROD || item == Items.FLINT_AND_STEEL || item == Items.SHEARS) {
                    other.with(item);
                }
            }

            if (item instanceof ItemFood) {
                food.with(item);
            }

            try {
                NonNullList<ItemStack> subItems = new NonNullList<>(new LinkedList<ItemStack>(), null);
                item.getSubItems(CreativeTabs.SEARCH, subItems);
                for (ItemStack stack : subItems) {
                    if (PotionHelper.isReagent(stack)) {
                        potioningredients.add(stack);
                    }
                }

            } catch (Exception e) {
                LogHelper.errorError("Error loading brewing ingredients for: " + item, e);
            }
        }

        ProgressManager.pop(bar);

        API.addSubset("Items.Tools.Pickaxes", picks);
        API.addSubset("Items.Tools.Shovels", shovels);
        API.addSubset("Items.Tools.Axes", axes);
        API.addSubset("Items.Tools.Hoes", hoes);
        API.addSubset("Items.Tools.Other", other);
        API.addSubset("Items.Weapons.Swords", swords);
        API.addSubset("Items.Weapons.Ranged", ranged);
        API.addSubset("Items.Armor.ChestPlates", chest);
        API.addSubset("Items.Armor.Leggings", legs);
        API.addSubset("Items.Armor.Helmets", helmets);
        API.addSubset("Items.Armor.Boots", boots);
        API.addSubset("Items.Food", food);
        API.addSubset("Items.Potions.Ingredients", potioningredients);

        for (CreativeTabs tab : CreativeTabs.CREATIVE_TAB_ARRAY) {
            if (tab.getTabIndex() >= creativeTabRanges.size()) {
                continue;
            }
            ItemStackSet set = creativeTabRanges.get(tab.getTabIndex());
            if (set != null && !set.isEmpty()) {
                API.addSubset("CreativeTabs." + I18n.format(tab.getTranslatedTabLabel()), set);
            }
        }
    }

    public static void loadPotionSubsets() {
        ArrayList<ItemStack> allPotions = new ArrayList<>();
        for (IPotionRecipe recipe : PotionRecipeHelper.getRecipes()) {
            allPotions.add(recipe.getRecipeOutput());
        }

        ItemStackSet positiveEffects = new ItemStackSet();
        ItemStackSet negativeEffects = new ItemStackSet();
        ItemStackSet neutralEffects = new ItemStackSet();
        for (ItemStack potionStack : allPotions) {
            PotionType potionType = PotionRecipeHelper.getPotionTypeFromStack(potionStack);
            if (potionType == null) {
                continue;
            }
            List<PotionEffect> stackEffects = potionType.getEffects();
            if (stackEffects.isEmpty()) {
                neutralEffects.add(potionStack);
                continue;
            }
            for (PotionEffect effect : stackEffects) {
                //If for some reason a vanilla potion has positive and negative effects, make sure we don't add it to the list more than once.
                if (effect.getPotion().isBadEffect()) {
                    if (!negativeEffects.contains(potionStack)) {
                        negativeEffects.add(potionStack);
                    }
                } else {
                    if (!positiveEffects.contains(potionStack)) {
                        positiveEffects.add(potionStack);
                    }
                }
            }
        }

        API.addSubset("Items.Potions", new ItemStackSet().with(Items.POTIONITEM).with(Items.SPLASH_POTION).with(Items.LINGERING_POTION));
        API.addSubset("Items.Potions.Splash", new ItemStackSet().with(Items.SPLASH_POTION));
        API.addSubset("Items.Potions.Lingering", new ItemStackSet().with(Items.LINGERING_POTION));
        API.addSubset("Items.Potions.Positive", positiveEffects);
        API.addSubset("Items.Potions.Negative", negativeEffects);
        API.addSubset("Items.Potions.Neutral", neutralEffects);
    }

    private static void loadModSubsets() {
        ProgressBar bar = ProgressManager.push("Mod Subsets", ForgeRegistries.ITEMS.getKeys().size());
        HashMap<String, ItemStackSet> modSubsets = new HashMap<>();

        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation ident = item.getRegistryName();
            bar.step(ident.toString());
            if (ident == null) {
                LogHelper.error("Failed to find identifier for: " + item);
                continue;
            }
            String modId = ident.getResourceDomain();
            ItemInfo.itemOwners.put(item, modId);
            ItemStackSet itemset = modSubsets.computeIfAbsent(modId, k -> new ItemStackSet());
            itemset.with(item);
        }
        ProgressManager.pop(bar);

        API.addSubset("Mod.Minecraft", modSubsets.remove("minecraft"));
        for (Entry<String, ItemStackSet> entry : modSubsets.entrySet()) {
            ModContainer mc = FMLCommonHandler.instance().findContainerFor(entry.getKey());
            if (mc == null) {
                LogHelper.error("Missing container for " + entry.getKey());
            } else {
                API.addSubset("Mod." + mc.getName(), entry.getValue());
            }
        }
    }

    private static void loadRegistryDumps() {
        API.addOption(new ForgeRegistryDumper<Item>("tools.dump.item") {
            @Override
            public IForgeRegistry<Item> registry() {
                return ForgeRegistries.ITEMS;
            }

            @Override
            public String[] dump(Item obj, ResourceLocation registryName) {
                int id = Item.getIdFromItem(obj);
                boolean hasBlock = Block.getBlockFromItem(obj) != null && Block.getBlockFromItem(obj) != Blocks.AIR;
                return new String[] { registryName.toString(), Integer.toString(id), Boolean.toString(hasBlock), obj.getClass().getCanonicalName() };
            }

            @Override
            public String[] header() {
                return new String[] { "Registry Name", "ID", "Has Block", "Class" };
            }
        });
        API.addOption(new ForgeRegistryDumper<Block>("tools.dump.block") {
            @Override
            public IForgeRegistry<Block> registry() {
                return ForgeRegistries.BLOCKS;
            }

            @Override
            public String[] dump(Block obj, ResourceLocation registryName) {
                int id = Block.getIdFromBlock(obj);
                boolean hasBlock = Item.getItemFromBlock(obj) != null; //&& Block.getBlockFromItem(obj) != Blocks.AIR;
                return new String[] { registryName.toString(), Integer.toString(id), Boolean.toString(hasBlock), obj.getClass().getCanonicalName() };
            }

            @Override
            public String[] header() {
                return new String[] { "Registry Name", "ID", "Has Item", "Class" };
            }
        });
        API.addOption(new FluidRegistryDumper());
        API.addOption(new ForgeRegistryDumper<Potion>("tools.dump.potion") {
            @Override
            public IForgeRegistry<Potion> registry() {
                return ForgeRegistries.POTIONS;
            }

            @Override
            public String[] dump(Potion obj, ResourceLocation registryName) {
                int id = Potion.getIdFromPotion(obj);
                return new String[] { registryName.toString(), Integer.toString(id), Boolean.toString(obj.isBadEffect()), Boolean.toString(obj.isBeneficial()), obj.getClass().getCanonicalName() };
            }

            @Override
            public String[] header() {
                return new String[] { "Registry Name", "ID", "Is bad Effect", "Is beneficial", "Class" };
            }
        });
        API.addOption(new ForgeRegistryDumper<Biome>("tools.dump.biome") {
            @Override
            public IForgeRegistry<Biome> registry() {
                return ForgeRegistries.BIOMES;
            }

            @Override
            public String[] dump(Biome obj, ResourceLocation registryName) {
                int id = Biome.getIdForBiome(obj);

                Set<Type> types = BiomeDictionary.getTypes(obj);
                StringBuilder s_types = new StringBuilder();
                for (BiomeDictionary.Type t : types) {
                    if (s_types.length() > 0) {
                        s_types.append(", ");
                    }
                    s_types.append(t.getName());
                }
                return new String[] { registryName.toString(), Integer.toString(id), obj.getBiomeName(), Float.toString(obj.getTemperature(BlockPos.ORIGIN)), Float.toString(obj.getRainfall()), Float.toString(obj.getSpawningChance()), Float.toString(obj.getBaseHeight()), Float.toString(obj.getHeightVariation()), s_types.toString(), obj.getClass().getCanonicalName() };
            }

            @Override
            public String[] header() {
                return new String[] { "Registry Name", "ID", "Name", "Temperature", "Rainfall", "Spawn Chance", "Root Height", "Height Variation", "Types", "Class" };
            }
        });
        API.addOption(new ForgeRegistryDumper<SoundEvent>("tools.dump.sound_event") {
            @Override
            public IForgeRegistry<SoundEvent> registry() {
                return ForgeRegistries.SOUND_EVENTS;
            }

            @Override
            public String[] dump(SoundEvent obj, ResourceLocation registryName) {
                int id = SoundEvent.REGISTRY.getIDForObject(obj);
                return new String[] { registryName.toString(), Integer.toString(id), obj.getClass().getCanonicalName() };
            }

            @Override
            public String[] header() {
                return new String[] { "Registry Name", "ID", "Class" };
            }
        });
        API.addOption(new ForgeRegistryDumper<Enchantment>("tools.dump.enchantment") {
            @Override
            public IForgeRegistry<Enchantment> registry() {
                return ForgeRegistries.ENCHANTMENTS;
            }

            @Override
            public String[] dump(Enchantment obj, ResourceLocation registryName) {
                int id = Enchantment.getEnchantmentID(obj);
                StringBuilder s_slots = new StringBuilder();
                for (EntityEquipmentSlot slot : obj.applicableEquipmentTypes) {
                    if (s_slots.length() > 0) {
                        s_slots.append(", ");
                    }
                    s_slots.append(slot.getName());
                }
                return new String[] { registryName.toString(), Integer.toString(id), obj.getName(), obj.type.toString(), Integer.toString(obj.getMinLevel()), Integer.toString(obj.getMinLevel()), obj.getRarity().name(), s_slots.toString(), obj.getClass().getCanonicalName() };
            }

            @Override
            public String[] header() {
                return new String[] { "Registry Name", "ID", "Name", "Type", "Min Level", "Max Level", "Rarity", "Slots", "Class" };
            }
        });
        API.addOption(new ItemPanelDumper("tools.dump.itempanel"));
    }
}
