package dad.practicafinal.services;

import java.io.*;
import java.net.*;

import dad.practicafinal.model.*;

public class FTPService {
    Socket ftpSocket;
    private BufferedReader entrada;
    private PrintWriter salida;


    public void leerIntro() throws IOException
    {
        String linea;
        do {
            linea = entrada.readLine();
        } while (linea != null && (linea.length() < 4 || linea.charAt(3) == '-')); // Continuar si es parte de una línea multilínea (código-)
    }

    public void enviarComando(String comando) throws IOException
    {
        salida.println(comando);
        salida.flush();
    }

    public String leerRespuesta() throws IOException {
        return entrada.readLine();
    }


    public Socket modoPasivo() throws IOException {
        // Solicitar modo pasivo al servidor
        enviarComando("PASV");
        String respuesta = leerRespuesta();
        if (!respuesta.startsWith("227")) {
            System.err.println("Error al entrar en modo pasivo: " + respuesta);
            throw new IOException("Error al entrar en modo pasivo: " + respuesta);
        }

        // Utilizar la función extraerIPyPuerto para obtener la IP y el puerto
        String[] ipYPuerto = extraerIPyPuerto(respuesta);
        String ip = ipYPuerto[0];
        int puerto = Integer.parseInt(ipYPuerto[1]);

        // Establecer la conexión de datos con la IP y el puerto
        return new Socket(ip, puerto);
    }

    public void conectar(ConfiguracionFTP config) throws IOException {
        ftpSocket = new Socket(config.getIp() , config.getPuerto());
        entrada = new BufferedReader(new InputStreamReader(ftpSocket.getInputStream()));
        salida = new PrintWriter(new OutputStreamWriter(ftpSocket.getOutputStream()));
        leerIntro();

        enviarComando("USER "+config.getUsuarioFTP());
        if(!(entrada.readLine()).startsWith("331"))
        {
            throw  new IOException("Error: "+entrada.readLine());
        }

        enviarComando("PASS "+config.getContrasenaFTP());
        if(!(entrada.readLine()).startsWith("230"))
        {
            throw  new IOException("Error: "+entrada.readLine());
        }
        System.out.println("Conexión realizada con éxito.");
    }


    //Función para listar archivos
    public void listarArchivos() {
        try {
            // Conectar al canal de datos
            try (Socket dataSocket = modoPasivo();
                 BufferedReader dataBr = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()))) {

                // Enviar comando LIST
                enviarComando("NLST");
                String respuesta = leerRespuesta();
                if (!respuesta.startsWith("150")) {
                    System.out.println("Error al listar archivos: " + respuesta);
                    return;
                }

                // Leer y mostrar la lista de archivos desde el canal de datos
                String archivo;
                while ((archivo = dataBr.readLine()) != null) {
                    System.out.println(archivo);
                }

                // Leer respuesta final del canal de control
                respuesta = leerRespuesta();
                if (!respuesta.startsWith("226")) {
                    System.out.println("Error finalizando listado: " + respuesta);
                } else {
                    System.out.println("Listado completado exitosamente.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error al listar archivos: " + e.getMessage());
        }
    }



    // Método auxiliar para extraer IP y puerto de la respuesta PASV
    private String[] extraerIPyPuerto(String respuestaPASV) {
        // La respuesta PASV
        int inicio = respuestaPASV.indexOf('('); //Para encontrar el inicio de la respuestaPASV
        int fin = respuestaPASV.indexOf(')');//Para encontrar el fin de la respuestaPASV
        String datos = respuestaPASV.substring(inicio + 1, fin);//Extraer la cadena del paréntesis
        String[] partes = datos.split(","); // Dividir con una coma
        String ip = partes[0] + "." + partes[1] + "." + partes[2] + "." + partes[3]; //Formamos la ip
        int puerto = Integer.parseInt(partes[4]) * 256 + Integer.parseInt(partes[5]);
        return new String[]{ip, String.valueOf(puerto)}; //Devolución de un array con la ip y el puerto
    }


    //Método para subir un archivo al servidor
    public void subirArchivo(String rutaLocal, String rutaRemota)
    {
        try {
            // Establecer la conexión de datos con la IP y el puerto
            Socket socketDatos = modoPasivo();

            // Enviar el comando STOR con la ruta remota al servidor
            enviarComando("STOR " + rutaRemota);
            String respuesta = leerRespuesta();
            if (!respuesta.startsWith("150")) {
                System.err.println("Error al preparar la transferencia: " + respuesta);
                socketDatos.close();
                return;
            }

            // Leer el archivo local y enviarlo a través de la conexión de datos
            try (FileInputStream fis = new FileInputStream(rutaLocal);
                 BufferedOutputStream bos = new BufferedOutputStream(socketDatos.getOutputStream())) {

                byte[] buffer = new byte[4096];
                int bytesLeidos;
                while ((bytesLeidos = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesLeidos);
                }
            } catch (IOException e) {
                System.err.println("Error al leer o enviar el archivo: " + e.getMessage());
            } finally {
                // Cerrar la conexión de datos
                socketDatos.close();
            }

            // Leer la respuesta final del servidor para confirmar el éxito
            respuesta = leerRespuesta();
            if (!respuesta.startsWith("226")) {
                System.err.println("Error durante la transferencia: " + respuesta);
            } else {
                System.out.println("Archivo subido exitosamente a " + rutaRemota);
            }

        } catch (IOException e) {
            System.err.println("Error al subir el archivo: " + e.getMessage());
        }
    }


    //Método para descargar archivo
    public void descargarArchivo(String rutaLocal, String rutaRemota)
    {
        try {

            // Establecer la conexión de datos con la IP y el puerto
            Socket socketDatos = modoPasivo();

            // Enviar el comando RETR con la ruta remota al servidor
            enviarComando("RETR " + rutaRemota);
            String respuesta = leerRespuesta();
            if (!respuesta.startsWith("150")) {
                System.err.println("Error al preparar la transferencia: " + respuesta);
                socketDatos.close();
                return;
            }

            // Leer los datos del servidor y escribirlos en el archivo local
            try (BufferedInputStream bis = new BufferedInputStream(socketDatos.getInputStream());
                 FileOutputStream fos = new FileOutputStream(rutaLocal)) {

                byte[] buffer = new byte[4096];
                int bytesLeidos;
                while ((bytesLeidos = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesLeidos);
                }
            } catch (IOException e) {
                System.err.println("Error al recibir o guardar el archivo: " + e.getMessage());
            } finally {
                // Cerrar la conexión de datos
                socketDatos.close();
            }

            // Leer la respuesta final del servidor para confirmar el éxito
            respuesta = leerRespuesta();
            if (!respuesta.startsWith("226")) {
                System.err.println("Error durante la transferencia: " + respuesta);
            } else {
                System.out.println("Archivo descargado exitosamente a " + rutaLocal);
            }

        } catch (IOException e) {
            System.err.println("Error al descargar el archivo: " + e.getMessage());
        }
    }

    //Método para crear carpeta  en el servidor
    public void crearCarpeta(String rutaRemota)
    {
        try {
            // Enviar el comando MKD con la ruta remota al servidor
            enviarComando("MKD " + rutaRemota);
            String respuesta = leerRespuesta();

            // Verificar la respuesta del servidor FTP
            if (respuesta.startsWith("257")) {
                System.out.println("Carpeta creada exitosamente en: " + rutaRemota);
            } else {
                // Interpretar y mostrar un mensaje de error detallado basado en la respuesta FTP
                System.err.println("Error al crear la carpeta. Respuesta del servidor: " + respuesta);
                if (respuesta.startsWith("450")) {
                    System.err.println("Error de carpeta temporal o acceso denegado.");
                } else if (respuesta.startsWith("550")) {
                    System.err.println("Error: No se puede acceder a la ruta especificada.");
                } else if (respuesta.startsWith("553")) {
                    System.err.println("Error: No se permite la creación de directorios aquí.");
                } else {
                    System.err.println("Error desconocido.");
                }
            }

        } catch (IOException e) {
            System.err.println("Error al crear la carpeta: " + e.getMessage());
        }
    }

    //Método para eliminar carpeta  en el servidor
    public void eliminarCarpeta(String rutaRemota)
    {
        try {
            // Verificar si hay archivos en la carpeta antes de proceder
            if (hayArchivosEnCarpeta(rutaRemota)) {
                System.out.println("La carpeta contiene archivos, eliminándolos antes...");
                eliminarArchivos(rutaRemota);
            }

            // Ahora intentar eliminar la carpeta
            enviarComando("RMD " + rutaRemota);
            String respuesta = leerRespuesta();

            // Verificar si la carpeta se eliminó correctamente
            if (!respuesta.startsWith("250")) {
                System.err.println("Error al eliminar la carpeta: " + respuesta);
            } else {
                System.out.println("Carpeta eliminada exitosamente: " + rutaRemota);
            }

        } catch (IOException e) {
            System.err.println("Error al eliminar la carpeta: " + e.getMessage());
        }
    }

    // Método para verificar si hay archivos en la carpeta
    public boolean hayArchivosEnCarpeta(String rutaRemota) {
        try {
            // Conectar al canal de datos para lista de archivos
            Socket dataSocket = modoPasivo();
            BufferedReader dataBr = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

            // Enviar comando NLST para listar archivos
            enviarComando("NLST " + rutaRemota);
            String respuesta = leerRespuesta();
            if (!respuesta.startsWith("150")) {
                System.out.println("Error al listar archivos en la carpeta: " + respuesta);
                dataSocket.close();
                return false;
            }

            // Leer la lista de archivos
            String archivo;
            boolean hayArchivos = false;
            while ((archivo = dataBr.readLine()) != null) {
                hayArchivos = true;
                System.out.println();
                System.out.println(archivo); // Mostrar los archivos encontrados
            }

            // Leer respuesta final del canal de control
            respuesta = leerRespuesta();
            if (!respuesta.startsWith("226")) {
                System.out.println("Error finalizando listado: " + respuesta);
            }

            // Cerrar el socket de datos
            dataSocket.close();
            return hayArchivos;

        } catch (IOException e) {
            System.out.println("Error al listar archivos en la carpeta: " + e.getMessage());
            return false;
        }
    }


    //Método para renombrar un elemento en el servidor
    public void renombrarElemento(String rutaOriginal , String nuevaRuta)
    {
        try {
            // Enviar el comando RNFR para especificar el archivo o directorio a renombrar
            enviarComando("RNFR " + rutaOriginal);
            String respuestaRNFR = leerRespuesta();

            if (!respuestaRNFR.startsWith("350")) {
                System.err.println("No se puede preparar la renombración: " + respuestaRNFR);
                return;
            }

            // Enviar el comando RNTO para especificar el nuevo nombre
            enviarComando("RNTO " + nuevaRuta);
            String respuestaRNTO = leerRespuesta();

            if (respuestaRNTO == null) {
                System.err.println("Error: respuesta del servidor es nula.");
            } else if (!respuestaRNTO.startsWith("250")) {
                System.err.println("Error al renombrar el elemento: " + respuestaRNTO);
            } else {
                System.out.println("Elemento con nombre "+rutaOriginal+" renombrado exitosamente a " + nuevaRuta);
            }

        } catch (IOException e) {
            System.err.println("Error al renombrar el elemento: " + e.getMessage());
        }
    }

    //Método para cambiar de directorio
    public void cambiarDirectorio(String nuevaRuta) throws IOException
    {

        enviarComando("CWD "+ nuevaRuta);// Enviar comando FTP para cambiar de directorio
        String respuesta = leerRespuesta();// Leer respuesta del servidor
        if (respuesta.startsWith("250")) {
            System.out.println("Cambio de directorio exitoso a: " + nuevaRuta);
        } else {
            System.err.println("Error al cambiar de directorio. \nRespuesta del servidor: " + respuesta);
        }
    }

    public void obtenerDirectorio() throws IOException
    {
        enviarComando("PWD");// Enviar comando FTP para obtener el directorio actual
        String respuesta = leerRespuesta();// Leer respuesta del servidor
        if (respuesta.startsWith("257")) {
            // Extraer y mostrar el directorio actual desde la respuesta del servidor
            int inicio = respuesta.indexOf('"');
            int fin = respuesta.lastIndexOf('"');
            if (inicio != -1 && fin != -1) {
                String directorioActual = respuesta.substring(inicio + 1, fin);
                System.out.println("\nDirectorio actual: " + directorioActual);
            } else {
                System.err.println("No se pudo obtener el directorio actual.");
            }
        } else {
            System.err.println("Error al obtener el directorio actual. Respuesta del servidor: " + respuesta);
        }
    }

    public void moverFichero(String archivoOrigen, String carpetaDestino) throws IOException {
        // Enviar comando RNFR (Rename From)
        enviarComando("RNFR " + archivoOrigen);
        String respuesta = leerRespuesta();
        if (!respuesta.startsWith("350")) { // 350 es la respuesta estándar para RNFR aceptado
            System.out.println("Error al seleccionar el archivo para mover: " + respuesta);
            return;
        }

        // Enviar comando RNTO (Rename To)
        String nuevoNombre = carpetaDestino + "/" + new File(archivoOrigen).getName();
        enviarComando("RNTO " + nuevoNombre);
        respuesta = leerRespuesta();
        if (respuesta.startsWith("250")) { // 250 significa éxito en RNTO
            System.out.println("Archivo movido exitosamente a " + carpetaDestino);
        } else {
            System.out.println("Error al mover el archivo: " + respuesta);
        }
    }


    //Método desconectar al usuario
    public void desconectar() throws IOException
    {
        enviarComando("QUIT");
        String respuesta = leerRespuesta();
        if (respuesta.startsWith("221")) {
            System.out.println("Desconexión del cliente exitosa.");
        } else {
            System.err.println("Error al desconectar al cliente. Respuesta del servidor: " + respuesta);
        }

    }

    //Método desconectar al usuario
    public void moverDirectorioPrincipal() throws IOException
    {
        enviarComando("CDUP");
        String respuesta = leerRespuesta();
        if (!respuesta.startsWith("250")) {
            System.err.println("Error al mover al cliente. Respuesta del servidor: " + respuesta);
        }

        System.out.println("Cambio al directorio principal exitoso.");
    }

    //Método para eliminar todos los archivos en una carpeta remota
    public void eliminarArchivos(String rutaCarpeta) {
        try {
            // Conectar al canal de datos
            try (Socket dataSocket = modoPasivo();
                 BufferedReader dataBr = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()))) {

                // Enviar el comando LIST para obtener una lista de archivos en la carpeta
                enviarComando("LIST " + rutaCarpeta);
                String respuesta = leerRespuesta();
                if (!respuesta.startsWith("150")) {
                    System.out.println("Error al listar archivos en la carpeta: " + respuesta);
                    return;
                }

                // Leer y eliminar cada archivo desde el canal de datos
                String archivo;
                while ((archivo = dataBr.readLine()) != null) {
                    String nombreArchivo = archivo.split(" ")[8]; // Obtener nombre del archivo
                    enviarComando("DELE " + rutaCarpeta + "/" + nombreArchivo);
                    respuesta = leerRespuesta();
                }

                // Leer respuesta final del canal de control
                respuesta = leerRespuesta();
                if (!respuesta.startsWith("250")) {
                    System.out.println("Error finalizando eliminación de archivos: " + respuesta);
                } else {
                    System.out.println("Todos los archivos han sido eliminados exitosamente.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error al eliminar archivos en la carpeta: " + e.getMessage());
        }
    }
}

