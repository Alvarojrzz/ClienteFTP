package dad.practicafinal.main;

import dad.practicafinal.model.*;
import dad.practicafinal.services.*;
import dad.practicafinal.interfaz.*;

import java.io.*;
import java.util.*;


public class Cliente {

    private UserService userService;
    private FTPService ftpService;
    private Boolean continuar;

    public static void main(String[] args) throws IOException {
        Cliente cliente = new Cliente();
        cliente.ejecutar();
    }

    public void ejecutar() throws IOException {
        UserService.cargarUsuarios();
        UserService.cargarConfiguraciones();
        ftpService = new FTPService();
        userService = new UserService();
        Scanner sc = new Scanner(System.in);

        System.out.println("***INICIA SESIÓN***");
        System.out.print("Introduzca el usuario: ");
        String usuario = sc.nextLine();

        System.out.print("Introduzca la contraseña: ");
        String password = sc.nextLine();
        if(!userService.verificarCredenciales(usuario, password))
        {
            System.out.println("Inicio de sesión erróneo.");
            return;
        }

        System.out.println("\nBienvenido, " + usuario);

        // Verificar el rol del usuario
        if (userService.verificarRol(usuario)) {
            mostrarMenuAdmin(sc,usuario);
        } else {
            mostrarMenuUsuario(sc,usuario);
        }
    }

    // Mostrar el menú para administradores
    private void mostrarMenuAdmin(Scanner sc , String usuario) throws IOException {
        String opt = "";
        Menu menu = new Menu();
        continuar = true;
        while (continuar) {
            menu.menuAdmin();
            opt = sc.nextLine();
            if(opt.equals("7"))
            {
                mostrarMenuFTP(sc , usuario);
            }
            else
            {	// Mostrar opciones admin
                procesarOpcionAdmin(opt, sc);
            }
        }
        UserService.guardarUsuarios();
        UserService.guardarConfiguraciones();
    }

    // Mostrar el menú para usuarios sin permisos
    private void mostrarMenuUsuario(Scanner sc , String usuario) throws IOException {
        String opt = "";
        Menu menu = new Menu();
        continuar = true;
        while (continuar) {
            menu.menuUsuario();
            opt = sc.nextLine();
            if(opt.equals("2"))
            {
                mostrarMenuFTP(sc , usuario);
            }
            else
            {
                procesarOpcionUsuario(opt, sc);
            }

        }
        UserService.guardarConfiguraciones();
    }

    private void mostrarMenuFTP(Scanner sc , String usuario) throws IOException {
        String opt2 ="";
        Menu menu = new Menu();
        continuar = true;
        ConfiguracionFTP config = elegirConfiguracion(sc, usuario);
        if (config == null) {
            return; // Configuración no seleccionada o inválida
        }
        ftpService.conectar(config);
        while(continuar)
        {
            menu.menuFTP();
            opt2 = sc.nextLine();
            procesarOpcionFTP(opt2 , sc);
        }
    }

    //Función para utilizar una opción u otra
    private void procesarOpcionAdmin(String opt, Scanner sc)  {
        String resultado = "";
        switch (opt) {
            case "1": // Agregar usuario
                System.out.print("Introduzca el nombre de usuario: ");
                String nuevoUsuario = sc.nextLine();
                System.out.print("Introduzca la contraseña: ");
                String nuevaContrasena = sc.nextLine();
                System.out.print("¿Es administrador? (true/false): ");
                String esAdmin = sc.nextLine();

                resultado = userService.agregarUsuario(nuevoUsuario, nuevaContrasena, esAdmin);
                System.out.println(resultado);
                break;

            case "2": // Modificar contraseña
                System.out.print("Introduzca el nombre de usuario: ");
                String usuario = sc.nextLine();
                System.out.print("Introduzca la nueva contraseña: ");
                String nuevaPassword = sc.nextLine();

                resultado = userService.modificarContra(usuario, nuevaPassword);
                System.out.println(resultado);
                break;

            case "3": // Modificar rol
                System.out.print("Introduzca el nombre de usuario: ");
                String usuarioModificarRol = sc.nextLine();

                resultado = userService.modificarRol(usuarioModificarRol);
                System.out.println(resultado);
                break;

            case "4": // Eliminar usuario
                System.out.print("Introduzca el nombre de usuario a eliminar: ");
                String usuarioEliminar = sc.nextLine();

                resultado = userService.eliminarUsuario(usuarioEliminar);
                System.out.println(resultado);
                break;

            case "5": // Listar un usuario
                System.out.print("Introduzca el nombre de usuario: ");
                String usuarioListar = sc.nextLine();

                resultado = userService.mostrarUsuario(usuarioListar);
                System.out.println(resultado);
                break;
            case "6": // Crear configuración
                System.out.print("Introduzca la dirección IP: ");
                String ip = sc.nextLine();
                System.out.print("Introduzca el puerto: ");
                String puerto = sc.nextLine();
                System.out.print("Introduzca el usuario FTP: ");
                String usuarioFTP = sc.nextLine();
                System.out.print("Introduzca la contraseña FTP: ");
                String contrasenaFTP = sc.nextLine();

                
                UserService.agregarConfig(usuarioFTP, ip, puerto, usuarioFTP, contrasenaFTP);
                System.out.println("Configuración creada con éxito.");
                break;
            case "0"://Opción salir
                System.out.println("Saliendo del menú de administrador.");
                continuar = false;
                break;
            default:
                System.out.println("Opción no válida.");
                break;
        }
    }

    //Función para utilizar una opción u otra
    private void procesarOpcionFTP(String opt, Scanner sc) throws IOException {
        switch (opt) {
            case "1": // listar archivos
                ftpService.listarArchivos(); // Modifica el método para recibir el socket directamente
                break;
            case "2": // Subir archivo
                System.out.print("Introduzca la ruta del archivo local: ");
                String rutaArchivo = sc.nextLine();
                System.out.print("Introduzca la ruta remota: ");
                String rutaRemota = sc.nextLine();
                ftpService.subirArchivo(rutaArchivo, rutaRemota);
                break;
            case "3"://Descargar archivo
                System.out.print("Introduzca la ruta local donde se va a descargar el archivo: ");
                String rutaArchivoD = sc.nextLine();
                System.out.print("Introduzca la ruta remota del archivo: ");
                String rutaRemotaD = sc.nextLine();
                ftpService.descargarArchivo(rutaArchivoD, rutaRemotaD);
                break;
            case "4"://Eliminación de archivos en directorio
                System.out.print("Introduzca la ruta donde se desea eliminar los archivos: ");
                String rutaArchivoDel = sc.nextLine();
                ftpService.eliminarArchivos(rutaArchivoDel);
                break;
            case "5"://Mover fichero
                System.out.print("Introduzca la ruta del archivo : ");
                String rutaMov = sc.nextLine();
                System.out.print("Introduzca la ruta del directorio a mover: ");
                String rutaMovDir = sc.nextLine();
                ftpService.moverFichero(rutaMov, rutaMovDir);
                break;
            case "6": //Crear carpeta
                System.out.print("Introduzca la ruta remota donde desea crear la carpeta: ");
                String rutaRemotaC = sc.nextLine();
                ftpService.crearCarpeta(rutaRemotaC);
                break;
            case "7": //Eliminar carpeta
                System.out.print("Introduzca la ruta remota donde desea eliminar la carpeta: ");
                String rutaRemotaDel = sc.nextLine();
                ftpService.eliminarCarpeta(rutaRemotaDel);
                break;
            case "8": //Renombrar carpeta o archivo
                System.out.print("Introduzca la ruta remota donde está el elemento a renombrar: ");
                String rutaRemotaR = sc.nextLine();
                System.out.print("Introduzca el nuevo nombre del elemento: ");
                String nuevaRuta = sc.nextLine();
                ftpService.renombrarElemento(rutaRemotaR , nuevaRuta);
                break;
            case "9": // Moverse entre carpetas
                ftpService.obtenerDirectorio();
                System.out.print("Introduzca la ruta de la carpeta a la que desea moverse: ");
                String nuevaRutaM = sc.nextLine();
                ftpService.cambiarDirectorio(nuevaRutaM);
                ftpService.obtenerDirectorio();
                break;
            case "10": // Moverse al directorio principal
                ftpService.obtenerDirectorio();
                ftpService.moverDirectorioPrincipal();
                ftpService.obtenerDirectorio();
                break;
            case "11": //Mostrar archivos en una carpeta
                System.out.print("Introduca la ruta de la carpeta: ");
                String rutaGet = sc.nextLine();
                if(!ftpService.hayArchivosEnCarpeta(rutaGet))
                {
                    System.out.println("No hay archivos en carpeta.");
                }
                break;
            case "0":
                System.out.println("Saliendo del menú FTP.");
                ftpService.desconectar();
                continuar = false;
                return;
            default:
                System.out.println("Opción no válida.");
                break;
        }
    }

    private void procesarOpcionUsuario(String opt, Scanner sc)
    {
        switch (opt) {
            case "1": // Crear configuración
            	 System.out.print("Introduzca la dirección IP: ");
                 String ip = sc.nextLine();
                 System.out.print("Introduzca el puerto: ");
                 String puerto = sc.nextLine();
                 System.out.print("Introduzca el usuario FTP: ");
                 String usuarioFTP = sc.nextLine();
                 System.out.print("Introduzca la contraseña FTP: ");
                 String contrasenaFTP = sc.nextLine();
                 
                 UserService.agregarConfig(usuarioFTP, ip, puerto, usuarioFTP, contrasenaFTP);
                 System.out.println("Configuración creada con éxito.");
                break;
            case "0":
                System.out.println("Saliendo del menú FTP.");
                break;
            default:
                System.out.println("Opción no válida.");
                break;
        }
    }

    // Función para elegir configuración que la vamos a necesitar en varias funciones
    private ConfiguracionFTP elegirConfiguracion(Scanner sc, String usuarioConectar)
    {
        // Obtener el usuario desde el Hashtable de usuarios
        Usuario usuario = UserService.obtenerUsuario(usuarioConectar);

        // Obtener la lista de configuraciones del usuario
        if(usuario==null)
        {
            System.out.println("Usuario no encontrado.");
            return null;
        }

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
}
