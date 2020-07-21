package services;

import encapsulations.DetalleProducto;

public class DetalleProductoServices extends DBManage<DetalleProducto>{

    private static DetalleProductoServices instancia;

    private DetalleProductoServices() { super(DetalleProducto.class); }

    public static DetalleProductoServices getInstancia(){
        if(instancia==null){
            instancia = new DetalleProductoServices();
        }

        return instancia;
    }

    /**\
     * METHODS FOR THIS CLASS
     */
}
