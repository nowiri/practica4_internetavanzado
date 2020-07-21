package services;

import encapsulations.Producto;

public class ProductoServices extends DBManage<Producto>{

    private static ProductoServices instancia;

    private ProductoServices() { super(Producto.class); }

    public static ProductoServices getInstancia(){
        if(instancia==null){
            instancia = new ProductoServices();
        }

        return instancia;
    }

    /**\
     * METHODS FOR THIS CLASS
     */
}
