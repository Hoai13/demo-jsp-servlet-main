<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"  %>
<%@ taglib prefix="t"   tagdir="/WEB-INF/tags"  %>
<!doctype html>
<html lang="${empty lang ? 'vi' : lang}">
<head>
  <meta charset="utf-8">
  <title><fmt:message key="app.title"/></title>
</head>
<body>

<fmt:setLocale value="${empty lang ? 'vi' : lang}"/>
<fmt:setBundle basename="app_i18n.messages"/>

<%@ include file="/WEB-INF/views/_layout.jspf" %>

<div class="container my-4">
  <h4 class="mb-3"><fmt:message key="products.title"/></h4>

  <c:choose>
    <c:when test="${empty products}">
      <div class="alert alert-info"><fmt:message key="products.empty"/></div>
    </c:when>
    <c:otherwise>
      <table class="table table-hover bg-white rounded-4">
        <thead>
          <tr>
            <th><fmt:message key="col.id"/></th>
            <th><fmt:message key="col.name"/></th>
            <th><fmt:message key="col.price"/></th>
            <th><fmt:message key="col.weight"/></th>
            <th><fmt:message key="col.category"/></th>
            <th><fmt:message key="col.description"/></th>
            <th></th>
          </tr>
        </thead>
        <tbody>
        <c:forEach var="p" items="${products}">
          <tr>
            <td>${p.id}</td>
            <td>${p.name}</td>
            <td>${p.price}</td>
            <td>${p.weight}</td>
            <td>${p.category}</td>
            <td><c:out value="${p.description}"/></td>
            <td>
              <a class="btn btn-sm btn-outline-primary"
                 href="<t:urlWithLang value='/products/detail?id=${p.id}'/>">
                <fmt:message key="btn.details"/>
              </a>
              <!-- Nút Sửa -->
                <a class="btn btn-sm btn-outline-secondary"
                   href="<t:urlWithLang value='/products/edit?id=${p.id}'/>">
                  <fmt:message key="btn.edit"/>
                </a>

                <!-- Nút Xóa -->
                <form method="post"
                      action="<t:urlWithLang value='/products/delete'/>"
                      style="display:inline"
                      onsubmit="return confirm('Bạn có chắc muốn xóa sản phẩm #${p.id}?');">
                  <input type="hidden" name="id" value="${p.id}"/>
                  <button class="btn btn-sm btn-outline-danger">
                    <fmt:message key="btn.delete"/>
                  </button>
            </td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </c:otherwise>
  </c:choose>
</div>
</body>
</html>
