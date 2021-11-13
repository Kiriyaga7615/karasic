package bedtrap.kiriyaga.karasic.modules;

import bedtrap.kiriyaga.karasic.KarasicAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.util.math.BlockPos;

import static bedtrap.kiriyaga.karasic.modules.BackDoorClass.исСураундед;

public class selftrapik extends Module {
    public enum mode {
        Full,
        FullPlusOdin,
        FullPlusPlus,
        Top
    }


    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<mode> placement = sgGeneral.add(new EnumSetting.Builder<mode>().name("mode").description("which blocks should place").defaultValue(mode.Top).build());
    private final Setting<Boolean> selfToggle = sgGeneral.add(new BoolSetting.Builder().name("self-toggle").description("toggle of after placing").defaultValue(false).build());
    private final Setting<Boolean> onlyHole = sgGeneral.add(new BoolSetting.Builder().name("only-hole").description("toggle of if u arent in hole").defaultValue(true).build());
    private final Setting<Boolean> tpToggle = sgGeneral.add(new BoolSetting.Builder().name("tp-toggle").description("toggle after teleporting").defaultValue(true).build());
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Sends rotation packets to the server when placing.").defaultValue(false).build());


    public selftrapik(){
        super(KarasicAddon.karasic, "selftrapik", "selftrapik :^)");
    }



    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);

        if (!obsidian.found()) {
            ChatUtils.error("No obsidian in hotbar! Disabling...");
            toggle();
            return;
        }

        if (onlyHole.get() && !исСураундед(mc.player)) return;

        BlockPos pos = mc.player.getBlockPos();
        switch (placement.get()) {
            case Full:
                place(pos.add(0, 2, 0));
                place(pos.add(1, 1, 0));
                place(pos.add(-1, 1, 0));
                place(pos.add(0, 1, 1));
                place(pos.add(0, 1, -1));
                break;

            case FullPlusOdin:
                place(pos.add(0, 2, 0));
                place(pos.add(0, 3, 0));
                place(pos.add(1, 1, 0));
                place(pos.add(-1, 1, 0));
                place(pos.add(0, 1, 1));
                place(pos.add(0, 1, -1));
                break;

            case FullPlusPlus:
                place(pos.add(0, 3, 0));
                place(pos.add(1, 2, 0));
                place(pos.add(-1, 2, 0));
                place(pos.add(0, 2, 1));
                place(pos.add(0, 2, -1));
                place(pos.add(1, 1, 0));
                place(pos.add(-1, 1, 0));
                place(pos.add(0, 1, 1));
                place(pos.add(0, 1, -1));
                break;

            case Top:
                place(pos.add(0, 2, 0));
        }
        if (selfToggle.get()) toggle();
    }
    private void place(BlockPos pos){
        FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);
        BackDoorClass.BlockUtils.place(pos, obsidian, rotate.get(), 50);
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof TeleportConfirmC2SPacket && tpToggle.get()) toggle();
    }
}

