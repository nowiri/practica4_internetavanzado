package encapsulations;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Comentario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToOne (fetch = FetchType.EAGER)
    private Usuario usuario;
    @ManyToOne (fetch = FetchType.EAGER)
    private Producto producto;
    private String texto;

    public Comentario(){}

    public Comentario(Usuario usuario, Producto producto, String texto) {
        this.usuario = usuario;
        this.producto = producto;
        this.texto = texto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
