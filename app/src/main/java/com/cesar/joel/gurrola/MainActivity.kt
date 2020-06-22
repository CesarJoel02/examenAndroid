package com.cesar.joel.gurrola

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.cesar.joel.gurrola.Util.ApiInterface
import com.cesar.joel.gurrola.Util.ClickListener
import com.cesar.joel.gurrola.Util.DataBaseHelper
import com.cesar.joel.gurrola.model.DataClass
import com.cesar.joel.gurrola.model.GetConjuntotiendasUsuarioResult
import com.cesar.joel.gurrola.model.ModeloRespuestaApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.data_item_layout.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity(), OnCompleteListener<Location> {

    val PERMISSION_ID = 1
    private lateinit var datalist : ModeloRespuestaApi
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        obtenerDatosdeApi()
        requestPermissions()
        val recyclerview = this.findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.adapter = dataAdapter(null, object :ClickListener{
            override fun onclick(vista: View, position: Int) {
                recyclerview.adapter = dataAdapter(datalist.getConjuntotiendasUsuarioResult.toMutableList(), object :ClickListener{
                    override fun onclick(vista: View, position: Int) {

                        if (checkPermissions()) {
                            if (isLocationEnabled()) {

                                mFusedLocationClient.lastLocation.addOnCompleteListener(this@MainActivity){task->
                                    var location:Location = task.result!!
                                    val startPoint = Location("locationA")
                                    startPoint.setLatitude(location.latitude)
                                    startPoint.setLongitude(location.latitude)

                                    val endPoint = Location("locationB")
                                    endPoint.setLatitude(datalist.getConjuntotiendasUsuarioResult[position].Latitud)
                                    endPoint.setLongitude(datalist.getConjuntotiendasUsuarioResult[position].Longitud)

                                    val distance = startPoint.distanceTo(endPoint)
                                    Toast.makeText(applicationContext, "La distancia es de: $distance metros",Toast.LENGTH_SHORT).show()

                                }
                            }
                        }



                    }
                })
            }
        })
        val searchView = this.findViewById<SearchView>(R.id.searchview)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {



                return true
            }
            override fun onQueryTextChange(p0: String?): Boolean {
               val filteredList = datalist.getConjuntotiendasUsuarioResult.filter {

                   it.determinante.contains(p0.toString()) || it.Cadena.contains(p0.toString()) || it.Sucursal.contains(p0.toString())

                   return true
               }

                recyclerview.adapter = dataAdapter(filteredList.toMutableList(), object :ClickListener{
                    override fun onclick(vista: View, position: Int) {

                        if (checkPermissions()) {
                            if (isLocationEnabled()) {

                                mFusedLocationClient.lastLocation.addOnCompleteListener(this@MainActivity){task->
                                    var location:Location = task.result!!
                                    val startPoint = Location("locationA")
                                    startPoint.setLatitude(location.latitude)
                                    startPoint.setLongitude(location.latitude)

                                    val endPoint = Location("locationB")
                                    endPoint.setLatitude(filteredList[position].Latitud)
                                    endPoint.setLongitude(filteredList[position].Longitud)

                                    val distance = startPoint.distanceTo(endPoint)
                                    Toast.makeText(applicationContext, "La distancia es de: $distance metros",Toast.LENGTH_SHORT).show()

                                }
                            }
                        }



                    }
                })

                return true
            }
        }

        )
    }

    fun obtenerDatosdeApi(){
        val json = JSONObject()
        //{
        //  "Usuario":
        //      {
        //          "Id":"11208"
        //      },
        //  "Proyecto":
        //      {
        //          "Id":"137",
        //          "Ufechadescarga" : 0
        //      }
        //}
        val jsonUsuario = JSONObject()
        jsonUsuario.put("Id","11208")
        json.put("Usuario",jsonUsuario)
        val jsonProyecto = JSONObject()
        jsonProyecto.put("Id","137")
        jsonProyecto.put("Ufechadescarga" , 0)
        json.put("Proyecto",jsonProyecto)
        val call = ApiInterface.create().obtenerData(json)
        Log.d("PeticionApi", json.toString())
        Log.d("PeticionApi", call.request().url().toString())
        call.enqueue(object:retrofit2.Callback<ModeloRespuestaApi>{
            override fun onFailure(call: Call<ModeloRespuestaApi>, t: Throwable) {
                Log.e("ErrorPeticionAPI", t.localizedMessage)
            }

            override fun onResponse(call: Call<ModeloRespuestaApi>, response: Response<ModeloRespuestaApi>) {
                Log.d("PeticionApi", "No fallo")
                when(response.code()){
                    200->{
                        datalist = response.body()!!
                        datalist.getConjuntotiendasUsuarioResult?.forEach { item->
                            val dataitem = DataClass(
                                item.DeterminanteTienda,
                                item.Cadena,
                                item.Sucursal,
                                item.Latitud,
                                item.Longitud
                            )
                            DataBaseHelper(
                                applicationContext,
                                "DataDB",
                                1
                            ).addData(dataitem)
                        }
                        Toast.makeText(applicationContext,"Data saved Succesfully", Toast.LENGTH_SHORT).show()
                    }
                    400->{

                        Log.d("PeticionApi", "Algo Fallo 400 ")
                        Log.d("PeticionApi", response.message())
                        Log.d("PeticionApi", response.errorBody().toString())
                        Log.d("PeticionApi", response.raw().toString())
                    }
                    404->{
                        Log.d("PeticionApi", "Algo Fallo 404")
                    }
                    500->{
                        Log.d("PeticionApi", "Algo Fallo 500")
                    }
                    503->{
                        Log.d("PeticionApi", "Algo Fallo 503")
                    }
                }
            }
        })
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onComplete(p0: Task<Location>) {
        TODO("Not yet implemented")
    }
}

class dataAdapter(items: MutableList<GetConjuntotiendasUsuarioResult>?,
                  val listener : ClickListener
): RecyclerView.Adapter<dataAdapter.ViewHolder>(){
    val items :MutableList<GetConjuntotiendasUsuarioResult> = mutableListOf()
    var viewHolder: ViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.data_item_layout, parent, false)
        viewHolder = ViewHolder(v, listener)
        return viewHolder!!
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.determinante?.text = item.determinante
        holder.cadena?.text = item.Cadena
        holder.sucursal?.text = item.Sucursal
        holder.latitud?.text = item.Latitud.toString()
        holder.longitud?.text = item.Longitud.toString()
    }

    class ViewHolder (vista: View, listener: ClickListener) : RecyclerView.ViewHolder(vista), View.OnClickListener{
        override fun onClick(v: View?) {
            this.listener?.onclick(v!!,adapterPosition)
        }

        var v = vista
        var determinante : TextView ?= null
        var sucursal : TextView ?= null
        var cadena : TextView ?= null
        var longitud : TextView?= null
        var latitud : TextView ?= null
        var listener : ClickListener ?= null

        init {
            determinante  = v.textDeterminante
            sucursal = v.textSucursal
            cadena = v.textCadena
            latitud = v.textLatitud
            longitud = v.textLongitud
            this.listener = listener
            vista.setOnClickListener(this)
        }

    }
}


//Determinante, Cadena, sucursal, latitud y longitud