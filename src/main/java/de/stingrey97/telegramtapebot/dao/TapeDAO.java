package de.stingrey97.telegramtapebot.dao;

import de.stingrey97.telegramtapebot.exceptions.DatabaseException;
import de.stingrey97.telegramtapebot.model.Tape;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TapeDAO {

    /**
     * Package private constructor
     */
    TapeDAO() {
    }

    public int addTape(String title, String addedBy, String addedFor) throws DatabaseException {
        String query = "INSERT INTO tapes (title, by_username, for_username) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, title);
            stmt.setString(2, addedBy);
            stmt.setString(3, addedFor);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new DatabaseException("Creating tape failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Tape getTapeById(int id) throws DatabaseException {
        String query = "SELECT id, title, by_username, for_username, date_added FROM tapes WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    String byUsername = rs.getString("by_username");
                    String forUsername = rs.getString("for_username");
                    Date added = new Date(rs.getTimestamp("date_added").getTime());
                    return new Tape(id, title, byUsername, forUsername, added);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Tape getLastAddedTape() throws DatabaseException {
        String query = "SELECT id, title, by_username, for_username, date_added FROM tapes ORDER BY date_added DESC LIMIT 1";
        List<Tape> tapes = executeQuery(query);
        return tapes.isEmpty() ? null : tapes.getFirst();
    }

    public int markTapeAsDeleted(int tapeId) throws DatabaseException {
        String query = "UPDATE tapes SET title = 'deleted', by_username = 'deleted', for_username = 'deleted' WHERE id = ?";
        int affectedRows;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, tapeId);
            affectedRows = stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return affectedRows;
    }

    public List<Tape> getTapesByUser(String username) throws DatabaseException {
        String query = "SELECT * FROM tapes WHERE by_username = ?";
        return getTapesWithParameter(username, query);
    }

    public List<Tape> getTapesForUser(String username) throws DatabaseException {
        String query = "SELECT * FROM tapes WHERE for_username = ?";
        return getTapesWithParameter(username, query);
    }

    /*
    Do not delete tapes. Mark them as deleted. See TapeService.deleteTape(int id)

    public int deleteTape(long id) throws DatabaseException {
        String query = "DELETE FROM tapes WHERE id = ?";
        int affectedRows;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, id);
            affectedRows = stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return affectedRows;
    }
    */

    public List<Tape> getAllTapes() throws DatabaseException {
        String query = "SELECT * FROM tapes";
        return executeQuery(query);
    }

    @NotNull
    private List<Tape> getTapesWithParameter(String username, String query) throws DatabaseException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return mapResultSetToTapes(rs);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @NotNull
    private List<Tape> executeQuery(String query) throws DatabaseException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetToTapes(rs);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @NotNull
    private List<Tape> mapResultSetToTapes(ResultSet rs) throws SQLException {
        List<Tape> tapes = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            String title = rs.getString("title");
            String addedBy = rs.getString("by_username");
            String addedFor = rs.getString("for_username");
            Date added = new Date(rs.getTimestamp("date_added").getTime());
            tapes.add(new Tape(id, title, addedBy, addedFor, added));
        }
        return tapes;
    }
}