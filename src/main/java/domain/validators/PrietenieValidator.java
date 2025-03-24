package domain.validators;

import domain.Prietenie;

public class PrietenieValidator implements Validator<Prietenie> {

    @Override
    public void validate(Prietenie entity) throws ValidationException {
        if (entity.getNodPrietenie1() <= 0)
            throw new ValidationException("Id primului utilizator nu este valid!");
        if (entity.getNodPrietenie2() <= 0) {
            throw new ValidationException("Id al doilea utilizator nu este valid!");
        }
    }
}
