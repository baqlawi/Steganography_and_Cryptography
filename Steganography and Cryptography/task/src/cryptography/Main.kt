package cryptography

import java.awt.Color
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.system.exitProcess

fun main() {
    while (true) {
        captureUserInput()
    }
}

fun captureUserInput() {
    println("Task (hide, show, exit):")
    val userInput = readln()
    when (UserInput.byValue(userInput)) {
        UserInput.HIDE -> hide()
        UserInput.SHOW -> show()
        UserInput.EXIT -> exit()
        else -> invalidUserInput(userInput)
    }
}

fun hide(){
//    println("Hiding message in image.")
    println("Input image file:")
    val inputFileName = readln()
    println("Output image file:")
    val outputFileName = readln()
    try {
        val inputFile = File(inputFileName)
        val image = ImageIO.read(inputFile)

//        println("Input Image: $inputFileName")
//        println("Output Image: $outputFileName")
        println("Message to hide:")
        val msg = readln()

        println("Password:")
        val password = readln()

        val encodedMsg = encodeAsIntList(msg, password)
//        val encodedPassword = encodePasswordAsIntList(password)

        var counter = 0
        if (encodedMsg.size > image.width * image.height) {
            println("The input image is not large enough to hold this message.")
        } else {
            for (y in 0 until image.height) {
                for (x in 0 until image.width) {
                    Color(image.getRGB(x, y)).run {
                        val lsBit =
                            if (counter < encodedMsg.size) encodedMsg[counter++] else null
                        val updatedColor = getUpdatedColor(this, lsBit)
                        image.setRGB(x, y, updatedColor.rgb)
                    }
                }
            }

            val outputFile = File(outputFileName)
            ImageIO.write(image, "png", outputFile)
            println("Message saved in ${outputFileName.split("/").last()} image.")
        }
    } catch (e: IOException) {
        println("Can't read input file!")
    }
}

fun encodePassword(password: String) =
    password
        .encodeToByteArray()
        .toMutableList()
        .joinToString("") {
            Integer.toBinaryString(it.toInt())
                .padStart(8, '0')
                .takeLast(8)
        }

fun encodePasswordAsIntList(password: String) =
    encodePassword(password).map { it.digitToInt() }

//val terminator = listOf<Byte>(0, 0, 3)
val terminator = listOf(0, 0, 3)
/*fun encode(message: String) =
    message
        .encodeToByteArray()
        .toMutableList()
        .apply { addAll(terminator) }
        .joinToString("") {
            Integer.toBinaryString(it.toInt())
                .padStart(8, '0')
                .takeLast(8)
        }*/
fun termination() =
    terminator.joinToString("") {
            Integer.toBinaryString(it)
                .padStart(8, '0')
                .takeLast(8)
        }
fun encodeWithPassword(message: String, password: String): String {
    val pass = encodePasswordAsIntList(password)
    val msg = message
            .encodeToByteArray()
            .joinToString("") {
                Integer.toBinaryString(it.toInt())
                    .padStart(8, '0')
                    .takeLast(8)
            }.mapIndexed { index, c -> c.digitToInt() xor pass[index % pass.size] }
        .toMutableList()
        .apply { addAll(termination().map { it.digitToInt() }) }
        .joinToString("")
    return msg
}

fun encodeAsIntList(message: String, password: String) =
//    encode(message).map { it.digitToInt() }
    encodeWithPassword(message, password).map { it.digitToInt() }

/*fun decode(encodedMessage: List<Int>) =
    encodedMessage
        .joinToString("")
        .split(
            encode("")
        )
        .first()
        .chunked(8)
        .joinToString("") { it.toInt(2).toChar().toString() }*/
fun decodeWithPassword(encodedMessage: List<Int>, password: String):String {
    val pass = encodePasswordAsIntList(password)
    val msg = encodedMessage
        .joinToString("")
        .split(
            termination()
        )
        .first()
        .mapIndexed { index, c ->  c.digitToInt() xor pass[index % pass.size] }
        .chunked(8)
        .joinToString("") { it.joinToString("").toInt(2).toChar().toString() }
    return msg
}

fun getUpdatedColor(color: Color, msgBit: Int?) =
    if (msgBit != null) {
        color.run {
            val nBlue = /*if (blue % 2 != msgBit) if (msgBit == 0) blue xor 1 else blue or 1 else blue*/ blue shr 1 shl 1 or msgBit
            Color(red, green, nBlue)
        }
    } else color

fun show(){
//    println("Obtaining message from image.")
    println("Input image file:")
    val inputFileName = readln()
    println("Password:")
    val password = readln()
//    val encodedPassword = encodePasswordAsIntList(password)

    try {
        val inputFile = File(inputFileName)
        val image = ImageIO.read(inputFile)

        val encodedMessage = mutableListOf<Int>()
//        val termination = termination()
//        val terminationSize = termination.length
        decodeMSG@ for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val color = Color(image.getRGB(x, y))
                val byte = color.blue.toString(2).last().digitToInt()

                encodedMessage += byte
                /*if (encodedMessage.size >= terminationSize
                    && encodedMessage.takeLast(terminationSize) == termination){
                    break@decodeMSG
                }*/
            }
        }

        println("Message:")
        val message = decodeWithPassword(encodedMessage, password)
            /*encodedMessage
                .joinToString("")
                .split(
                    terminator.joinToString("") { it.toString(2).padStart(8, '0')}
                )
                .first()
                .chunked(8)
                .joinToString("") { it.toInt(2).toChar().toString() }*/
        println("$message")

    } catch (e: IOException) {
        println("Can't read input file!")
    }

}

fun invalidUserInput(input: String){
    println("Wrong task: $input")
}

fun exit(){
    println("Bye!")
    exitProcess(0)
}

enum class UserInput(val input: String) {
    HIDE("hide"), SHOW("show"), EXIT("exit");

    companion object {
        fun byValue(value: String) =
            values().firstOrNull { it.input == value }
    }
}
