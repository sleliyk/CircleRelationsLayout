package com.monkeycoders.sl.circlerelationslayout

import android.graphics.Path
import java.util.*

/**
 * @author Stas Lelyuk
 * @since 8/11/16
 */
class CircleItem(val id: Int) {
    var active: Boolean = false
    val relationsToCircles: ArrayList<Int> = ArrayList()
    private val relationsWithPath: HashMap<Int, RelationPath> = HashMap()

    fun hasRelationFor(otherId: Int): Boolean {
        if (otherId.equals(id) || relationsWithPath.isEmpty())
            return false

        return relationsWithPath.containsKey(otherId)
    }

    fun addRelation(otherId: Int, path: RelationPath) {
        if (hasRelationFor(otherId)) return
        relationsToCircles.add(otherId)
        relationsWithPath.put(otherId, path)
    }

    fun buildPath(): Path {
        val path: Path = Path()
        relationsWithPath.values.forEach {
            val subPath: Path = it.buildPath()
            path.addPath(subPath)
        }
        return path
    }

    fun buildPath(multiplier: Long): Path {
        val path: Path = Path()
        relationsWithPath.values.forEach {
            val subPath: Path = it.buildPath(multiplier)
            path.addPath(subPath)
        }
        return path
    }
}