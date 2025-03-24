package domain;

import java.util.Optional;

public class FilterUserDTO {
    Optional<Long> idUser;

    public Optional<Long> getIdUser() {
        return idUser;
    }
    public void setIdUser(Optional<Long> idUser) {
        this.idUser = idUser;
    }
}
