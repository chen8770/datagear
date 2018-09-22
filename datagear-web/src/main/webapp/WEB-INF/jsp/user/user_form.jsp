<%--
/*
 * Copyright (c) 2018 by datagear.org.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.datagear.web.controller.UserController" %>
<%@ include file="../include/jsp_import.jsp" %>
<%@ include file="../include/jsp_ajax_request.jsp" %>
<%@ include file="../include/jsp_jstl.jsp" %>
<%@ include file="../include/jsp_page_id.jsp" %>
<%@ include file="../include/jsp_method_get_string_value.jsp" %>
<%@ include file="../include/html_doctype.jsp" %>
<%
//标题标签I18N关键字，不允许null
String titleMessageKey = getStringValue(request, UserController.KEY_TITLE_MESSAGE_KEY);
//表单提交action，允许为null
String formAction = getStringValue(request, UserController.KEY_FORM_ACTION, "#");
//是否只读操作，允许为null
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, UserController.KEY_READONLY)));

boolean isAdd = "saveAdd".equals(formAction);
%>
<html>
<head>
<%@ include file="../include/html_head.jsp" %>
<title><%@ include file="../include/html_title_app_name.jsp" %><fmt:message key='<%=titleMessageKey%>' /></title>
</head>
<body>
<div id="${pageId}" class="page-data-form page-data-form-user">
	<form id="${pageId}-form" action="<%=request.getContextPath()%>/user/<%=formAction%>" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="<c:out value='${user.id}' />" />
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='user.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="<c:out value='${user.name}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<%if(!readonly){%>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='user.password' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="password" value="" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='user.confirmPassword' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="confirmPassword" value="" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<%}%>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='user.realName' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="realName" value="<c:out value='${user.realName}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='user.email' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="email" value="<c:out value='${user.email}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<%--禁用新建管理员账号功能
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='user.admin' /></label>
				</div>
				<div class="form-item-value">
					<div class="user-admin-radios">
					<label for="${pageId}-userAdminYes"><fmt:message key='yes' /></label>
		   			<input type="radio" id="${pageId}-userAdminYes" name="admin" value="1" <c:if test='${user.admin}'>checked="checked"</c:if> />
					<label for="${pageId}-userAdminNo"><fmt:message key='no' /></label>
		   			<input type="radio" id="${pageId}-userAdminNo" name="admin" value="0" <c:if test='${!user.admin}'>checked="checked"</c:if> />
		   			</div>
				</div>
			</div>
			--%>
		</div>
		<div class="form-foot" style="text-align:center;">
			<%if(!readonly){%>
			<input type="submit" value="<fmt:message key='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<fmt:message key='reset' />" />
			<%}%>
		</div>
	</form>
</div>
<%@ include file="../include/page_js_obj.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.form = pageObj.element("#${pageId}-form");
	
	pageObj.element("input:submit, input:button, input:reset, button, .fileinput-button").button();
	<%--禁用新建管理员账号功能
	pageObj.element("input[name='admin']").checkboxradio({icon:false});
	pageObj.element(".user-admin-radios").controlgroup();
	--%>
	
	pageObj.url = function(action)
	{
		return contextPath + "/user/" + action;
	};
	
	<%if(!readonly){%>
	
	pageObj.form.validate(
	{
		rules :
		{
			name : "required",
			<%if(isAdd){%>
			password : "required",
			<%}%>
			confirmPassword :
			{
				<%if(isAdd){%>
				"required" : true,
				<%}%>
				"equalTo" : pageObj.element("input[name='password']")
			},
			email : "email"
		},
		messages :
		{
			name : "<fmt:message key='validation.required' />",
			<%if(isAdd){%>
			password : "<fmt:message key='validation.required' />",
			<%}%>
			confirmPassword :
			{
				<%if(isAdd){%>
				"required" : "<fmt:message key='validation.required' />",
				<%}%>
				"equalTo" : "<fmt:message key='user.validation.confirmPasswordError' />"
			},
			email : "<fmt:message key='validation.email' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function()
				{
					var pageParam = pageObj.pageParam();
					
					var close = true;
					
					if(pageParam && pageParam.afterSave)
						close = (pageParam.afterSave() != false);
					
					if(close)
						pageObj.close();
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item"));
		}
	});
	<%}%>
})
(${pageId});
</script>
</body>
</html>