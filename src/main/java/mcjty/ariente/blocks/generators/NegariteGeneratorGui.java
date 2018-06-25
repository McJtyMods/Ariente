package mcjty.ariente.blocks.generators;

import mcjty.ariente.Ariente;
import mcjty.ariente.network.ArienteMessages;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import net.minecraft.util.ResourceLocation;

public class NegariteGeneratorGui extends GenericGuiContainer<NegariteGeneratorTile> {

    public NegariteGeneratorGui(NegariteGeneratorTile tileEntity, GenericContainer container) {
        super(Ariente.instance, ArienteMessages.INSTANCE, tileEntity, container, -1, "negarite_generator");
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, ArienteMessages.INSTANCE, new ResourceLocation(Ariente.MODID, "gui/negarite_generator.gui"));
        super.initGui();

        initializeFields();
    }

    private void initializeFields() {
        ((ImageChoiceLabel) window.findChild("redstone")).setCurrentChoice(tileEntity.getRSMode().ordinal());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
    }
}
