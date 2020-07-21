package encapsulations;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Venta implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String fechaCompra;
    private String nombreCliente;

    @OneToMany (fetch = FetchType.EAGER)
    private List<DetalleProducto> listaProductos;

    private float total;

    public Venta(){

    }

    public Venta(String fechaCompra, String nombreCliente, List<DetalleProducto> listaProductos) {
        this.fechaCompra = fechaCompra;
        this.nombreCliente = nombreCliente;
        this.listaProductos = listaProductos;
        this.total = calcularTotal();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(String fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public List<DetalleProducto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(List<DetalleProducto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    private float calcularTotal(){
        float sum=0;
        float aux;
        for (DetalleProducto p: listaProductos) {
            aux = p.getCantidad()*p.getProducto().getPrecio();
            sum += aux;
        }
        return sum;
    }
}
