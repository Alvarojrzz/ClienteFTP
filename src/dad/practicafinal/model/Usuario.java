package dad.practicafinal.model;


import java.util.*;

public class Usuario {
    private String usuario;
    private String password;
    private boolean admin; // True si es administrador, false si es cliente
    private List<ConfiguracionFTP> configuraciones;

    public Usuario(String usuario, String password, boolean admin) {
        this.usuario = usuario;
        this.password = password;
        this.admin = admin;
        this.configuraciones = new ArrayList<ConfiguracionFTP>();
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public List<ConfiguracionFTP> getConfiguraciones() {
        return configuraciones;
    }

    public void setConfiguraciones(List<ConfiguracionFTP> configuraciones) {
        this.configuraciones = configuraciones;
    }

    @Override
    public String toString() {
        String rol = admin ? "Sí" : "No";
        return "Usuario [usuario=" + usuario + ", password=" + password + ", ¿Es administrador ?" + rol + ", configuraciones="
                + configuraciones + "]";
    }
}



