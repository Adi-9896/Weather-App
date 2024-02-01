package com.example.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weather.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private var apikey = "a853fc337158576f4e2ccfc1f348326c"
    private var requestcode = 123
    lateinit var fusedlocation: FusedLocationProviderClient
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        fusedlocation = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(binding.root)
        binding.progress.visibility = View.VISIBLE

        //search view se focus or blinking hatana
        binding.maibackfground.setOnClickListener {
            binding.searchView.clearFocus()
        }
        getlocation()

        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.trim()

                if (!query.isNullOrBlank()) {
                    getcityweather(query)
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }


        })
    }

    private fun getcityweather(query: String?) {
        binding.progress.visibility = View.GONE
        if (query != null) {
            Retrofitinstance.json.getcityWeather(query, apikey).enqueue(object : Callback<Modal> {
                override fun onResponse(call: Call<Modal>, response: Response<Modal>) {
                    updatedata(response.body())
                }

                override fun onFailure(call: Call<Modal>, t: Throwable) {
                    TODO("Not yet implemented")
                }


            })
        }

    }

    private fun getlocation() {
        if (checkpermssion()) {
            if (islocationenable()) {
                // GPS YA LOCATION ON HA YA NHI  latitudes and longitude
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    requestpermission()
                    return
                }
                fusedlocation.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    //val latitude = locaion?.latitude
                    //val longiude = locaion?.longitude
                    if (location == null) {
                        Toast.makeText(this, "NULL recieved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "sucess", Toast.LENGTH_SHORT).show()
                        fetchweatherlocation(
                            location.latitude.toString(), location.longitude.toString()
                        )
                    }
                }

            } else {
                //open settings
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

        } else {
            //AGAR LOCAION KI PERMISIOON NHI DI TO REQUEST KRO DIALOG BOX SE
            requestpermission()


        }
    }

    private fun fetchweatherlocation(latitude: String, longitude: String) {
        binding.progress.visibility = View.VISIBLE
        Retrofitinstance.json.getWeather(latitude, longitude, apikey)
            .enqueue(object : Callback<Modal> {
                override fun onResponse(call: Call<Modal>, response: Response<Modal>) {
                    if (response.isSuccessful) {
                        updatedata(response.body())
                        binding.progress.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<Modal>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })

    }

    @SuppressLint("SetTextI18n")
    private fun updatedata(body: Modal?) {
        if (body != null) {
            val date = SimpleDateFormat("d MMMM yyyy, hh:mm a", Locale.ENGLISH)
            date.format(Date())
            binding.dateandtime.text = body.name
            binding.dayMax.text = "Day " + kelvintocelcius(body.main.temp_max) + "°"
            binding.dayMin.text = "Night " + kelvintocelcius(body.main.temp_min) + "°"
            binding.temprature.text = "" + kelvintocelcius(body.main.temp) + "°"
            binding.feelslike.text = " Feels like " + kelvintocelcius(body.main.feels_like) + "°"
            binding.weatherType.text = body.weather[0].main
            binding.sunset.text = timestamptolocaldate(body.sys.sunset.toLong())
            binding.sunrise.text = timestamptolocaldate(body.sys.sunrise.toLong())
            binding.pressure.text = body.main.pressure.toString()
            binding.humidity.text = "${body.main.humidity} %"
            binding.windspeed.text = metertokilometer(body.wind.speed)
            binding.Temprature.text =
                "" + (kelvintocelcius(body.main.temp).times(1.8).plus(32).roundToInt())
            // binding.searchView.setQuery(body.name,false)
            updateui(body.weather[0].id)
        }  //  binding.cityName.text = body.name


        //()
    }

    private fun updateui(id: Int) {
        //thunderstorm
        when (id) {
            in 200..232 -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.thunderstorm)
                //changing ui of layouts
                binding.mainlayout.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.thunderstorm)
                binding.linearlayout1.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.thunderstorm)
                binding.linearlayout2.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.thunderstorm)
                binding.maibackfground.setImageResource(R.drawable.thunderstorm)


                binding.weathericon.setImageResource(R.drawable.storms)
            }
            in 300..321 -> {
                //dizzel
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.dizzel)
                //changing ui of layouts
                binding.mainlayout.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.dizzel)
                binding.linearlayout1.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.dizzel)
                binding.linearlayout2.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.dizzel)
                binding.maibackfground.setImageResource(R.drawable.dizzel)

                binding.weathericon.setImageResource(R.drawable.drizzle)

            }
            in 500..531 -> {
                //rainy
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.rainy)
                //changing ui of layouts
                binding.mainlayout.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.rainy)
                binding.linearlayout1.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.rainy)
                binding.linearlayout2.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.rainy)
                binding.maibackfground.setImageResource(R.drawable.rainy)


                binding.weathericon.setImageResource(R.drawable.rainy1)
            }
            in 600..622 -> {
                //snowy
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.snow)
                //changing ui of layouts
                binding.mainlayout.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.snow_bg)
                binding.linearlayout1.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.snow_bg)
                binding.linearlayout2.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.snow_bg)
                binding.maibackfground.setImageResource(R.drawable.snow_bg)


                binding.weathericon.setImageResource(R.drawable.snowy)


            }
            in 701..781 -> {
                //Mist

                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.mist)
                //changing ui of layouts
                binding.mainlayout.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.mist)
                binding.linearlayout1.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.mist)
                binding.linearlayout2.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.mist)
                binding.maibackfground.setImageResource(R.drawable.mist)

                binding.weathericon.setImageResource(R.drawable.mist2)
                //MIST
            }
            800 -> {
                //CLEAR
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.clear)
                //changing ui of layouts
                binding.mainlayout.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.clear)
                binding.maibackfground.setImageResource(R.drawable.clear)
                binding.linearlayout1.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.clear)
                binding.linearlayout2.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.clear)
                binding.mainlayout.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.clear)

                binding.weathericon.setImageResource(R.drawable.sunnys)

            }
            in 801..804 -> {
                //cloudy
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = resources.getColor(R.color.clouds)
                //changing ui of layouts
                binding.mainlayout.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.cloud)
                binding.linearlayout1.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.cloud)
                binding.linearlayout2.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.cloud)
                binding.maibackfground.setImageResource(R.drawable.cloud)

                binding.weathericon.setImageResource(R.drawable.cloudy_1)

            }
        }


    }

    private fun metertokilometer(speed: Double): CharSequence {
        val windSpeedKilometersPerHour = speed * 3.6 // Convert m/s to km/h
        return String.format("%.2f", windSpeedKilometersPerHour) + " km/h"

    }

    @SuppressLint("SimpleDateFormat")
    private fun timestamptolocaldate(timestamp: Long): String? {
        val milliseconds = timestamp * 1000
        // Create a Date object
        val date = Date(milliseconds)
        // Create a SimpleDateFormat object with desired date format
        val sdf = SimpleDateFormat("hh:mm:ss")
        // Set the timezone to the local timezone
        sdf.timeZone = TimeZone.getDefault()
        // Format the date and time
        return sdf.format(date)

    }

    private fun kelvintocelcius(tempMax: Double): Double {
        val celsius = tempMax - 273.15
        // Round to two decimal places
        return "%.1f".format(celsius).toDouble()

    }

    private fun islocationenable(): Boolean {
        //check krna ki phone ki location on ha a off
        val locationmanager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationmanager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    }

    private fun checkpermssion(): Boolean {

        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestpermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            ), requestcode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestcode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "GRANTED", Toast.LENGTH_SHORT).show()
                getlocation()
            } else {
                Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}