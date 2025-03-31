package dad.practicafinal.services;

import java.io.*;
import java.util.*;

import dad.practicafinal.model.*;

public class UserService {
    protected static Hashtable<String, Usuario> usuarios = new Hashtable<String, Usuario>();

    //Método para obtener el usuario
    public static Usuario obtenerUsuario(String nombreUsuario) {
        // Verificar si el usuario existe en el Hashtable
        if (usuarios.containsKey(nombreUsuario)) {
            return usuarios.get(nombreUsuario);
        } else {
            System.out.println("Usuario no encontrado: " + nombreUsuario);
            return null;
        }
    }

    // Método para cargar usuarios desde un archivo
    public static void cargarUsuarios() {
    	String archivo = "Recursos/Usuarios.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("-");
                boolean esAdmin = Boolean.parseBoolean(partes[2]); // Obtenemos el rol del usuario
                Usuario usuario = new Usuario(partes[0], partes[1], esAdmin); // Creamos el objeto Usuario
                usuarios.put(partes[0], usuario); // Guardamos el objeto Usuario en el mapa
            }
            System.out.println("Usuarios cargados correctamente desde " + archivo);
        } catch (IOException e) {
            System.err.println("Error al cargar el archivo de usuarios: " + e.getMessage());
        }
    }

    public boolean verificarCredenciales(String usuario, String password) {
        if (usuarios.containsKey(usuario)) {
            Usuario user = usuarios.get(usuario);
            return user.getPassword().equals(password);
        }
        return false; // El usuario no existe o la contraseña no coincide
    }

    public boolean verificarRol(String usuario) {
        Usuario user = usuarios.get(usuario);
        if(user.isAdmin())
        {
            return true; // Devuelve true si es admin
        }
        else
        {
            return false; // Devuelve false si no es admin
        }
    }

    public String agregarUsuario(String usuario, String password, String admin)
    {
        if(!usuarios.containsKey(usuario))
        {
            Usuario u = new Usuario(usuario, password, Boolean.parseBoolean(admin));
            usuarios.put(usuario, u);
            return "Usuario creado con exito.";
        }
        else
        {
            return "El usuario "+usuario+" ya se encuentra en el sistema";
        }
    }

    public String eliminarUsuario(String usuario)
    {
        if(usuarios.containsKey(usuario))
        {
            usuarios.remove(usuario);
            return "Usuario eliminado con exito.";
        }
        else
        {
            return "Usuario: "+usuario+" no se encuentra en el sistema.";
        }
    }

    public String modificarContra(String usuario, String nuevaContraseña)
    {
        if(usuarios.containsKey(usuario))
        {
            usuarios.get(usuario).setPassword(nuevaContraseña);
            return "Usuario modificado con exito.";
        }
        else
        {
            return "Usuario no encontrado.";
        }
    }

    public String modificarRol(String usuario)
    {
        if(usuarios.containsKey(usuario))
        {
            if(usuarios.get(usuario).isAdmin()==true)
            {
                usuarios.get(usuario).setAdmin(false);
            }
            else
            {
                usuarios.get(usuario).setAdmin(false);
            }

            return "Usuario modificado con exito.";
        }
        else
        {
            return "Usuario no encontrado.";
        }
    }

    public String mostrarUsuario(String usuario)
    {
        if(usuarios.containsKey(usuario))
        {
            String rol = usuarios.get(usuario).isAdmin() ? " Administrador." : " Usuario sin permisos.";
            return "Usuario: "+usuarios.get(usuario).getUsuario()+" Contraseña: "+usuarios.get(usuario).getPassword()+" Rol:"+rol;
        }
        else
        {
            return "Usuario: "+usuario+" no se encuentra en el sistema.";
        }
    }

    public static void guardarUsuarios() {
    	String archivo = "Recursos/Usuarios.txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            for (Usuario usuario : usuarios.values()) {
                // Escribir cada usuario en el archivo en el formato: usuario-contraseña-admin
                bw.write(usuario.getUsuario() + "-" + usuario.getPassword() + "-" + usuario.isAdmin());
                bw.newLine(); // Añadir un salto de línea después de cada usuario
            }
            System.out.println("Usuarios guardados correctamente en " + archivo);
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo de usuarios: " + e.getMessage());
        }
    }


    //MÉTODOS DE LAS CONFIGURACIONES
    // Método para guardar las configuraciones en el archivo
 // Método para guardar las configuraciones en el archivo en el formato esperado
    public static void guardarConfiguraciones() {
        String archivo = "Recursos/ConfiguracionesFTP.txt";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            for (Map.Entry<String, Usuario> entry : usuarios.entrySet()) {
                String nombreUsuario = entry.getKey();
                Usuario usuario = entry.getValue();

                // Iterar sobre las configuraciones del usuario
                for (ConfiguracionFTP config : usuario.getConfiguraciones()) {
                    // Crear la línea con el formato: usuario-ip-puerto-usuarioFTP-contraseñaFTP
                    String linea = nombreUsuario + "-"+ config.getIp() + "-"+ config.getPuerto() + "-"+ config.getUsuarioFTP() + "-"+ config.getContrasenaFTP();
                    bw.write(linea);
                    bw.newLine(); // Añadir un salto de línea después de cada configuración
                }
            }
            System.out.println("Configuraciones guardadas correctamente en " + archivo);
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo de configuraciones FTP: " + e.getMessage());
        }
    }


    // Métodos de configuración FTP
    public static void agregarConfig(String usuario, String direccionIP, String puerto, String usuarioFTP, String contrasenaFTP) {
        if (usuarios.containsKey(usuario)) {
            Usuario user = usuarios.get(usuario); // Obtén el usuario
            ConfiguracionFTP config = new ConfiguracionFTP(direccionIP, Integer.parseInt(puerto), usuarioFTP, contrasenaFTP);
            user.getConfiguraciones().add(config); // Añade la configuración al ArrayList del usuario
        } else {
            System.out.println("Usuario no encontrado: " + usuario);
        }
    }

    public static ConfiguracionFTP elegirConfiguracion(Scanner sc, String usuarioConectar)
    {
        // Obtener el usuario desde el Hashtable de usuarios
        Usuario usuario = obtenerUsuario(usuarioConectar);

        // Obtener la lista de configuraciones del usuario
        List<ConfiguracionFTP> configs = usuario.getConfiguraciones();

        if (configs.isEmpty()) {
            System.out.println("No se encontró una configuración para este usuario.");
            return null;
        }

        if (configs.size() != 1) {
            System.out.println("Listado de configuraciones de " + usuarioConectar + ":");
            for (int i = 0; i < configs.size(); i++) {
                System.out.println((i + 1) + ". " + configs.get(i));
            }

            System.out.print("Seleccione una configuración para conectarse: ");
            try {
                int opcion = Integer.parseInt(sc.nextLine()) - 1;

                if (opcion < 0 || opcion >= configs.size()) {
                    System.out.println("Selección inválida.");
                    return null;
                }

                return configs.get(opcion);
            } catch (NumberFormatException e) {
                System.out.println("Por favor, introduzca un número válido.");
                return null;
            }
        } else {
            // Si solo hay una configuración, la devuelve directamente
            return configs.get(0);
        }
    }

    // Método para cargar las configuraciones desde el archivo
    public static void cargarConfiguraciones() {
    	String archivo = "Recursos/ConfiguracionesFTP.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("-");
                String usuario = partes[0]; // Extraer el nombre del usuario
                String ip = partes[1];
                String puerto = partes[2];
                String usuarioFTP = partes[3];
                String contrasenaFTP = partes[4];
                agregarConfig(usuario, ip, puerto, usuarioFTP, contrasenaFTP);
            }
            System.out.println("Configuraciones FTP cargadas correctamente desde " + archivo + "\n");
        } catch (IOException e) {
            System.err.println("Error al cargar el archivo de configuraciones FTP: " + e.getMessage());
        }
    }

}
