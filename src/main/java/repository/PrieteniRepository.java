package repository;

import domain.FilterUserDTO;
import domain.Prietenie;
import domain.Utilizator;
import paginare.Page;
import paginare.Pageable;

public interface PrieteniRepository extends PagingRepository<Long, Prietenie> {

    Page<Prietenie> findAllOnPage(Pageable pageable, FilterUserDTO filter);
}
