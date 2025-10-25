package com.codeTeam_3.controller;

import com.codeTeam_3.dao.CategoryDao;
import com.codeTeam_3.dao.ProductDao;
import com.codeTeam_3.model.Category;
import com.codeTeam_3.model.ProductView;

import com.codeTeam_3.web.LangUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "home", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {
    private final CategoryDao categoryDao = new CategoryDao();
    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String lang = LangUtil.resolveLang(req);  // Lấy lang từ param/session/cookie
        List<Category> categories = categoryDao.findAll(lang);

        String keyword = req.getParameter("keyword");
        List<ProductView> products;

        if (keyword != null && !keyword.isBlank()) {
            // Nếu có từ khóa thì tìm tất cả sản phẩm phù hợp
            products = productDao.search(keyword, lang);
            // Không highlight category khi search
            req.setAttribute("activeCatId", -1);
        } else {
            // Lấy category active
            Integer catParam = null;
            try { catParam = Integer.valueOf(req.getParameter("cat")); } catch (Exception ignore) {}
            int activeCatId = (catParam != null) ? catParam :
                    (categories.isEmpty() ? -1 : categories.get(0).getId());

            products = activeCatId > 0
                    ? productDao.findByCategory(activeCatId, lang, 30)
                    : java.util.Collections.emptyList();

            req.setAttribute("activeCatId", activeCatId);
        }

        req.setAttribute("categories", categories);
        req.setAttribute("products", products);

        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}
