package by.htp.ex.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.mindrot.jbcrypt.BCrypt;

import by.htp.ex.bean.NewUserInfo;
import by.htp.ex.dao.DaoException;
import by.htp.ex.dao.IUserDAO;
import by.htp.ex.dao.connectionPool.ConnectionPool;
import by.htp.ex.dao.connectionPool.ConnectionPoolException;

public class UserDAO implements IUserDAO {

	private static final String SQL_QUERY_FOR_LOGINATION = "SELECT * FROM users WHERE login=?";
	private static final String QUEST_ROLE = "quest";
	private static final String SQL_ROLE_PARAM = "title";

	@Override
	public boolean logination(String login, String password) throws DaoException {

		boolean isLoggedIn = false;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try (Connection connection = ConnectionPool.getInstance().takeConnection()) {
			try {
				statement = connection.prepareStatement(SQL_QUERY_FOR_LOGINATION);
				statement.setString(1, login);
				resultSet = statement.executeQuery();

				while (resultSet.next()) {
					isLoggedIn = BCrypt.checkpw(password, resultSet.getString("password"));
				}
				return isLoggedIn;
			} catch (SQLException e) {
				throw new DaoException("error during execution of request", e);
			}
		} catch (ConnectionPoolException e) {
			throw new DaoException("error trying to take connection", e);
		} catch (SQLException e) {
			throw new DaoException("error in sql", e);
		}
	}

	private static final String SQL_QUERY_FIND_ROLE = "SELECT * FROM users INNER JOIN roles ON users.roles_id=roles.id WHERE login=?";

	public String getRole(String login, String password) throws DaoException {
		
		String role = QUEST_ROLE;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try (Connection connection = ConnectionPool.getInstance().takeConnection()) {
			try {
				statement = connection.prepareStatement(SQL_QUERY_FIND_ROLE);
				statement.setString(1, login);
				resultSet = statement.executeQuery();
				while (resultSet.next()) {
					if (BCrypt.checkpw(password, resultSet.getString("password"))) {
						role = resultSet.getString(SQL_ROLE_PARAM);
					}
				}
				return role;
			} catch (SQLException e) {
				throw new DaoException("error in sql", e);
			}
		} catch (ConnectionPoolException e) {
			throw new DaoException("error trying to take connection", e);
		} catch (SQLException e) {
			throw new DaoException("error in sql", e);
		}
	}

	private static final String SQL_QUERY_ADD_NEW_USER = "INSERT INTO users(login,password,roles_id,user_status_id,date_of_creation) VALUES(?,?,?,?,?)";
	private static final String SQL_QUERY_ADD_NEW_USER_DETAILS = "INSERT INTO user_details(users_id,name,surname,email) VALUES(?,?,?,?)";
	private static final String SQL_QUERY_FIND_ID_BY_LOGIN = "SELECT * FROM users WHERE login=?";

	@Override
	public boolean registration(NewUserInfo user) throws DaoException {

		boolean isNotRegistered = true;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection;

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String dateTime = formatter.format(date);

		String hashpw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

		try {
			connection = ConnectionPool.getInstance().takeConnection();
		} catch (ConnectionPoolException e) {
			throw new DaoException("error trying to take connection", e);
		}

		try {
			statement = connection.prepareStatement(SQL_QUERY_FIND_ID_BY_LOGIN);
			statement.setString(1, user.getLogin());
			resultSet = statement.executeQuery();

			while (resultSet.next()) {
				isNotRegistered = false;
				return isNotRegistered;
			}
			statement = connection.prepareStatement(SQL_QUERY_ADD_NEW_USER);
			statement.setString(1, user.getLogin());
			statement.setString(2, hashpw);
			statement.setInt(3, 1);
			statement.setInt(4, 1);
			statement.setString(5, dateTime);
			statement.executeUpdate();
			try {
				statement.close();
			} catch (SQLException e) {
				throw new DaoException("error trying to close statement", e);
			}
			statement = connection.prepareStatement(SQL_QUERY_FIND_ID_BY_LOGIN);
			statement.setString(1, user.getLogin());
			resultSet = statement.executeQuery();
			int id = 0;

			while (resultSet.next()) {
				id = resultSet.getInt("id");
			}
			try {
				statement.close();
			} catch (SQLException e) {
				throw new DaoException("error trying to close statement", e);
			}
			statement = connection.prepareStatement(SQL_QUERY_ADD_NEW_USER_DETAILS);
			statement.setInt(1, id);
			statement.setString(2, user.getName());
			statement.setString(3, user.getSurname());
			statement.setString(4, user.getEmail());
			statement.executeUpdate();

			return isNotRegistered;

		} catch (SQLException e) {
			System.out.println(e.toString());
			throw new DaoException("error in registration", e);
		}

	}

}
