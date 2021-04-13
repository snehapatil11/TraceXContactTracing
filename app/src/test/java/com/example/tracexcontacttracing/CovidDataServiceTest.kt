package com.example.tracexcontacttracing

import com.example.tracexcontacttracing.service.CovidDataService

import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
//@RunWith(AndroidJUnit4::class)
class CovidDataServiceTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
       // val appContext = InstrumentationRegistry.getInstrumentation().targetContext
       // assertEquals("com.example.tracexcontacttracing", appContext.packageName)

        val covidDataService = CovidDataService()
        val res = covidDataService.getTotalCasesDeaths()
        print(res);
    }
}