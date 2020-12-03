package com.ajvierci.inventario.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajvierci.inventario.R;
import com.ajvierci.inventario.bdsqlite.ConexionSQLiteHelper;
import com.ajvierci.inventario.bdsqlite.MovimientoBD;
import com.ajvierci.inventario.entidades.Articulos;
import com.ajvierci.inventario.entidades.Detalle;
import com.ajvierci.inventario.entidades.DetalleTemp;
import com.ajvierci.inventario.entidades.DevolucionIndividual;

import java.util.ArrayList;

public class DetalleAdapterModificar extends RecyclerView.Adapter<DetalleAdapterModificar.ViewHolderDatos> implements View.OnClickListener, AdapterView.OnItemClickListener {
    private ArrayList<DevolucionIndividual> listDatos;
    private View.OnClickListener listener;
    private AdapterView.OnItemClickListener clickListener;
    String codigoDoco="";
    private ConexionSQLiteHelper conexion;
    private Context contexto;
    private ArrayList<Articulos>listaArticulos;
    private ArrayList<Detalle>ListaDetalle;

    //private ArrayList<String>listDatos;
    private int posicion=0;
  /*  public ClienteAdapter(ArrayList<DevolucionesJSON> listDatos) {
        this.listDatos = listDatos;
    }*/

    public DetalleAdapterModificar(ArrayList<DevolucionIndividual> listDatos, Context contexto) {
        this.listDatos = listDatos;
        this.contexto=contexto;
    }

    @NonNull
    @Override
    public ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        conexion= new ConexionSQLiteHelper(contexto,"bd_inventario", null, 1);
        //enlaza el adaptador con el itemlist
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_detalle_editar
                        , null
                        , false);
        view.setOnClickListener(this);
        return new ViewHolderDatos(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderDatos holder, final int position) {
        posicion=position;
        //establece la la comunicacion entre el adatodor y la clase viewHolderDatos
        holder.DOCO.setText(String.valueOf(listDatos.get(position).getDOCO()));
        holder.codigoArticulo.setText(String.valueOf(listDatos.get(position).getCodigo_aticulo()));
        holder.id.setText(String.valueOf(listDatos.get(position).getIdDetalle()));
        holder.descripcion.setText(String.valueOf(listDatos.get(position).getDescripcionArticulo()));
        holder.cantidad.setText(String.valueOf((listDatos.get(position).getCantidad()/100)));
        holder.migrado.setText(String.valueOf(listDatos.get(position).getMigrado()));
        holder.codigoArticulo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    String codigoArticulo=BuscarCodigo(String.valueOf(holder.codigoArticulo.getText().toString()));
                    System.out.println(codigoArticulo);
                    if(!codigoArticulo.equals("ND")){
                        holder.descripcion.setText(codigoArticulo);
                    }else{
                        Toast.makeText(contexto,"No se encontro el articulo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    System.out.println("dentro");
                }
            }
        });
        holder.eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MovimientoBD movimientoBD=new MovimientoBD();

       if(holder.migrado.getText().toString().equals("no")){
           boolean res=movimientoBD.eliminarDetalle(holder.id.getText().toString(), conexion);
           if(res){
               Toast.makeText(contexto,"registro eliminado.", Toast.LENGTH_SHORT).show();
           }else{
               Toast.makeText(contexto,"Ocurrio un error al intentar eliminar el detalle", Toast.LENGTH_SHORT).show();
           }
       }else{
           Toast.makeText(contexto,"El registro ya se encuentra sincronizado, no se puede modificar.", Toast.LENGTH_LONG).show();
       }
            }
        });

        codigoDoco=holder.codigoArticulo.getText().toString();
        holder.actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("actualizar: "+ holder.migrado.getText().toString());
                if(holder.migrado.getText().toString().equals("no")){

                                    String articulo=String.valueOf(holder.codigoArticulo.getText().toString());
                String cantidad=holder.cantidad.getText().toString();
                String codigo=holder.id.getText().toString();
                String codigoDoco=holder.DOCO.getText().toString();
                int idDetalle=0;
                    if(!BuscarCodigo(articulo).equals("ND")){
                        if(comprobarDocoArticuloExiste(Integer.parseInt(codigoDoco),articulo, Integer.parseInt(codigo))){
                            if(cantidad.equals("")||cantidad.equals("0")||cantidad.isEmpty()){
                                Toast.makeText(contexto,"Verifique la cantidad, no puede ser nula ni tampoco 0", Toast.LENGTH_LONG).show();
                            }else{
                                if(getDetallePorID(Integer.parseInt(articulo),Integer.parseInt(codigoDoco)).size()>0){
                                    idDetalle=getDetallePorID(Integer.parseInt(articulo),Integer.parseInt(codigoDoco)).get(0).getId();
                                }else{
                                    idDetalle=Integer.parseInt(codigo);
                                }
                                int cantidad_=Integer.parseInt(cantidad)*100;
                                String queryUpdate="UPDATE devoluciones_detalle SET UORG = "+cantidad_+" where DOCO="+codigo+" and CODARTICULO="+articulo;
                                if(actualizar(articulo,cantidad_,idDetalle)){
                                    Toast.makeText(contexto, "Registro actualizado", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(contexto, "Error: Verifique sus datos", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }else{

                        Toast.makeText(contexto,"El codigo que intenta registrar no existe. Favor de verificar.", Toast.LENGTH_LONG).show();
                    }

                }
                else
                    {
                        Toast.makeText(contexto,"El registro ya se encuentra sincronizado, no se puede modificar.", Toast.LENGTH_LONG).show();
                }



            }
        });
        holder.agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
       if(holder.migrado.getText().toString().equals("no")){
           if(!comprobarDocoArticuloExisteAgregar(Integer.parseInt(holder.DOCO.getText().toString()),holder.codigoArticulo.getText().toString())){
               MovimientoBD movimientoBD=new MovimientoBD();
               ArrayList<Detalle>detalleActutual=movimientoBD.getFilaDetalle(holder.DOCO.getText().toString(),conexion);
               int LNID=(movimientoBD.contarFilaDetalle(Integer.parseInt(holder.DOCO.getText().toString()), conexion)+1)*1000;
               System.out.println(LNID);
               int AN8=detalleActutual.get(0).getAN8();
               ArrayList<Detalle>ListaDetalle=new ArrayList<>();
               Detalle detalle=new Detalle();
               detalle.setKCOO("00001");
               detalle.setDCTO("CD");
               detalle.setDOCO(Integer.parseInt(holder.DOCO.getText().toString()));
               detalle.setLNID((LNID+1));
               detalle.setAN8(AN8);
               detalle.setAITM(detalleActutual.get(0).getAITM());
               detalle.setUORG(Integer.parseInt(holder.cantidad.getText().toString())*100);
               detalle.setLPRC(0);
               detalle.setUOM("UN");
               detalle.setI55DEPR(0);
               detalle.setDRQJ(Integer.parseInt(movimientoBD.obtenerFecha()));
               detalle.setUPC3("");
               detalle.setS55PROMFM("");
               detalle.setLOCN("");
               detalle.setCodarticulo(holder.codigoArticulo.getText().toString());
               ListaDetalle.add(detalle);
               if(movimientoBD.insertarDetalle(conexion, ListaDetalle)){
                   Toast.makeText(contexto, "Registro agregado.", Toast.LENGTH_SHORT).show();
               }else{
                   Toast.makeText(contexto,"Error intentando registrar el producto", Toast.LENGTH_SHORT).show();
               }
           }else{
               Toast.makeText(contexto,"El articulo ya se encuentra en el detalle.", Toast.LENGTH_SHORT).show();
           }
       }
       else{
           Toast.makeText(contexto,"El registro ya se encuentra sincronizado, no se puede modificar.", Toast.LENGTH_LONG).show();
       }


            }
        });
    }

    private boolean actualizar(String articuloCodio, int cantidad, int id) {
        boolean resultado=false;
        try {
            SQLiteDatabase db=conexion.getWritableDatabase();
           ContentValues values= new ContentValues();
           values.put("UORG",cantidad);
           values.put("CODARTICULO",articuloCodio);
            System.out.println(id);
           int update=db.update("devoluciones_detalle", values, "id="+id,null);
            System.out.println(update);
            if(update!=-1){
                System.out.println("actualizado");
                resultado=true;
                db.close();
            }else{
                resultado=false;
                System.out.println("Error");
            }

        }catch (Exception e){
            resultado=false;
            System.out.println("Exepction actualizarDetalle "+e);
        }
        return resultado;
    }

    private boolean comprobarDocoArticuloExiste(int doco, String articulo, int idDetalle) {

        boolean resultado=false;
        ArrayList<Detalle>detalle_doco=getDetallePorDOCOTemp(doco);
        for (int i = 0; i < detalle_doco.size(); i++) {
            int id=detalle_doco.get(i).getId();
            String idArticulo=detalle_doco.get(i).getCodarticulo();
            if(idArticulo.equals(articulo)){
                if(id==idDetalle){
                    System.out.println("articulo: "+detalle_doco.get(i).getCodarticulo());
                    resultado=true;
                }else{
                    resultado=false;
                }

            }
        }
        return resultado;
    }
    private boolean comprobarDocoArticuloExisteAgregar(int doco, String articulo) {

        boolean resultado=false;
        ArrayList<Detalle>detalle_doco=getDetallePorDOCOTemp(doco);
        for (int i = 0; i < detalle_doco.size(); i++) {
            int id=detalle_doco.get(i).getId();
            String idArticulo=detalle_doco.get(i).getCodarticulo();
            if(idArticulo.equals(articulo)){
                resultado=true;

            }
        }
        return resultado;
    }
    private String BuscarCodigo(String codigo) {

        String empresa;
        String queryConsultar="select * from articulos where NroArticuloERP="+codigo;
        SQLiteDatabase db=conexion.getReadableDatabase();
        Articulos articulos =null;
        listaArticulos=new ArrayList<>();

        Cursor cursor= db.rawQuery(queryConsultar, null);
        if(cursor!=null){

            while (cursor.moveToNext()){
                articulos=new Articulos();
                articulos.setDescripcionArticulo(cursor.getString(3));
                articulos.setCodigoBarra(cursor.getString(6));
                listaArticulos.add(articulos);

            }
            if(listaArticulos.size()>0){
                empresa=listaArticulos.get(0).getDescripcionArticulo();
            }else{
                empresa="ND";
            }

        }else{
            empresa="ND";
        }
      //  System.out.println(empresa);
        return empresa;
    }
    public ArrayList<Detalle> getDetallePorDOCOTemp(int doco){
        SQLiteDatabase db=conexion.getReadableDatabase();
        Detalle cliente=null;
        ListaDetalle= new ArrayList<Detalle>();
        Cursor cursor= db.rawQuery("select * from devoluciones_detalle where DOCO = '"+doco+"'", null);

        while (cursor.moveToNext()){
            cliente= new Detalle();
            cliente.setId(cursor.getInt(0));
            cliente.setKCOO(cursor.getString(1));
            cliente.setDCTO(cursor.getString(2));
            cliente.setDOCO(cursor.getInt(3));
            cliente.setLNID(cursor.getInt(0)*1000);
            cliente.setAN8(cursor.getInt(5));
            cliente.setAITM(cursor.getString(6));
            cliente.setUORG(cursor.getInt(7));
            cliente.setLPRC(cursor.getInt(8));
            cliente.setUOM(cursor.getString(9));
            cliente.setI55DEPR(cursor.getInt(10));
            cliente.setDRQJ(cursor.getInt(11));
            cliente.setUPC3(cursor.getString(12));
            cliente.setS55PROMFM(cursor.getString(13));
            cliente.setLOCN(cursor.getString(14));
            cliente.setCodarticulo(cursor.getString(15));


            ListaDetalle.add(cliente);
            db.close();
        }
        return ListaDetalle;
    }
    public ArrayList<Detalle> getDetallePorID(int articulo, int doco){
        SQLiteDatabase db=conexion.getReadableDatabase();
        Detalle cliente=null;
        ListaDetalle= new ArrayList<Detalle>();
        Cursor cursor= db.rawQuery("select * from devoluciones_detalle where CODARTICULO = '"+articulo+"' and DOCO="+doco, null);

        while (cursor.moveToNext()){
            cliente= new Detalle();
            cliente.setId(cursor.getInt(0));
            cliente.setKCOO(cursor.getString(1));
            cliente.setDCTO(cursor.getString(2));
            cliente.setDOCO(cursor.getInt(3));
            cliente.setLNID(cursor.getInt(0)*1000);
            cliente.setAN8(cursor.getInt(5));
            cliente.setAITM(cursor.getString(6));
            cliente.setUORG(cursor.getInt(7));
            cliente.setLPRC(cursor.getInt(8));
            cliente.setUOM(cursor.getString(9));
            cliente.setI55DEPR(cursor.getInt(10));
            cliente.setDRQJ(cursor.getInt(11));
            cliente.setUPC3(cursor.getString(12));
            cliente.setS55PROMFM(cursor.getString(13));
            cliente.setLOCN(cursor.getString(14));
            cliente.setCodarticulo(cursor.getString(15));


            ListaDetalle.add(cliente);
            db.close();
        }
        return ListaDetalle;
    }

    @Override
    public int getItemCount() {
       //retorna el tamaño de esa lista
        return listDatos.size();
    }
    public void setOnClickListener(View.OnClickListener listener){
    this.listener=listener;
    }
    public void setOnClickItemListener(AdapterView.OnItemClickListener listener){
        this.clickListener=listener;
    }
    @Override
    public void onClick(View v) {
        if(listener!=null){
                listener.onClick(v);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public class ViewHolderDatos extends RecyclerView.ViewHolder {

        TextView DOCO, id,  descripcion, migrado;
        EditText codigoArticulo, cantidad;
        Button actualizar, eliminar, agregar;

        public ViewHolderDatos(@NonNull View itemView) {
            super(itemView);
            migrado=itemView.findViewById(R.id._item_lista_detalle_editar_Migrado);
            DOCO=itemView.findViewById(R.id._item_lista_detalle_editar_codigo_DOCO);
            id=itemView.findViewById(R.id._item_lista_detalle_editar_id_devolucion);
            descripcion=itemView.findViewById(R.id._item_lista_detalle_editar_articulo);
            cantidad=itemView.findViewById(R.id._item_lista_detalle_editar_cantidad);
            codigoArticulo=itemView.findViewById(R.id._item_lista_detalle_editar_codigo_articulo);
            actualizar=itemView.findViewById(R.id._item_list_detalle_editar_btn_actualizar);
            eliminar=itemView.findViewById(R.id._item_list_detalle_editar_btn_eliminar);
            agregar=itemView.findViewById(R.id._item_list_detalle_editar_btn_agregar);
        }

    }
}
