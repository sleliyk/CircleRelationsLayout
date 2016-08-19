package com.monkeycoders.sl.circlerelationslayout

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val circleLayout = findViewById(R.id.circle_relations_layout) as CircleLayout
        val circles: LinkedList<CircleItem> = LinkedList()
        for (i in 0..15) {
            circles.add(CircleItem(i))
        }

        circles.forEachIndexed outer@ { i, circleItem ->
            circles.forEach inner@ {
                if (it.id == circleItem.id)
                    return@inner

                circleItem.relationsToCircles.add(it.id)
            }
        }

        circles.forEach { circleLayout.addCircle(it) }
    }
}
