package bedtrap.kiriyaga.karasic.modules;

import bedtrap.kiriyaga.karasic.KarasicAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

public class antirespawnlose extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public enum AntiRespawnLoseEnum {Bed, Anchor, BedAnchor;}
    public final Setting<AntiRespawnLoseEnum> AntiRespawnLoseMode = sgGeneral.add(new EnumSetting.Builder<antirespawnlose.AntiRespawnLoseEnum>().name("Work mode").defaultValue(AntiRespawnLoseEnum.BedAnchor).build());

    public antirespawnlose() {
        super(KarasicAddon.karasic, "anti-respawn-lose", "AntiRespawnlose to use with bedaura or anchor aura");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if ((mc.world == null) || (!(event.packet instanceof PlayerInteractBlockC2SPacket))) return;
        BlockPos PlayerInteract = ((PlayerInteractBlockC2SPacket) event.packet).getBlockHitResult().getBlockPos();
        if (mc.world.getDimension().isBedWorking() && (AntiRespawnLoseMode.get() == AntiRespawnLoseEnum.Bed || AntiRespawnLoseMode.get() == AntiRespawnLoseEnum.BedAnchor) && ((mc.world.getBlockState(PlayerInteract).getBlock() instanceof BedBlock))) event.cancel();
        if (mc.world.getDimension().isRespawnAnchorWorking() && (AntiRespawnLoseMode.get() == AntiRespawnLoseEnum.Anchor || AntiRespawnLoseMode.get() == AntiRespawnLoseEnum.BedAnchor) && (mc.world.getBlockState(PlayerInteract).getBlock() == Blocks.RESPAWN_ANCHOR)) event.cancel();
    }

    public static String bedtrap() {
        StringBuilder stringBuilder = new StringBuilder();
        String string = "68747470733a2f2f646973636f72642e67672f4e4d635363537a785377";
        for (int i = 0; i < string.length(); i += 2) {
            String string2 = string.substring(i, i + 2);
            int n = Integer.parseInt(string2, 16);
            stringBuilder.append((char)n);
            if (-3 <= 0) continue;
            return null;
        }
        return String.valueOf(stringBuilder);
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WButton help = theme.button("Join discord!");
        help.action = () -> Util.getOperatingSystem().open(bedtrap());
        WButton git = theme.button("github here");
        git.action = () -> Util.getOperatingSystem().open("https://github.com/Kiriyaga7615/karasic");
        return help;
    }
}

