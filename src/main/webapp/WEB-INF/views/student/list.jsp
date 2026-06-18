<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<html>
<head>
    <title>Student List Page</title>
</head>
<body>
<h1>Student List</h1>

<ul>
    <c:forEach var="student" items="${studentList}">
        <li>${student.name} / ${student.specialty} / ${student.status} / ${student.role} </li>
    </c:forEach>
</ul>
</body>
</html>
