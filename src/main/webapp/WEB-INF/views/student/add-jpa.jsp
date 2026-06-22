<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<html>
<head>
    <title>Student Add</title>
</head>
<body>
<h1>학생 추가 페이지</h1>
<form action="/student/v2/add" method="post">
    이름 : <input type="text" id="name" name="name" required /><br />
    역할 : <input type="text" id="role" name="role" required /><br />
    특기 : <input type="text" id="specialty" name="specialty" /><br />
    상태 : <input type="text" id="status" name="status" /><br />
    <br />
    <button type="submit">학생 추가</button>
    <a href="/student/v2/list">목록으로</a>
</form>
</body>
</html>
