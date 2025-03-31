package dad.practicafinal.interfaz;

public class Menu {

    public void menuAdmin()
    {
        System.out.println("\n***MENU ADMIN***");
        System.out.println("1. Agregar usuario.");
        System.out.println("2. Modificar contraseña de usuario.");
        System.out.println("3. Modificar rol de usuario.");
        System.out.println("4. Eliminar usuario.");
        System.out.println("5. Listar un usuario.");
        System.out.println("6. Crear configuración FTP.");
        System.out.println("7. Gestión FTP.");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    public void menuUsuario()
    {
        System.out.println("\n***MENU USUARIO***");
        System.out.println("1. Crear configuración FTP.");
        System.out.println("2. Gestión FTP.");
        System.out.println("0. Salir.");
        System.out.print("Seleccione una opción: ");
    }

    public void menuFTP()
    {
        System.out.println("\n***MENU FTP***");
        System.out.println("1. Listar archivos.");
        System.out.println("2. Subir archivo.");
        System.out.println("3. Descargar archivo.");
        System.out.println("4. Eliminar archivos de un directorio.");
        System.out.println("5. Mover archivo.");
        System.out.println("6. Crear carpeta.");
        System.out.println("7. Borrar carpeta.");
        System.out.println("8. Renombrar Carpeta/Archivo.");
        System.out.println("9. Moverse de directorio.");
        System.out.println("10. Moverse al directorio principal.");
        System.out.println("11. Mostrar Archivos de un directorio.");
        System.out.println("0. Desconectar y salir.");
        System.out.print("Seleccione una opción: ");
    }

}
