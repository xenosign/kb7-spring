<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<html>
<head>
    <title>Student Edit</title>
</head>
<body>
<h1>학생 수정 페이지</h1>
<form action="/student/v1/edit" method="post">
    <input type="hidden" name="id" value="${student.id}" />
    이름 : <input type="text" id="name" name="name" value="${student.name}" required /><br />
    역할 : <input type="text" id="role" name="role" value="${student.role}" required /><br />
    특기 : <input type="text" id="specialty" name="specialty" value="${student.specialty}" /><br />
    상태 : <input type="text" id="status" name="status" value="${student.status}" /><br />
    <br />
    <button type="submit">수정 완료</button>
    <a href="/student/v1/list">목록으로</a>
</form>
</body>
</html>
