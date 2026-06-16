<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<html>
<head>
    <title>Spring</title>
</head>
<body>
  <h1>Hello, Spring World!!</h1>
  <ul>
      <c:forEach var="member" items="${memberList}">
          <li>${member.name} / ${member.email}</li>
      </c:forEach>
  </ul>
</body>
</html>
