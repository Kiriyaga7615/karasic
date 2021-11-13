package bedtrap.kiriyaga.karasic.modules;

import bedtrap.kiriyaga.karasic.KarasicAddon;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.misc.Vec3;
import meteordevelopment.meteorclient.utils.player.*;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;


public class citybreaker extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");


    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder().name("target-range").description("The radius in which players get targeted.").defaultValue(6).min(0).sliderMax(7).build());
    private final Setting<Boolean> autoSwap = sgGeneral.add(new BoolSetting.Builder().name("auto-swap").description("auto swap to pick axe").defaultValue(true).build());
    public final Setting<swapMode> autoSwapSetting = sgGeneral.add(new EnumSetting.Builder<swapMode>().name("swap-mode").defaultValue(swapMode.normal).visible(autoSwap::get).build());
    private final Setting<Boolean> swapBack = sgGeneral.add(new BoolSetting.Builder().name("swap-back").description("swap to previous slot").defaultValue(false).visible(autoSwap::get).build());
    private final Setting<Boolean> support = sgGeneral.add(new BoolSetting.Builder().name("support").description("If there is no block below a city block it will place one before mining.").defaultValue(true).build());
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Automatically rotates you towards the city block.").defaultValue(false).build());
    private final Setting<Boolean> instant = sgGeneral.add(new BoolSetting.Builder().name("instant").description("instant re break").defaultValue(false).build());
    private final Setting<Integer> instantDelay = sgGeneral.add(new IntSetting.Builder().name("delay").description("delay to re break blocks").defaultValue(1).min(0).sliderMax(5).visible(instant::get).build());
    private final Setting<Boolean> test = sgGeneral.add(new BoolSetting.Builder().name("test").description("test").defaultValue(true).build());

    private final Setting<Boolean> swing = sgRender.add(new BoolSetting.Builder().name("swing").description("client-side swing").defaultValue(true).build());
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder().name("render").description("render city block").defaultValue(true).build());
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color for positions to be placed.").defaultValue(new SettingColor(255, 0, 0, 75)).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color for positions to be placed.").defaultValue(new SettingColor(255, 0, 150)).build());
    private final Setting<Boolean> renderProgress = sgRender.add(new BoolSetting.Builder().name("render-progress").description("render mine progress").defaultValue(true).build());
    private final Setting<Double> progressScale = sgRender.add(new DoubleSetting.Builder().name("progress-scale").description("scale of text").defaultValue(1.5).min(0).sliderMax(3).build());
    private final Setting<SettingColor> progressColor = sgRender.add(new ColorSetting.Builder().name("progress-color").description("the color of text rendered").defaultValue(new SettingColor(255, 255, 255)).build());

    public enum swapMode {normal, smart}
    FindItemResult ironPick;
    FindItemResult diamondPick;
    FindItemResult netheritePick;
    private boolean antiSpam;
    private PlayerEntity target;
    private BlockPos blockPosTarget;
    private int breakTimer;
    private boolean shouldCount;
    private int instantTimer;
    private int swapSlot;


    public citybreaker() {
        super(KarasicAddon.karasic, "city-breaker", "auto city breaker");

    }

    @Override
    public void onActivate() {
        swapSlot = mc.player.getInventory().selectedSlot;
        instantTimer = 0;
        breakTimer = 0;
        antiSpam = false;
        blockPosTarget = null;
        target = null;
        shouldCount = false;
    }

    @EventHandler
    @Override
    public void onDeactivate() {
        if (blockPosTarget != null)
            mc.player.networkHandler.sendPacket((Packet) new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPosTarget, Direction.UP));
        if (swapBack.get())mc.player.getInventory().selectedSlot = swapSlot;
    }

    @EventHandler
    private void onTick(final TickEvent.Pre event) {
        instantTimer++;
        if (shouldCount)breakTimer++;
        if (TargetUtils.isBadTarget(target, targetRange.get())) {
            target = TargetUtils.getPlayerTarget(targetRange.get(), SortPriority.LowestDistance);
        }

        if (TargetUtils.isBadTarget(target, targetRange.get())) {
            target = null;
            antiSpam = false;
            blockPosTarget = null;
            breakTimer = 0;
            ChatUtils.error("No target founded! Disabling...");
            toggle();
            return;
        }

        blockPosTarget = ((blockPosTarget == null) ? EntityUtils.getCityBlock(target) : blockPosTarget);

        ironPick = InvUtils.find(itemStack -> itemStack.getItem() == Items.IRON_PICKAXE);
        diamondPick = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE);
        netheritePick = InvUtils.find(itemStack -> itemStack.getItem() == Items.NETHERITE_PICKAXE);

        if (delay() == 0) {
            ChatUtils.error("No pick in hotbar! Disabling...");
            toggle();
            return;
        }

        if (blockPosTarget == null) {
            ChatUtils.error("No city block found! Disabling...");
            toggle();
            return;
        }

         if (PlayerUtils.distanceTo(blockPosTarget) > mc.interactionManager.getReachDistance()) {
            ChatUtils.error("Block to mine too far! Disabling...");
        }

         if (test.get()){
             if (mc.player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(mc.player)) return;
             if (!mc.player.isOnGround()) return;
         }

        if (!antiSpam) {
            ChatUtils.info("breaking " + target.getGameProfile().getName() +" surround");
            if (support.get()) {
                BlockUtils.place(blockPosTarget.down(1), InvUtils.findInHotbar(Items.OBSIDIAN), rotate.get(), 0, true);
            }
            if ((autoSwapSetting.get() == swapMode.normal)) pickSwap();

            if (rotate.get()) {
                Rotations.rotate(Rotations.getYaw(blockPosTarget), Rotations.getPitch(blockPosTarget));
                mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPosTarget, Direction.UP));

            }
            else {
                mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPosTarget, Direction.UP));
            }
            if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
            mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPosTarget, Direction.UP));
            shouldCount = true;
            antiSpam = true;
        }

        if (breakTimer>=delay()){
            if (!instant.get()){
            pickSwap();
            } else if (instantTimer>=instantDelay.get()){
                pickSwap();
                mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPosTarget, Direction.UP));
                instantTimer = 0;
                return;
            }
            if (breakTimer>=delay()+3) toggle();
        }
    }


    private int delay() {
        if (netheritePick.isHotbar()) {
            int slot = netheritePick.getSlot();
            ItemStack pick = mc.player.getInventory().getStack(slot);
            int eff = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, pick);
            switch (eff) {
                case 0 -> {return 170;}
                case 1 -> {return 140;}
                case 2 -> {return 110;}
                case 3 -> {return 80;}
                case 4 -> {return 60;}
                case 5 -> {return 45;}
            }
        }
        if (diamondPick.isHotbar()) {
            int slot = diamondPick.getSlot();
            ItemStack pick = mc.player.getInventory().getStack(slot);
            int eff = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, pick);
            switch (eff) {
                case 0 -> {return 190;}
                case 1 -> {return 151;}
                case 2 -> {return 118;}
                case 3 -> {return 85;}
                case 4 -> {return 61;}
                case 5 -> {return 46;}
            }
        }
        if (ironPick.isHotbar()) {
            int slot = ironPick.getSlot();
            ItemStack pick = mc.player.getInventory().getStack(slot);
            int eff = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, pick);
            switch (eff) {
                case 0 -> {return 836;}
                case 1 -> {return 627;}
                case 2 -> {return 457;}
                case 3 -> {return 315;}
                case 4 -> {return 220;}
                case 5 -> {return 160;}
            }
        }
        return 0;
    }

    private void pickSwap() {
        if (!autoSwap.get()) return;
        if (netheritePick.isHotbar()) {
            int slot = netheritePick.getSlot();
            mc.player.getInventory().selectedSlot = slot;
            return;
        }
        if (diamondPick.isHotbar()) {
            int slot = diamondPick.getSlot();
            mc.player.getInventory().selectedSlot = slot;
            return;
        }
        if (ironPick.isHotbar()) {
            int slot = ironPick.getSlot();
            mc.player.getInventory().selectedSlot = slot;
            return;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (blockPosTarget == null || !render.get() || target == null || mc.player.getAbilities().creativeMode) {
            return;
        }
        event.renderer.box(blockPosTarget, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }

    @EventHandler
    private void onRender2D(final Render2DEvent event) {
        if (blockPosTarget == null || !renderProgress.get() || target == null || mc.player.getAbilities().creativeMode) {
            return;
        }
        final Vec3 pos = new Vec3(blockPosTarget.getX() + 0.5, blockPosTarget.getY() + 0.5,blockPosTarget.getZ() + 0.5);
        if (NametagUtils.to2D(pos, progressScale.get())) {
            String progress;
            NametagUtils.begin(pos);
            TextRenderer.get().begin(1.0, false, true);
            progress = Math.round(100*breakTimer)/delay()+"%";
            if (Math.round(100*breakTimer)/delay()>=100) progress = "Done!";
            TextRenderer.get().render(progress, -TextRenderer.get().getWidth(progress) / 2.0, 0.0, progressColor.get());
            TextRenderer.get().end();
            NametagUtils.end();
        }
    }

}
