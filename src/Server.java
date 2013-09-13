package j3dwc;

import java.net. * ;
import java.io. * ;
import java.util. * ;
import java.util.zip.Deflater;


public class Server {
    Vector < Integer > disponible;
    Vector < ChatHandler > handlers;
    Vector < Elemento > elementos;
    //funcion que inicializa todos los vectores del servidor
    public void inicializar() {
        int maximos = 256; //usuarios maximos
        disponible = new Vector < Integer > (maximos); //vector de identificadores disponibles
        handlers = new Vector < ChatHandler > (maximos); //vector de hebras por cada usuario
        elementos = new Vector < Elemento > (); //vector de elementos en el servidor
        for (int i = 0; i < maximos; i++) { //rellena el vector de identificadores
            disponible.addElement(i);
        }
        generaElementos(1); //numero de elementos a generar al iniciar el servidor
    }

    //funcion que agrega al final de la lista de identificadores un identificador libre
    public void reutilizar(int aid) {
        disponible.add(aid);
    }

    //servidor, resibe, procesa y envia datos
    public Server(int port) throws IOException {
        inicializar(); //inicializa los vectores
        Fisica f = new Fisica(handlers, elementos, this); //hebra que se encarga del proceso
        f.start(); //inicializa la hebra
        ServerSocket server = new ServerSocket(port); //crea un socket en el puerto indicado
        System.out.println("Servidor iniciado en " + server.getLocalSocketAddress());
        while (true) { //bucle infinito a la espera de nuevos clientes
            if (disponible.size() > 0) { //comprueba si quedan identificadores disponibles
                Socket client = server.accept(); //acepta la peticion de coneccion de un cliente
                int id = disponible.get(0); //asigna un identificador
                System.out.println(" Nc:" + id + " " + client.getInetAddress());
                ChatHandler c = new ChatHandler(client, id, this, handlers, elementos); //hebra que mantiene la comunicacion con el cliente
                disponible.remove(0); //quita el identificador del vector de identificadores
                c.start(); //inicia la hebra
            } else {
                // System.out.print("servidor lleno");
            }
        }
    }

    //genera Objetos en el servidor controlados por el servidor
    void generaElementos(int h) {
        int radio = 15;
        String m = "";
        for (int i = 0; i < h; i++) {
            if (disponible.size() > 0) { //cada elemento tiene un identificador unico
                int id = disponible.get(0); //se le asigna un identificador a cada elemento
                disponible.remove(0);
                float x = (float)(Math.random() * radio); //los paramentros de posicion y tamaño son aleatorios
                if (Math.random() > 0.5) {
                    x = x * -1;
                }
                float y = 1 + (float)(Math.random() * radio);
                float z = (float)(Math.random() * radio);
                if (Math.random() > 0.5) {
                    z = z * -1;
                }
                float t = 0.10f + (float)(Math.random() * 0.39); //los elementos generados por el sercidor son mas pequeños
                float cx = (float)(Math.random()); //el color es otro parametro aleatorio
                float cy = (float)(Math.random());
                float cz = (float)(Math.random());
                Elemento elemento = new Elemento(id, x, y, z, cx, cy, cz, t, 0); //el ultimo parametro en 0 indica que esta controlado por el servidor
                m += elemento.getUpdate(1) + "/"; //se concatenan los mensajes de elementos nuevos
                elementos.add(elemento); //se agrega el elemento al vector de elementos
            }
        }
        if (handlers.size() > 0) { //si hay usuarios conectados
            broadcast(m); //avisa a todos los usuarios de elementos nuevos creados en el servidor
        }
    }
    public String comprimir(String message) {
        byte[] input = null; //vector de bytes para comprimir
        try {
            input = message.getBytes("UTF-8"); //pasa de caracteres a una cadena de bytes para comprimir
        } catch (UnsupportedEncodingException ex) {
            System.out.print("imposible pasar a UTF-8");
        }
        byte[] output = new byte[Math.max(40000, input.length)]; //vector donde se optienen los bytes comprimidos
        Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION); //compresion de la informacion
        compresser.setInput(input);
        compresser.finish(); //finalizar la informacion
        int compressedDataLength = compresser.deflate(output);
        byte[] out = new byte[compressedDataLength];
        System.arraycopy(output, 0, out, 0, compressedDataLength); //vector de bytes con el tamaño justo a enviar
        String base64men = new sun.misc.BASE64Encoder().encode(out); //mensaje en base64 listo para enviar
        return base64men;
    }

    //elimina los elementos que siguen al usuario que lo pida
    void EliminaElementos(int id) {
        for (int k = 0; k < elementos.size(); k++) {
            Elemento e = elementos.get(k);
            if (e.controlado == 0 && e.siguiendo == id) {
                e.eliminar = 1;
            }
        }
    }

    //funcion que envia mensajes a todos los usuarios conectados
    protected int broadcast(String message) {
        String comprimido = comprimir(message);
        int enviado = 0;
        Enumeration e = handlers.elements();
        while (e.hasMoreElements()) {
            ChatHandler c = (ChatHandler) e.nextElement();
            enviado = c.unicast(comprimido);
        }
        return enviado;
    }

    //funcion que comprueba si esta instalado Java3D
    public static boolean hasJ3D() {
        try {
            Class.forName("com.sun.j3d.utils.universe.SimpleUniverse");
            return true;
        }
        catch (ClassNotFoundException e) {
            System.err.println("Java 3D no instalado");
            return false;
        }

    }
    //Servidor, tiene como parametro el puerto en el que escucha
    public static void main(String args[]) throws IOException {
        if (hasJ3D()) { //hace falta java3D para las librerias de vector3f que usa el servidor
            if (args.length != 1) {
                throw new RuntimeException("indique el parametro puerto");
            } else {
                new Server(Integer.parseInt(args[0]));
            }
        }
    }
}