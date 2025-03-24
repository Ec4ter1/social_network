package repository.database;

import domain.Utilizator;
import domain.validators.Validator;
import paginare.Page;
import paginare.Pageable;
import repository.Repository;

import java.sql.*;
import java.util.*;

public class UtilizatorDBRepository implements Repository<Long,Utilizator> {
    private String url;
    private String username;
    private final String password;
    private final Validator<Utilizator> validator;

    public UtilizatorDBRepository(Validator<Utilizator> validator, String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    /**
     * @param id - long, the id of a user to found
     * @return Optional<User> - the user with the given id
     *                        -Optional.empty() otherwise
     */
    @Override
    public Optional<Utilizator> findOne(Long id) {
        Utilizator user;
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from utilizatori U where U.id_utilizator = '%d'", id))) {
            if(resultSet.next()){
                user = createUserFromResultSet(resultSet);
                return Optional.ofNullable(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    private Utilizator createUserFromResultSet(ResultSet resultSet) {
        try {
            String firstName = resultSet.getString("firstname");
            String lastName = resultSet.getString("lastname");
            String username = resultSet.getString("username");
            String password = resultSet.getString("password");
            String poza = resultSet.getString("poza");

            Long idd = resultSet.getLong("id_utilizator");
            Utilizator user = new Utilizator(firstName, lastName, username, password, poza);
            user.setId(idd);
            return user;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Iterable<Utilizator> findAll() {
        Set<Utilizator> users = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from utilizatori");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id_utilizator");
                String firstName = resultSet.getString("firstname");
                String lastName = resultSet.getString("lastname");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String poza = resultSet.getString("poza");

                Utilizator utilizator = new Utilizator(firstName, lastName, username, password, poza);
                utilizator.setId(id);
                users.add(utilizator);
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public Optional<Utilizator> save(Utilizator entity) {
        String sql = "insert into utilizatori (firstname, lastname, username, password, poza) values (?, ?, ?, ?, ?)";
        validator.validate(entity);
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getFirstName());
            ps.setString(2, entity.getLastName());
            ps.setString(3, entity.getUserName());
            ps.setString(4, entity.getPassword());
            ps.setString(5, entity.getImagine());
            ps.executeUpdate();
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("Eroare SQL: " + e.getMessage());
            return Optional.of(entity);
        }
    }

    @Override
    public Optional<Utilizator> delete(Long id) {
        String sql = "delete from utilizatori where id_utilizator = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            Optional<Utilizator> user = findOne(id);
            if(!user.isEmpty()) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Utilizator> update(Utilizator user) {
        if(user == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(user);
        String sql = "update utilizatori set firstname = ?, lastname = ? where id_utilizator = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1,user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setLong(3, user.getId());
            if( ps.executeUpdate() > 0 )
                return Optional.empty();
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public int size() {
        return 0;
    }

}

