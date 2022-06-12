package com.leon.xplayer.DB;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserConnect {

    public static boolean register(String name, String password) {

        Connection conn = JdbcUtil.getConnection("music_player_db", "root", "root");

        if (conn == null) {
            Log.d("login", "register:conn is null");
            return false;
        } else {
            String sql = "insert into user(user,pass) values(?,?)";
            try {
                PreparedStatement pre = conn.prepareStatement(sql);
                pre.setString(1, name);

                pre.setString(2, password);
                return pre.execute();
            } catch (SQLException e) {
                return false;
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean login(String name, String password) {

        Connection conn = JdbcUtil.getConnection("music_player_db", "root", "root");

        if (conn == null) {
            Log.i("login", "conn is null");
            return false;
        } else {
            String sql = "select * from user where user=? and pass=?";
            try {
                PreparedStatement pres = conn.prepareStatement(sql);
                pres.setString(1, name);
                pres.setString(2, password);
                ResultSet res = pres.executeQuery();
                return res.next();
            } catch (SQLException e) {
                return false;
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}

class JdbcUtil {
    static final String host = "119.3.139.21";
    static final String port = "3306";

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Log.d("sql", "getConnection: not found ");
        }
    }

    public static Connection getConnection(String dbName, String name, String password) {
        try {
            return DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false",
                    name, password
            );
        } catch (SQLException e) {
            Log.d("sql", "getConnection: error");
            return null;
        }
    }
}

