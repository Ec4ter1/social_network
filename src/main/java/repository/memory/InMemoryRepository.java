package repository.memory;

import domain.Entity;
import domain.validators.ValidationException;
import domain.validators.Validator;
import repository.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {

    private final Validator<E> validator;
    protected Map<ID, E> entities;

    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
        entities = new HashMap<ID, E>();

    }

    @Override
    public Optional<E> findOne(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }


    /**
     * @param entity entity must be not null
     * @return null- if the given entity is saved
     * otherwise returns the entity (id already exists)
     * @throws ValidationException      if the entity is not valid
     * @throws IllegalArgumentException if the given entity is null.
     */
    @Override
    public Optional<E> save(E entity) throws ValidationException {

        if (entity == null)
            throw new IllegalArgumentException("ENTITY CANNOT BE NULL");
        validator.validate(entity);

        if (entities.containsKey(entity.getId()))
            return Optional.ofNullable(entities.get(entity.getId()));
        entities.put(entity.getId(), entity);
        return Optional.empty();
    }

    @Override
    public Optional<E> delete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        E entity = entities.get(id);
        if(entity == null)
            return Optional.empty();

        return Optional.ofNullable(entities.remove(entity.getId()));
    }

    @Override
    public Optional<E> update(E entity) throws ValidationException {
        if (entity == null) {
            throw new IllegalArgumentException("ENTITY is null");
        }
        validator.validate(entity);
        entities.put(entity.getId(), entity);
        if (entities.get(entity.getId()) != null) {
            entities.put(entity.getId(), entity);
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    @Override
    public int size() {
        return entities.size();
    }

}
