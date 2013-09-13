package j3dwc;

import javax.vecmath.Vector3f;

//clase que guarda el estado de cada elemento en el servidor
//Identificador, tamaño, posición, velocidad, rotación, color, fuerzas, y variables de cálculo necesarias
public class Elemento {
    int id; //Identificador
    float tamaño; //Tamaño del elemento
    Vector3f posicion; //Posición del elemento en el espacio
    Vector3f velocidad; //Velocidad instantánea
    Vector3f rotacion; //Rotación
    Vector3f color; //Color
    float fx, fy, fz; //Fuerza aplicada al elemento
    int controlado; //Si el elemento está controlado por el usuario
    int siguiendo; //Elemento al que está siguiendo este elemento
    int eliminar; //Si está marcado para eliminar
    String c = ",";
    public Elemento(int i, float x, float y, float z, float cx, float cy, float cz, float t, int c) {
        id = i;
        tamaño = t;
        posicion = new Vector3f(x, y, z);
        velocidad = new Vector3f();
        rotacion = new Vector3f();
        color = new Vector3f(cx, cy, cz);
        fx = fy = fz = 0;
        controlado = c;
        siguiendo = -1;
        eliminar = 0;
    }
    //Función para truncar el número de decimales a 3
    float tr(float f) {
        float a = Math.round(f * 100);
        return a / 100;
    }
    //Función que forma un mensaje con todos los parámetros del elemento
    String getUpdate(int p) { //El parámetro p indica si el mensaje es para todos o para uno solo
        String mes = (p + c + id + c + 1023 + c + tr(posicion.x) + c + tr(posicion.y) + c + tr(posicion.z) + c + tr(rotacion.x) + c + tr(rotacion.y) + c + tr(rotacion.z) + c + tr(color.x) + c + tr(color.y) + c + tr(color.z) + c + tr(tamaño));
        return mes;
    }

    //Función que genera un mensaje con solo los parámetros que han cambiado
    String getMupdate(char[] m, int val) {
        String r = 1 + c + id + c + val;
        if (m[9] == '1') {
            r += c + tr(posicion.x);
        }
        if (m[8] == '1') {
            r += c + tr(posicion.y);
        }
        if (m[7] == '1') {
            r += c + tr(posicion.z);
        }
        if (m[6] == '1') {
            r += c + tr(rotacion.x);
        }
        if (m[5] == '1') {
            r += c + tr(rotacion.y);
        }
        if (m[4] == '1') {
            r += c + tr(rotacion.z);
        }
        if (m[3] == '1') {
            r += c + tr(color.x);
        }
        if (m[2] == '1') {
            r += c + tr(color.y);
        }
        if (m[1] == '1') {
            r += c + tr(color.z);
        }
        if (m[0] == '1') {
            r += c + tr(tamaño);
        }
        return (r);
    }
}