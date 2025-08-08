package xyz.milosworks.klib.test.client.ui

import androidx.compose.runtime.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import xyz.milosworks.klib.ui.base.ComposeContainerScreen
import xyz.milosworks.klib.ui.base.UINode
import xyz.milosworks.klib.ui.composables.basic.*
import xyz.milosworks.klib.ui.composables.containers.Collapsible
import xyz.milosworks.klib.ui.composables.containers.Scrollable
import xyz.milosworks.klib.ui.composables.containers.Surface
import xyz.milosworks.klib.ui.composables.input.Button
import xyz.milosworks.klib.ui.composables.input.Checkbox
import xyz.milosworks.klib.ui.composables.input.ColorPicker
import xyz.milosworks.klib.ui.composables.input.textfield.BasicTextField
import xyz.milosworks.klib.ui.composables.input.textfield.TextField
import xyz.milosworks.klib.ui.composables.input.textfield.TextFieldValue
import xyz.milosworks.klib.ui.layout.containers.Box
import xyz.milosworks.klib.ui.layout.containers.Column
import xyz.milosworks.klib.ui.layout.containers.Row
import xyz.milosworks.klib.ui.layout.primitive.Arrangement
import xyz.milosworks.klib.ui.modifiers.appearance.background
import xyz.milosworks.klib.ui.modifiers.appearance.outline
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.input.*
import xyz.milosworks.klib.ui.modifiers.layout.size
import xyz.milosworks.klib.ui.modifiers.position.inset.inset
import xyz.milosworks.klib.ui.modifiers.position.outset.outset
import xyz.milosworks.klib.ui.utils.HsvColor
import xyz.milosworks.klib.ui.utils.KColor

enum class Tabs {
    COLOR_PICKER,
    SCROLLABLE,
    OUTSET_INSET,
    BLOCK_ITEM_TEXTURE,
    INPUT_EVENTS,
    TEXT_FIELD,
    COLLAPSIBLE
}

class UIScreen(menu: UIMenu, inventory: Inventory, title: Component) :
    ComposeContainerScreen<UIMenu>(menu, inventory, title) {

    init {
        start {
            content()
        }
    }

    @Composable
    fun content() {
        var tab by remember { mutableStateOf(Tabs.COLOR_PICKER) }

        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(2)) {
                Button(onClick = {
                    if (tab != Tabs.COLOR_PICKER) tab = Tabs.COLOR_PICKER
                }) { Text(Component.literal("Color Picker"), modifier = Modifier.outset(5)) }
                Button(onClick = {
                    tab = Tabs.SCROLLABLE
                }) { Text(Component.literal("Scrollable"), modifier = Modifier.outset(5)) }
                Button(onClick = {
                    tab = Tabs.OUTSET_INSET
                }) { Text(Component.literal("Outset Inset"), modifier = Modifier.outset(5)) }
                Button(onClick = {
                    tab = Tabs.BLOCK_ITEM_TEXTURE
                }) { Text(Component.literal("Block Item Texture"), modifier = Modifier.outset(5)) }
                Button(onClick = {
                    tab = Tabs.INPUT_EVENTS
                }) { Text(Component.literal("Input Events"), modifier = Modifier.outset(5)) }
                Button(onClick = {
                    tab = Tabs.TEXT_FIELD
                }) { Text(Component.literal("Text Field"), modifier = Modifier.outset(5)) }
                Button(onClick = {
                    tab = Tabs.COLLAPSIBLE
                }) { Text(Component.literal("Collapsible"), modifier = Modifier.outset(5)) }
            }

            Surface(modifier = Modifier.inset(10)) {
                when (tab) {
                    Tabs.COLOR_PICKER -> colorPicker()
                    Tabs.SCROLLABLE -> scrollable()
                    Tabs.OUTSET_INSET -> outsetInset()
                    Tabs.BLOCK_ITEM_TEXTURE -> blockItemTexture()
                    Tabs.INPUT_EVENTS -> inputEvents()
                    Tabs.TEXT_FIELD -> textField()
                    Tabs.COLLAPSIBLE -> collapsible()
                }
            }
        }
    }

    @Composable
    fun scrollable() {
        Box(modifier = Modifier.size(100, 150)) {
            Scrollable {
                Column {
                    repeat(20) {
                        Text(
                            Component.literal("Item Number $it"),
                            modifier = Modifier.outset(top = 2)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun colorPicker() {
        var colorState by remember { mutableStateOf(HsvColor.from(KColor.RED)) }

        Column {
            ColorPicker(
                colorState,
                modifier = Modifier.size(150, 100)
            ) { newColor ->
                colorState = newColor
            }

            Spacer(
                modifier = Modifier
                    .size(100, 20)
                    .outset(top = 10)
                    .background(colorState.toKColor())
                    .outline(KColor.BLACK)
            )

            Button(
                modifier = Modifier.outset(top = 10),
                onClick = {
                    colorState = HsvColor.from(KColor.BLUE)
                },
            ) {
                Text(Component.literal("Set to Blue"), modifier = Modifier.outset(5))
            }
        }
    }

    @Composable
    fun outsetInset() {
        Column(modifier = Modifier.outset(left = 10)) {
            Spacer(Modifier.background(KColor.GREEN).size(20))
            Spacer(Modifier.background(KColor.BLUE).size(20).outset(top = 10, bottom = 10))
            Spacer(Modifier.background(KColor.RED).size(20))
        }
    }

    @Composable
    fun blockItemTexture() {
        Row(horizontalArrangement = Arrangement.spacedBy(10)) {
            Block(
                Blocks.CHEST.defaultBlockState(),
                Minecraft.getInstance().level!!.getBlockEntity(BlockPos(-95, 74, 212)),
                modifier = Modifier.size(50)
            )
            Item(
                Items.DIAMOND_AXE.defaultInstance,
                modifier = Modifier.size(50)
            )
            Texture(
                ResourceLocation.parse("minecraft:textures/block/diamond_block.png"),
                0f,
                0f,
                64,
                64,
                64,
                64,
                Modifier.size(50)
//                    .onPointerEvent<UINode>(PointerEventType.MOVE) { _, event -> println("moved texture"); event.consume() }
            )
        }
    }

    @Composable
    fun inputEvents() {
        var checked: Boolean by remember { mutableStateOf(false) }
        var active: Boolean by remember { mutableStateOf(false) }
        var text: String by remember { mutableStateOf("") }
        var mousePosition by remember { mutableStateOf(Pair(0.0, 0.0)) }
        var scrollDelta by remember { mutableStateOf(Pair(0.0, 0.0)) }
        var dragDelta by remember { mutableStateOf(Pair(0.0, 0.0)) }
        var mouseInside by remember { mutableStateOf(false) }

        Column(verticalArrangement = Arrangement.spacedBy(5)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2),
            ) {
                Text(Component.literal(text))

//                TextField(
//                    text,
//                    onValueChange = { text = if (it.endsWith("1")) it.dropLast(1) else it },
//                    placeholder = "Hello World!",
//                    modifier = Modifier.size(90, 18).outset(bottom = 10)
//                )

                Spacer(
                    Modifier.size(50, 50)
                        .background(KColor.BLUE)
                        .outline(KColor.RED)
                        .combinedClickable<UINode>(
                            onLongClick = { _, _ -> println("Long Click"); false },
                            onDoubleClick = { _, _ -> println("Double Click"); false },
                            onClick = { _, _ -> println("Click"); false }
                        )
                )

                Checkbox(
                    checked,
                    onCheckedChange = { checked = it },
                )

                Button(
                    modifier = Modifier.size(20, 20),
                    onClick = {
                        println("Button Clicked")
                        active = !active
                    }
                )

                val textSize = getTextSize(Component.literal("Clicked Button!"))
                if (active) Text(Component.literal("Clicked Button!"))
                else Spacer(Modifier.size(textSize.width, textSize.height))
            }

            Box(
                modifier = Modifier.background(KColor.MAGENTA)
                    .onPointerEvent<UINode>(PointerEventType.MOVE) { _, event ->
                        mousePosition = Pair(event.mouseX, event.mouseY)
                        println("MOVE: MouseX: ${mousePosition.first}, MouseY: ${mousePosition.second}")
                    }
                    .onScroll<UINode> { _, event ->
                        scrollDelta = Pair(event.scrollX, event.scrollY)
                        println("SCROLL: ScrollX: ${scrollDelta.first}, ScrollY: ${scrollDelta.second}")
                    }
                    .onDrag<UINode> { _, event ->
                        dragDelta = Pair(event.dragX, event.dragY)
                        println("DRAG: DragX: ${dragDelta.first}, DragY: ${dragDelta.second}")
                    }
                    .onPointerEvent<UINode>(PointerEventType.ENTER) { _, event ->
                        mouseInside = true
                        println("Mouse Entered")
                    }
                    .onPointerEvent<UINode>(PointerEventType.EXIT) { _, event ->
                        mouseInside = false
                        println("Mouse Exited")
                    }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2)) {
                    Text(
                        Component.literal(
                            "Mouse Position: X=${"%.2f".format(mousePosition.first)}, Y=${
                                "%.2f".format(
                                    mousePosition.second
                                )
                            }"
                        )
                    )
                    Text(
                        Component.literal(
                            "Scroll Delta: scrollX=${"%.2f".format(scrollDelta.first)}, scrollY=${
                                "%.2f".format(
                                    scrollDelta.second
                                )
                            }"
                        )
                    )
                    Text(
                        Component.literal(
                            "Drag Delta: dragX=${"%.2f".format(dragDelta.first)}, dragY=${
                                "%.2f".format(
                                    dragDelta.second
                                )
                            }"
                        )
                    )
                    Text(
                        Component.literal("Mouse Inside Box: $mouseInside")
                    )
                }
            }
        }
    }

    @Composable
    fun textField() {
        var text by remember { mutableStateOf("hello") }

        Column {
            Text(Component.literal(text), modifier = Modifier.outset(bottom = 5))

//            TextField(
//                text,
//                onValueChange = { text = it },
//                singleLine = false,
//                modifier = Modifier.size(90, 18),
//                maxLines = 2,
//                maxLength = 32
//            )
//            TextField(text, onValueChange = { text = it }, modifier = Modifier.size(90, 18))
            BasicTextField(
                text,
                onValueChange = { text = it },
                modifier = Modifier.size(100, 30),
                singleLine = false,
                maxLines = 2,
                maxLength = 10
            )
        }
    }

    @Composable
    fun collapsible() {
        var text by remember { mutableStateOf(TextFieldValue("hello")) }
        var active by remember { mutableStateOf(false) }

        Collapsible(Component.literal("Collapsible")) {
            Column {
                if (active) {
                    Text(Component.literal("Active"))
                }

                Button(onClick = { active = !active }) {
                    Text(
                        Component.literal("Toggle"),
                        modifier = Modifier.outset(5)
                    )
                }
                TextField(text, onValueChange = { text = it }, modifier = Modifier.size(150, 18))
            }
        }
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {}
}