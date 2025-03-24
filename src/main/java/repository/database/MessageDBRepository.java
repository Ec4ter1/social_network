package repository.database;

import domain.Message;
import domain.Utilizator;
import domain.validators.Validator;
import paginare.Page;
import paginare.Pageable;
import repository.Repository;

import repository.database.UtilizatorDBRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MessageDBRepository implements Repository<Long, Message> {
    private String url;
    private String username;
    private final String password;
    private final Validator<Message> validator;
    protected final Repository<Long, Utilizator> repoUsers;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public MessageDBRepository(String url, String username, String password, Validator<Message> validator, Repository<Long, Utilizator> repoUsers1) {
        this.repoUsers = repoUsers1;
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Optional<Message> findOne(Long id) {
        try (var connection = DriverManager.getConnection(url, username, password);
             var statement = connection.prepareStatement("SELECT * FROM mesaje WHERE id_mesaj = ?")) {
            statement.setLong(1, id);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Message message = createMessageFromResultSet(resultSet);
                    return Optional.of(message);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Message createMessageFromResultSet(ResultSet resultSet) throws SQLException {
        try {
            Long id = resultSet.getLong("id_mesaj");
            Long fromDB = resultSet.getLong("from_user");
            String toIds = resultSet.getString("to_user");
            String mesaj = resultSet.getString("mesaj");
            String date = resultSet.getString("data");
            LocalDateTime dataLocal = LocalDateTime.parse(date, formatter);
            long replay = resultSet.getLong("replay");

            Optional<Utilizator> u1 = repoUsers.findOne(fromDB);

            Set<Utilizator> toUsers = new HashSet<>();
            if (toIds != null && !toIds.isEmpty()) {
                for (String idString : toIds.split(",")) {
                    Optional<Utilizator> user = repoUsers.findOne(Long.parseLong(idString));
                    user.get().setId(Long.parseLong(idString));
                    toUsers.add(user.get());
                }
            }

            Message replyMessage = null;
            if (replay != 0) {
                replyMessage = findOne(replay).orElse(null);
            }
            Message message = new Message(u1.get(), new ArrayList<>(toUsers), mesaj, dataLocal);
            message.setId(id);
            message.setReply(replyMessage);

            return message;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Eroare la crearea obiectului Message din ResultSet", e);
        }

    }

    @Override
    public Iterable<Message> findAll() {
        Set<Message> mesaje = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from mesaje");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Message message = createMessageFromResultSet(resultSet);
                mesaje.add(message);
            }
            return mesaje;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mesaje;
    }

    @Override
    public Optional<Message> save(Message entity) {
        String sql = "INSERT INTO mesaje (to_user, mesaj, data, from_user, replay) VALUES (?, ?, ?, ?, ?)";
        validator.validate(entity);

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            String toUsersIds = entity.getTo().stream()
                    .map(user -> user.getId().toString())
                    .reduce((id1, id2) -> id1 + "," + id2)
                    .orElse("");
            ps.setString(1,toUsersIds);
            ps.setString(2, entity.getMessage());
            String formattedDate = entity.getData().format(formatter);
            ps.setString(3, formattedDate);
            ps.setLong(4, entity.getFrom().getId());
            //System.out.println(entity.getReply());
            if(entity.getReply() == null) {
                ps.setLong(5, 0L);
            }
            else {
                ps.setLong(5, entity.getReply().getId());
            }
            //System.out.println(entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    @Override
    public Optional<Message> delete(Long id) {
        String sql = "DELETE FROM mesaje WHERE id_mesaj = ?";
        try (var connection = DriverManager.getConnection(url, username, password);
             var statement = connection.prepareStatement(sql)) {

            Optional<Message> message = findOne(id);
            if (message.isPresent()) {
                statement.setLong(1, id);
                statement.executeUpdate();
                return message;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    @Override
    public Optional<Message> update(Message entity) {
        return Optional.empty();
    }

    @Override
    public int size() {
        return 0;
    }
}
