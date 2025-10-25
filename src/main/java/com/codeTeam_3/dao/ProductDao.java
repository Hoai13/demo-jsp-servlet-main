package com.codeTeam_3.dao;

import com.codeTeam_3.model.ProductView;
import com.codeTeam_3.util.dbMysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    public List<ProductView> findByCategory(int categoryId, String lang, int limit) {
        String sql = """
                    SELECT p.ProductID, p.Price, p.Weight,
                           pt.ProductName, pt.ProductDescription,
                           pct.CategoryName
                    FROM Product p
                    LEFT JOIN ProductTranslation pt
                      ON pt.ProductID = p.ProductID AND pt.LanguageID = ?
                    LEFT JOIN ProductCategoryTranslation pct
                      ON pct.ProductCategoryID = p.ProductCategoryID AND pct.LanguageID = ?
                    WHERE p.ProductCategoryID = ?
                    ORDER BY p.ProductID
                    LIMIT ?
                """;
        List<ProductView> list = new ArrayList<>();
        try (Connection con = dbMysql.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lang);
            ps.setString(2, lang);
            ps.setInt(3, categoryId);
            ps.setInt(4, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ProductView(
                            rs.getInt("ProductID"),
                            rs.getString("ProductName"),
                            rs.getBigDecimal("Price"),
                            rs.getBigDecimal("Weight"),
                            rs.getString("CategoryName"),
                            rs.getString("ProductDescription")
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<ProductView> findAll(String lang) {
        String sql = """
            SELECT p.ProductID, p.Price, p.Weight,
                   pt.ProductName, pt.ProductDescription,
                   pct.CategoryName
            FROM Product p
            LEFT JOIN ProductTranslation pt
              ON pt.ProductID = p.ProductID AND pt.LanguageID = ?
            LEFT JOIN ProductCategoryTranslation pct
              ON pct.ProductCategoryID = p.ProductCategoryID AND pct.LanguageID = ?
            ORDER BY p.ProductID
        """;
        List<ProductView> list = new ArrayList<>();
        try (Connection conn = dbMysql.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lang);
            ps.setString(2, lang);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ProductView(
                            rs.getInt("ProductID"),
                            rs.getString("ProductName"),
                            rs.getBigDecimal("Price"),
                            rs.getBigDecimal("Weight"),
                            rs.getString("CategoryName"),
                            rs.getString("ProductDescription")
                    ));
                }
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ProductView findById(int id, String lang) {
        String sql = """
            SELECT p.ProductID, p.Price, p.Weight,
                   pt.ProductName, pt.ProductDescription,
                   pct.CategoryName
            FROM Product p
            LEFT JOIN ProductTranslation pt
              ON pt.ProductID = p.ProductID AND pt.LanguageID = ?
            LEFT JOIN ProductCategoryTranslation pct
              ON pct.ProductCategoryID = p.ProductCategoryID AND pct.LanguageID = ?
            WHERE p.ProductID = ?
        """;
        try (Connection c = dbMysql.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, lang);
            ps.setString(2, lang);
            ps.setInt(3, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ProductView(
                            rs.getInt("ProductID"),
                            rs.getString("ProductName"),
                            rs.getBigDecimal("Price"),
                            rs.getBigDecimal("Weight"),
                            rs.getString("CategoryName"),
                            rs.getString("ProductDescription")
                    );
                }
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public boolean deleteById(int id) {
        String delTr = "DELETE FROM ProductTranslation WHERE ProductID = ?";
        String delPd = "DELETE FROM Product WHERE ProductID = ?";
        try (Connection c = dbMysql.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p1 = c.prepareStatement(delTr);
                 PreparedStatement p2 = c.prepareStatement(delPd)) {
                p1.setInt(1, id);
                p1.executeUpdate();
                p2.setInt(1, id);
                int rows = p2.executeUpdate();
                c.commit();
                return rows > 0;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void updateCore(int id, java.math.BigDecimal price, java.math.BigDecimal weight, int categoryId) {
        String sql = "UPDATE Product SET Price=?, Weight=?, ProductCategoryID=? WHERE ProductID=?";
        try (Connection c = dbMysql.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, price);
            ps.setBigDecimal(2, weight);
            ps.setInt(3, categoryId);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void upsertTranslation(int productId, String lang, String name, String desc) {
        String sql = """
      INSERT INTO ProductTranslation(ProductID, LanguageID, ProductName, ProductDescription)
      VALUES(?,?,?,?)
      ON DUPLICATE KEY UPDATE ProductName=VALUES(ProductName), ProductDescription=VALUES(ProductDescription)
    """;
        try (Connection c = dbMysql.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, lang);
            ps.setString(3, name);
            ps.setString(4, desc);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<ProductView> search(String keyword, String lang) {
        String sql = """
            SELECT p.ProductID, p.Price, p.Weight,
                   pt.ProductName, pt.ProductDescription,
                   COALESCE(pct.CategoryName, '') AS CategoryName
            FROM Product p
            LEFT JOIN ProductTranslation pt
              ON pt.ProductID = p.ProductID AND pt.LanguageID = ?
            LEFT JOIN ProductCategoryTranslation pct
              ON pct.ProductCategoryID = p.ProductCategoryID AND pct.LanguageID = ?
            WHERE COALESCE(pt.ProductName,'') LIKE ?
               OR COALESCE(pt.ProductDescription,'') LIKE ?
            ORDER BY p.ProductID
        """;

        List<ProductView> list = new ArrayList<>();
        try (Connection c = dbMysql.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, lang);
            ps.setString(2, lang);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ProductView(
                            rs.getInt("ProductID"),
                            rs.getString("ProductName"),
                            rs.getBigDecimal("Price"),
                            rs.getBigDecimal("Weight"),
                            rs.getString("CategoryName"),
                            rs.getString("ProductDescription")
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

}
