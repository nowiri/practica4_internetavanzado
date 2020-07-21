package services;

import encapsulations.Venta;

public class VentaServices extends DBManage<Venta>{

    private static VentaServices instancia;

    private VentaServices() { super(Venta.class); }

    public static VentaServices getInstancia(){
        if(instancia==null){
            instancia = new VentaServices();
        }

        return instancia;
    }

    /**\
     * METHODS FOR THIS CLASS
     */
}
