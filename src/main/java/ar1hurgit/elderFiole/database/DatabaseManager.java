package ar1hurgit.elderFiole.database;

import ar1hurgit.elderFiole.ElderFiole;
import ar1hurgit.elderFiole.data.ActiveBoost;
import ar1hurgit.elderFiole.data.JobBoostItem;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private final ElderFiole plugin;
    private Connection connection;

    public DatabaseManager(ElderFiole plugin) {
        this.plugin = plugin;
        initDatabase();
    }

    private void initDatabase() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "elderfiole.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);

            // Create tables
            createTables();
            plugin.getLogger().info("[ElderFiole] Base de données SQLite initialisée.");
        } catch (SQLException e) {
            plugin.getLogger()
                    .severe("[ElderFiole] Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String createBoostsTable = """
                CREATE TABLE IF NOT EXISTS active_boosts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid TEXT NOT NULL,
                    job_name TEXT NOT NULL,
                    multiplier REAL NOT NULL,
                    expiration_time INTEGER NOT NULL,
                    created_at INTEGER NOT NULL
                );
                """;

        String createJobItemsTable = """
                CREATE TABLE IF NOT EXISTS job_boost_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid TEXT NOT NULL,
                    job_name TEXT NOT NULL,
                    multiplier REAL NOT NULL,
                    duration_minutes INTEGER NOT NULL,
                    remaining_time INTEGER NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 0,
                    is_permanent INTEGER NOT NULL DEFAULT 0,
                    created_at INTEGER NOT NULL,
                    UNIQUE(player_uuid, job_name)
                );
                """;

        String createDailyCooldownsTable = """
                CREATE TABLE IF NOT EXISTS daily_cooldowns (
                    player_uuid TEXT PRIMARY KEY,
                    last_use_time INTEGER NOT NULL
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createBoostsTable);
            stmt.execute(createJobItemsTable);
            stmt.execute(createDailyCooldownsTable);
        }
    }

    // ========== Active Boosts ==========

    public void saveActiveBoost(UUID playerUuid, ActiveBoost boost) {
        String sql = "INSERT INTO active_boosts (player_uuid, job_name, multiplier, expiration_time, created_at) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, boost.getJobName());
            pstmt.setDouble(3, boost.getMultiplier());
            pstmt.setLong(4, boost.getExpirationTime());
            pstmt.setLong(5, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[ElderFiole] Erreur lors de la sauvegarde du boost: " + e.getMessage());
        }
    }

    public Map<UUID, List<ActiveBoost>> loadActiveBoosts() {
        Map<UUID, List<ActiveBoost>> boosts = new HashMap<>();
        String sql = "SELECT player_uuid, job_name, multiplier, expiration_time FROM active_boosts WHERE expiration_time > ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, System.currentTimeMillis());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                String jobName = rs.getString("job_name");
                double multiplier = rs.getDouble("multiplier");
                long expiration = rs.getLong("expiration_time");

                ActiveBoost boost = new ActiveBoost(jobName, multiplier, expiration);
                boosts.computeIfAbsent(uuid, k -> new ArrayList<>()).add(boost);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[ElderFiole] Erreur lors du chargement des boosts: " + e.getMessage());
        }

        return boosts;
    }

    public void clearExpiredBoosts() {
        String sql = "DELETE FROM active_boosts WHERE expiration_time <= ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[ElderFiole] Erreur lors du nettoyage des boosts expirés: " + e.getMessage());
        }
    }

    public void clearPlayerBoosts(UUID playerUuid) {
        String sql = "DELETE FROM active_boosts WHERE player_uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger()
                    .severe("[ElderFiole] Erreur lors de la suppression des boosts du joueur: " + e.getMessage());
        }
    }

    // ========== Job Boost Items ==========

    public void saveJobBoostItem(UUID playerUuid, JobBoostItem item) {
        String sql = """
                INSERT OR REPLACE INTO job_boost_items
                (player_uuid, job_name, multiplier, duration_minutes, remaining_time, is_active, is_permanent, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, item.getJobName());
            pstmt.setDouble(3, item.getMultiplier());
            pstmt.setInt(4, item.getDurationMinutes());
            pstmt.setInt(5, item.getRemainingTime());
            pstmt.setInt(6, item.isActive() ? 1 : 0);
            pstmt.setInt(7, item.isPermanent() ? 1 : 0);
            pstmt.setLong(8, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger()
                    .severe("[ElderFiole] Erreur lors de la sauvegarde de l'item de boost: " + e.getMessage());
        }
    }

    public Map<UUID, List<JobBoostItem>> loadJobBoostItems() {
        Map<UUID, List<JobBoostItem>> items = new HashMap<>();
        String sql = "SELECT * FROM job_boost_items";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                String jobName = rs.getString("job_name");
                double multiplier = rs.getDouble("multiplier");
                int durationMinutes = rs.getInt("duration_minutes");
                int remainingTime = rs.getInt("remaining_time");
                boolean isActive = rs.getInt("is_active") == 1;
                boolean isPermanent = rs.getInt("is_permanent") == 1;

                JobBoostItem item = new JobBoostItem(jobName, multiplier, durationMinutes, isPermanent);
                item.setRemainingTime(remainingTime);
                item.setActive(isActive);

                items.computeIfAbsent(uuid, k -> new ArrayList<>()).add(item);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[ElderFiole] Erreur lors du chargement des items de boost: " + e.getMessage());
        }

        return items;
    }

    public void deleteJobBoostItem(UUID playerUuid, String jobName) {
        String sql = "DELETE FROM job_boost_items WHERE player_uuid = ? AND job_name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, jobName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger()
                    .severe("[ElderFiole] Erreur lors de la suppression de l'item de boost: " + e.getMessage());
        }
    }

    // ========== Daily Cooldowns ==========

    public void saveDailyCooldown(UUID playerUuid, long lastUseTime) {
        String sql = "INSERT OR REPLACE INTO daily_cooldowns (player_uuid, last_use_time) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setLong(2, lastUseTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[ElderFiole] Erreur lors de la sauvegarde du cooldown: " + e.getMessage());
        }
    }

    public Long loadDailyCooldown(UUID playerUuid) {
        String sql = "SELECT last_use_time FROM daily_cooldowns WHERE player_uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("last_use_time");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[ElderFiole] Erreur lors du chargement du cooldown: " + e.getMessage());
        }

        return null;
    }

    public Map<UUID, Long> loadAllDailyCooldowns() {
        Map<UUID, Long> cooldowns = new HashMap<>();
        String sql = "SELECT player_uuid, last_use_time FROM daily_cooldowns";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                long lastUseTime = rs.getLong("last_use_time");
                cooldowns.put(uuid, lastUseTime);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[ElderFiole] Erreur lors du chargement des cooldowns: " + e.getMessage());
        }

        return cooldowns;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("[ElderFiole] Connexion à la base de données fermée.");
            }
        } catch (SQLException e) {
            plugin.getLogger()
                    .severe("[ElderFiole] Erreur lors de la fermeture de la base de données: " + e.getMessage());
        }
    }
}
