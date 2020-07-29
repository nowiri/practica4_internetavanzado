package services;

import encapsulations.Comentario;

public class ComentarioServices extends DBManage<Comentario>{

    private static ComentarioServices instancia;

    private ComentarioServices() { super(Comentario.class); }

    public static ComentarioServices getInstancia(){
        if(instancia==null){
            instancia = new ComentarioServices();
        }

        return instancia;
    }

    /**
     * METHODS FOR THIS CLASS
     */



}
