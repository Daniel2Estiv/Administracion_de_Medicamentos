package Centro.modelo;

/**
 * DTO para la tabla TipoProducto.
 * Clasifica los productos: Insumo, Medicamento, Bien Devolutivo, etc.
 */
public class TipoProducto {

    private int    idTipo;
    private String nombre;

    public TipoProducto() {}

    public TipoProducto(int idTipo, String nombre) {
        this.idTipo = idTipo;
        this.nombre = nombre;
    }

    public int getIdTipo()           { return idTipo; }
    public void setIdTipo(int id)    { this.idTipo = id; }

    public String getNombre()        { return nombre; }
    public void setNombre(String n)  { this.nombre = n; }

    @Override
    public String toString() { return nombre; }
}
