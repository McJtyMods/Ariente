package mcjty.ariente.blocks.decorative;

public class DoubleMarbleSlabBlock extends MarbleSlabBlock {

    @Override
    public boolean isDouble() {
        return true;
    }

    // @todo 1.14
//    public void initModel() {
//        for (MarbleColor type : MarbleColor.VALUES) {
//            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.ordinal(), new ModelResourceLocation(getRegistryName(),
//                    "type=" + type.getName()));
//        }
//    }
}
