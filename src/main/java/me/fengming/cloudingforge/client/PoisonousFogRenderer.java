package me.fengming.cloudingforge.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class PoisonousFogRenderer {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final ResourceLocation locationFogPng = new ResourceLocation("clouding:textures/fog.png");
    public static float fogHeight = 194;
    public static float depth;
    public static float density;
    public static float red;
    public static float green;
    public static float blue;
    public static float cel;
    private boolean inBlock;
    private int prevPlayerTick;
    private float roof;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void renderEvent(TickEvent.RenderTickEvent e) {
        if (e.phase == TickEvent.Phase.START && e.side == LogicalSide.CLIENT) {
            Entity entity = mc.getCameraEntity();
            if (entity != null) {
                Level world = entity.level();
                if (world.isClientSide && mc.player.getY() > 194) {
                    float f = Mth.cos(world.getSunAngle(e.renderTickTime) * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
                    cel = Mth.clamp(f, 0.0F, 1.0F);
                    this.roof += (getRoof(world, entity.getOnPos().above().offset((int)-(Math.sin(Math.toRadians(entity.yRotO)) * 4), 0, (int)(Math.cos(Math.toRadians(entity.yRotO)) * 4))) - this.roof) * 0.1F;
                    // fogHeight = MistWorld.getFogHight(world, e.renderTickTime) + 4.0F;
                    depth = (float) (fogHeight - entity.getY() - entity.getEyeHeight());
                    if (this.prevPlayerTick != entity.tickCount) {
                        this.prevPlayerTick = entity.tickCount;
                    }
                }
            }
        }
    }
    /*
        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public void fogDensity(FogDensity e) {
            Entity entity = e.getEntity();
            Level world = entity.world;
            if (world.isClientSide && world.provider.dimension() == Mist.getID()) {
                float tick = (float)e.getRenderPartialTicks();
                float densityUp = Math.max(0.004F, 0.004F * 16 / (this.mc.gameSettings.renderLogicalSideanceChunks)) + world.getRainStrength(tick) * 0.005F + getMorningFog(world, tick);
                float densityBorder = 0.15F;
                float densityDown = 0.08F;
                float depth = depth;
                if (depth < 0) {
                    density = densityUp;
                    RenderSystem.setFog(RenderSystem.FogMode.EXP2);
                } else if (depth < 4) {
                    density = calculate(densityUp, densityBorder, densityUp, densityBorder, depth, 4);
                    RenderSystem.setFog(RenderSystem.FogMode.EXP);
                } else if (depth < 8) {
                    density = calculate(densityBorder, densityDown, densityDown, densityBorder, depth - 4, 4);
                    RenderSystem.setFog(RenderSystem.FogMode.EXP);
                } else {
                    density = densityDown;
                    RenderSystem.setFog(RenderSystem.FogMode.EXP);
                }
                if (entity instanceof LivingEntity && ((LivingEntity)entity).isPotionActive(MobEffects.BLINDNESS)) {
                    float d1 = 0.3F;
                    int i = ((LivingEntity)entity).getActivePotionEffect(MobEffects.BLINDNESS).getDuration();
                    if (i < 20)
                        d1 = density + (d1 - density) * i / 20.0F;
                    density = d1;
                } else if (e.getState().getMaterial() == Material.WATER) {
                    RenderSystem.setFog(RenderSystem.FogMode.EXP);
                    if (entity instanceof LivingEntity) {
                        if (((LivingEntity)entity).isPotionActive(MobEffects.WATER_BREATHING)) {
                            density = Math.max(density, 0.01F);
                        } else {
                            density = Math.max(density, 0.1F - EnchantmentHelper.getRespirationModifier((LivingEntity)entity) * 0.03F);
                        }
                    } else {
                        density = 0.1F;
                    }
                } else if (e.getState().getMaterial() == Material.LAVA) {
                    RenderSystem.setFog(RenderSystem.FogMode.EXP);
                    density = 2.0F;
                }
                e.setDensity(density);
                e.setCanceled(true);
            }
        }

        private float getMorningFog(Level world, float tick) {
            float i = ((world.getGameTime() + 23000) % 24000) + tick;
            i = Math.abs(21000 - i);
            i = 3000 - Mth.clamp(i, 0, 3000);
            return i/600000;
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public void fogColor(FogColors e) {
            Entity entity = e.getEntity();
            Level world = entity.world;
            if (world.isClientSide && world.provider.dimension() == Mist.getID()) {
                if (e.getState().getMaterial() != Material.WATER && e.getState().getMaterial() != Material.LAVA) {
                    float tick = (float)e.getRenderPartialTicks();
                    Vec3 colUp;
                    float rUp;
                    float gUp;
                    float bUp;
                    Vec3 colBorder;
                    float rBorder;
                    float gBorder;
                    float bBorder;
                    Vec3 colDown;
                    float rDown;
                    float gDown;
                    float bDown;
                    float depth = depth;
                    if (depth < 0) {
                        colUp = getFogUpColor(world, tick);
                        red = (float)colUp.x;
                        green = (float)colUp.y;
                        blue = (float)colUp.z;
                    } else if (depth < 4) {
                        colUp = getFogUpColor(world, tick);
                        colBorder = getFogBorderColor(world, tick);
                        rUp = (float)colUp.x;
                        gUp = (float)colUp.y;
                        bUp = (float)colUp.z;
                        rBorder = (float)colBorder.x;
                        gBorder = (float)colBorder.y;
                        bBorder = (float)colBorder.z;
                        float r = calculate(rUp, rBorder, Math.min(rUp, rBorder), Math.max(rUp, rBorder), depth, 4);
                        float g = calculate(gUp, gBorder, Math.min(gUp, gBorder), Math.max(gUp, gBorder), depth, 4);
                        float b = calculate(bUp, bBorder, Math.min(bUp, bBorder), Math.max(bUp, bBorder), depth, 4);
                        red = r;
                        green = g;
                        blue = b;
                    } else if (depth < 8) {
                        colBorder = getFogBorderColor(world, tick);
                        colDown = getFogDownColor(world, tick);
                        rBorder = (float)colBorder.x;
                        gBorder = (float)colBorder.y;
                        bBorder = (float)colBorder.z;
                        rDown = (float)colDown.x;
                        gDown = (float)colDown.y;
                        bDown = (float)colDown.z;
                        float r = calculate(rBorder, rDown, Math.min(rBorder, rDown), Math.max(rBorder, rDown), depth - 4, 4);
                        float g = calculate(gBorder, gDown, Math.min(gBorder, gDown), Math.max(gBorder, gDown), depth - 4, 4);
                        float b = calculate(bBorder, bDown, Math.min(bBorder, bDown), Math.max(bBorder, bDown), depth - 4, 4);
                        red = r;
                        green = g;
                        blue = b;
                    } else {
                        colDown = getFogDownColor(world, tick);
                        red = (float)colDown.x;
                        green = (float)colDown.y;
                        blue = (float)colDown.z;
                    }
                    double d1 = (entity.yOld + (entity.position().y - entity.yOld) * e.getRenderPartialTicks()) * world.provider.getVoidFogYFactor();
                    if (entity instanceof LivingEntity && ((LivingEntity)entity).isPotionActive(MobEffects.BLINDNESS)) {
                        int i = ((LivingEntity)entity).getActivePotionEffect(MobEffects.BLINDNESS).getDuration();
                        if (i < 20)
                            d1 *= 1.0F - i / 20.0F;
                        else d1 = 0.0D;
                    }
                    if (d1 < 1.0D) {
                        if (d1 < 0.0D)
                            d1 = 0.0D;
                        d1 = d1 * d1;
                        red = (float)(red * d1);
                        green = (float)(green * d1);
                        blue = (float)(blue * d1);
                    }
                    if (this.mc.gameSettings.anaglyph) {
                        float fr = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
                        float fg = (red * 30.0F + green * 70.0F) / 100.0F;
                        float fb = (red * 30.0F + blue * 70.0F) / 100.0F;
                        red = fr;
                        green = fg;
                        blue = fb;
                    }
                } else {
                    if (e.getState().getMaterial() == Material.WATER) {
                        float wb = 0.0F;
                        if (entity instanceof LivingEntity && depth < 0) {
                            wb = EnchantmentHelper.getRespirationModifier((LivingEntity)entity) * 0.2F;
                            if (((LivingEntity)entity).isPotionActive(MobEffects.WATER_BREATHING))
                                wb = wb * 0.3F + 0.6F;
                        }
                        red = 0.02F + wb;
                        green = 0.02F + wb;
                        blue = 0.2F + wb;
                    } else if (e.getState().getMaterial() == Material.LAVA) {
                        red = 0.6F;
                        green = 0.1F;
                        blue = 0.0F;
                    }
                }
                e.setRed(red);
                e.setGreen(green);
                e.setBlue(blue);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private Vec3 getFogUpColor(Level world, float partialTicks, float cel) {
            Entity entity = this.mc.getCameraEntity();
            float d = 0.25F + 0.75F * this.mc.gameSettings.renderLogicalSideanceChunks / 32.0F;
            d = 1.0F - (float)Math.pow(d, 0.25D);
            Vec3 vec3d = world.getSkyColor(entity, partialTicks);
            float f1 = (float)vec3d.x;
            float f2 = (float)vec3d.y;
            float f3 = (float)vec3d.z;
            float r = 160F / 255;//170
            float g = 210F / 255;
            float b = 210F / 255;//200
            r = r * (cel * 0.82F + 0.18F);
            g = g * (cel * 0.79F + 0.21F);
            b = b * (cel * 0.81F + 0.19F);
            if (this.mc.gameSettings.renderLogicalSideanceChunks >= 4) {
                double d0 = Mth.sin(world.getCelestialAngleRadians(partialTicks)) > 0.0F ? -1.0D : 1.0D;
                Vec3 vec3d2 = new Vec3(d0, 0.0D, 0.0D);
                float f = (float)entity.getLook(partialTicks).dotProduct(vec3d2);
                if (f < 0.0F)
                    f = 0.0F;
                if (f > 0.0F) {
                    float[] afloat = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);
                    if (afloat != null) {
                        f = f * afloat[3];
                        r = r * (1.0F - f) + afloat[0] * f;
                        g = g * (1.0F - f) + afloat[1] * f;
                        b = b * (1.0F - f) + afloat[2] * f;
                    }
                }
            }
            r += (f1 - r) * d;
            g += (f2 - g) * d;
            b += (f3 - b) * d;
            float rain = world.getRainStrength(partialTicks);
            if (rain > 0.0F) {
                float f4 = 1.0F - rain * 0.4F;
                float f5 = 1.0F - rain * 0.38F;
                r *= f4;
                g *= f4;
                b *= f5;
            }
            float thunder = world.getThunderStrength(partialTicks);
            if (thunder > 0.0F) {
                float f6 = 1.0F - thunder * 0.58F;
                float f7 = 1.0F - thunder * 0.56F;
                r *= f6;
                g *= f6;
                b *= f7;
            }
            r *= 0.75F + 0.25F * this.roof;
            g *= 0.8F + 0.2F * this.roof;
            b *= 0.85F + 0.15F * this.roof;
            if (entity instanceof LivingEntity && ((LivingEntity)entity).isPotionActive(MobEffects.NIGHT_VISION)) {
                float f8 = this.getNightVisionBrightness((LivingEntity)entity, partialTicks);
                float f9 = 1.0F / r;
                if (f9 > 1.0F / g)
                    f9 = 1.0F / g;
                if (f9 > 1.0F / b)
                    f9 = 1.0F / b;
                r = r * (1.0F - f8 * 0.5F) + r * f9 * f8 * 0.5F;
                g = g * (1.0F - f8 * 0.4F) + g * f9 * f8 * 0.4F;
                b = b * (1.0F - f8 * 0.65F) + b * f9 * f8 * 0.65F;
            }
            return new Vec3(r, g, b);
        }

        @OnlyIn(Dist.CLIENT)
        private Vec3 getFogBorderColor(Level world, float partialTicks, float cel) {
            float r = 1.0F;
            float g = 1.0F;
            float b = 1.0F;
            r = r * (cel * 0.62F + 0.38F);
            g = g * (cel * 0.59F + 0.41F);
            b = b * (cel * 0.56F + 0.44F);
            float rain = world.getRainStrength(partialTicks);
            if (rain > 0.0F) {
                float f4 = 1.0F - rain * 0.45F * cel;
                float f5 = 1.0F - rain * 0.38F * cel;
                r *= f4;
                g *= f5;
                b *= f5;
            }
            float thunder = world.getThunderStrength(partialTicks);
            if (thunder > 0.0F) {
                float f6 = 1.0F - thunder * 0.48F * cel;
                float f7 = 1.0F - thunder * 0.42F * cel;
                float f8 = 1.0F - thunder * 0.39F * cel;
                r *= f6;
                g *= f7;
                b *= f8;
            }
            r = r * this.roof + 0.5F * (1.0F - this.roof);
            g = g * this.roof + 0.6F * (1.0F - this.roof);
            b = b * this.roof + 0.7F * (1.0F - this.roof);
            Entity entity = this.mc.getCameraEntity();
            if (entity instanceof LivingEntity && ((LivingEntity)entity).isPotionActive(MobEffects.NIGHT_VISION)) {
                float f8 = this.getNightVisionBrightness((LivingEntity)entity, partialTicks);
                float f9 = 1.0F / r;
                if (f9 > 1.0F / g)
                    f9 = 1.0F / g;
                if (f9 > 1.0F / b)
                    f9 = 1.0F / b;
                r = r * (1.0F - f8) + r * f9 * f8;
                g = g * (1.0F - f8 * 0.95F) + g * f9 * f8 * 0.95F;
                b = b * (1.0F - f8) + b * f9 * f8;
            }
            return new Vec3(r, g, b);
        }
     */

        @OnlyIn(Dist.CLIENT)
        private Vec3 getFogDownColor(Level world, float partialTicks, float cel) {
            float r = 0.63F;
            float g = 0.70F;
            float b = 0.67F;
            r = r * (cel * 0.75F + 0.25F);
            g = g * (cel * 0.70F + 0.30F);
            b = b * (cel * 0.65F + 0.35F);
            float rain = world.getRainLevel(partialTicks);
            if (rain > 0.0F) {
                float f4 = 1.0F - rain * 0.40F * cel;
                float f5 = 1.0F - rain * 0.35F * cel;
                r *= f4;
                g *= f5;
                b *= f5;
            }
            float thunder = world.getThunderLevel(partialTicks);
            if (thunder > 0.0F) {
                float f6 = 1.0F - thunder * 0.50F * cel;
                float f7 = 1.0F - thunder * 0.45F * cel;
                float f8 = 1.0F - thunder * 0.40F * cel;
                r *= f6;
                g *= f7;
                b *= f8;
            }
//            if (world.getLastLightningBolt() > 0) {
//                float f10 = world.getLastLightningBolt() - partialTicks;
//                if (f10 > 1.0F)
//                    f10 = 1.0F;
//                f10 = f10 * 0.25F;
//                r = r * (1.0F - f10) + 0.8F * f10;
//                g = g * (1.0F - f10) + 0.8F * f10;
//                b = b * (1.0F - f10) + 1.0F * f10;
//            }
            r = r * this.roof + 0.1F * (1.0F - this.roof);
            g = g * this.roof + 0.12F * (1.0F - this.roof);
            b = b * this.roof + 0.15F * (1.0F - this.roof);
            Entity entity = mc.getCameraEntity();
            if (entity instanceof LivingEntity le && le.hasEffect(MobEffects.NIGHT_VISION)) {
                float f8 = this.getNightVisionBrightness(le, partialTicks);
                float f9 = 1.0F / r;
                if (f9 > 1.0F / g)
                    f9 = 1.0F / g;
                if (f9 > 1.0F / b)
                    f9 = 1.0F / b;
                r = r * (1.0F - f8 * (0.85F + 0.1F * this.roof)) + r * f9 * f8 * (0.85F + 0.1F * this.roof);
                g = g * (1.0F - f8 * 0.85F) + g * f9 * f8 * 0.85F;
                b = b * (1.0F - f8 * (1.0F - 0.1F * this.roof)) + b * f9 * f8 * (1.0F - 0.1F * this.roof);
            }
            return new Vec3(r, g, b);
        }

    @OnlyIn(Dist.CLIENT)
    private Vec3 getFogLayerColor(Level world, float partialTicks, float cel) {
        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        r = r * (cel * 0.85F + 0.15F);
        g = g * (cel * 0.82F + 0.18F);
        b = b * (cel * 0.80F + 0.20F);
        float rain = world.getRainLevel(partialTicks);
        if (rain > 0.0F) {
            float f4 = 1.0F - rain * 0.4F * cel;
            float f5 = 1.0F - rain * 0.38F * cel;
            float f6 = 1.0F - rain * 0.35F * cel;
            r *= f4;
            g *= f5;
            b *= f6;
        }
        float thunder = world.getThunderLevel(partialTicks);
        if (thunder > 0.0F) {
            float f7 = 1.0F - thunder * 0.55F * cel;
            float f8 = 1.0F - thunder * 0.50F * cel;
            float f9 = 1.0F - thunder * 0.45F * cel;
            r *= f7;
            g *= f8;
            b *= f9;
        }
        r *= 0.75F + 0.25F * this.roof;
        g *= 0.8F + 0.2F * this.roof;
        b *= 0.85F + 0.15F * this.roof;
        Entity entity = mc.getCameraEntity();
        if (entity instanceof LivingEntity le && le.hasEffect(MobEffects.NIGHT_VISION)) {
            float f8 = this.getNightVisionBrightness(le, partialTicks);
            float f9 = 1.0F / r;
            if (f9 > 1.0F / g)
                f9 = 1.0F / g;
            if (f9 > 1.0F / b)
                f9 = 1.0F / b;
            r = r * (1.0F - f8) + r * f9 * f8;
            g = g * (1.0F - f8) + g * f9 * f8;
            b = b * (1.0F - f8) + b * f9 * f8;
        }
        return new Vec3(r, g, b);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void renderEvent(RenderLevelStageEvent e) {
        Level world = mc.player.level();
        //if (e.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL)
        {
            fogRenderOld(e.getPartialTick(), (ClientLevel) world, mc);
        }
    }

    private static int planeCount = (int) Math.pow(2, 2);
    private static float layerRange = 4.0F / planeCount;
    private static float colorRange = 0.65F / planeCount;
    private static float alpha = Math.min(1.0F, 5.0F / planeCount);

    public static void updateFogQuality() {
        planeCount = (int) Math.pow(2, 2);
        layerRange = 4.0F / planeCount;
        colorRange = 0.65F / planeCount;
        alpha = Math.min(1.0F, 5.0F / planeCount);
    }

    private void fogRenderOld(float partialTicks, ClientLevel world, Minecraft mc) {
        Entity entity = mc.getCameraEntity();
        float cameraHeight = mc.getCameraEntity().getEyeHeight();
        float playerHeight = (float)(entity.yOld + (entity.position().y - entity.yOld) * partialTicks);
        byte segmentCount = 4; //Segment count
        int renderDistance = (mc.options.renderDistance().get() + 2) * 16;
        int segmentSize = renderDistance / segmentCount; //Segment size
        int doubleRenderDistance = renderDistance * 2;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuilder();
        RenderSystem.setShaderColor(0.819608F, 0.886275F, 0.498039F, 1.0F);
        //RenderSystem.setShaderTexture(0, locationFogPng);
        RenderSystem.enableBlend();
        // RenderSystem.enableFog();
        // RenderSystem.blendFunc(GL11.GL_GREATER, 0);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        float _red;
        float _green;
        float _blue;
        if (depth < 4) {
            Vec3 vecLayer = getFogLayerColor(world, partialTicks);
            _red = (float)vecLayer.x;
            _green = (float)vecLayer.y;
            _blue = (float)vecLayer.z;
        } else {
            Vec3 vecDown = getFogDownColor(world, partialTicks);
            _red = (float)vecDown.x;
            _green = (float)vecDown.y;
            _blue = (float)vecDown.z;
        }
        float colorOffset;
        float layerOffset = -0.0035F;
        float height; float red; float green; float blue; float alphaFin;
        for (int n = 0; n <= planeCount; n++) {
            height = fogHeight - 4 - playerHeight + layerOffset;
            colorOffset = 0.65F - n * colorRange;
            if (height - cameraHeight < -0.1) {
                vertexbuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                red = Math.min(1, _red + colorOffset);
                green = Math.min(1, _green + colorOffset);
                blue = Math.min(1, _blue + colorOffset);
                alphaFin = alpha;
                for (int x = -segmentSize * segmentCount; x < segmentSize * segmentCount; x += segmentSize) {
                    for (int z = -segmentSize * segmentCount; z < segmentSize * segmentCount; z += segmentSize) {
                        vertexbuffer.vertex(x, height, z).uv((float)(renderDistance + z) / doubleRenderDistance, (float)(renderDistance - x) / doubleRenderDistance).color(red, green, blue, alphaFin).endVertex();
                        vertexbuffer.vertex(x, height, z + segmentSize).uv((float)(renderDistance + z + segmentSize) / doubleRenderDistance, (float)(renderDistance - x) / doubleRenderDistance).color(red, green, blue, alphaFin).endVertex();
                        vertexbuffer.vertex(x + segmentSize, height, z + segmentSize).uv((float)(renderDistance + z + segmentSize) / doubleRenderDistance, (float)(renderDistance - x - segmentSize) / doubleRenderDistance).color(red, green, blue, alphaFin).endVertex();
                        vertexbuffer.vertex(x + segmentSize, height, z).uv((float)(renderDistance + z) / doubleRenderDistance, (float)(renderDistance - x - segmentSize) / doubleRenderDistance).color(red, green, blue, alphaFin).endVertex();
                    }
                }
                tessellator.end();
            }
            height = fogHeight - 4 - playerHeight - layerOffset;
            if (height - cameraHeight > 0.1) {
                vertexbuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                red = Math.min(1, _red + colorOffset);
                green = Math.min(1, _green + colorOffset);
                blue = Math.min(1, _blue + colorOffset);
                alphaFin = Math.min(1.0F, alpha + Math.max(0, (height) / 100));
                for (int x = -segmentSize * segmentCount; x < segmentSize * segmentCount; x += segmentSize) {
                    for (int z = -segmentSize * segmentCount; z < segmentSize * segmentCount; z += segmentSize) {
                        vertexbuffer.vertex(x, height, z).uv((float)(renderDistance + z) / doubleRenderDistance, (float)(renderDistance - x) / doubleRenderDistance).color(red, green, blue, alphaFin).endVertex();
                        vertexbuffer.vertex(x + segmentSize, height, z).uv((float)(renderDistance + z) / doubleRenderDistance, (float)(renderDistance - x - segmentSize) / doubleRenderDistance).color(red, green, blue, alphaFin).endVertex();
                        vertexbuffer.vertex(x + segmentSize, height, z + segmentSize).uv((float)(renderDistance + z + segmentSize) / doubleRenderDistance, (float)(renderDistance - x - segmentSize) / doubleRenderDistance).color(red, green, blue, alphaFin).endVertex();
                        vertexbuffer.vertex(x, height, z + segmentSize).uv((float)(renderDistance + z + segmentSize) / doubleRenderDistance, (float)(renderDistance - x) / doubleRenderDistance).color(red, green, blue, alphaFin).endVertex();
                    }
                }
                tessellator.end();
            }
            layerOffset += layerRange;
        }
        // RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        // RenderSystem.blendFunc(GL11.GL_GREATER, 0.1F);
        RenderSystem.disableBlend();
    }

    private static int viewX, viewY, viewZ;
    private static boolean shadowInit;
    private static float shadowMultiplier;

    private float getRoof(Level world, BlockPos pos) {
        if (pos.getX() == viewX && pos.getY() == viewY && pos.getZ() == viewZ && shadowInit)
            return shadowMultiplier;
        if (world.isLoaded(pos)) shadowInit = true;

        float i = 0.0F;
        int count = 1;
        int r = 6;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos pos1 = pos.offset(x, 0, z);
                if (!world.getBlockState(pos1).isRedstoneConductor(world, pos1)) {
                    count += 1;
                    i += world.getLightEmission(pos1);
					/*if (world.canBlockSeeSky(pos.add(x, 0, z))) {
						i += 1;
					}*/
                }
            }
        }

        viewX = pos.getX();
        viewY = pos.getY();
        viewZ = pos.getZ();
        shadowMultiplier = (i / count) / 15;
        return shadowMultiplier;
    }

    private float calculate(float up, float down, float min, float max, float depth, float layerHeight) {
        return Mth.clamp(up + (down - up) * depth / layerHeight, min, max);
    }

    private float getNightVisionBrightness(LivingEntity entitylivingbaseIn, float partialTicks) {
        int i = entitylivingbaseIn.getEffect(MobEffects.NIGHT_VISION).getDuration();
        return i > 200 ? 1.0F : 0.7F + Mth.sin((i - partialTicks) * (float)Math.PI * 0.2F) * 0.3F;
    }
/*
    @OnlyIn(Dist.CLIENT)
    private Vec3 getFogUpColor(Level world, float partialTicks) {
        return getFogUpColor(world, partialTicks, cel);
    }

    @OnlyIn(Dist.CLIENT)
    private Vec3 getFogBorderColor(Level world, float partialTicks) {
        return getFogBorderColor(world, partialTicks, cel);
    }

 */

    @OnlyIn(Dist.CLIENT)
    private Vec3 getFogDownColor(Level world, float partialTicks) {
        return getFogDownColor(world, partialTicks, cel);
    }

    @OnlyIn(Dist.CLIENT)
    private Vec3 getFogLayerColor(Level world, float partialTicks) {
        return getFogLayerColor(world, partialTicks, cel);
    }
}