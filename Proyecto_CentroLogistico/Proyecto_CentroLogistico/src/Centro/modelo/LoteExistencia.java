package Centro.modelo;

/**
 * DTO para la tabla LoteExistencia.
 * Representa un lote de un producto en una ubicación concreta.
 *
 * Tabla real: LoteExistencia (id_lote, id_producto, id_ubicacion,
 *                              lote, fecha_vencimiento, existencias, costo)
 */
public class LoteExistencia {

    private int    idLote;
    private int    idProducto;
    private int    idUbicacion;
    private String lote;
    private String fechaVencimiento;   // TEXT en BD, formato YYYY-MM-DD
    private int    existencias;
    private double costo;

    // Campos desnormalizados para UI (llenar con JOIN)
    private String productoNombre;
    private String ubicacionDescripcion;

    public LoteExistencia() {}

    public LoteExistencia(int idLote, int idProducto, int idUbicacion,
                           String lote, String fechaVencimiento,
                           int existencias, double costo) {
        this.idLote           = idLote;
        this.idProducto       = idProducto;
        this.idUbicacion      = idUbicacion;
        this.lote             = lote;
        this.fechaVencimiento = fechaVencimiento;
        this.existencias      = existencias;
        this.costo            = costo;
    }

    // ── Getters / Setters ──────────────────────────────────────

    public int    getIdLote()                     { return idLote; }
    public void   setIdLote(int id)               { this.idLote = id; }

    public int    getIdProducto()                 { return idProducto; }
    public void   setIdProducto(int id)           { this.idProducto = id; }

    public int    getIdUbicacion()                { return idUbicacion; }
    public void   setIdUbicacion(int id)          { this.idUbicacion = id; }

    public String getLote()                       { return lote; }
    public void   setLote(String lote)            { this.lote = lote; }

    public String getFechaVencimiento()           { return fechaVencimiento; }
    public void   setFechaVencimiento(String f)   { this.fechaVencimiento = f; }

    public int    getExistencias()                { return existencias; }
    public void   setExistencias(int e)           { this.existencias = e; }

    public double getCosto()                      { return costo; }
    public void   setCosto(double c)              { this.costo = c; }

    public String getProductoNombre()             { return productoNombre; }
    public void   setProductoNombre(String n)     { this.productoNombre = n; }

    public String getUbicacionDescripcion()       { return ubicacionDescripcion; }
    public void   setUbicacionDescripcion(String u){ this.ubicacionDescripcion = u; }
}
