package jp.example.mekanismgizmo.item;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.IConfigCardAccess;
import mekanism.api.Upgrade;
import mekanism.api.NBTConstants;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * Improved configuration card. It is Mekanism's own {@link ItemConfigurationCard} with two additions:
 * <ul>
 *     <li>the installed upgrades and the digital miner's filter list travel with the recorded configuration, and</li>
 *     <li>installing those upgrades is paid with the upgrade items in the player's inventory, all or nothing.</li>
 * </ul>
 * Everything else is Mekanism's behaviour, unchanged: sneak + right click records a machine
 * ({@code data_name} / {@code data_type} plus the machine's own {@code getConfigurationData} tag), right click injects it
 * into a machine that accepts the same kind of configuration, and sneak + right click in the air clears the card. The
 * record lives in the card's item NBT ({@code mekanism:data}), so a card written by this item can be read by Mekanism's
 * card and the other way around, and both warn with Mekanism's own "Unequal configuration data formats."
 */
public class ImprovedConfiguratorItem extends ItemConfigurationCard {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("mekanismgizmo-configurator");

    /** Installed upgrades, stored inside the same tag Mekanism uses for the configuration. */
    private static final String UPGRADES = "mekanismgizmo:upgrades";
    /** The digital miner's item / tag / mod filters and their replacement settings. */
    private static final String MINER = "mekanismgizmo:miner";
    /** Written by Mekanism's card; the machine's translated name, for the tooltip and the messages. */
    private static final String DATA_NAME = "data_name";
    /** Written by Mekanism's card; the block whose configuration was recorded. */
    private static final String DATA_TYPE = "data_type";

    public ImprovedConfiguratorItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity tile = WorldUtils.getTileEntity(level, pos);
        if (tile == null) {
            return InteractionResult.PASS;
        }
        Optional<IConfigCardAccess> capability = CapabilityUtils.getCapability(tile, Capabilities.CONFIG_CARD, context.getClickedFace()).resolve();
        if (capability.isEmpty()) {
            return InteractionResult.PASS;
        }
        IConfigCardAccess configCard = capability.get();
        if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile)) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = context.getItemInHand();
        if (player.isShiftKeyDown()) {
            // Record: Mekanism's own data, plus the upgrades and the digital miner's filters
            if (!level.isClientSide) {
                try {
                    record(stack, configCard, player);
                } catch (RuntimeException e) {
                    // A broken or third party configuration provider must not crash the server on a sneak click
                    LOGGER.error("Could not record a configuration at {}", pos, e);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
                tell(player, MekanismLang.CONFIG_CARD_GOT.translate(EnumColor.INDIGO,
                      TextComponentUtil.translate(configCard.getConfigCardName())), true);
                if (player instanceof ServerPlayer serverPlayer) {
                    // Mekanism's own card also advances on recording (true), not only on pasting (false)
                    MekanismCriteriaTriggers.CONFIGURATION_CARD.trigger(serverPlayer, true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        CompoundTag data = storedData(stack);
        if (data == null) {
            // Nothing recorded: let the machine handle the click itself (its GUI, for example)
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            BlockEntityType<?> type = readStoredType(data);
            if (type == null || !configCard.isConfigurationDataCompatible(type)) {
                tell(player, MekanismLang.CONFIG_CARD_UNEQUAL.translateColored(EnumColor.RED), true);
            } else {
                try {
                    configCard.setConfigurationData(player, data);
                } catch (RuntimeException e) {
                    // A record written by another machine layout must not break the click (or open the machine's GUI)
                    LOGGER.error("Could not apply a recorded configuration at {}", pos, e);
                    tell(player, MekanismLang.CONFIG_CARD_UNEQUAL.translateColored(EnumColor.RED), true);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
                configCard.configurationDataSet();
                tell(player, MekanismLang.CONFIG_CARD_SET.translate(EnumColor.INDIGO, cardName(data)), true);
                if (player instanceof ServerPlayer serverPlayer) {
                    MekanismCriteriaTriggers.CONFIGURATION_CARD.trigger(serverPlayer, false);
                }
                try {
                    applyExtras(data, configCard, player);
                } catch (RuntimeException e) {
                    // A hand-written or outdated card must not crash the server after the configuration was applied
                    LOGGER.error("Could not apply extra configuration data at {}", pos, e);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** The recorded tag in Mekanism's format, or {@code null} when this card is empty. */
    private static CompoundTag storedData(ItemStack stack) {
        CompoundTag data = ItemDataUtils.getCompound(stack, NBTConstants.DATA);
        return data.isEmpty() ? null : data;
    }

    /** The recorded machine's name, or Mekanism's "None". */
    private static Component cardName(CompoundTag data) {
        return data.contains(NBTConstants.DATA_NAME, Tag.TAG_STRING)
              ? TextComponentUtil.translate(data.getString(NBTConstants.DATA_NAME))
              : MekanismLang.NONE.translate();
    }

    /** Writes Mekanism's configuration tag plus this mod's extras into the card. */
    private static void record(ItemStack stack, IConfigCardAccess configCard, Player player) {
        CompoundTag data = configCard.getConfigurationData(player);
        if (data == null) {
            // IConfigCardAccess promises a tag, but a third party implementation may return null
            data = new CompoundTag();
        } else {
            // Never write this mod's extras into a tag owned by the machine: a live tag would gain the upgrade and
            // filter data permanently and a second copy of the card could inherit it.
            data = data.copy();
        }
        data.putString(NBTConstants.DATA_NAME, configCard.getConfigCardName());
        NBTUtils.writeRegistryEntry(data, NBTConstants.DATA_TYPE, ForgeRegistries.BLOCK_ENTITY_TYPES, configCard.getConfigurationDataType());
        if (configCard instanceof TileEntityMekanism tile) {
            CompoundTag upgrades = upgradeTag(tile);
            if (!upgrades.isEmpty()) {
                data.put(UPGRADES, upgrades);
            }
            if (tile instanceof TileEntityDigitalMiner miner) {
                CompoundTag filters = new CompoundTag();
                miner.getFilterManager().writeToNBT(filters);
                data.put(MINER, filters);
            }
        }
        ItemDataUtils.setCompound(stack, NBTConstants.DATA, data);
    }

    /** Applies the upgrades and the digital miner filters that travel with the configuration. */
    private static void applyExtras(CompoundTag data, IConfigCardAccess configCard, Player player) {
        if (!(configCard instanceof TileEntityMekanism tile)) {
            return;
        }
        if (data.contains(UPGRADES, Tag.TAG_COMPOUND)) {
            installUpgrades(tile, data.getCompound(UPGRADES), player);
        }
        if (data.contains(MINER, Tag.TAG_COMPOUND) && tile instanceof TileEntityDigitalMiner miner) {
            miner.getFilterManager().readFromNBT(data.getCompound(MINER));
        }
    }

    /**
     * Installs the recorded upgrades, paid with the upgrade items in the player's inventory. The upgrades are all or
     * nothing among themselves: if the player cannot pay for every one of them, none is installed and the warning lists
     * each shortfall. The configuration itself has already been applied at that point, exactly like Mekanism's card
     * does, so a card can always be pasted.
     */
    private static void installUpgrades(TileEntityMekanism tile, CompoundTag upgrades, Player player) {
        Map<Upgrade, Integer> toAdd = new EnumMap<>(Upgrade.class);
        List<Component> unsupported = new ArrayList<>();
        for (Upgrade upgrade : Upgrade.values()) {
            // Never ask for more than the game allows: addUpgrades would cap it, but the payment must be capped too
            int wanted = Math.min(upgrades.getInt(upgrade.getRawName()), upgrade.getMax());
            if (wanted <= 0) {
                continue;
            }
            if (!tile.getComponent().supports(upgrade)) {
                unsupported.add(Component.translatable("message.mekanismgizmo.configurator.unsupported",
                      upgradeName(upgrade)).withStyle(ChatFormatting.YELLOW));
                continue;
            }
            int missing = wanted - tile.getComponent().getUpgrades(upgrade);
            if (missing > 0) {
                toAdd.put(upgrade, missing);
            }
        }
        List<Component> shortfall = new ArrayList<>();
        for (Map.Entry<Upgrade, Integer> entry : toAdd.entrySet()) {
            int available = count(player.getInventory(), upgradeItem(entry.getKey()));
            if (available < entry.getValue()) {
                shortfall.add(Component.translatable("message.mekanismgizmo.configurator.upgrade_short",
                      upgradeName(entry.getKey()), entry.getValue(), available).withStyle(ChatFormatting.RED));
            }
        }
        if (!shortfall.isEmpty()) {
            // Chat, not the action bar: the paste itself succeeded, so this has to be readable afterwards
            message(player, Component.translatable("message.mekanismgizmo.configurator.upgrades_skipped")
                  .withStyle(ChatFormatting.RED), false);
            for (Component line : shortfall) {
                message(player, line, false);
            }
        } else {
            for (Map.Entry<Upgrade, Integer> entry : toAdd.entrySet()) {
                consume(player.getInventory(), upgradeItem(entry.getKey()), entry.getValue());
                tile.getComponent().addUpgrades(entry.getKey(), entry.getValue());
            }
            // The items were removed directly from the inventory, not through a container slot. Without this the
            // client would keep showing the consumed upgrade items until the next full inventory sync.
            player.getInventory().setChanged();
            if (player.containerMenu != null) {
                player.containerMenu.broadcastChanges();
            }
        }
        for (Component line : unsupported) {
            message(player, line, false);
        }
    }

    /** Sends a message when the player can receive one (a player without a connection, as in tests, is skipped). */
    private static void message(Player player, Component message, boolean actionBar) {
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            player.displayClientMessage(message, actionBar);
        }
    }

    /** Mekanism's own action bar messages, guarded so a player without a connection cannot break the paste. */
    private static void tell(Player player, Component message, boolean actionBar) {
        message(player, message, actionBar);
    }

    private static CompoundTag upgradeTag(TileEntityMekanism tile) {
        CompoundTag upgrades = new CompoundTag();
        for (Upgrade upgrade : Upgrade.values()) {
            int installed = Math.min(tile.getComponent().getUpgrades(upgrade), upgrade.getMax());
            if (installed > 0) {
                upgrades.putInt(upgrade.getRawName(), installed);
            }
        }
        return upgrades;
    }

    /**
     * The upgrade's own item name ("Speed Upgrade", "Energy Upgrade"), not {@link Upgrade#getDescription()}: that one
     * is the lore sentence ("Increases speed of machinery.") and reads badly inside a message.
     */
    private static Component upgradeName(Upgrade upgrade) {
        return Component.translatable(upgradeItem(upgrade).getDescriptionId());
    }

    /** The Mekanism item that pays for an upgrade. */
    private static Item upgradeItem(Upgrade upgrade) {
        return switch (upgrade) {
            case SPEED -> MekanismItems.SPEED_UPGRADE.get();
            case ENERGY -> MekanismItems.ENERGY_UPGRADE.get();
            case FILTER -> MekanismItems.FILTER_UPGRADE.get();
            case GAS -> MekanismItems.GAS_UPGRADE.get();
            case MUFFLING -> MekanismItems.MUFFLING_UPGRADE.get();
            case ANCHOR -> MekanismItems.ANCHOR_UPGRADE.get();
            case STONE_GENERATOR -> MekanismItems.STONE_GENERATOR_UPGRADE.get();
        };
    }

    private static int count(Inventory inventory, Item item) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void consume(Inventory inventory, Item item, int amount) {
        int left = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && left > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(item)) {
                continue;
            }
            int taken = Math.min(left, stack.getCount());
            stack.shrink(taken);
            left -= taken;
        }
    }

    @Nullable
    private static BlockEntityType<?> readStoredType(CompoundTag data) {
        if (!data.contains(NBTConstants.DATA_TYPE, Tag.TAG_STRING)) {
            return null;
        }
        net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.tryParse(data.getString(NBTConstants.DATA_TYPE));
        return id == null ? null : ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(id);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        // Mekanism's own "Data: <machine>" line (and "None" for an empty card)
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag data = storedData(stack);
        if (data == null) {
            return;
        }
        CompoundTag upgrades = data.getCompound(UPGRADES);
        for (Upgrade upgrade : Upgrade.values()) {
            int installed = upgrades.getInt(upgrade.getRawName());
            if (installed > 0) {
                tooltip.add(Component.translatable("tooltip.mekanismgizmo.improved_configurator.upgrade",
                      upgradeName(upgrade), installed).withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        if (data.contains(MINER, Tag.TAG_COMPOUND)) {
            tooltip.add(Component.translatable("tooltip.mekanismgizmo.improved_configurator.miner")
                  .withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}