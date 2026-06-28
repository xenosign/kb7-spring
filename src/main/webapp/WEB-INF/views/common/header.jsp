<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div>
<%--  <a href="/">Home</a>--%>
<%--  <h3>회원 V0 - Plain</h3>--%>
<%--  <a href="/member/v0">회원 홈</a>--%>
<%--  <a href="/member/v0/list">회원 목록</a>--%>

<%--  <h3>회원 V1 - Spring</h3>--%>
<%--  <a href="/member/v1">회원 홈</a>--%>
<%--  <a href="/member/v1/list">회원 목록</a>--%>

<%--  <h3>회원 V2 - MyBatis</h3>--%>
<%--  <a href="/member/v2">회원 홈</a>--%>
<%--  <a href="/member/v2/list">회원 목록</a>--%>

<%--  <h3>교육생 - MyBatis</h3>--%>
<%--  <a href="/student/v1">교육생 홈</a>--%>
<%--  <a href="/student/v1/list">교육생 목록</a>--%>
<%--  <a href="/student/v1/add">교육생 추가</a>--%>
<%--  <a href="/student/v1/search">교육생 검색</a>--%>

    <h3>교육생 - JPA</h3>
    <a href="/student/v2">교육생 홈</a>
    <a href="/student/v2/list">교육생 목록</a>
    <a href="/student/v2/add">교육생 추가</a>
    <a href="/student/v2/search">교육생 검색</a>
    <a href="/404">404</a>
    <a href="/student/v2/error">error</a>

    <h3>교육생 - REST</h3>
    <a href="/api/student/v1/list">교육생 목록</a>
    <a href="/v1/api/student/v2/error">404</a>

    <h3>시큐리티 V1</h3>
    <a href="/">Home</a>
    <a href="/user/register">회원 가입</a>
    <a href="/user/login">로그인</a>
    <a href="/admin">어드민</a>
    <a href="#" onclick="document.getElementById('logout-form').submit();">로그아웃</a>
    <form id="logout-form" action="/user/logout" method="post" style="display: none;">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    </form>
</div>