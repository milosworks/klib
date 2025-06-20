package xyz.milosworks.klib.test.client.ui

import androidx.compose.runtime.Composable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import xyz.milosworks.klib.ui.base.ComposeContainerScreen
import xyz.milosworks.klib.ui.components.basic.Block
import xyz.milosworks.klib.ui.components.basic.Item
import xyz.milosworks.klib.ui.components.basic.Spacer
import xyz.milosworks.klib.ui.components.containers.Surface
import xyz.milosworks.klib.ui.layout.containers.Column
import xyz.milosworks.klib.ui.layout.containers.Row
import xyz.milosworks.klib.ui.modifiers.appearance.background
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.layout.size
import xyz.milosworks.klib.ui.modifiers.position.inset.inset
import xyz.milosworks.klib.ui.modifiers.position.outset.outset
import xyz.milosworks.klib.ui.utils.KColor

class UIScreen(menu: UIMenu, inventory: Inventory, title: Component) :
    ComposeContainerScreen<UIMenu>(menu, inventory, title) {

    init {
        start {
            content()
        }
    }

    @Composable
    fun content() {
        Surface(modifier = Modifier.inset(10)) {
            Row {
                Block(
                    Blocks.CHEST.defaultBlockState(),
                    Minecraft.getInstance().level!!.getBlockEntity(BlockPos(-95, 74, 212)),
                    modifier = Modifier.size(50)
                )
                Item(
                    Items.DIAMOND_AXE.defaultInstance,
                    modifier = Modifier.size(50)
                )
                Column(modifier = Modifier.outset(left = 10)) {
                    Spacer(Modifier.background(KColor.GREEN).size(20))
                    Spacer(Modifier.background(KColor.BLUE).size(20).outset(top = 10, bottom = 10))
                    Spacer(Modifier.background(KColor.RED).size(20))
                }
            }
        }
    }

    //                Spacer(
//                    Modifier
//                        .size(50)
//                        .onScroll { node, event ->
//                            println("Scrolled: X=${event.scrollX}, Y=${event.scrollY}")
//                            event.consume()
//                        }
//                        .onDrag { node, event ->
//                            println("Dragged: button=${event.button}, dragX=${event.dragX}, dragY=${event.dragY}")
//                            event.consume()
//                        }
//                )

//    @Composable
//    fun content2() {
//        var active: Boolean by remember { mutableStateOf(false) }
//        var checked: Boolean by remember { mutableStateOf(false) }
//        var text: String by remember { mutableStateOf("") }
//
//        Surface {
//            Column(
//                modifier = Modifier
//                    .onPointerEvent(PointerEventType.SCROLL) { _, event -> println("scroll column"); event.consume() }
//            ) {
//                Slot()
//                Text(
//                    Component.literal("Hello World!"),
//                    2f,
//                    modifier = Modifier
//                        .onPointerEvent(PointerEventType.ENTER) { _, event -> println("enter text"); event.consume() }
//                        .onPointerEvent(PointerEventType.EXIT) { _, event -> println("exit text"); event.consume() }
//                )
//                Theme(type = ThemeTypes.BEDROCK) {
//                    TextField(
//                        text,
////						enabled = false,
//                        onValueChange = { text = if (it.endsWith("1")) it.dropLast(1) else it },
//                        placeholder = "Hello World!",
//                        modifier = Modifier.size(90, 18)
//                    )
//                }
//                Spacer(
//                    Modifier.size(50, 50)
//                        .background(KColor.BLUE)
//                        .outline(KColor.RED)
//                        .combinedClickable(
//                            onLongClick = { _, _ -> println("onLongClick spacer"); false },
//                            onDoubleClick = { _, _ -> println("onDoubleClick spacer"); false },
//                            onClick = { _, _ -> println("onClick spacer"); false }
//                        )
//                )
//                Theme(type = ThemeTypes.BEDROCK) {
//                    Button(modifier = Modifier.size(20, 20)) {
//                        println("button clicked"); active = (active == false)
//                    }
//                }
//                Button(modifier = Modifier.size(20, 20)) {
//                    println("button clicked"); active = (active == false)
//                }
//                Row {
//                    Texture(
//                        ResourceLocation.parse("minecraft:textures/block/diamond_block.png"),
//                        0f,
//                        0f,
//                        64,
//                        64,
//                        64,
//                        64,
//                        Modifier.size(50)
//                            .onPointerEvent(PointerEventType.MOVE) { _, event -> println("moved texture"); event.consume() }
//                    )
//
//                    Checkbox(
//                        checked,
//                        onCheckedChange = { checked = it },
//                        modifier = Modifier.padding(start = 5)
//                    )
//                }
//
//                val textSize = getTextSize(Component.literal("Clicked Button!"))
//                if (active) Text(Component.literal("Clicked Button!"))
//                else Spacer(Modifier.size(textSize.first, textSize.second))
//            }
//        }
//    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {}
}