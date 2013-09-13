package j3dwc;
import java.awt.event.KeyEvent;
import java.net. * ;
import java.io. * ;
import java.util. * ;


//hebra que mantiene las comunicaciones con el cliente
//Recibe, envía, comprime e interpreta información entre el servidor y el usuario.
public class ChatHandler extends Thread {
    protected Socket s; //Socket
    protected DataInputStream i; //Entrada de datos
    protected DataOutputStream o; //Salida de datos
    protected int id; //Identificador del cliente y hebra
    protected Server servidor; //Servidor
    protected Vector handlers; //Vector de hebras de otros clientes
    protected Vector elementos; //Vector de elementos
    protected Elemento elemento; //Elemento que representa al cliente
    protected float Af = 3f; //Incremento de fuerza que aplica el cliente
    private final static int forwardKey = KeyEvent.VK_DOWN; //Identifica las teclas
    private final static int backKey = KeyEvent.VK_UP;
    private final static int leftKey = KeyEvent.VK_LEFT;
    private final static int rightKey = KeyEvent.VK_RIGHT;
    private final static int tam = 15; //Tamaño del escenario
    //Los parámetros son:
    //El socket donde se conecta, el identificador de la hebra y cliente, el servidor, el vector de hebras y el vector de elementos.
    public ChatHandler(Socket s, int idd, Server ser, Vector h, Vector e) throws IOException {
        this.s = s;
        this.servidor = ser;
        this.handlers = h;
        this.elementos = e;
        i = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        o = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
        id = idd;
    }

    //Devuelve el identificador
    public int getid() {
        return id;
    }
    //Hebra infinita a la espera de comandos del cliente.
    @Override
    public void run() {
        //Inicialización de parámetros
        float xr = (float)(Math.random()); //Genera una posición aleatoria en X y Z
        float x = xr * tam; //Porque en Y estará siempre sobre el suelo, por tanto
        if (Math.random() > 0.5) { //Su posición Y es su radio
            x = x * -1;
        }
        float zr = (float)(Math.random());
        float z = zr * tam;
        if (Math.random() > 0.5) {
            z = z * -1;
        }
        float t = 0.5f + (float)(Math.random()); //Su tamaño puede ser de 0.5 a 1.5
        float cx = (float)(Math.random()); //El color también es aleatorio
        float cy = (float)(Math.random());
        float cz = (float)(Math.random());
        //Fin parámetros
        elemento = new Elemento(id, x, t, z, cx, cy, cz, t, 1); //Se genera un nuevo elemento
        unicast(servidor.comprimir(elemento.getUpdate(0) + "/")); //Envía los nuevos parámetros al cliente
        String men = ""; //Concatena una cadena de mensajes con los estados de los demás elementos
        for (int ec = 0; ec < elementos.size(); ec++) {
            Elemento e = (Elemento) elementos.get(ec);
            men += e.getUpdate(1) + "/";
        }
        unicast(servidor.comprimir(men)); //La lista de elementos se hace con todos menos el nuevo
        servidor.broadcast(elemento.getUpdate(1) + "/"); //Se envía a todos los conectados los datos del nuevo menos a si mismo
        synchronized(elementos) { //Se agrega el elemento al vector
            elementos.add(elemento);
        }
        synchronized(handlers) { //Agrega esta hebra al vector para que la función broadcast
            handlers.addElement(this); //Pueda enviar a todos los usuarios la misma información
        } //Se agrega después del broadcast para evitar enviar dos veces la misma información al nuevo cliente
        String msg;
        String[] comandos;
        try {
            while (true) { //Bucle infinito a la espera de comandos del cliente
                msg = i.readUTF(); //Lee el mensaje del cliente
                comandos = msg.split(":"); //Separa los comandos
                int cmd = Integer.parseInt(comandos[0]); //El primer comando puede tener varios valores
                if (cmd < 2) { //Si es 0 o 1 significa que es un mensaje del cliente que contiene una tecla
                    boolean isShiftDown = false, isAltDown = false;
                    int pr = Integer.parseInt(comandos[0]); // Indica si se ha presionado o dejado de presionar una tecla
                    int keyCode = Integer.parseInt(comandos[1]); //La tecla en cuestión
                    if (Integer.parseInt(comandos[2]) == 0) { //Indica si el shift está presionado
                        isShiftDown = false;
                    } else if (Integer.parseInt(comandos[2]) == 1) {
                        isShiftDown = true;
                    }
                    if (Integer.parseInt(comandos[3]) == 0) { //Indica si la tecla Alt está presionada
                        isAltDown = false;
                    } else if (Integer.parseInt(comandos[3]) == 1) {
                        isAltDown = true;
                    }
                    processKey(pr, keyCode, isShiftDown, isAltDown); //Procesa la información
                } else if (cmd == 2) {
                    salir(1);
                } else if (cmd == 3) { //Si llega un 3 como primer comando significa que al cliente no le han llegado
                    int idr = Integer.parseInt(comandos[1]); //Los datos necesarios para crear un nuevo objeto, pero si el identificador
                    int b = 0; //Y los cambios del objeto, así que lo pide y el servidor responde
                    boolean encontrado = false;
                    Elemento en;
                    while (b < elementos.size() && encontrado) { //Busca el objeto con ese identificador y lo devuelve
                        en = (Elemento) elementos.get(b);
                        if (en.id == idr) {
                            encontrado = true;
                            men = en.getUpdate(1);
                        } else {
                            b++;
                        }
                    }
                    unicast(servidor.comprimir(men)); //Envía al usuario los parámetros para crear el nuevo elemento
                }

            }
        } catch (IOException ex) { //En el caso de que se haya perdido la comunicación con el servidor
            //  ex.printStackTrace ();
        } finally {
            salir(0);
        }
    }

    //Procesa la tecla enviada por el usuario, aplica fuerzas al objeto que controla el cliente
    //Crea nuevos objetos en el servidor, borra objetos que le siguen, etc...
    private void processKey(int pr, int keyCode, boolean shiftDown, boolean altDown) {
        float k = 1;
        if (shiftDown) {
            k = 1 / 2f;
        }
        if (altDown) {
            k = 2f;
        }
        if (shiftDown && altDown) {
            k = 1f;
        }
        if (pr == 0) {
            if (keyCode == forwardKey || keyCode == KeyEvent.VK_S) { //Mueve al elemento en las 3 dimensiones
                elemento.fz = Af * k;
            } else if (keyCode == backKey || keyCode == KeyEvent.VK_W) { //Aplicando una fuerza en cada una de las direcciones
                elemento.fz = -Af * k;
            } else if (keyCode == leftKey || keyCode == KeyEvent.VK_A) { //Esta fuerza se detiene en cuanto el cliente deja de pulsar la tecla
                elemento.fx = -Af * k;
            } else if (keyCode == rightKey || keyCode == KeyEvent.VK_D) {
                elemento.fx = Af * k;
            } else if (keyCode == KeyEvent.VK_Q) {
                elemento.fy = Af * k;
            } else if (keyCode == KeyEvent.VK_E) {
                elemento.fy = -Af * k;
            }
            else if (keyCode == KeyEvent.VK_T) { //Indica a todos los elementos que deben seguir este objeto
                for (int t = 0; t < elementos.size(); t++) {
                    Elemento tt = (Elemento) elementos.get(t);
                    tt.siguiendo = id;
                }
            }
            else if (keyCode == KeyEvent.VK_Y) { //Indica a todos los elementos que no sigan a nadie
                for (int t = 0; t < elementos.size(); t++) {
                    Elemento tt = (Elemento) elementos.get(t);
                    tt.siguiendo = -1;
                }
            }
            else if (keyCode == KeyEvent.VK_G) { //Aplica una fuerza de atracción hacia abajo a todos los elementos
                for (int t = 0; t < elementos.size(); t++) {
                    Elemento tt = (Elemento) elementos.get(t);
                    tt.fy = -2 * tt.tamaño;
                }
            }
            else if (keyCode == KeyEvent.VK_H) { //Deja de aplicar una fuerza de atracción hacia abajo
                for (int t = 0; t < elementos.size(); t++) {
                    Elemento tt = (Elemento) elementos.get(t);
                    tt.fy = 0;
                }
            }
            else if (keyCode == KeyEvent.VK_U) { //Los elementos se repelen todos entre sí, sin un centro de atracción
                for (int t = 0; t < elementos.size(); t++) {
                    Elemento tt = (Elemento) elementos.get(t);
                    tt.siguiendo = -2;
                }
            }
            else if (keyCode == KeyEvent.VK_0) { //Elimina todos los elementos que siguen a este
                servidor.EliminaElementos(id);
            }
            else if (keyCode == KeyEvent.VK_1) { //Genera 10 nuevos elementos
                servidor.generaElementos(10);
            }
            else if (keyCode == KeyEvent.VK_2) {
                servidor.generaElementos(20);
            }
            else if (keyCode == KeyEvent.VK_3) {
                servidor.generaElementos(30);
            }
            else if (keyCode == KeyEvent.VK_4) {
                servidor.generaElementos(40);
            }
            else if (keyCode == KeyEvent.VK_5) {
                servidor.generaElementos(50);
            }
            else if (keyCode == KeyEvent.VK_6) { //Genera un elemento
                servidor.generaElementos(1);
            }
        } else if (pr == 1) { //Si ha dejado de pulsar la tecla el cliente deja de aplicar fuerza
            if (keyCode == forwardKey || keyCode == KeyEvent.VK_S) {
                elemento.fz = 0;
            } else if (keyCode == backKey || keyCode == KeyEvent.VK_W) {
                elemento.fz = 0;
            } else if (keyCode == leftKey || keyCode == KeyEvent.VK_A) {
                elemento.fx = 0;
            } else if (keyCode == rightKey || keyCode == KeyEvent.VK_D) {
                elemento.fx = 0;
            } else if (keyCode == KeyEvent.VK_Q) {
                elemento.fy = 0;
            } else if (keyCode == KeyEvent.VK_E) {
                elemento.fy = 0;

            }
        }
    }

    //funcion que envia mensajes al cliente
    //pasa los mensajes a binario, lo comprime y lo pasa a base64 para enviarlo.
    protected int unicast(String message) {
        try {
            o.writeUTF(message); //Envío del mensaje al cliente.
            o.flush();
        } catch (IOException ex) {
            salir(0);
        }

        return 0;
    }
    void salir(int t) {
        if (t == 1) {
            unicast(servidor.comprimir("2," + id + ",0/")); //Mensaje de elemento borrado
        }
        try {
            s.close(); //Cierra el socket
        } catch (IOException ex) {
            System.out.print("imposible cerrar el socket" + id);
        }
        synchronized(elementos) {
            elementos.removeElement(elemento);
        }
        synchronized(handlers) {
            handlers.removeElement(this);
        }
        servidor.broadcast("2," + id + ",0");
        System.out.print("Ds:" + id + " ");
        servidor.reutilizar(id); //Reutiliza el identificador
        stop();
    }
}