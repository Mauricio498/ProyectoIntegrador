package biblioteca.utng.model;

/**
 * Representa a un usuario (alumno) de la biblioteca digital.
 */
public class Usuario {

    private final int id;
    private String nombre;
    private final String matricula;
    private String correo;
    private String telefono;
    private String carrera;
    private String grupo;

    public Usuario(int id, String nombre, String matricula, String correo,
                    String telefono, String carrera, String grupo) {
        this.id = id;
        this.nombre = nombre;
        this.matricula = matricula;
        this.correo = correo;
        this.telefono = telefono;
        this.carrera = carrera;
        this.grupo = grupo;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    /** Iniciales del nombre completo, usadas en el avatar circular. */
    public String getIniciales() {
        String[] partes = nombre.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String parte : partes) {
            if (!parte.isBlank() && sb.length() < 2) {
                sb.append(Character.toUpperCase(parte.charAt(0)));
            }
        }
        return sb.toString();
    }
}
