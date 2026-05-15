package Centro.modelo;

/**
 * DTO para la tabla Ubicacion.
 * La BD almacena la ubicación como una descripción libre (ej. "Bodega A - Pasillo 3").
 */
public class Ubicacion {

    private int    idUbicacion;
    private String descripcion;

    public Ubicacion() {}

    public Ubicacion(int idUbicacion, String descripcion) {
        this.idUbicacion = idUbicacion;
        this.descripcion = descripcion;
    }

    public int getIdUbicacion()            { return idUbicacion; }
    public void setIdUbicacion(int id)     { this.idUbicacion = id; }

    public String getDescripcion()                  { return descripcion; }
    public void setDescripcion(String descripcion)  { this.descripcion = descripcion; }

    /** Mostrado en JComboBox */
    @Override
    public String toString() { return descripcion; }
}
