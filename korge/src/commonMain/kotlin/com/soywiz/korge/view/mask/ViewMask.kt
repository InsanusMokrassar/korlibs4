package com.soywiz.korge.view.mask

import com.soywiz.kds.extraProperty
import com.soywiz.korag.*
import com.soywiz.korag.annotation.KoragExperimental
import com.soywiz.korag.shader.Program
import com.soywiz.korge.render.RenderContext
import com.soywiz.korge.render.Texture
import com.soywiz.korge.view.View
import com.soywiz.korge.view.ViewRenderPhase
import com.soywiz.korim.color.Colors
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
private var View.__mask: View? by extraProperty { null }

fun <T : View> T.mask(mask: View?): T {
    this.mask = mask
    return this
}

var View.mask: View?
    get() = __mask
    set(value) {
        removeRenderPhaseOfType<ViewRenderPhaseMask>()
        if (value != null) {
            addRenderPhase(ViewRenderPhaseMask(value))
        }
        __mask = value
    }

@OptIn(KoragExperimental::class)
class ViewRenderPhaseMask(var mask: View) : ViewRenderPhase {
    companion object {
        const val PRIORITY = -100
    }

    override val priority: Int get() = PRIORITY
    override fun render(view: View, ctx: RenderContext) {
        ctx.useBatcher { batcher ->
            val maskBounds = mask.getLocalBoundsOptimized()
            val boundsWidth = maskBounds.width.toInt()
            val boundsHeight = maskBounds.height.toInt()
            ctx.tempAllocateFrameBuffers2(boundsWidth, boundsHeight) { maskFB, viewFB ->
                batcher.setViewMatrixTemp(mask.globalMatrixInv) {
                    ctx.renderToFrameBuffer(maskFB) {
                        ctx.clear(color = Colors.TRANSPARENT)
                        val oldVisible = mask.visible
                        try {
                            mask.visible = true
                            mask.renderFirstPhase(ctx)
                        } finally {
                            mask.visible = oldVisible
                        }
                    }
                    ctx.renderToFrameBuffer(viewFB) {
                        ctx.clear(color = Colors.TRANSPARENT)
                        view.renderNextPhase(ctx)
                    }
                }
                //batcher.drawQuad(Texture(maskFB), 100f, 200f, m = view.parent!!.globalMatrix)
                //batcher.drawQuad(Texture(viewFB), 300f, 200f, m = view.parent!!.globalMatrix)
                batcher.flush {
                //batcher.keepTextureUnit(DefaultShaders.u_TexEx.index, flush = true) {
                    //batcher.keepTextureUnit(DefaultShaders.u_Tex.index, flush = true) {
                        //batcher.keepTextureUnit(DefaultShaders.u_Tex.index, flush = true) {
                        ctx.textureUnits.set(DefaultShaders.u_Tex, viewFB.tex)
                        ctx.textureUnits.set(DefaultShaders.u_TexEx, maskFB.tex)
                        //ctx[DefaultShaders.TexExUB].push {
                        //    it.set(u_TexEx, maskFB.tex)
                        //}
                        batcher.drawQuad(
                            Texture(viewFB), m = mask.globalMatrix, program = DefaultShaders.MERGE_ALPHA_PROGRAM,
                        )
                        //}
                //    }
                }
            }
        }
        view.renderNextPhase(ctx)
    }
}
