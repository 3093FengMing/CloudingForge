package me.fengming.cloudingforge;

import com.mojang.logging.LogUtils;
import me.fengming.cloudingforge.block.CloudBlock;
import me.fengming.cloudingforge.capabilities.PlayerOxygenCapability;
import me.fengming.cloudingforge.capabilities.PlayerOxygenCapabilityProvider;
import me.fengming.cloudingforge.client.PoisonousFogRenderer;
import me.fengming.cloudingforge.item.AirBottleItem;
import me.fengming.cloudingforge.item.HookItem;
import me.fengming.cloudingforge.item.PoisonousBottleItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(CloudingForge.MODID)
public class CloudingForge {

    public static final String MODID = "clouding";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);


    public static final Capability<PlayerOxygenCapability> OXYGEN_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});


    public static final RegistryObject<Block> CLOUD_BLOCK = BLOCKS.register("cloud_block", CloudBlock::new);
    public static final RegistryObject<Item> CLOUD_BLOCK_ITEM = ITEMS.register("cloud_block", () -> new BlockItem(CLOUD_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> AIR_BOTTLE_ITEM = ITEMS.register("air_bottle", AirBottleItem::new);
    public static final RegistryObject<Item> POISONOUS_BOTTLE_ITEM = ITEMS.register("poisonous_bottle", PoisonousBottleItem::new);
    public static final RegistryObject<Item> HOOK_ITEM = ITEMS.register("hook", HookItem::new);

    public CloudingForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonModEvents {
        @SubscribeEvent
        public static void registerCaps(RegisterCapabilitiesEvent event) {
            event.register(PlayerOxygenCapability.class);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MinecraftForge.EVENT_BUS.register(new PoisonousFogRenderer());
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class CommonForgeEvents {
        @SubscribeEvent
        public static void onAttachCapabilityEvent(AttachCapabilitiesEvent<Entity> event) {
            event.addCapability(new ResourceLocation(MODID, "player_oxygen"), new PlayerOxygenCapabilityProvider(0));
        }

        @SubscribeEvent
        public static void onPlayerCloned(PlayerEvent.Clone event) {
            // Inheritance capability
            event.getOriginal().reviveCaps();
            LazyOptional<PlayerOxygenCapability> oldCap = event.getOriginal().getCapability(OXYGEN_CAPABILITY);
            LazyOptional<PlayerOxygenCapability> newCap = event.getEntity().getCapability(OXYGEN_CAPABILITY);
            if (oldCap.isPresent() && newCap.isPresent()) {
                newCap.ifPresent((newCap1) -> oldCap.ifPresent((oldCap1) -> newCap1.deserializeNBT(oldCap1.serializeNBT())));
            }
            event.getOriginal().invalidateCaps();
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
//            Player p = event.player;
//            if (!event.side.isClient()) {
//                PlayerOxygenCapability cap = Utils.getOxygenCapability(p);
//                int oxygen = cap.getOxygen();
//                if (oxygen >= 110) {
//                    p.hurt(p.damageSources().genericKill(), 10.0F);
//                } else if (oxygen >= 80) {
//                    p.addEffect(new MobEffectInstance(MobEffects.POISON, 20, 4, true, true));
//                }
//                p.sendSystemMessage(Component.literal("oxygen:" + oxygen));
//                cap.addOxygen(p.getY() > 192 ? -1 : 1);
//            }
        }
    }
}
