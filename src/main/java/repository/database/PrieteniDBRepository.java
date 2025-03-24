package repository.database;

import domain.FilterUserDTO;
import domain.Pair;
import domain.Prietenie;
import domain.Utilizator;
import domain.validators.PrietenieValidator;
import domain.validators.Validator;
import paginare.Page;
import paginare.Pageable;
import repository.PagingRepository;
import repository.PrieteniRepository;
import repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PrieteniDBRepository implements PrieteniRepository {
    private final String url;
    private final String username;
    private final String password;
    private final Validator<Prietenie> validator;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PrieteniDBRepository(Validator<Prietenie> validator,String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Optional<Prietenie> findOne(Long id) {
        Prietenie p;
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from prietenii P where P.id_prietenie = '%d'", id))) {
            if(resultSet.next()){
                //System.out.println(resultSet.getString("id_prietenie"));
                p = createPrietenieFromResultSet(resultSet);
                return Optional.ofNullable(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Prietenie createPrietenieFromResultSet(ResultSet resultSet) {
        try {
            Long id1 = resultSet.getLong("id_prieten1");
            Long id2 = resultSet.getLong("id_prieten2");
            String dataString = resultSet.getString("friends_from");
            LocalDateTime data = null;
            if (dataString != null && !dataString.isEmpty()) {
                data = LocalDateTime.parse(dataString, formatter);
            } else {
                System.out.println("Data este NULL sau goală");
            }
            String status = resultSet.getString("status");
            Long idd = resultSet.getLong("id_prietenie");
            Prietenie p = new Prietenie(id1, id2, data);
            p.setId(idd);
            p.setStatus(status);
            return p;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Iterable<Prietenie> findAll() {
        Set<Prietenie> prietenii = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from prietenii");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id1 = resultSet.getLong("id_prieten1");
                Long id2 = resultSet.getLong("id_prieten2");
                //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String dateString = resultSet.getString("friends_from");
                LocalDateTime data = LocalDateTime.parse(dateString, formatter);
                Long iddi = resultSet.getLong("id_prietenie");
                String status = resultSet.getString("status");
                Prietenie p = new Prietenie(id1, id2, data);
                p.setId(iddi);
                p.setStatus(status);
                prietenii.add(p);
            }
            return prietenii;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prietenii;
    }

    @Override
    public Optional<Prietenie> save(Prietenie entity) {
        String sql = "insert into prietenii (id_prieten1, id_prieten2, friends_from, status) values (?, ?, ?, ?)";
        validator.validate(entity);
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, entity.getNodPrietenie1());
            ps.setLong(2, entity.getNodPrietenie2());
            String formattedDate = entity.getFriendsFrom().format(formatter);
            ps.setString(3, formattedDate);
            ps.setString(4, entity.getStatus());

            ps.executeUpdate();
        } catch (SQLException e) {
            //e.printStackTrace();
            return Optional.ofNullable(entity);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Prietenie> delete(Long id) {
        String sql = "delete from prietenii where id_prietenie = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            Optional<Prietenie> p = findOne(id);
            //System.out.println(p.get().getId());
            if(p.isPresent()) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
            return p;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Prietenie> update(Prietenie entity) {
        if(entity == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(entity);
        String sql = "update prietenii set friends_from =?, status=? where id_prietenie = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            entity.acceptFriend();
            String formattedDate = entity.getFriendsFrom().format(formatter);
            ps.setString(1, formattedDate);
            ps.setString(2, entity.getStatus());
            System.out.println(entity.getFriendsFrom() +" " + entity.getStatus() + " " + entity.getId());
            if(entity.getId()!=null) {
                System.out.println("Aciiii");
                ps.setLong(3, entity.getId());
                if (ps.executeUpdate() > 0)
                    return Optional.empty();
            }
            return Optional.ofNullable(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Page<Prietenie> findAllOnPage(Pageable pageable) {
        return findAllOnPage(pageable, null);
    }

    @Override
    public Page<Prietenie> findAllOnPage(Pageable pageable, FilterUserDTO filter) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            int totalNumberOfMovies = count(connection, filter);
            List<Prietenie> friendsOnPage;
            if (totalNumberOfMovies > 0) {
                friendsOnPage = findAllOnPage(connection, pageable, filter);
            } else {
                friendsOnPage = new ArrayList<>();
            }
            return new Page<>(friendsOnPage, totalNumberOfMovies);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Prietenie> findAllOnPage(Connection connection, Pageable pageable, FilterUserDTO filter) throws SQLException {
        List<Prietenie> prieteniiOnPage = new ArrayList<>();
        String sql = "select * from prietenii";
        Pair<String, List<Object>> sqlFilter = toSql(filter);
        if (!sqlFilter.getFirst().isEmpty()) {
            sql += " where " + sqlFilter.getFirst();
        }
        sql += " limit ? offset ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int paramIndex = 0;
            for (Object param : sqlFilter.getSecond()) {
                statement.setObject(++paramIndex, param);
            }
            statement.setInt(++paramIndex, pageable.getPageSize());
            statement.setInt(++paramIndex, pageable.getPageSize() * pageable.getPageNumber());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Prietenie p = createPrietenieFromResultSet(resultSet);
                    prieteniiOnPage.add(p);
                }
            }
        }
        return prieteniiOnPage;
    }

    private Pair<String, List<Object>> toSql(FilterUserDTO filter) {
        if (filter == null) {
            return new Pair<>("", Collections.emptyList());
        }
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        filter.getIdUser().ifPresent(idFilter -> {
            conditions.add("id_prieten1 = ? OR id_prieten2 = ?");
            params.add(idFilter);
            params.add(idFilter);
        });
        String sql = String.join(" and ", conditions);
        return new Pair<>(sql, params);
    }

    private int count(Connection connection, FilterUserDTO filter) throws SQLException {
        String sql = "select count(*) as count from prietenii";
        Pair<String, List<Object>> sqlFilter = toSql(filter);
        if (!sqlFilter.getFirst().isEmpty()) {
            sql += " where " + sqlFilter.getFirst();
        }
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int paramIndex = 0;
            for (Object param : sqlFilter.getSecond()) {
                statement.setObject(++paramIndex, param);
            }
            try (ResultSet result = statement.executeQuery()) {
                int totalNumberOfMovies = 0;
                if (result.next()) {
                    totalNumberOfMovies = result.getInt("count");
                }
                //System.out.println("nr totoal "+ totalNumberOfMovies);
                return totalNumberOfMovies;
            }
        }
    }
}

