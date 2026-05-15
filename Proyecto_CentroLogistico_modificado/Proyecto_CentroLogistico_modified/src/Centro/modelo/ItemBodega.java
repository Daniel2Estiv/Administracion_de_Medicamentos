package Centro.modelo;

/**
 * DTO para la tabla Producto.
 * Mantiene el nombre "ItemBodega" por compatibilidad con las clases heredadas
 * (Insumo, Medicamentos, BienesDevolutivos).
 *
 * Tabla real: Producto (id_producto, codigo TEXT, invima, nombre, id_presentacion, id_tipo)
 */
public class ItemBodega {

    private int    idProducto;
    private String codigo;        // código interno (TEXT en BD)
    private String invima;
    private String nombre;
    private int    idPresentacion;
    private int    idTipo;

    // Campos desnormalizados para mostrar en UI (se llenan con JOIN)
    private String presentacion;
    private String tipo;

    public ItemBodega() {}

    public ItemBodega(int idProducto, String codigo, String invima,
                      String nombre, int idPresentacion, int idTipo) {
        this.idProducto     = idProducto;
        this.codigo         = codigo;
        this.invima         = invima;
        this.nombre         = nombre;
        this.idPresentacion = idPresentacion;
        this.idTipo         = idTipo;
    }

    // ── Getters / Setters ──────────────────────────────────────

    public int    getIdProducto()            { return idProducto; }
    public void   setIdProducto(int id)      { this.idProducto = id; }

    /** Alias getCodigo() → devuelve el código TEXT del producto */
    public String getCodigo()                { return codigo; }
    public void   setCodigo(String codigo)   { this.codigo = codigo; }

    public String getInvima()                { return invima; }
    public void   setInvima(String invima)   { this.invima = invima; }

    public String getNombre()                { return nombre; }
    public void   setNombre(String nombre)   { this.nombre = nombre; }

    public int    getIdPresentacion()        { return idPresentacion; }
    public void   setIdPresentacion(int id)  { this.idPresentacion = id; }

    public int    getIdTipo()                { return idTipo; }
    public void   setIdTipo(int id)          { this.idTipo = id; }

    public String getPresentacion()          { return presentacion; }
    public void   setPresentacion(String p)  { this.presentacion = p; }

    public String getTipo()                  { return tipo; }
    public void   setTipo(String tipo)       { this.tipo = tipo; }

    @Override
    public String toString() {
        return "[" + codigo + "] " + nombre;
    }
}
