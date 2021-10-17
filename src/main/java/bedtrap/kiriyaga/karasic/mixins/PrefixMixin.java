package bedtrap.kiriyaga.karasic.mixins;

import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatUtils.class)
public class PrefixMixin {
    @Inject(method = "getMeteorPrefix", at = @At("HEAD"), cancellable = true, remap = false)
    private static void getPrefix(CallbackInfoReturnable<BaseText> cir) {
        BaseText newPrefix = new LiteralText("karasic.cc");
        BaseText prefix = new LiteralText("");
        newPrefix.setStyle(newPrefix.getStyle().withFormatting(Formatting.DARK_RED));
        prefix.setStyle(prefix.getStyle().withFormatting(Formatting.DARK_RED));
        prefix.append("");
        prefix.append(newPrefix);
        prefix.append(" ");
        cir.setReturnValue(prefix);
    }
}
