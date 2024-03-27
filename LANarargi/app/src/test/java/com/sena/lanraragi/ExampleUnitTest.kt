package com.sena.lanraragi

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)

        val s = "parody:fate: grand order,  character:abigail williams,  character:artoria pendragon alter,  character:asterios,  character:ereshkigal,  character:gilgamesh,  character:hans christian andersen,  character:hassan of serenity,  character:hector,  character:helena blavatsky,  character:irisviel von einzbern,  character:jeanne alter,  character:jeanne darc,  character:kiara sessyoin,  character:kiyohime,  character:lancer,  character:martha,  character:minamoto no raikou,  character:mochizuki chiyome,  character:mordred pendragon,  character:nitocris,  character:oda nobunaga,  character:osakabehime,  character:penthesilea,  character:queen of sheba,  character:rin tosaka,  character:saber,  character:sakata kintoki,  character:scheherazade,  character:sherlock holmes,  character:suzuka gozen,  character:tamamo no mae,  character:ushiwakamaru,  character:waver velvet,  character:xuanzang,  character:zhuge liang,  group:wadamemo,  artist:wada rco,  artbook,  full color"

        val regex = Regex(",\\s+")

        val a = s.split(regex)

        val b = a.mapNotNull {
            val t = it.split(Regex(":"), 2)
            if (t.size == 2) {
                Pair(t[0], t[1])
                print("${t[0]}, ${t[1]}\n")
            } else {
                null
                print("未知:$it")
            }
        }


    }


    @Test
    fun testFileUrlRegex() {
        val regex1 = Regex("api/archives/[a-z0-9]+/page")
        val regex2 = Regex("path=.*")
        val s = "http://192.168.0.102:3002/api/archives/ab375397919f1cb1f9caf7fa596a6dd7d2aeecce/page?path=01.jpg"

        val match1 = Regex("api/archives/[a-z0-9]+/page").find(s)?.value?.split(Regex("/"))?.getOrNull(2)
        val match2 = Regex("path=.*").find(s)?.value?.replace("path=", "")


        println("match1: $match1\nmatch2: $match2")
    }
}