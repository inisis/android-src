package hitrouble.com.api

import org.json.JSONObject

class PlateApi private constructor() {


    companion object {

        fun platesInResult(obj: JSONObject): Array<Plate> {
            var list = arrayOf<Plate>()
            val cars = obj.getJSONObject("Result").getJSONArray("Vehicles")
            if (cars?.length() ?: 0 > 0) {
                for (i in 0 until cars.length()) {
                    val car = cars.getJSONObject(i)
                    val type = car.getJSONObject("ModelType")
                    val name = type.getString("Brand")
                    val subname = type.getString("SubBrand")
                    val plats = car.getJSONArray("Plates")
                    for (j in 0 until plats.length()) {
                        val plat = Plate()
                        val pObj = plats.getJSONObject(j)
                        val color = pObj.getJSONObject("Color").getString("ColorName")
                        plat.modelName = name
                        plat.subName = subname
                        plat.setPlateColor(color)
                        plat.plate = pObj.getString("PlateText")
                        plat.score = pObj.getDouble("Confidence")
                        list = list.plus(plat)
                    }

                }
            }
            return list
        }

    }

}