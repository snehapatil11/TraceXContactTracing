package com.example.tracexcontacttracing.service

import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.sql.Date

class CovidDataService {

    /*fun getTimeSeriesDataByMonth() : Map<String, Int> {

        val result = HashMap<String, Int>()

        val response  = khttp.get("https://api.covidactnow.org/v2/states.json?apiKey=eda8d946097542ebb42907f9f1b69040")
        val jsonResult:JSONArray = response.jsonArray


        var cases =0;
        for (i in 0 until jsonResult.length()-1) {
            val obj = jsonResult.getJSONObject(i);
            cases+= obj.getJSONObject("actuals").getInt("cases")
        }

        val actualTimeSeries  = jsonResult
        // for each actual ts
          val date = "2020-05-01"
        return result;
    }*/

    fun getTotalCasesDeaths() : Pair<Int,Int> {

        try {

            val response  = khttp.get("https://api.covidactnow.org/v2/states.json?apiKey=eda8d946097542ebb42907f9f1b69040")
            val jsonResult:JSONArray = response.jsonArray

            var cases = 0;
            var deaths = 0;
            for (i in 0 until jsonResult.length()-1) {
                val obj = jsonResult.getJSONObject(i);
                cases+= obj.getJSONObject("actuals").getInt("cases");
                deaths+= obj.getJSONObject("actuals").getInt("deaths")
            }
            return Pair(cases,deaths);

        }
        catch (e:Exception) {
            e.printStackTrace();
            return Pair(0, 0);
        }

    }


}