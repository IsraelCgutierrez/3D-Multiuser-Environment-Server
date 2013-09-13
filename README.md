3D-Multiuser-Environment-Server
===============================

The objective of this project is to develop a 3D environment with network communication technology based on Java3D. A virtual space in which users can interact with each other and change their environment. For this purpose we have developed a server application in order to maintain the virtual environment, and a client application that each user can view and interact with the environment in real time.

PROYECTO FIN DE CARRERA - UNIVERSIDAD DE JAÉN

Desarrollo de un entorno 3D multiusuario.


TITULACION: Ingeniería Técnica de Telecomunicación 

ESPECIALIDAD: Telemática 

AUTOR: Israel Choque Gutiérrez 

TUTOR: Raquel Viciana Abad, José Enrique Muñoz Expósito

Linares, Septiembre, 2010


MANUAL DE INSTALACIÓN
Para la ejecución de la aplicación es necesario tener instalado el siguiente software:
-	Java Runtime Environment (JRE). Entorno en tiempo de ejecución de java formado por una máquina virtual JVM, un conjunto de bibliotecas y otros componentes	disponibles	en	esta	dirección: http://www.oracle.com/technetwork/java/javase/downloads/index.html
- Java 3D: API para gráficos 3D para el lenguaje de programación Java disponible en https://java3d.dev.java.net/binary-builds.html

MANUAL DE EJECUCION
Los ficheros fuente de extensión .java del cliente como los del servidor pueden ser compilados de forma manual siguiendo el siguiente manual http://www.arrakis.es/~abelp/ApuntesJava/CompilacionYEjecucion.htm. Otra manera más fácil y rápida de ejecutar las aplicaciones es haciendo uso de los archivos comprimidos .jar
Para ejecutar al servidor es necesario abrir una ventana de comando en Windows y desplazarse al directorio que contiene los archivos .jar.

Una vez situados en el correspondiente directorio, es necesario escribir el comando java –jar <nombre del archivo .jar> puerto. El servidor responderá entonces con un mensaje, indicando la IP y el puerto en el que se ha iniciado.

Igualmente el cliente se ejecuta en una ventana de comandos de Windows, donde el formato es: java –jar <normbre del archivo .jar> <IP del servidor> <Puerto>.

MANUAL DE REFERENCIA
1. Añadir o quitar parámetros.
A continuación se describen los pasos necesarios para añadir o quitar parámetros a los elementos como forma, textura, etc. Los siguientes pasos explican la forma de añadir el parámetro forma a los elementos. Los pasos para quitar parámetros son análogos a éstos.
Los parámetros de cada elemento se almacenan en la clase Elemento para el servidor y Objeto en el cliente. Modificar la clase Elemento añadiendo la variable forma como entero en su estructura e inicializándola en el constructor.
Modificar la función getUpdate añadiendo la nueva variable en una posición de la cadena adecuada. Un parámetro que sufre muchas modificaciones en corto espacio de tiempo, deberá ser añadido cerca del campo máscara, como los parámetros x,y,z . En otro caso, se debe añadir en el otro extremo de la
cadena. -	Modificar la función getMupdate añadiendo el nuevo parámetro en la posición
escogida y actualizando los índices del vector máscara. -	En la clase Fisica inicializar la posición escogida del nuevo parámetro en la
máscara a 0 y modificar el bucle infinito con las reglas del nuevo parámetro.A continuación, se debe comprobar si en el proceso se da el caso de cambio de máscara para notificarlo poniendo el bit de la máscara a 1.
-	En la clase ChatHandler inicializar el nuevo parámetro -	En la clase Controlador2 modificar la función actualizarObjeto convirtiendo al
entero el nuevo parámetro del vector de parámetros Datos y generar el nuevo
objeto con el nuevo parámetro. -	Modificar la clase Objeto añadiendo el nuevo parámetro a su estructura e
inicializándolo en el constructor. -	Modificar la función Actualiza extrayendo el nuevo parámetro de la posición
adecuada. -	Si el parámetro indica una modificación en la estructura 3D añadir a la clase
Objeto una nueva función que aplique la modificación, como por ejemplo la
función setAPP que modifica la apariencia de la esfera. -	En este ejemplo para modificar la forma habría que modificar la clase Esfera
para que en función del nuevo parámetro genere una forma diferente.

2. Cambiar las reglas del servidor.
Esta modificación se encuentra únicamente en la clase Fisica y empieza en la inicialización del vector Fuerzas hasta el envío de mensajes. Está compuesta principalmente por un bucle que recorre el vector de usuarios conectados y otro bucle anidado de la misma magnitud, consiguiendo procesar las nuevas posiciones de cada elemento en función de todos los elementos.
Para implementar nuevas reglas o comportamiento de los elementos en el servidor se debe mantener la comprobación de la modificación de parámetros reflejada en la máscara, y el formato de concatenación de mensajes por elemento.

3. Añadir o quitar controles de usuario.
En este proyecto únicamente la tecla ESC genera un mensaje especial del tipo desconexión enviado al servidor, el resto de teclas se envían al servidor sin apenas modificación con el objetivo de simplificar el cliente. Para seleccionar las teclas a enviar hace falta modificar la clase KeyBehavior del cliente.
Para añadir una función a una tecla en concreto se modifica la clase ChatHandler. del servidor, en concreto la función processKey a la que llega el identificador de la tecla, y otros parámetros como pr para saber si se ha presionado o dejado de presionar, y el estado de las teclas Shift y Alt.
4. Cambiar la compresión.
En la función comprimir de la clase Server. el parámetro del constructor de la clase Deflater tiene el valor BEST_COMPRESSION, y puede ser modificado en función de las necesidades, siguiendo la documentación de java24.
5. Añadir un nuevo tipo de mensaje cliente – servidor.
En cualquiera de las clases que implementan el cliente y que estén enlazadas con la clase ChatClient como por ejemplo KeyBehavior se puede implementar una función que envíe un mensaje al servidor definiendo en el campo de tipo un nuevo identificador a partir del 4, o modificando uno de los anteriores.
En la clase ChatHandler dentro del bucle infinito de espera de comandos del cliente se debe añadir la detección del nuevo tipo de parámetro e implementar sus consecuencias en el servidor.
6. Añadir nuevo tipo de mensaje servidor – cliente.
En cualquier clase del servidor que esté enlazada con la clase Server se puede implementar un nuevo tipo de mensaje definiendo un nuevo identificador para el campo tipo, para luego enviarlo a la función broadcast.
En el cliente se tiene que modificar la función Controlador2, siguiendo la secuencia de comprobaciones del valor del parámetro servidor que contiene el tipo de mensaje enviado por el servidor.
