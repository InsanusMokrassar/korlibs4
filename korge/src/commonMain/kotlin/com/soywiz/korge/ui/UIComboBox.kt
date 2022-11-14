package com.soywiz.korge.ui

import com.soywiz.kds.Extra
import com.soywiz.korge.input.onClick
import com.soywiz.korge.input.onDown
import com.soywiz.korge.input.onOut
import com.soywiz.korge.input.onOver
import com.soywiz.korge.input.onUp
import com.soywiz.korge.render.RenderContext
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korio.async.Signal
import com.soywiz.korma.geom.*
import com.soywiz.korui.layout.HorizontalUiLayout.pt
import kotlin.native.concurrent.ThreadLocal

inline fun <T> Container.uiComboBox(
    width: Double = UI_DEFAULT_WIDTH,
    height: Double = UI_DEFAULT_HEIGHT,
    selectedIndex: Int = 0,
    items: List<T>,
    block: @ViewDslMarker UIComboBox<T>.() -> Unit = {}
) = UIComboBox(width, height, selectedIndex, items).addTo(this).apply(block)

@ThreadLocal
var Views.openedComboBox by Extra.Property<UIComboBox<*>?>() { null }

open class UIComboBox<T>(
    width: Double = UI_DEFAULT_WIDTH,
    height: Double = UI_DEFAULT_HEIGHT,
    selectedIndex: Int = 0,
    items: List<T> = listOf(),
) : UIView(width, height) {
    val onSelectionUpdate = Signal<UIComboBox<T>>()

    var selectedIndex by uiObservable(selectedIndex) {
        updateState()
        onSelectionUpdate(this)
    }
    var selectedItem: T?
        get() = items.getOrNull(selectedIndex)
        set(value) {
            selectedIndex = items.indexOf(value)
        }
    var items: List<T> by uiObservable(items) { updateItems() }
    //var itemHeight by uiObservable(height) { updateItemsSize() }
    val itemHeight get() = height
    var viewportHeight by uiObservable(196) { onSizeChanged() }

    private val selectedButton = uiButton(width - height, height, "")
    private val expandButton = uiButton(height, height, icon = comboBoxExpandIcon).position(width - height, 0.0)
    private val invisibleRect = solidRect(width, height, Colors.TRANSPARENT_BLACK)

    private val itemsView = uiScrollable(width, height = 128.0)
    private val itemsViewBackground = itemsView.fastMaterialBackground(width, height = 128.0) {
        radius = RectCorners(0.0, 0.0, 12.0, 12.0)
        zIndex = -1000.0
    }
    private val verticalList = itemsView.container.uiVerticalList(object : UIVerticalList.Provider {
        override val numItems: Int = items.size
        override val fixedHeight: Double = itemHeight
        override fun getItemHeight(index: Int): Double = fixedHeight
        override fun getItemView(index: Int, vlist: UIVerticalList): View {
            val it = UIButton(text = items[index].toString(), width = width, height = itemHeight).apply {
                this.radius = 0.pt
                this.textColor = Colors.BLACK
                this.bgColorOut = Colors["#fff"]
                this.bgColorOver = Colors["#ddd"]
                this.elevation = false
            }
            it.onClick {
                this@UIComboBox.selectedIndex = index
                this@UIComboBox.close()
            }
            return it
        }
    }, width = width)
    private var showItems = false

    init {
        updateItems()
        invisibleRect.onOver {
            selectedButton.simulateOver()
            expandButton.simulateOver()
        }
        invisibleRect.onOut {
            selectedButton.simulateOut()
            expandButton.simulateOut()
        }
        invisibleRect.onDown {
            selectedButton.simulateDown()
            expandButton.simulateDown()
        }
        invisibleRect.onUp {
            selectedButton.simulateUp()
            expandButton.simulateUp()
        }
        invisibleRect.onClick {
            showItems = !showItems
            onSizeChanged()
        }
        onSizeChanged()
    }

    fun open() {
        val views = stage?.views
        if (views != null) {
            if (views.openedComboBox != this) {
                views.openedComboBox?.close()
            }
            views?.openedComboBox = this
        }

        containerRoot.addChild(itemsView)

        //itemsView.size(width, viewportHeight.toDouble()).position(0.0, height)
        itemsView
            .size(width, viewportHeight.toDouble())
            .setGlobalXY(localToGlobal(Point(0.0, height + 8.0)))
        itemsViewBackground
            .xy(0, -8)
            .size(width, itemsView.height + 16)
        verticalList
            .size(width, verticalList.height)

        verticalList.invalidateList()

        showItems = true
        updateProps()
    }

    fun close() {
        val views = stage?.views
        if (views != null) {
            if (views.openedComboBox == this) {
                views.openedComboBox = null
            }
        }

        itemsView.removeFromParent()
        showItems = false
        updateProps()
    }

    private fun updateItemsSize() {
        itemsView.container.forEachChildWithIndex { index, child ->
            child.scaledHeight = itemHeight
            child.position(0.0, index * itemHeight)
        }
    }

    var itemsDirty = false

    override fun renderInternal(ctx: RenderContext) {
        if (itemsDirty && showItems) {
            itemsDirty = false
            verticalList.updateList()
            updateState()
        }
        super.renderInternal(ctx)
    }

    private fun updateItems() {
        itemsDirty = true
    }

    override fun updateState() {
        super.updateState()
        onSizeChanged()
        for (i in items.indices) {
            val button = itemsView.container.getChildAtOrNull(i) as? UIButton ?: continue
            button.forcePressed = selectedIndex == i
        }
    }

    override fun onSizeChanged() {
        super.onSizeChanged()
        if (showItems) {
            open()
        } else {
            close()
        }
    }

    private fun updateProps() {
        selectedButton.simulatePressing(showItems)
        expandButton.simulatePressing(showItems)
        expandButton.icon = if (showItems) comboBoxShrinkIcon else comboBoxExpandIcon
        invisibleRect.size(width, height)
        selectedButton.size(width - height, height)
        selectedButton.text = selectedItem?.toString() ?: ""
        expandButton.position(width - height, 0.0).size(height, height)
    }
}
