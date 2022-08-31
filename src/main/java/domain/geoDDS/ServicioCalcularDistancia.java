package domain.geoDDS;

import domain.geoDDS.adapters.ServicioGeoDDSAdapter;
import domain.geoDDS.entidades.Distancia;

import java.io.IOException;

public class ServicioCalcularDistancia {
    static ServicioCalcularDistancia instancia = null;
    ServicioGeoDDSAdapter adapter;

    public void setAdapter(ServicioGeoDDSAdapter adapter) {
        this.adapter = adapter;
    }

    public ServicioGeoDDSAdapter getAdapter() {
        return adapter;
    }

    public static ServicioCalcularDistancia getInstance(){ //borrar esto todo
        if (instancia==null){
            instancia=new ServicioCalcularDistancia();
        }
        return instancia;
    }

    public Distancia distanciaEntre(Direccion direccionOrigen, Direccion direccionDestino) throws IOException {
        return adapter.distanciaEntre(direccionOrigen,direccionDestino);
    }

    public int obtenerLocalidadId(Direccion direccion) {
        return direccion.getLocalidad().getIdLocalidad();
    }

}
