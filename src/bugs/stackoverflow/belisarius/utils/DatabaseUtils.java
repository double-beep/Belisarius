package bugs.stackoverflow.belisarius.utils;

import java.sql.Statement;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import bugs.stackoverflow.belisarius.database.SQLiteConnection;

public class DatabaseUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtils.class);
	
	public static void createVandalisedPostTable() {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "CREATE TABLE IF NOT EXISTS VandalisedPost(PostId integer, \n" +
		                                                      " RevisionId integer, \n" +
				                                              " OwnerId integer, \n" +
				                                              " Title text, \n" +
		                                                      " LastTitle text, \n" +
				                                              " Body text, \n" +
		                                                      " LastBody text, \n" +
				                                              " IsRollback integer, \n" +
		                                                      " PostType text, \n" +
				                                              " Comment text, \n" +
		                                                      " Site text, \n" +
				                                              " SiteUrl, \n" +
				                                              " Severity text, \n" +
				                                              " PRIMARY KEY(PostId, RevisionId));";
		                                                        
        try (Connection conn = connection.getConnection();
             Statement stmt = conn.createStatement()) {
        	stmt.execute(sql);
         } catch (SQLException e) {
			 LOGGER.info("Failed to create VandalisedPost table.", e);
         }
    }
	
	public static void createReasonTable() {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "CREATE TABLE IF NOT EXISTS Reason(ReasonId integer, \n" +
		                                              " Reason integer, \n" +
				                                      " PRIMARY KEY(ReasonId));";

		                                                        	
        try (Connection conn = connection.getConnection();
             Statement stmt = conn.createStatement()) {
        	stmt.execute(sql);
         } catch (SQLException e) {
			 LOGGER.info("Failed to create Reason table.", e);
         }
    }
	
	public static void createReasonCaughtTable() {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "CREATE TABLE IF NOT EXISTS ReasonCaught(PostId integer, \n" +
		                                                    " RevisionId integer, \n" +
				                                            " ReasonId text, \n" +
				                                            " Score integer, \n" +
				                                            " PRIMARY KEY(PostId, RevisionId, ReasonId), \n" +
						                                    " FOREIGN KEY(PostId) REFERENCES VandalisedPost(PostId), \n" +
						                                    " FOREIGN KEY(ReasonId) REFERENCES Reason(ReasonId));";
		                                                        	
        try (Connection conn = connection.getConnection();
             Statement stmt = conn.createStatement()) {
        	stmt.execute(sql);
         } catch (SQLException e) {
			 LOGGER.info("Failed to create ReasonCaught table.", e);
         }
    }
	
	public static void createBlacklistedWordTable() {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "CREATE TABLE IF NOT EXISTS BlacklistedWord(BlacklistedWordId integer, \n" +
		                                                       " BlacklistedWord text, \n" +
				                                               " PostType text, \n" +
				                                               " PRIMARY KEY(BlacklistedWordId));";
		
		try (Connection conn = connection.getConnection();
			 Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		 } catch(SQLException e) {
			 LOGGER.info("Failed to create BlacklistWord table.", e);
		 }
	}
	
	public static void createBlacklistedWordCaughtTable() {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "CREATE TABLE IF NOT EXISTS BlacklistedWordCaught(PostId integer, \n" +
		                                                             " RevisionId integer, \n" +
				                                                     " BlacklistWordId v, \n" +
				                                                     " PRIMARY KEY(PostId, RevisionId), \n " +
				                                                     " FOREIGN KEY(PostId) REFERENCES VandalisedPost(PostId), \n" +
				                                                     " FOREIGN KEY(BlacklistWordId) REFERENCES BlacklistedWord(BlacklistWordId));";
		
		try (Connection conn = connection.getConnection();
			 Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		 } catch(SQLException e) {
			 LOGGER.info("Failed to create BlacklistedWordCaught table.", e);
		 }
	}
	
	public static void createOffensiveWordTable() {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "CREATE TABLE IF NOT EXISTS OffensiveWord(OffensiveWordId integer, \n" +
		                                                     " OffensiveWord text, \n" +
				                                             " PRIMARY KEY(OffensiveWordId));";
		
		try (Connection conn = connection.getConnection();
			 Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		 } catch(SQLException e) {
			 LOGGER.info("Failed to create OffensiveWord table.", e);
		 }
	}	
	
	public static void createOffensiveWordCaughtTable() {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "CREATE TABLE IF NOT EXISTS OffensiveWordCaught(PostId integer, \n" +
		                                                           " RevisionId integer, \n" +
				                                                   " OffensiveWordId v, \n" +
				                                                   " PRIMARY KEY(PostId, RevisionId), \n " +
				                                                   " FOREIGN KEY(PostId) REFERENCES VandalisedPost(PostId), \n" +
				                                                   " FOREIGN KEY(OffensiveWordId) REFERENCES OffensiveWord(OffensiveWordId));";
		
		try (Connection conn = connection.getConnection();
			 Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		 } catch(SQLException e) {
			 LOGGER.info("Failed to create OffensiveWordCaught table.", e);
		 }
	}


	
	public static void createFeedbackTable() {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "CREATE TABLE IF NOT EXISTS Feedback(PostId integer, \n" +
		                                                " RevisionId integer, \n" +
				                                        " Feedback text, \n" +
				                                        " UserId integer, \n" +
				                                        " PRIMARY KEY(PostId, RevisionId), \n" +
				                                        " FOREIGN KEY(PostId) REFERENCES VandalisedPost(PostId));";
		                                                        	
        try (Connection conn = connection.getConnection();
             Statement stmt = conn.createStatement()) {
        	stmt.execute(sql);
         } catch (SQLException e) {
			 LOGGER.info("Failed to create Feedback table.", e);
         }
    }
	
	public static boolean checkVandalisedPostExists(long postId, int revisionId) {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "SELECT (COUNT(*) > 0) As Found FROM VandalisedPost WHERE PostId = ? AND RevisionId = ?;";
		
        try (Connection conn = connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	pstmt.setLong(1, postId);
        	pstmt.setInt(2,  revisionId);

        	ResultSet rs = pstmt.executeQuery();
        	while (rs.next()) {
        		return rs.getBoolean("Found");
        	}
        } catch (SQLException e) {
		 LOGGER.info("Failed to check for vandalised post. PostId: " + String.valueOf(postId) + "; RevisionId: " + String.valueOf(revisionId) + ".", e);
        }
		return false;
	}
	
	public static void storeVandalisedPost(long postId, int revisionId, long ownerId, String title, String lastTitle, String body, String lastBody,
			                                boolean IsRollback, String postType, String comment, String site, String siteUrl, String severity) {
		
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "INSERT INTO VandalisedPost VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		
        try (Connection conn = connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	pstmt.setLong(1, postId);
        	pstmt.setInt(2,  revisionId);
        	pstmt.setLong(3, ownerId);
        	pstmt.setString(4, title);
        	pstmt.setString(5, lastTitle);
        	pstmt.setString(6, body);
        	pstmt.setString(7, lastBody);
        	pstmt.setInt(8, (IsRollback) ? 1 : 0);
        	pstmt.setString(9, postType);
        	pstmt.setString(10, comment);
        	pstmt.setString(11, site);
        	pstmt.setString(12, siteUrl);
        	pstmt.setString(13, severity);

        	pstmt.executeUpdate();
        } catch (SQLException e) {
  			 LOGGER.info("Failed to store vandalised post. PostId: " + String.valueOf(postId) + "; RevisionId: " + String.valueOf(revisionId) + ".", e);
        }
	}
	
	public static void storeReasonCaught(long postId, int revisionId, int reasonId, double score) {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "INSERT INTO ReasonCaught(PostId, RevisionId, ReasonId, Score) VALUES (?, ?, ?, ?);";
			
		try (Connection conn = connection.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, postId);
			pstmt.setInt(2,  revisionId);
			pstmt.setInt(3, reasonId);
			pstmt.setDouble(4, score);
			
			pstmt.executeUpdate();
		} catch (SQLException e) {
 			 LOGGER.info("Failed to store reason for vandalised post. PostId: " + String.valueOf(postId) + "; RevisionId: " + String.valueOf(revisionId) + "; ReasonId: " + String.valueOf(reasonId) + ".", e);
		}
	}
	
	public static void storeFeedback(long postId, int revisionId, String feedback, long userId) {

		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "INSERT OR REPLACE INTO Feedback VALUES (?, ?, ?, ?);";
    	
		try (Connection conn = connection.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, postId);
			pstmt.setInt(2,  revisionId);
			pstmt.setString(3, feedback);
			pstmt.setLong(4, userId);
			
			pstmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.info("Failed to store feedback for vandalised post. PostId: " + String.valueOf(postId) + "; RevisionId: " + String.valueOf(revisionId) + "; Feedback: " + feedback + ".", e);
		}
	}
	
	public static HashMap<Integer, String> getBlacklistedWords(String postType) {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "SELECT BlacklistedWordId, BlacklistedWord FROM BlacklistedWord WHERE PostType = ?;";
		
		HashMap<Integer, String> blacklistedWords = new HashMap<Integer, String>();
        try (Connection conn = connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	pstmt.setString(1, postType);

        	ResultSet rs = pstmt.executeQuery();
        	while (rs.next()) {
        		blacklistedWords.put(rs.getInt("BlacklistedWordId"), rs.getString("BlacklistedWord"));
        	}
        } catch (SQLException e) {
		 LOGGER.info("Failed to get blacklisted words.", e);
        }
		return blacklistedWords;
	}
	
	public static HashMap<Integer, String> getOffensiveWords() {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "SELECT OffensiveWordId, OffensiveWord FROM OffensiveWord;";
		
		HashMap<Integer, String> offensiveWords = new HashMap<Integer, String>();
        try (Connection conn = connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

        	ResultSet rs = pstmt.executeQuery();
        	while (rs.next()) {
        		offensiveWords.put(rs.getInt("OffensiveWordId"), rs.getString("OffensiveWord"));
        	}
        } catch (SQLException e) {
		 LOGGER.info("Failed to get offensive words.", e);
        }
		return offensiveWords;
	}
	
	public static int getReasonId(String reason) {
		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "SELECT ReasonId FROM Reason WHERE Reason = ?;";
		
        try (Connection conn = connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	pstmt.setString(1, reason);

        	ResultSet rs = pstmt.executeQuery();
        	while (rs.next()) {
        		return rs.getInt("ReasonId");
        	}
        } catch (SQLException e) {
		 LOGGER.info("Failed to get blacklisted words.", e);
        }
		return 0;
	}
	
	public static void storeCaughtBlacklistedWord(long postId, int revisionId, int blacklistedWordId) {

		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "INSERT INTO BlacklistedWordCaught VALUES (?, ?, ?);";
    	
		try (Connection conn = connection.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, postId);
			pstmt.setInt(2,  revisionId);
			pstmt.setInt(3, blacklistedWordId);
			
			pstmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.info("Failed to store caught blacklisted word for vandalised post. PostId: " + String.valueOf(postId) + "; RevisionId: " + String.valueOf(revisionId) + "; BlacklistedWordId: " + String.valueOf(blacklistedWordId) + ".", e);
		}
	}
	
	public static void storeCaughtOffensiveWord(long postId, int revisionId, int offensiveWordId) {

		SQLiteConnection connection = new SQLiteConnection();
		
		String sql = "INSERT INTO OffensivedWordCaught VALUES (?, ?, ?);";
    	
		try (Connection conn = connection.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, postId);
			pstmt.setInt(2,  revisionId);
			pstmt.setInt(3, offensiveWordId);
			
			pstmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.info("Failed to store caught offensive word for vandalised post. PostId: " + String.valueOf(postId) + "; RevisionId: " + String.valueOf(revisionId) + "; OffensiveWordId: " + String.valueOf(offensiveWordId) + ".", e);
		}
	}
}