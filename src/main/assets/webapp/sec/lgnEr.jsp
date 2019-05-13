<%@ page language="java" pageEncoding="UTF-8" session="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>    
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <title>Beige WEB login</title>
  <link rel="stylesheet" href="../css/bsCmn.css">
  <link rel="icon" type="image/png" href="../img/favicon.png">
</head>
<body>
  ${pageContext.servletContext.getAttribute("sesTrk").fail(pageContext.request)}
  <c:if test="${not empty sessionScope.isBanned}">
    <div class="alert-20">${pageContext.servletContext.getAttribute("i18n").getMsg('loginBan')}</div>
  </c:if>
  <c:if test="${empty sessionScope.isBanned}">
    <div class="alert-20">${loginErrorJsp}</div>
    
    <div class="form-std form-33-33">
      <div class="dialog-title">
        Authorization
      </div>
      <form id="formLogin" action="j_security_check" method="post">
        <label>User</label>
        <input type="text" name="j_username"/>
        <label>Password</label>
        <input type="password" name="j_password"/>
        <div class="form-actions">
          <input type="submit" value="Login"/>
          <a href="../">Close</a>
        </div>
      </form>
    </div>
  </c:if>
</body>
</html>
