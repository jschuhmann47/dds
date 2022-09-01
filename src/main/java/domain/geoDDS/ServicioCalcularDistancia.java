package domain.geoDDS;

import domain.geoDDS.adapters.ServicioGeoDDSAdapter;
import domain.geoDDS.entidades.Distancia;

import java.io.IOException;

public class ServicioCalcularDistancia {

    static ServicioGeoDDSAdapter adapter;

    public static void setAdapter(ServicioGeoDDSAdapter adapter) {
        ServicioCalcularDistancia.adapter = adapter;
    }

    public ServicioGeoDDSAdapter getAdapter() {
        return adapter;
    }


    public static Distancia distanciaEntre(Direccion direccionOrigen, Direccion direccionDestino) throws IOException {
        return adapter.distanciaEntre(direccionOrigen,direccionDestino);
    }

    public static int obtenerLocalidadId(Direccion direccion) {
        return direccion.getLocalidad().getId();
    }

}
