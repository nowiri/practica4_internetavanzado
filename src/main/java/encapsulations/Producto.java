package encapsulations;

import com.sun.istack.NotNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Producto implements Serializable {

    @Id
    @Column(name = "prod_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @NotNull
    String nombre;
    @NotNull
    float precio;
    String descripcion;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "producto", fetch = FetchType.EAGER)
    @Lob
    private List<Foto> fotos = new ArrayList<>();


    public Producto(){
    }

    public Producto(String nombre, float precio,String descripcion) {
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
    }

    public Producto(int id, String nombre, float precio, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Float getPrecio() {
        return precio;
    }

    public void setPrecio(Float precio) {
        this.precio = precio;
    }

    public List<Foto> getFotos() {
        return fotos;
    }

    public void setFotos(List<Foto> fotos) {
        this.fotos = fotos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
