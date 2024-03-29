package mcjty.ariente.client;

// Kindly 'stolen' from gigaherz! Thanks!
public class ModelHandle {
    // @todo 1.15
//    static Map<String, IBakedModel> loadedModels = Maps.newHashMap();
//
//    private final Map<String, String> textureReplacements = Maps.newHashMap();
//    private final ResourceLocation model;
//    private final String key;
//    private final VertexFormat vertexFormat;
//    private final IModelState state;
//    private final boolean uvLock;
//
//    private static int reloadCount = 0;
//    private int cacheCount = 0;
//    private IBakedModel cacheCopy;
//
//    private ModelHandle(ResourceLocation model) {
//        this.model = model;
//        this.vertexFormat = DefaultVertexFormats.ITEM;
//        this.state = null;
//        this.uvLock = false;
//        this.key = computeKey();
//    }
//
//    private ModelHandle(ModelHandle handle, String texChannel, String resloc) {
//        this.model = handle.model;
//        this.vertexFormat = handle.vertexFormat;
//        this.state = handle.state;
//        this.uvLock = handle.uvLock;
//        textureReplacements.putAll(handle.textureReplacements);
//        textureReplacements.put(texChannel, resloc);
//        this.key = computeKey();
//    }
//
//    private ModelHandle(ModelHandle handle, VertexFormat fmt) {
//        this.model = handle.model;
//        this.vertexFormat = fmt;
//        this.state = handle.state;
//        this.uvLock = handle.uvLock;
//        textureReplacements.putAll(handle.textureReplacements);
//        this.key = computeKey();
//    }
//
//    private ModelHandle(ModelHandle handle, IModelState state) {
//        this.model = handle.model;
//        this.vertexFormat = handle.vertexFormat;
//        this.state = state;
//        this.uvLock = handle.uvLock;
//        textureReplacements.putAll(handle.textureReplacements);
//        this.key = computeKey();
//    }
//
//    private ModelHandle(ModelHandle handle, boolean uvLock) {
//        this.model = handle.model;
//        this.vertexFormat = handle.vertexFormat;
//        this.state = handle.state;
//        this.uvLock = uvLock;
//        textureReplacements.putAll(handle.textureReplacements);
//        this.key = computeKey();
//    }
//
//    private String computeKey() {
//        StringBuilder b = new StringBuilder();
//        b.append(model.toString());
//        for (Map.Entry<String, String> entry : textureReplacements.entrySet()) {
//            b.append("//");
//            b.append(entry.getKey());
//            b.append("/");
//            b.append(entry.getValue());
//        }
//        b.append("//VF:");
//        b.append(vertexFormat.hashCode());
//        b.append("//S:");
//        b.append((state != null) ? state.hashCode() : "n");
//        b.append("//UVL:");
//        b.append(uvLock);
//        return b.toString();
//    }
//
//    /**
//     * @param texChannel : the texture channel, a.k.a the texture identifier name (example for blocks : "all", or "side")
//     * @param resloc     : the new texture location
//     */
//    public ModelHandle replace(String texChannel, String resloc) {
//        if (textureReplacements.containsKey(texChannel) && textureReplacements.get(texChannel).equals(resloc))
//            return this;
//        return new ModelHandle(this, texChannel, resloc);
//    }
//
//    public ModelHandle vertexFormat(VertexFormat fmt) {
//        if (vertexFormat == fmt)
//            return this;
//        return new ModelHandle(this, fmt);
//    }
//
//    public ModelHandle state(IModelState newState) {
//        if (state == newState)
//            return this;
//        return new ModelHandle(this, newState);
//    }
//
//    public ModelHandle uvLock(boolean uvLock) {
//        if (this.uvLock == uvLock)
//            return this;
//        return new ModelHandle(this, uvLock);
//    }
//
//    public ResourceLocation getModel() {
//        return model;
//    }
//
//    public String getKey() {
//        return key;
//    }
//
//    public Map<String, String> getTextureReplacements() {
//        return textureReplacements;
//    }
//
//    public VertexFormat getVertexFormat() {
//        return vertexFormat;
//    }
//
//    @Nullable
//    public IModelState getState() {
//        return state;
//    }
//
//    public boolean uvLocked() {
//        return uvLock;
//    }
//
//    public IBakedModel get() {
//        if (cacheCount == reloadCount && cacheCopy != null)
//            return cacheCopy;
//        return (cacheCopy = loadModel(this));
//    }
//
//    public void render() {
//        renderModel(get(), getVertexFormat());
//    }
//
//    public void render(int color) {
//        renderModel(get(), getVertexFormat(), color);
//    }
//
//    // ========================================================= STATIC METHODS
//
//    private static boolean initialized = false;
//
//    public static void init() {
//        if (initialized)
//            return;
//
//        initialized = true;
//
//        IResourceManager rm = Minecraft.getInstance().getResourceManager();
//        if (rm instanceof IReloadableResourceManager) {
//            ((IReloadableResourceManager) rm).addReloadListener(new IResourceManagerReloadListener() {
//                @Override
//                public void onResourceManagerReload(IResourceManager __) {
//                    loadedModels.clear();
//                    reloadCount++;
//                }
//            });
//        }
//    }
//
//    @Nonnull
//    public static ModelHandle of(String model) {
//        return new ModelHandle(new ResourceLocation(model));
//    }
//
//    @Nonnull
//    public static ModelHandle of(ResourceLocation model) {
//        return new ModelHandle(model);
//    }
//
//    private static void renderModel(IBakedModel model, VertexFormat fmt) {
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder worldrenderer = tessellator.getBuffer();
//        worldrenderer.begin(GL11.GL_QUADS, fmt);
//        for (BakedQuad bakedquad : model.getQuads(null, null, new Random())) {  // @todo 1.14 new Random?
//            worldrenderer.addVertexData(bakedquad.getVertexData());
//        }
//        tessellator.draw();
//    }
//
//    private static void renderModel(IBakedModel model, VertexFormat fmt, int color) {
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder worldrenderer = tessellator.getBuffer();
//        worldrenderer.begin(GL11.GL_QUADS, fmt);
//        for (BakedQuad bakedquad : model.getQuads(null, null, new Random())) {  // @todo 1.14 new Random?
//            LightUtil.renderQuadColor(worldrenderer, bakedquad, color);
//        }
//        tessellator.draw();
//    }
//
//    private static IBakedModel loadModel(ModelHandle handle) {
//        IBakedModel model = loadedModels.get(handle.getKey());
//        if (model != null)
//            return model;
//
//        try {
//            IModel mod = ModelLoaderRegistry.getModel(handle.getModel());
//            if (handle.getTextureReplacements().size() > 0) {
//                mod = mod.retexture(ImmutableMap.copyOf(handle.getTextureReplacements()));
//            }
//            if (handle.uvLocked()) {
//                // @todo 1.14
////                mod = mod.uvlock(true);
//            }
//            IModelState state = handle.getState();
//            if (state == null) state = mod.getDefaultState();
//            // @todo 1.14
////            model = mod.bake(state, handle.getVertexFormat(), ModelLoader.defaultTextureGetter());
//            loadedModels.put(handle.getKey(), model);
//            return model;
//        } catch (Exception e) {
//            throw new ReportedException(new CrashReport("Error loading custom model " + handle.getModel(), e));
//        }
//    }
}
