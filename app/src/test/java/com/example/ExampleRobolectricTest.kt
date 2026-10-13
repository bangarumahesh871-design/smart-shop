package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.smartshop.data.model.AppLanguage
import com.example.smartshop.data.model.StringsProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SmartShop", appName)
  }

  @Test
  fun `test multilingual strings provider`() {
    val teluguTitle = StringsProvider.get("today_sales", AppLanguage.TELUGU)
    val hindiTitle = StringsProvider.get("today_sales", AppLanguage.HINDI)
    val englishTitle = StringsProvider.get("today_sales", AppLanguage.ENGLISH)

    assertTrue(teluguTitle.contains("ఈ రోజు"))
    assertTrue(hindiTitle.contains("आज की"))
    assertEquals("Today's Sales", englishTitle)
  }
}
