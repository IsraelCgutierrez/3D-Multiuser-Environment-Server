package j3dwc;
import java.util.Vector;
import javax.vecmath.Vector3f;

public class Fisica extends Thread {
    Vector handlers; //Vector de hebras de usuarios
    Vector elementos; //Vector de elementos en el servidor activos
    Server servidor; //Puntero al servidor
    float At = 0.1f; //Incremento del tiempo
    float Fr = 1.1f; //Factor de rozamiento
    float m = 0.001f; //Margen
    float er = 0.9f; //Coeficiente de restitución
    String mensaje; //Cadena de mensajes
    long tiempoEspera; //Tiempo de espera en milisegundos
    long fps = (long) 40; //25 mensajes por segundo
    long t0 = 0; //Para medir los tiempos
    long t1 = 0;
    int pared = 15; //Distancia de las paredes verticales
    //inicializa los parámetros
    public Fisica(Vector c, Vector e, Server s) {
        handlers = c;
        elementos = e;
        mensaje = "";
        servidor = s;
        tiempoEspera = 0;
    }

    //Hebra infinita que calcula constantemente las posiciones de los elementos según la fuerza y velocidad
    public void run() {

        while (true) {
            t0 = System.currentTimeMillis(); //Momento en el que empieza a calcular
            mensaje = ""; //Cada cálculo envía un nuevo mensaje
            if (elementos.size() < 1) { //Comprueba si existe al menos un elemento
                servidor.generaElementos(1);
            }
            Vector3f[][] Fuerzas = new Vector3f[256][256]; //Vector bidimensional para guardar la atracción entre elementos
            for (int i = 0; i < elementos.size(); i++) { //Bucle para calcular la fuerza total de cada elemento con los demás
                Elemento e = (Elemento) elementos.get(i);
                //Cálculo de fuerzas /aceleraciones
                float nfx = 0; //Variables temporales para acumular las fuerzas de todos los elementos que se aplican al
                float nfy = 0; //Elemento que las está calculando
                float nfz = 0;
                float dmin = 1000; //Distancia al elemento más cercano para evitar errores de colisión
                int emin = i; //Elemento más cercano
                char[] cmasc = new char[10]; //Máscara
                Vector3f nc = e.color; //Nuevo color
                //Colisión con el suelo
                float altura = e.posicion.y;
                float ds = altura - e.tamaño;
                if (ds < m) { //Si toca el suelo
                    float vey = (-1 * (e.tamaño) * e.velocidad.y) / (e.tamaño);
                    e.velocidad.y = vey;
                }
                //Colisión con paredes
                if (Math.abs(e.posicion.x) + e.tamaño > pared) {
                    float vex = (-1 * (e.tamaño) * e.velocidad.x) / (e.tamaño);
                    e.velocidad.x = vex;
                }
                if (Math.abs(e.posicion.z) + e.tamaño > pared) {
                    float vez = (-1 * (e.tamaño) * e.velocidad.z) / (e.tamaño);
                    e.velocidad.z = vez;
                }

                //Bucle para calcular la fuerza que ejercen los demás elementos
                for (int j = 0; j < elementos.size(); j++) {

                    if (i != j && Fuerzas[i][j] == null) { //Comprueba si se trata de sí mismo o ya se ha calculado
                        Elemento ej = (Elemento) elementos.get(j);
                        float dx = e.posicion.x - ej.posicion.x; //Calculo de distancias
                        float dy = e.posicion.y - ej.posicion.y;
                        float dz = e.posicion.z - ej.posicion.z;
                        float dist = new Vector3f(dx, dy, dz).length(); //Uso del vector3f
                        if (e.controlado == 0) {
                            if (Math.abs(dist) < Math.abs(dmin)) {
                                dmin = dist;
                                emin = j;
                            } //guarda el elemento mas cercano
                        } else if (ej.controlado == 1) {
                            if (Math.abs(dist) < Math.abs(dmin)) {
                                dmin = dist;
                                emin = j;
                            } //guarda el elemento mas cercano
                        }
                        float db = dist - e.tamaño - ej.tamaño; //Distancia entre los bordes de los elementos
                        float alpha = (float) Math.acos(dx / dist); //Directores
                        float beta = (float) Math.acos(dy / dist);
                        float ganma = (float) Math.acos(dz / dist);
                        if (db < m) { //Comprueba si esta tocando
                            if (ej.controlado == 1) { //Si con el que hemos chocado está controlado por el usuario
                                e.siguiendo = ej.id;
                                nc = ej.color; //Seguimos al usuario y cambiamos el color
                            }
                            if (e.controlado == 1) { //Si chocamos con un elemento controlado por el servidor
                                ej.siguiendo = e.id;
                                nc = e.color; //Empieza a seguirnos y cambia su color
                            }
                            //Cinética, cambio de velocidades por colisiones
                            if (e.controlado == ej.controlado) {
                                float vex = (((e.tamaño - ej.tamaño * er) * e.velocidad.x) + (ej.tamaño * (1 + er) * ej.velocidad.x)) / (e.tamaño + ej.tamaño);
                                float vejx = (((1 + er) * e.tamaño * e.velocidad.x) + ((ej.tamaño - e.tamaño * er) * ej.velocidad.x)) / (e.tamaño + ej.tamaño);
                                e.velocidad.x = vex;
                                ej.velocidad.x = vejx;
                                float vey = (((e.tamaño - ej.tamaño * er) * e.velocidad.y) + (ej.tamaño * (1 + er) * ej.velocidad.y)) / (e.tamaño + ej.tamaño);
                                float vejy = (((1 + er) * e.tamaño * e.velocidad.y) + ((ej.tamaño - e.tamaño * er) * ej.velocidad.y)) / (e.tamaño + ej.tamaño);
                                e.velocidad.y = vey;
                                ej.velocidad.y = vejy;
                                float vez = (((e.tamaño - ej.tamaño * er) * e.velocidad.z) + (ej.tamaño * (1 + er) * ej.velocidad.z)) / (e.tamaño + ej.tamaño);
                                float vejz = (((1 + er) * e.tamaño * e.velocidad.z) + ((ej.tamaño - e.tamaño * er) * ej.velocidad.z)) / (e.tamaño + ej.tamaño);
                                e.velocidad.z = vez;
                                ej.velocidad.z = vejz;
                            }


                        }
                        //Cálculo de fuerzas
                        if (e.controlado == 0) { //Si es un elemento controlado por el servidor
                            if (e.siguiendo != -1) { //Si está siguiendo a algún elemento
                                if (db < 0) { //Evitar grandes repulsiones
                                    db = 0;
                                }

                                if (e.siguiendo == ej.id) { //Calcula la fuerza para el que está siguiendo
                                    // float fm = -1*(db+1f)*((e.tamaño*ej.tamaño));// fuerza en modulo
                                    float fm = (float)(-(e.tamaño * ej.tamaño) - Math.log10(db + 1));
                                    nfx += (float) fm * Math.cos(alpha);
                                    nfy += (float) fm * Math.cos(beta);
                                    nfz += (float) fm * Math.cos(ganma);
                                }

                                //Calcula la fuerza de repulsión con todos
                                //float fe = (e.tamaño*ej.tamaño)/((db+1f));
                                float fe = (float)((e.tamaño * ej.tamaño) / Math.pow(db + 1, 2));
                                float tx = (float)((float) fe * Math.cos(alpha));
                                float ty = (float)((float) fe * Math.cos(beta));
                                float tz = (float)((float) fe * Math.cos(ganma));
                                nfx += tx;
                                nfy += ty;
                                nfz += tz;

                                if (ej.controlado == 0) { //Guarda la fuerza calculada entre estos dos elementos
                                    Fuerzas[j][i] = new Vector3f(-1 * tx, -1 * ty, -1 * tz); //Para evitar ser calculada de nuevo
                                }
                            }
                        }

                    } else { //Si ya se han calculado las fuerzas entre estos dos elementos, las suma
                        if (Fuerzas[i][j] != null) {
                            nfx += Fuerzas[i][j].x;
                            nfy += Fuerzas[i][j].y;
                            nfz += Fuerzas[i][j].z;

                        }

                    }

                }
                //Para evitar enviar información por cálculos minúsculos.
                if (Math.abs(nfx) < m) {
                    nfx = 0;
                }
                if (Math.abs(nfy) < m) {
                    nfy = 0;
                }
                if (Math.abs(nfz) < m) {
                    nfz = 0;
                }
                e.fx += nfx;
                e.fy += nfy;
                e.fz += nfz;



                //Cálculo del incremento proporcional del tiempo
                float Am = new Vector3f(e.fx, e.fy, e.fz).length();
                float Ax, Ay, Az;

                if (Am != 0) {
                    Ax = (float)(At + At * 0.5 * Math.abs(e.fx / Am)) / (0.5f + e.tamaño);
                    Ay = (float)(At + At * 0.5 * Math.abs(e.fy / Am)) / (0.5f + e.tamaño);
                    Az = (float)(At + At * 0.5 * Math.abs(e.fz / Am)) / (0.5f + e.tamaño);
                } else {
                    Ax = Ay = Az = At / e.tamaño;
                }

                //Calculo de posiciones
                float ix = e.velocidad.x * Ax + (0.5f * e.fx * Ax * Ax); //Incremento de x
                float nx = e.posicion.x + ix; //Nueva posicion teorica
                e.velocidad.x += e.fx * Ax; //Actualizacion de la velocidad
                float iy = e.velocidad.y * Ay + (0.5f * e.fy * Ay * Ay);
                float ny = e.posicion.y + iy;
                e.velocidad.y += e.fy * Ay;

                float iz = e.velocidad.z * Az + (0.5f * e.fz * Az * Az);
                float nz = e.posicion.z + iz;
                e.velocidad.z += e.fz * Az;

                //Antelar toques
                if (emin != i) {
                    Elemento ej = (Elemento) elementos.get(emin);
                    float dx = e.posicion.x + ix - ej.posicion.x;
                    float dy = e.posicion.y + iy - ej.posicion.y;
                    float dz = e.posicion.z + iz - ej.posicion.z;
                    float PD = new Vector3f(dx, dy, dz).length(); //Posible distancia
                    float PDb = PD - e.tamaño - ej.tamaño; //Posible distancia al borde
                    if (PDb < 0) {
                        nx = e.posicion.x + ix - PDb * dx / PD;
                        ny = e.posicion.y + iy - PDb * dy / PD;
                        nz = e.posicion.z + iz - PDb * dz / PD;
                    }
                }

                //Antelar toques con el suelo
                if (ny - e.tamaño < 0) {
                    ny = e.tamaño;
                }
                //Antelar toques con paredes
                if (nx + e.tamaño > pared) {
                    nx = pared - e.tamaño;
                }
                if (nx - e.tamaño < -pared) {
                    nx = -pared + e.tamaño;
                }
                if (nz + e.tamaño > pared) {
                    nz = pared - e.tamaño;
                }
                if (nz - e.tamaño < -pared) {
                    nz = -pared + e.tamaño;
                }

                //Actualiza posiciones y decide si envía la actualización o no
                if (e.tr(e.posicion.x) == e.tr(nx)) {
                    cmasc[9] = '0';
                } else {
                    e.posicion.x = nx;
                    cmasc[9] = '1';
                }
                if (e.tr(e.posicion.y) == e.tr(ny)) {
                    cmasc[8] = '0';
                } else {
                    e.posicion.y = ny;
                    cmasc[8] = '1';
                }
                if (e.tr(e.posicion.z) == e.tr(nz)) {
                    cmasc[7] = '0';
                } else {
                    e.posicion.z = nz;
                    cmasc[7] = '1';
                }
                //Aplica una fuerza de rozamiento a la velocidad
                e.velocidad.x = e.velocidad.x / Fr;
                e.velocidad.y = e.velocidad.y / Fr;
                e.velocidad.z = e.velocidad.z / Fr;
                //Elimina la fuerza calculada para que las fuerzas constantes sigan trabajando
                e.fx -= nfx;
                e.fy -= nfy;
                e.fz -= nfz;
                //Rotación
                cmasc[6] = '0';
                cmasc[5] = '0';
                cmasc[4] = '0';

                //Color
                if (nc.equals(e.color)) {
                    cmasc[3] = '0';
                    cmasc[2] = '0';
                    cmasc[1] = '0';
                }
                else {
                    e.color = nc;
                    cmasc[3] = '1';
                    cmasc[2] = '1';
                    cmasc[1] = '1';
                }
                //Tamaño
                cmasc[0] = '0';

                //Actualiza mensaje
                //Genera la máscara que indica que valores se han modificado
                String masc = String.valueOf(cmasc);
                int val = Integer.parseInt(masc, 2);
                if (val != 0) { //Comprueba  si se ha modificado al menos un parámetro
                    String ma = e.getMupdate(cmasc, val) + "/";
                    mensaje += ma; //Concatena los mensajes de cada elemento separándolos con /
                }
                if (e.controlado == 0) { //Comprueba si el elemento se ha salido de un rango
                    if (e.posicion.length() > 50 || e.posicion.y < (-1 * e.tamaño / 2)) { //O si ha traspasado el suelo
                        e.eliminar = 1; //Marca el elemento para eliminar
                        servidor.generaElementos(1); //Genera un nuevo elemento cuando se elimina otro
                    }
                    if (e.eliminar == 1) { //Si el elemento esta marcado para ser eliminado lo elimina
                        servidor.reutilizar(e.id); //reutiliza el identificador
                        mensaje += ("2," + e.id + ",0/"); //envia a todos el mensaje de que se ha eliminado el elemento
                        synchronized(elementos) { //remueve el elemento del vector elementos
                            elementos.removeElement(e);
                        }
                    }
                }

            }

            if (mensaje != "" && handlers.size() > 0) { //comprueba si el vector mensajes tiene algo que enviar y si hay alguien
                servidor.broadcast(mensaje); //a quien enviarlo
            }
            t1 = System.currentTimeMillis();
            if (t1 - t0 < fps) { //si el servidor ha tardado menos de lo establecido
                tiempoEspera = (long)(fps - (t1 - t0)); //espera lo necesario para garantizar los fps
            } else {
                tiempoEspera = 1; //si tarda demaciado espera un milisegundo para atender a las otras hebras.
            }
            try {
                Thread.sleep(tiempoEspera); //el servidor queda en espera por un tiempo calculado
            } catch (Exception e) {
                System.out.println(e);
            }

        }
    }
}