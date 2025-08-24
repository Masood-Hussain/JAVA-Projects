package com.facerecognition.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.util.*;

/**
 * Database Manager for storing and retrieving face embeddings
 * Uses SQLite for lightweight, embedded database storage
 * 
 * Features:
 * - Face embedding storage and retrieval
 * - Person management (add, update, delete)
 * - Automatic database initialization
 * - Optimized queries for real-time performance
 * - Transaction support for data integrity
 * 
 * @author Face Recognition System
 * @version 1.0
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    
    // Database configuration
    private static final String DATABASE_NAME = "face_recognition.db";
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_NAME;
    
    // SQL statements
    private static final String CREATE_PERSONS_TABLE = 
        "CREATE TABLE IF NOT EXISTS persons (" +
        "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "    name TEXT NOT NULL UNIQUE," +
        "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
        ")";
    
    private static final String CREATE_FACE_EMBEDDINGS_TABLE = 
        "CREATE TABLE IF NOT EXISTS face_embeddings (" +
        "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "    person_id INTEGER NOT NULL," +
        "    embedding BLOB NOT NULL," +
        "    embedding_size INTEGER NOT NULL," +
        "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "    FOREIGN KEY (person_id) REFERENCES persons(id) ON DELETE CASCADE" +
        ")";
    
    private static final String CREATE_PERSON_INDEX = 
        "CREATE INDEX IF NOT EXISTS idx_persons_name ON persons(name)";
    
    private static final String CREATE_EMBEDDING_INDEX = 
        "CREATE INDEX IF NOT EXISTS idx_embeddings_person ON face_embeddings(person_id)";
    
    // Connection management
    private Connection connection;
    private boolean isInitialized = false;
    
    /**
     * Default constructor
     */
    public DatabaseManager() {
        // Constructor will be followed by initialize() call
    }
    
    /**
     * Initialize the database connection and create tables
     * @throws SQLException if database initialization fails
     */
    public void initialize() throws SQLException {
        try {
            logger.info("Initializing database: {}", DATABASE_NAME);
            
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Create connection
            connection = DriverManager.getConnection(DATABASE_URL);
            connection.setAutoCommit(true); // Enable auto-commit by default
            
            // Enable foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            
            // Create tables
            createTables();
            
            // Create indexes for better performance
            createIndexes();
            
            isInitialized = true;
            logger.info("Database initialized successfully");
            
        } catch (ClassNotFoundException e) {
            logger.error("SQLite JDBC driver not found", e);
            throw new SQLException("SQLite driver not available", e);
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeEx) {
                    logger.error("Error closing connection after failed initialization", closeEx);
                }
            }
            throw e;
        }
    }
    
    /**
     * Create database tables
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create persons table
            stmt.execute(CREATE_PERSONS_TABLE);
            logger.debug("Persons table created/verified");
            
            // Create face_embeddings table
            stmt.execute(CREATE_FACE_EMBEDDINGS_TABLE);
            logger.debug("Face embeddings table created/verified");
        }
    }
    
    /**
     * Create database indexes for performance
     */
    private void createIndexes() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_PERSON_INDEX);
            stmt.execute(CREATE_EMBEDDING_INDEX);
            logger.debug("Database indexes created/verified");
        }
    }
    
    /**
     * Store a face embedding for a person
     * @param personName Name of the person
     * @param embedding Face embedding vector
     * @return true if successful, false otherwise
     */
    public boolean storeFaceEmbedding(String personName, double[] embedding) {
        if (!isInitialized) {
            logger.error("Database not initialized");
            return false;
        }
        
        if (personName == null || personName.trim().isEmpty()) {
            logger.error("Person name cannot be null or empty");
            return false;
        }
        
        if (embedding == null || embedding.length == 0) {
            logger.error("Face embedding cannot be null or empty");
            return false;
        }
        
        try {
            connection.setAutoCommit(false); // Start transaction
            
            // Get or create person ID
            int personId = getOrCreatePersonId(personName.trim());
            
            // Store the embedding
            String sql = "INSERT INTO face_embeddings (person_id, embedding, embedding_size) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, personId);
                pstmt.setBytes(2, serializeEmbedding(embedding));
                pstmt.setInt(3, embedding.length);
                
                int result = pstmt.executeUpdate();
                
                if (result > 0) {
                    connection.commit();
                    logger.info("Stored face embedding for person: {} (ID: {})", personName, personId);
                    return true;
                } else {
                    connection.rollback();
                    logger.error("Failed to insert face embedding");
                    return false;
                }
            }
            
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Error during rollback", rollbackEx);
            }
            logger.error("Error storing face embedding for person: " + personName, e);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error("Error resetting auto-commit", e);
            }
        }
    }
    
    /**
     * Get or create a person ID by name
     * @param personName Name of the person
     * @return Person ID
     */
    private int getOrCreatePersonId(String personName) throws SQLException {
        // First, try to find existing person
        String selectSql = "SELECT id FROM persons WHERE name = ?";
        try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
            selectStmt.setString(1, personName);
            
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int personId = rs.getInt("id");
                    logger.debug("Found existing person: {} (ID: {})", personName, personId);
                    return personId;
                }
            }
        }
        
        // Person doesn't exist, create new one
        String insertSql = "INSERT INTO persons (name) VALUES (?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, personName);
            
            int result = insertStmt.executeUpdate();
            if (result > 0) {
                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int personId = generatedKeys.getInt(1);
                        logger.info("Created new person: {} (ID: {})", personName, personId);
                        return personId;
                    }
                }
            }
            
            throw new SQLException("Failed to create person: " + personName);
        }
    }
    
    /**
     * Retrieve all face embeddings with person names
     * @return List of maps containing person_name and embedding
     */
    public List<Map<String, Object>> getAllFaceEmbeddings() {
        List<Map<String, Object>> embeddings = new ArrayList<>();
        
        if (!isInitialized) {
            logger.error("Database not initialized");
            return embeddings;
        }
        
        String sql = "SELECT p.name as person_name, fe.embedding, fe.embedding_size " +
                    "FROM face_embeddings fe " +
                    "JOIN persons p ON fe.person_id = p.id " +
                    "ORDER BY p.name, fe.created_at";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> record = new HashMap<>();
                record.put("person_name", rs.getString("person_name"));
                record.put("embedding", deserializeEmbedding(rs.getBytes("embedding")));
                record.put("embedding_size", rs.getInt("embedding_size"));
                
                embeddings.add(record);
            }
            
            logger.debug("Retrieved {} face embeddings from database", embeddings.size());
            
        } catch (Exception e) {
            logger.error("Error retrieving face embeddings", e);
        }
        
        return embeddings;
    }
    
    /**
     * Get face embeddings for a specific person
     * @param personName Name of the person
     * @return List of embeddings for the person
     */
    public List<double[]> getFaceEmbeddings(String personName) {
        List<double[]> embeddings = new ArrayList<>();
        
        if (!isInitialized) {
            logger.error("Database not initialized");
            return embeddings;
        }
        
        String sql = "SELECT fe.embedding " +
                    "FROM face_embeddings fe " +
                    "JOIN persons p ON fe.person_id = p.id " +
                    "WHERE p.name = ? " +
                    "ORDER BY fe.created_at";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, personName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double[] embedding = deserializeEmbedding(rs.getBytes("embedding"));
                    embeddings.add(embedding);
                }
            }
            
            logger.debug("Retrieved {} embeddings for person: {}", embeddings.size(), personName);
            
        } catch (Exception e) {
            logger.error("Error retrieving embeddings for person: " + personName, e);
        }
        
        return embeddings;
    }
    
    /**
     * Get all registered person names
     * @return List of person names
     */
    public List<String> getAllPersonNames() {
        List<String> names = new ArrayList<>();
        
        if (!isInitialized) {
            logger.error("Database not initialized");
            return names;
        }
        
        String sql = "SELECT name FROM persons ORDER BY name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
            
            logger.debug("Retrieved {} registered persons", names.size());
            
        } catch (SQLException e) {
            logger.error("Error retrieving person names", e);
        }
        
        return names;
    }
    
    /**
     * Delete a person and all their face embeddings
     * @param personName Name of the person to delete
     * @return true if successful, false otherwise
     */
    public boolean deletePerson(String personName) {
        if (!isInitialized) {
            logger.error("Database not initialized");
            return false;
        }
        
        if (personName == null || personName.trim().isEmpty()) {
            logger.error("Person name cannot be null or empty");
            return false;
        }
        
        // First delete face embeddings for this person
        String deleteEmbeddingsSQL = "DELETE FROM face_embeddings WHERE person_id = (SELECT id FROM persons WHERE name = ?)";
        String deletePersonSQL = "DELETE FROM persons WHERE name = ?";
        
        try (PreparedStatement deleteEmbeddings = connection.prepareStatement(deleteEmbeddingsSQL);
             PreparedStatement deletePerson = connection.prepareStatement(deletePersonSQL)) {
            
            // Delete face embeddings first
            deleteEmbeddings.setString(1, personName.trim());
            int embeddingsDeleted = deleteEmbeddings.executeUpdate();
            
            // Then delete the person
            deletePerson.setString(1, personName.trim());
            int personDeleted = deletePerson.executeUpdate();
            
            if (personDeleted > 0) {
                logger.info("Deleted person '{}' and {} face embeddings", personName, embeddingsDeleted);
                return true;
            } else {
                logger.warn("Person not found for deletion: {}", personName);
                return false;
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting person: " + personName, e);
            return false;
        }
    }
    
    /**
     * Get database statistics
     * @return Map containing database statistics
     */
    public Map<String, Integer> getDatabaseStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("persons", 0);
        stats.put("embeddings", 0);
        
        if (!isInitialized) {
            return stats;
        }
        
        try {
            // Count persons
            String personCountSql = "SELECT COUNT(*) as count FROM persons";
            try (PreparedStatement pstmt = connection.prepareStatement(personCountSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("persons", rs.getInt("count"));
                }
            }
            
            // Count embeddings
            String embeddingCountSql = "SELECT COUNT(*) as count FROM face_embeddings";
            try (PreparedStatement pstmt = connection.prepareStatement(embeddingCountSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("embeddings", rs.getInt("count"));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error getting database statistics", e);
        }
        
        return stats;
    }
    
    /**
     * Clear all persons and embeddings (useful for incompatible data cleanup)
     * @return true if successful, false otherwise
     */
    public boolean clearAllData() {
        if (!isInitialized) {
            logger.error("Database not initialized");
            return false;
        }
        
        try {
            // Delete all face embeddings first
            String deleteEmbeddingsSQL = "DELETE FROM face_embeddings";
            try (PreparedStatement stmt = connection.prepareStatement(deleteEmbeddingsSQL)) {
                int embeddingsDeleted = stmt.executeUpdate();
                logger.info("Deleted {} face embeddings", embeddingsDeleted);
            }
            
            // Delete all persons
            String deletePersonsSQL = "DELETE FROM persons";
            try (PreparedStatement stmt = connection.prepareStatement(deletePersonsSQL)) {
                int personsDeleted = stmt.executeUpdate();
                logger.info("Deleted {} persons", personsDeleted);
            }
            
            // Reset auto-increment counters
            String resetPersonsSQL = "DELETE FROM sqlite_sequence WHERE name='persons'";
            String resetEmbeddingsSQL = "DELETE FROM sqlite_sequence WHERE name='face_embeddings'";
            
            try (PreparedStatement stmt1 = connection.prepareStatement(resetPersonsSQL);
                 PreparedStatement stmt2 = connection.prepareStatement(resetEmbeddingsSQL)) {
                stmt1.executeUpdate();
                stmt2.executeUpdate();
                logger.info("Reset database sequences");
            }
            
            return true;
            
        } catch (SQLException e) {
            logger.error("Error clearing database data", e);
            return false;
        }
    }
    
    /**
     * Serialize embedding array to byte array for database storage
     * @param embedding The embedding array
     * @return Serialized byte array
     */
    private byte[] serializeEmbedding(double[] embedding) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(embedding);
        oos.close();
        return baos.toByteArray();
    }
    
    /**
     * Deserialize byte array back to embedding array
     * @param data Serialized byte array
     * @return Embedding array
     */
    private double[] deserializeEmbedding(byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        double[] embedding = (double[]) ois.readObject();
        ois.close();
        return embedding;
    }
    
    /**
     * Check if database is initialized and connected
     * @return true if ready for operations
     */
    public boolean isInitialized() {
        return isInitialized && connection != null;
    }
    
    /**
     * Close database connection and cleanup resources
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            } finally {
                connection = null;
                isInitialized = false;
            }
        }
    }
    
    // Note: Using try-with-resources or explicit close() calls is preferred over finalize()
    // The close() method should be called explicitly when done with the DatabaseManager
}
