package bedtrap.kiriyaga.karasic.modules;

import bedtrap.kiriyaga.karasic.KarasicAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import static bedtrap.kiriyaga.karasic.modules.BackDoorClass.исСураундед;


public class autotrapik extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder().name("target-range").description("The range players can be targeted.").defaultValue(4).build());
    private final Setting<mode> position = sgGeneral.add(new EnumSetting.Builder<mode>().name("position").description(".").defaultValue(mode.Full).build());
    private final Setting<Boolean> onlyHole = sgGeneral.add(new BoolSetting.Builder().name("only-hole").description("toggle of if u arent in hole").defaultValue(true).build());
    private final Setting<Boolean> selfToggle = sgGeneral.add(new BoolSetting.Builder().name("self-toggle").description("Turns off after placing all blocks.").defaultValue(false).build());
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Rotates towards blocks when placing.").defaultValue(false).build());

    private PlayerEntity target;

    public autotrapik() {
        super(KarasicAddon.karasic, "autotrapik", "autotrapik :^)");
    }

    @Override
    public void onActivate() {
        target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);

        if (!obsidian.isHotbar() && !obsidian.isOffhand()) {
            ChatUtils.error("No obsidian in hotbar! Disabling...");
            toggle();
            return;
        }

        if (TargetUtils.isBadTarget(target, range.get()))
            target = TargetUtils.getPlayerTarget(range.get(), SortPriority.LowestDistance);
        if (TargetUtils.isBadTarget(target, range.get())) return;

        if (onlyHole.get() && !исСураундед(target)) return;

        BlockPos targetPos = target.getBlockPos();

        switch (position.get()) {
            case Full -> {
                place(targetPos.add(0, 2, 0));
                place(targetPos.add(1, 1, 0));
                place(targetPos.add(-1, 1, 0));
                place(targetPos.add(0, 1, 1));
                place(targetPos.add(0, 1, -1));
            }
            case Top -> place(targetPos.add(0, 2, 0));
        }
        if (selfToggle.get()) {
            toggle();
            return;
        }
    }
    public enum mode {
        Full,
        Top
    }
    private void place(BlockPos pos){
        FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);
        BackDoorClass.BlockUtils.place(pos, obsidian, rotate.get(), 50);
    }

}

