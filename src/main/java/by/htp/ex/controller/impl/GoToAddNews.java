package by.htp.ex.controller.impl;

import java.io.IOException;
import by.htp.ex.controller.Command;
import by.htp.ex.util.validation.AccessValidation;
import by.htp.ex.util.validation.ValidationProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class GoToAddNews implements Command {

	private final AccessValidation accessValidation = ValidationProvider.getInstance().getAccessValidation();

	private static final String HAVE_NO_ACCESS_ERROR_LOCAL_KEY = "local.error.name.no_access";
	private static final String ERROR_MESSAGE_PARAM = "errorMessage";
	private static final String PRESENTATION_PARAM = "presentation";
	private static final String PRESENTATION_VALUE_FOR_ADD_NEWS = "addNews";

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();

		if (accessValidation.canExecuteAdminCommand(session)) {

			request.setAttribute(PRESENTATION_PARAM, PRESENTATION_VALUE_FOR_ADD_NEWS);
			request.getRequestDispatcher("WEB-INF/pages/layouts/baseLayout.jsp").forward(request, response);

		} else {
			request.getSession().setAttribute(ERROR_MESSAGE_PARAM, HAVE_NO_ACCESS_ERROR_LOCAL_KEY);
			response.sendRedirect("controller?command=go_to_error_page");
		}
	}
}
