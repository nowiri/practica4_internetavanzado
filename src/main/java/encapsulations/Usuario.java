package encapsulations;

import com.sun.istack.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class Usuario implements Serializable {

    @Id
    private String user;
    private String name;
    @NotNull
    private String passw;

    public Usuario() {}

    public Usuario(String user, String name, String passw) {
        this.user = user;
        this.name = name;
        this.passw = passw;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassw() {
        return passw;
    }

    public void setPassw(String passw) {
        this.passw = passw;
    }
}
