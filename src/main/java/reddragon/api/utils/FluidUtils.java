package reddragon.api.utils;

import java.util.Collection;
import java.util.List;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class FluidUtils {

    public static void setupFluidRendering(final Fluid stillFluid,
                                              final Fluid flowingFluid,
                                              final Identifier fluidIdentifier,
                                              final int color) {

		final var stillSpriteId = new Identifier(fluidIdentifier.getNamespace(),
				"block/fluids/" + fluidIdentifier.getPath());

		final var flowingSpriteId = new Identifier(fluidIdentifier.getNamespace(),
				"block/fluids/" + fluidIdentifier.getPath() + "_flowing");

        final var fluidId = Registries.FLUID.getId(stillFluid);
		final var listenerId = new Identifier(fluidId.getNamespace(), fluidId.getPath() + "_reload_listener");

		final Sprite[] fluidSprites = { null, null };

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(new SimpleSynchronousResourceReloadListener() {

                @Override
                public Identifier getFabricId() {
                    return listenerId;
                }

                @Override
                public void reload(ResourceManager manager) {
                    var atlas = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);

                    fluidSprites[0] = atlas.apply(stillSpriteId);
                    fluidSprites[1] = atlas.apply(flowingSpriteId);
                }

                @Override
                public Collection<Identifier> getFabricDependencies() {
                    return List.of(ResourceReloadListenerKeys.TEXTURES);
                }
            });

		final FluidRenderHandler renderHandler = new FluidRenderHandler() {
			@Override
			public Sprite[] getFluidSprites(final BlockRenderView view, final BlockPos pos, final FluidState state) {
				return fluidSprites;
			}

			@Override
			public int getFluidColor(final BlockRenderView view, final BlockPos pos, final FluidState state) {
				return color;
			}
		};

        FluidRenderHandlerRegistry.INSTANCE.register(stillFluid, flowingFluid, renderHandler);
		BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), stillFluid, flowingFluid);
	}
}
