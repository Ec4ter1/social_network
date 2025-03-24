package repository;

import domain.Entity;
import paginare.Page;
import paginare.Pageable;

public interface PagingRepository<ID , E extends Entity<ID>> extends Repository<ID, E> {
    Page<E> findAllOnPage(Pageable pageable);
}

