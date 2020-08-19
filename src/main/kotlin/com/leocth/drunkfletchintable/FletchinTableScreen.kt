package com.leocth.drunkfletchintable

import com.leocth.drunkfletchintable.widgets.CraftingScreenWidget
import com.leocth.drunkfletchintable.widgets.TippingScreenWidget
import com.mojang.blaze3d.systems.RenderSystem
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class FletchinTableScreen(
    handler: FletchinTableScreenHandler,
    playerInventory: PlayerInventory,
    title: Text?
) : HandledScreen<FletchinTableScreenHandler>
    (handler, playerInventory, title)
{
    private lateinit var craftingWidget: CraftingScreenWidget
    private lateinit var tippingWidget: TippingScreenWidget

    companion object {
        private val TEXTURE = Identifier(MODID, "textures/gui/fletchin_table.png")
    }

    override fun init() {
        super.init()
        backgroundWidth = 216
        backgroundHeight = 175
        titleX = 44
        titleY = 6
        playerInventoryTitleX = 44
        playerInventoryTitleY = 81
        craftingWidget = CraftingScreenWidget(client, x, y)
        tippingWidget = TippingScreenWidget(client, x, y, handler)
        addButton(
            TexturedButtonWidget(
                x + 4, y + 20, 28, 14,
                0, 182, 14, TEXTURE
            )
            {
                if (handler.getDelegatedMode() != FletchinTableMode.CRAFTING) updateMode(FletchinTableMode.CRAFTING)
            }
        )
        addButton(
            TexturedButtonWidget(
                x + 4, y + 35, 28, 14,
                28, 182, 14, TEXTURE
            )
            {
                if (handler.getDelegatedMode() != FletchinTableMode.TIPPING) updateMode(FletchinTableMode.TIPPING)
            }
        )
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        super.drawForeground(matrices, mouseX, mouseY)
        val text = handler.getDelegatedMode().displayText
        textRenderer.draw(matrices, text, 202f - textRenderer.getWidth(text), 20f, 4210752)

    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        client?.textureManager?.bindTexture(TEXTURE)
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)
        when (handler.getDelegatedMode()) {
            FletchinTableMode.CRAFTING -> craftingWidget.render(matrices, mouseX, mouseY, delta)
            FletchinTableMode.TIPPING -> tippingWidget.render(matrices, mouseX, mouseY, delta)
            else -> {}
        }
    }

    private fun updateMode(mode: FletchinTableMode) {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeVarInt(mode.ordinal)
        handler.setDelegatedMode(mode)
        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_C2S_FT_MODE, buf)
    }
}