package com.example.ladm_u3_p2_andresh

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.opciones.*

class MainActivity : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var entrega = false
    var listaID =ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        entregado.setOnClickListener {
            entrega = entregado.isChecked
        }

        insertar.setOnClickListener {
            insertarRegistro()
            cargarLista()
        }
        cargarLista()
    }

    private fun insertarRegistro() {
        var data = hashMapOf(
            "nombre" to nombre.text.toString(),
            "domicilio" to domicilio.text.toString(),
            "numero" to numero.text.toString().toInt(),
            "pedido" to hashMapOf(
                "producto" to producto.text.toString(),
                "precio" to precio.text.toString().toFloat(),
                "cantidad" to cantidad.text.toString().toInt(),
                "entregado" to entrega.toString().toBoolean()
            )
        )

        baseRemota.collection("restaurante")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this,"Se agregó con exito",Toast.LENGTH_LONG)
                    .show()
                nombre.setText("");domicilio.setText("");numero.setText("");producto.setText("");precio.setText("");cantidad.setText("");
            }
            .addOnFailureListener {
                Toast.makeText(this,"No se pudo agregar",Toast.LENGTH_LONG)
                    .show()
            }
    }

        private fun cargarLista(){
            baseRemota.collection("restaurante")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if(firebaseFirestoreException != null){
                        Toast.makeText(this,"No se pudo realizar la consulta",Toast.LENGTH_LONG)
                        return@addSnapshotListener
                    }
                    var cont = 0
                    var res = ""
                    var total=querySnapshot!!.documents.size-1
                    var vector =Array<String>(querySnapshot.documents.size,{""})
                    listaID=ArrayList<String>()
                    for (document in querySnapshot!!){
                        res = "Nombre: "+document.getString("nombre")+"\nDomicilio: "+document.getString("domicilio")+"\nNúmero: "+document.get("numero")+"\n   Pedido\nProducto: "+document.get("pedido.producto")+"\nPrecio: "+document.get("pedido.precio")+" Cantidad: "+document.get("pedido.cantidad")+"\nEntregado: "+document.get("pedido.entregado")
                        vector[cont] = res
                        listaID.add(document.id.toString())
                        cont++
                    }
                    lista.adapter=ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,vector)
                    lista.setOnItemClickListener { parent, view, position, id ->
                        var idABuscar = listaID[position]
                        construirDialogo(idABuscar)

                    }

                }
        }

    private fun construirDialogo(idABuscar: String) {
        var dialogo = Dialog(this)
        dialogo.setContentView(R.layout.opciones)
        val docRef = baseRemota.collection("restaurante").document(idABuscar)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    dialogo.findViewById<EditText>(R.id.nnombre).setText(document.getString("nombre"))
                    dialogo.findViewById<EditText>(R.id.ddomicilio).setText(document.getString("domicilio"))
                    dialogo.findViewById<EditText>(R.id.nnumero).setText(document.get("numero").toString())
                    dialogo.findViewById<EditText>(R.id.pproducto).setText(document.get("pedido.producto").toString())
                    dialogo.findViewById<EditText>(R.id.pprecio).setText(document.get("pedido.precio").toString())
                    dialogo.findViewById<EditText>(R.id.ccantidad).setText(document.get("pedido.cantidad").toString())
                    if (document.get("pedido.entregado").toString().toBoolean()){
                        dialogo.findViewById<CheckBox>(R.id.eentregado).onPointerCaptureChange(true)
                    }
                } else {

                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this,"No se encontro ningún documento",Toast.LENGTH_LONG)
                    .show()
            }

        dialogo.show()

        dialogo.findViewById<Button>(R.id.actualizar).setOnClickListener {

                var nnombre =  dialogo.findViewById<EditText>(R.id.nnombre).text.toString()
                var domicilio = dialogo.findViewById<EditText>(R.id.ddomicilio).text.toString()
                var numero = dialogo.findViewById<EditText>(R.id.nnumero).text.toString().toInt()
                var producto = dialogo.findViewById<EditText>(R.id.pproducto).text.toString()
                var precio = dialogo.findViewById<EditText>(R.id.pprecio).text.toString().toFloat()
                var cantidad = dialogo.findViewById<EditText>(R.id.ccantidad).text.toString().toInt()
                 var entregado = dialogo.findViewById<CheckBox>(R.id.eentregado).isChecked



            baseRemota.collection("restaurante")
                .document(idABuscar)
                .update("nombre",nnombre,"domicilio",domicilio,"numero",numero,"pedido.producto",producto,"pedido.precio",precio,"pedido.cantidad",cantidad,"pedido.entregado",entregado)
            Toast.makeText(this,"Se ha actualizado",Toast.LENGTH_LONG)
                .show()
        }

        dialogo.findViewById<Button>(R.id.Cancelar).setOnClickListener {
            dialogo.dismiss()
        }

        dialogo.findViewById<Button>(R.id.eliminar).setOnClickListener {
            baseRemota.collection("restaurante")
                .document(idABuscar)
                .delete()
        }
    }

}
