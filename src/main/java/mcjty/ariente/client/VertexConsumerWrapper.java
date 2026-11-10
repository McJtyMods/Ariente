package mcjty.ariente.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class VertexConsumerWrapper implements VertexConsumer {

    protected final VertexConsumer delegate;

    public VertexConsumerWrapper(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        delegate.setColor(r, g, b, a);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        delegate.setNormal(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        return setColor(color >>> 16 & 255, color >>> 8 & 255, color & 255, color >>> 24);
    }
}
