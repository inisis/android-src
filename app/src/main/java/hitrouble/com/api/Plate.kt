package hitrouble.com.api

class Plate : CharSequence {
    override val length: Int
        get() = plate?.length ?: 0

    override fun get(index: Int): Char {
        return plate!![index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return plate!!.subSequence(startIndex, endIndex)
    }

    var plate: String? = null
    var score = 0.0
    var modelName = ""
    var subName = ""
    private var plateColor = ""

    fun getPlateColor(): String {
        return plateColor
    }

    fun setPlateColor(c: String) {
        plateColor = c.replace("色", "牌")
    }

    override fun toString(): String {
        return "$plateColor ${plate ?: "未识别"}\n$modelName $subName"
    }
}