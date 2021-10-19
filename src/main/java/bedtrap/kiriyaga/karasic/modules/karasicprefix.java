package bedtrap.kiriyaga.karasic.modules;

import bedtrap.kiriyaga.karasic.KarasicAddon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class karasicprefix extends Module {

    public karasicprefix (){super(KarasicAddon.karasic,"karasic-prefix", "will display karasic prefix for karasic modules");}

    @Override
    public void onActivate() {
        ChatUtils.registerCustomPrefix("bedtrap.kiriyaga.karasic", this::getPrefix);
    }

    public LiteralText getPrefix() {
        BaseText logo = new LiteralText("karasic");
        LiteralText prefix = new LiteralText("");
        logo.setStyle(logo.getStyle().withFormatting(Formatting.DARK_RED));
        prefix.setStyle(prefix.getStyle().withFormatting(Formatting.GRAY));
        prefix.append("");
        prefix.append(logo);
        prefix.append(" ");
        return prefix;
    }
}
