package com.example.tracexcontacttracing.service

import android.provider.SyncStateContract.Helpers.insert
import android.util.Log
import com.example.tracexcontacttracing.dao.StateCovidDataDao
import com.example.tracexcontacttracing.dao.TimeSeriesCovidDataDao
import com.example.tracexcontacttracing.data.StateCovidDataEntity
import com.example.tracexcontacttracing.data.TimeSeriesCovidDataEntity
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONObject

class CovidDataService {

    private val TAG = "CovidDataService"

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

    fun getSafeInt(jsonObject:JSONObject, key: String): Int {
        if (jsonObject==null) return 0;
        val t = jsonObject.getInt(key)
        if (t == null) {
            return 0
        }
        else return t
    }

    fun fetchAndStoreStateCovidData(stateCovidDataDao: StateCovidDataDao?) {
        try {
            Log.i(TAG, "Clearing state covid data table")
            stateCovidDataDao?.deleteAll()


            val response  = khttp.get("https://api.covidactnow.org/v2/states.json?apiKey=eda8d946097542ebb42907f9f1b69040")
            val jsonResult:JSONArray = response.jsonArray
            if(jsonResult!=null && jsonResult.length() > 0) {

            }
            for (i in 0 until jsonResult.length()-1) {
                val obj = jsonResult.getJSONObject(i);
                val actualJsonObj = obj.getJSONObject("actuals");
                if(actualJsonObj!=null) {
                    try {
                        val state = obj.getString("state")
                        val cases = getSafeInt(actualJsonObj,"cases");
                        val deaths= getSafeInt(actualJsonObj,"deaths");
                        val newCases = getSafeInt(actualJsonObj,"newCases");
                        val newDeaths = getSafeInt(actualJsonObj,"newDeaths");
                        val vaccinationsInitiated = getSafeInt(actualJsonObj,"vaccinationsInitiated");
                        val vaccinationsCompleted = getSafeInt(actualJsonObj,"vaccinationsCompleted");
                        val created_at = System.currentTimeMillis();
                        val modified_at = System.currentTimeMillis();
                        val entity = StateCovidDataEntity(state,cases,deaths,newCases,newDeaths,vaccinationsInitiated,vaccinationsCompleted,created_at, modified_at)
                        val id = stateCovidDataDao?.insert(entity)
                        Log.i(TAG,"Successfully saved State covid data $entity with id=$id")
                    } catch (e:Exception) {
                        Log.e(TAG, "Exception in storing state data "+e.message+", "+obj)
                        e.printStackTrace();
                    }
                }
            }

        }
        catch (e:Exception) {
            Log.e(TAG, "Exception in fetching state data"+e.message)
            e.printStackTrace();
        }

    }

    fun fetchAndStoreTimeSeriesCovidData(timeSeriesCovidDataDao: TimeSeriesCovidDataDao?) {
        try {
            Log.i(TAG, "Clearing TimeSeriesCovidData table")
            timeSeriesCovidDataDao?.deleteAll()

            val response  = khttp.get("https://api.covidactnow.org/v2/country/US.timeseries.json?apiKey=eda8d946097542ebb42907f9f1b69040")
            val mainResult = response.jsonObject

            val jsonResult:JSONArray = mainResult.getJSONArray("actualsTimeseries")

            for (i in 0 until jsonResult.length()-1) {
                val obj = jsonResult.getJSONObject(i);
                if(obj!=null) {
                    try {
                        val date = obj.getString("date");
                        val d =  DateTime.parse(date);
                        val newCases = getSafeInt(obj,"newCases");
                        val newDeaths = getSafeInt(obj,"newDeaths");
                        val year = d.year;
                        val month = d.monthOfYear;
                        val created_at = System.currentTimeMillis();
                        val modified_at = System.currentTimeMillis();
                        val entity = TimeSeriesCovidDataEntity(date, getMonthGroup(date),year,month,newCases,newDeaths,created_at,modified_at)
                        val id = timeSeriesCovidDataDao?.insert(entity)
                        Log.i(TAG,"Successfully saved timeseries covid data $entity with id=$id")
                    } catch (e:Exception) {
                        Log.e(TAG, "Exception in storing timeseries data "+e.message+", "+obj)
                        e.printStackTrace();
                    }
                }
            }

        }
        catch (e:Exception) {
            Log.e(TAG, "Exception in fetching state data"+e.message)
            e.printStackTrace();
        }

    }

    fun getMonthGroup(dateStr:String):String {
        val d =  DateTime.parse(dateStr)
        return "${d.monthOfYear().asShortText}'${Math.floorMod(d.year, 100)}"
    }



}