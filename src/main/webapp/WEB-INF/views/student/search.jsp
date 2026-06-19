<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<html>
<head>
    <title>Student List Page</title>
</head>
<body>
<h1>Student List</h1>

<form action="/student/v1/search" method="get">
    이름 검색 : <input type="text" name="name" value="${searchName}" />
    역할 :
    <select name="role">
        <option value="">전체</option>
        <option value="강사">강사</option>
        <option value="교육생">교육생</option>
    </select>
    <button type="submit">검색</button>
</form>
<br />
<ul>
    <li>이름 / 역할 / 특기 / 상태</li>
    <c:forEach var="student" items="${studentList}">
        <li>${student.name} / ${student.role} / ${student.specialty} / ${student.status}</li>
    </c:forEach>
</ul>
</body>
</html>
