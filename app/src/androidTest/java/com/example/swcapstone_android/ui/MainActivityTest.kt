package com.example.swcapstone_android.ui

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun NEST_알림_테스트() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val nestIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("SELECTED_NEST_ID", "76")
            putExtra("NOTIFICATION_TYPE", "NEST")
        }

        val scenario = ActivityScenario.launch<MainActivity>(nestIntent)

        scenario.onActivity { activity ->
            activity.triggerNewIntentForTest(nestIntent)
        }

        scenario.onActivity { activity ->
            val checkIntent = activity.intent
            assertEquals("76", checkIntent.getStringExtra("SELECTED_NEST_ID"))
            assertEquals("NEST", checkIntent.getStringExtra("NOTIFICATION_TYPE"))
        }

        scenario.close()
    }

    @Test
    fun POSTCARD_알림_테스트() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val postcardIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("NOTIFICATION_TYPE", "POSTCARD")
        }

        val scenario = ActivityScenario.launch<MainActivity>(postcardIntent)

        scenario.onActivity { activity ->
            activity.triggerNewIntentForTest(postcardIntent)
        }

        scenario.onActivity { activity ->
            val checkIntent = activity.intent
            assertEquals("POSTCARD", checkIntent.getStringExtra("NOTIFICATION_TYPE"))
        }

        scenario.close()
    }
}