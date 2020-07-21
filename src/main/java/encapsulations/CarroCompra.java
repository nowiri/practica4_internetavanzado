package encapsulations;

import java.util.ArrayList;

public class CarroCompra {

    private String id;
    private ArrayList<DetalleProducto> listaProductos;

    public CarroCompra(String id) {
        this.id = id;
        this.listaProductos = new ArrayList<DetalleProducto>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<DetalleProducto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(ArrayList<DetalleProducto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    public void addProducto(DetalleProducto dp){listaProductos.add(dp);}

    public void eliminarProducto(DetalleProducto dp){
        listaProductos.remove(dp);
    }

    public int cartSize(){
        int size = 0;
        for (DetalleProducto dp : listaProductos) {
            size += dp.getCantidad();
        }
        return size;
    }

    public DetalleProducto buscarProducto(int id){
        DetalleProducto tmp = null;
        for (DetalleProducto dp:listaProductos) {
            if(dp.getProducto().getId() == id){
                tmp = dp;
                break;
            }
        }
        return tmp;
    }
}
