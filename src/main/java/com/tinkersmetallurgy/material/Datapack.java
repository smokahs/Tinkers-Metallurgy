package com.tinkersmetallurgy.material;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;

import com.tinkersmetallurgy.TinkersMetallurgy;

// serves the generated material files as a built in pack.
public final class Datapack implements PackResources {

    private final String id;

    @Nullable
    private Gen.Generated generated;

    public Datapack(String id) {
        this.id = id;
    }

    private Gen.Generated generated() {
        Gen.Generated g = this.generated;
        if (g == null) {
            try {
                g = Gen.generate();
            } catch (Throwable failed) {
                TinkersMetallurgy.LOGGER.error("Failed to generate materials", failed);
                g = new Gen.Generated(Map.of(), Map.of());
            }
            this.generated = g;
        }
        return g;
    }

    private Map<ResourceLocation, byte[]> forType(PackType packType) {
        Gen.Generated g = generated();
        return packType == PackType.SERVER_DATA ? g.data() : g.assets();
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        byte[] bytes = forType(packType).get(location);
        return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput out) {
        for (Map.Entry<ResourceLocation, byte[]> entry : forType(packType).entrySet()) {
            ResourceLocation loc = entry.getKey();
            if (loc.getNamespace().equals(namespace) && loc.getPath().startsWith(path)) {
                byte[] bytes = entry.getValue();
                out.accept(loc, () -> new ByteArrayInputStream(bytes));
            }
        }
    }

    // read off the files themselves, since a namespace left out here is never asked for.
    @Override
    public Set<String> getNamespaces(PackType packType) {
        Set<String> namespaces = new HashSet<>();
        for (ResourceLocation location : forType(packType).keySet()) {
            namespaces.add(location.getNamespace());
        }
        return namespaces;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
        if (deserializer == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(
                    Component.literal("Tinkers' Metallurgy materials"),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA));
        }
        return null;
    }

    @Override
    public String packId() {
        return id;
    }

    @Override
    public void close() {}
}
