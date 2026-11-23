package pe.edu.upeu.sysventas.dto;

import lombok.Data;
import pe.edu.upeu.sysventas.enums.RolIngreso;

@Data
public class SessionManager {
    static SessionManager instance;
    Long userId;
    String userName;
    String userPerfil;
    RolIngreso rolIngreso;

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
}
