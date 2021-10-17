package bedtrap.kiriyaga.karasic.mixins;

import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TitleScreen.class, priority = 500)
public class TitleScreenMixin extends Screen {


    private final int Cvet = Color.fromRGBA(255, 0, 0, 255);

    private String karasiqueText1;
    private String karasiqueText2;
    private String karasiqueText3;
    private String karasiqueText4;

    private int karasiqueLength1;
    private int karasiqueLength2;
    private int karasiqueLength3;
    private int karasiqueLength4;

    private int karasiqueFullLength;
    private int karasiquePrevWidth;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {

        karasiqueText1 = "karasic by ";
        karasiqueText2 = "Kiriyaga.";
        karasiqueText3 = " github.com/Kiriyaga7615 ";
        karasiqueText4 = " discord.gg/NMcScSzxSw";


        karasiqueLength1 = textRenderer.getWidth(karasiqueText1);
        karasiqueLength2 = textRenderer.getWidth(karasiqueText2);
        karasiqueLength3 = textRenderer.getWidth(karasiqueText3);
        karasiqueLength4 = textRenderer.getWidth(karasiqueText4);

        karasiqueFullLength = karasiqueLength1 + karasiqueLength2 + karasiqueLength3 + karasiqueLength4;
        karasiquePrevWidth = 0;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
        if (!Config.get().titleScreenCredits) return;
        karasiquePrevWidth = 0;
        textRenderer.drawWithShadow(matrices, karasiqueText1, width - karasiqueFullLength - 3, 17, Cvet);
        karasiquePrevWidth += karasiqueLength1;
        textRenderer.drawWithShadow(matrices, karasiqueText2, width - karasiqueFullLength + karasiquePrevWidth - 3, 40, Cvet);
        karasiquePrevWidth += karasiqueLength2;
        textRenderer.drawWithShadow(matrices, karasiqueText3, width - karasiqueFullLength + karasiquePrevWidth - 3, 40, Cvet);
        karasiquePrevWidth += karasiqueLength3;
        textRenderer.drawWithShadow(matrices, karasiqueText4, width - karasiqueFullLength + karasiquePrevWidth - 3, 40, Cvet);
    }
}
