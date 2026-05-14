package Centro.modelo;

/**
 * DTO para la tabla Presentacion (ej. "Caja x 10", "Frasco 500ml", etc.)
 */
public class Presentacion {

    private int    idPresentacion;
    private String descripcion;

    public Presentacion() {}

    public Presentacion(int idPresentacion, String descripcion) {
        this.idPresentacion = idPresentacion;
        this.descripcion    = descripcion;
    }

    public int getIdPresentacion()               { return idPresentacion; }
    public void setIdPresentacion(int id)        { this.idPresentacion = id; }

    public String getDescripcion()               { return descripcion; }
    public void setDescripcion(String d)         { this.descripcion = d; }

    @Override
    public String toString() { return descripcion; }
}
