NOTA BENE: necessario per il registro privato

- quando creo un nuovo nodo docker swarm devo eseguire i seguenti passi:

- creare un file:
/etc/docker/daemon.json

- aggiungere  al file:
{
   "insecure-registries" :["167.86.83.61:5000"]
} 

- riavviare docker

- eseguire il comando:
docker login 167.86.83.61:5000
