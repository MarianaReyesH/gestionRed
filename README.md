# Proyecto GestiónRed

Este proyecto tiene como objetivo la implementación de una aplicación web para la ejecución y seguimiento de procesos de gestión de red. Para ello se ha implementado una aplicación Spring Boot que, comunicándose con un motor de procesos, es capaz de ejecutar procesos Java Business Process Management (jBPM) para gestionar fallos que se puedan producir en los servicios de una red de Tecnologías de Información y Comunicación (TIC) y seguir los resultados.
Para su desarrollo se han implementado tareas automáticas, también conocidos como Work Item Handlers (WIH) y tareas humanas, siempre pensando en la reusabilidad del código, la independencia entre los diferentes módulos que conforman el proyecto y el principio Keep It Simple Stupid (KISS).

## Módulos

1. model: definición de los datos que se comparten entre los procesos de negocio y la aplicación Spring Boot
2. kjar: paquete que contiene todos los artefactos necesarios para la ejecución de los procesos de negocio. Incluye definiciones de procesos, reglas de negocio y otros recursos necesarios
3. service: implementación de los controladores, los WIH, la lógica para tratar las tareas humanas, las plantillas para presentar la página web, la configuración de la aplicación, entre otros

## Generación

Se han utilizado los arquetipos maven para la generación de los tres componentes (modelo, kjar y service)
```
mvn archetype: generate -B "-DarchetypeGroupId=org.kie" 
"-DarchetypeArtifactId=kie-model-archetype" 
"-DarchetypeVersion=7.74.1.Final" 
"-DgroupId=us.dit" 
"-DartifactId=gestionRed-model" 
"-Dversion=1.0-SNAPSHOT" 
"-Dpackage=us.dit.gestionRed.model”
```
```
mvn archetype:
generate -B "-DarchetypeGroupId=org.kie" 
"-DarchetypeArtifactId=kie-kjar-archetype" 
"-DarchetypeVersion=7.74.1.Final" 
"-DgroupId=us.dit" 
"-DartifactId=gestionRed-kjar" 
"-Dversion=1.0-SNAPSHOT" 
"-Dpackage=us.dit.gestionRed”
```
```
mvn archetype:
generate -B "-DarchetypeGroupId=org.kie" 
"-DarchetypeArtifactId=kie-service-spring-boot-archetype" 
"-DarchetypeVersion=7.74.1.Final" 
"-DgroupId=us.dit" 
"-DartifactId=gestionRed-service" 
"-Dversion=1.0-SNAPSHOT" 
"-Dpackage=us.dit.gestionRed.service" "-DappType=bpm”
```

## Manual de instalación y ejecución
Se explica cómo instalar, configurar y ejecutar Logstash, como iniciar la aplicación Spring Boot, como montar un contenedor Docker que emulará un servicio NGINX caído. Dicho contenedor también estará dotado de un servidor SSH para poder conectarse a él de forma remota. Por último, se explica la secuencia que se sigue para ejecutar el proyecto.

### Logstash
1. Instalación

    Lo instalamos de la [pág oficial](https://www.elastic.co/es/downloads/logstash).

2. Archivo de configuración

    En la carpeta raíz creamos o modificamos (en el caso de que ya exista) el fichero _logstash.conf_ con el siguiente contenido:
    ```
    input {
    	stdin{}
    }
    
    filter {
    	grok {
    		match => { 
    			"message" => "%{WORD:month} %{NUMBER:day} %{HOUR:hour}:%{MINUTE:minute}:%{SECOND:second}
                        %{HOSTNAME:hostname_client} %{WORD:process}\[%{NUMBER:pid}\]: \[%{WORD}\]
                        %{WORD} %{WORD} %{WORD} %{WORD:service} %{WORD} %{WORD} %{WORD}
                        %{WORD:hostname_service}: %{GREEDYDATA:msj_error}" 
    		}
    	}
    	
    	mutate {
    		remove_field => ["host", "@timestamp", "event", "@version", "message"]
    	}
      
    	mutate {
    		add_field => { "timestamp" => "%{month} %{day} %{hour}:%{minute}:%{second}" }
    		remove_field => ["month", "day", "hour", "minute", "second"]
    	}
      
    	date {
    		match => [ "timestamp", "MMM dd HH:mm:ss" ]
    		target => "timestamp"
    	}
    }
    
    
    output {
    	http {
    		format => "json"
    		http_method => "post"
    		headers => {
    			"Content-Type" => "application/json"
    			"Authorization" => "Basic d2JhZG1pbjp3YmFkbWlu"
    		}
    		url => "http://localhost:8090/signals"
    		request_timeout => 60
    		socket_timeout => 60
    		connect_timeout => 60
    	}
    }
    ```
3. Ejecución 

    Abrimos una terminal, nos dirigimos al directorio raíz (donde tiene que estar el logstash.conf) y ejecutamos:    `.\bin\logstash -f logstash.conf`


### GestionRed
Para arrancar la aplicación Spring Boot primero clonamos el repositorio. Después abrimos un terminal, nos ubicamos en el directorio _gestionRed-service_ y ejecutamos:   `.\launch.bat clean install -Ppostgres`


### Máquina para pruebas
Para las pruebas tenemos un contenedor Docker que simula una máquina con un servidor web NGINX, contando también con un servidor ssh para poder conectarnos a él y ejecutar los comandos utilizados por los procesos jBPM implementados.
Teniendo el servidor Docker Desktop ejecutándose seguimos los siguientes pasos:
1)	Creamos un Dockerfile con el siguiente contenido:
    ```
    # Usa la imagen base de Ubuntu
    FROM ubuntu:20.04
    
    # Actualiza el sistema y instala los paquetes necesarios
    RUN apt-get update && apt-get install -y \
        openssh-server \
        nginx \
        && apt-get clean
    
    # Configura SSH
    RUN mkdir /var/run/sshd
    RUN echo 'root:password' | chpasswd
    RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
    RUN sed -i 's/PasswordAuthentication no/PasswordAuthentication yes/' /etc/ssh/sshd_config
    
    # Permite el uso de PAM (Pluggable Authentication Modules)
    RUN sed -i 's@session    required   pam_loginuid.so@session    optional   pam_loginuid.so@g' /etc/pam.d/sshd
    
    # Exponer puertos SSH y HTTP
    EXPOSE 22 80
    
    # Inicia el servidor SSH y Nginx
    CMD service ssh start && service nginx start && tail -f /dev/null
    ```
2)	Construimos la imagen:   `docker build -t my_ssh_nginx_container .`
3)	Ejecutamos el contenedor:  `docker run -d -p 2222:22 -p 8081:80 
--name my_running_container my_ssh_nginx_container`


## Ejecución del escenario
La secuencia empieza cuando un servicio falla, en nuestro caso sólo está implementado el servicio NGINX, y un cliente de dicho servicio, al darse cuenta, reporta el fallo.
Esto lo hace enviando un mensaje Syslog a Logstash. 

Se empieza escribiendo por la entrada estándar (stdin) de Logstash un mensaje con el formato adecuado. Un ejemplo sería (lo que está en negrita sí tiene que estar igual, ya que es lo que le indica a la aplicación cuál es el servicio que está fallando): 

abr 03 13:18:20 hostCliente processCliente[555]: [ERROR] Fallo del servicio ***nginxservice*** en el host hostService: Llega un error 5XX

