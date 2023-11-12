import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.io.util.*
import korlibs.korge.*
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import kotlin.time.Duration.Companion.seconds

private const val WINDOW_WIDTH = 940
private const val WINDOW_HEIGHT = 600
private const val IMAGE_WIDTH_AND_HEIGHT = 100
private const val IMAGE_SPACING = 10
private const val IMAGES_PER_ROW = 8
private const val X_POSITION_START = 30
private const val Y_POSITION_START = 30
private val HIDE_IMAGES_AFTER_DURATION = 1.seconds
private const val RESTART_BUTTON_WIDTH = 75
private const val RESTART_BUTTON_HEIGHT = 25

private val IMAGE_FILE_NAMES = listOf(
    "arrow-cursor.png",
    "cancel.png",
    "click.png",
    "cloud-download.png",
    "compact-disc.png",
    "database.png",
    "gamepad-cross.png",
    "hamburger-menu.png",
    "house.png",
    "infinity.png",
    "info.png",
    "keyboard.png",
    "magnifying-glass.png",
    "mouse.png",
    "move.png",
    "power-button.png",
    "smartphone.png",
    "speaker.png",
    "trash-can.png",
    "wireframe-globe.png",
)

private fun Image.hide() {
    this.alpha = 0.0
}

private fun Image.reveal() {
    this.alpha = 1.0
}

suspend fun main() = Korge(
    title = "Memory",
    windowSize = Size(WINDOW_WIDTH, WINDOW_HEIGHT),
    backgroundColor = Colors["#1b1b1b"]
) {
    val sceneContainer = sceneContainer()
    sceneContainer.changeTo { MemoryScene() }
}

class MemoryScene : Scene() {
    val revealedIds = mutableListOf<UUID>()
    val openedIds = mutableListOf<UUID>()
    val openedImages = mutableListOf<Image>()

    fun isAlreadyRevealed(id: UUID): Boolean = revealedIds.contains(id)

    suspend fun hideOpenedImagesAfterDelay() {
        delay(HIDE_IMAGES_AFTER_DURATION)
        openedImages.forEach { it.hide() }
        openedImages.clear()
        openedIds.clear()
    }

    fun isSameImageOpened(): Boolean = openedIds[0] == openedIds[1]

    override suspend fun SContainer.sceneMain() {
        initGame()
    }

    private suspend fun Container.initGame() {
        val container = this

        uiButton {
            size = Size(RESTART_BUTTON_WIDTH, RESTART_BUTTON_HEIGHT)
            text = "Restart"
            onClick {
                container.removeChildren()
                container.initGame()
            }
        }

        revealedIds.clear()
        openedIds.clear()
        openedImages.clear()

        var x = X_POSITION_START
        var y = Y_POSITION_START

        IMAGE_FILE_NAMES.flatMap {
            val id = UUID.randomUUID()
            val bitmap = resourcesVfs["images/$it"].readBitmap()
            (1..2).map { _ ->
                image(bitmap) {
                    size(IMAGE_WIDTH_AND_HEIGHT, IMAGE_WIDTH_AND_HEIGHT)
                    position(-100, -100)
                    onClick { _ ->
                        if (!isAlreadyRevealed(id)) {

                            if (openedIds.size < 2) {
                                reveal()
                                openedIds.add(id)
                                openedImages.add(this)
                            }

                            if (openedIds.size == 2) {
                                if (isSameImageOpened()) {
                                    openedIds.clear()
                                    openedImages.clear()
                                    revealedIds.add(id)
                                } else {
                                    hideOpenedImagesAfterDelay()
                                }
                            }
                        }
                    }
                    hide()
                }
            }
        }
            .shuffled()
            .forEachIndexed { index, image ->

                // Draw Placeholder
                fastRoundRect(
                    size = Size(IMAGE_WIDTH_AND_HEIGHT, IMAGE_WIDTH_AND_HEIGHT),
                    color = Colors.BLACK
                ).position(x, y).zIndex = -1.0

                // Reposition the image
                image.position(x, y)

                // Calculate next x and y positions
                x += IMAGE_WIDTH_AND_HEIGHT + IMAGE_SPACING
                if ((index + 1) % IMAGES_PER_ROW == 0) {
                    x = X_POSITION_START
                    y += IMAGE_WIDTH_AND_HEIGHT + IMAGE_SPACING
                }
            }
    }
}
