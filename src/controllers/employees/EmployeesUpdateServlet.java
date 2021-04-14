package controllers.employees;

import java.util.List;
import java.io.IOException;
import java.sql.Timestamp;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import utils.DBUtil;
import models.Employee;
import models.validators.EmployeeValidator;
import utils.EncryptUtil;

/**
 * Servlet implementation class EmployeesUpdateServlet
 */
@WebServlet("/employees/update")
public class EmployeesUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmployeesUpdateServlet() {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String _token = request.getParameter("_token");
        if(_token != null && _token.equals(request.getSession().getId())) {
            EntityManager em = DBUtil.createEntityManager();


            Employee e = em.find(Employee.class,(Integer)(request.getSession().getAttribute("employee_id")));


            //現在の値と異なる社員番号が入力されていた
            //重複チェックを行う指定をする
            Boolean codeDuplicateCheckFlag = true;
            if(e.getCode().equals(request.getParameter("code"))) {
                codeDuplicateCheckFlag = false;
            } else {
                e.setCode(request.getParameter("code"));
            }


            //パスワード欄に入力があったら
            //パスワードの入力値チェックを行う指定をする
            Boolean passwordCheckFlag = true;
            String password = request.getParameter("password");
            if(password == null || password.equals("")) {
                passwordCheckFlag = false;
            } else {
                e.setPassword(
                        EncryptUtil.getPasswordEncrypt(
                                password,
                                (String)this.getServletContext().getAttribute("pepper")
                                )
                        );
            }


            e.setName(request.getParameter("name"));
            e.setAdmin_flag(Integer.parseInt(request.getParameter("admin_flag")));
            e.setUpdated_at(new Timestamp(System.currentTimeMillis()));
            e.setDelete_flag(0);


            List<String> errors = EmployeeValidator.validate(e, codeDuplicateCheckFlag, passwordCheckFlag);
            if(errors.size() > 0) {
                em.close();


                request.setAttribute("_token",request.getSession().getId());
                request.setAttribute("employee",e);
                request.setAttribute("errors",errors);


                RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/employees/edit.jsp");
                rd.forward(request,response);
            } else {
                em.getTransaction().begin();
                em.getTransaction().commit();
                em.close();
                request.getSession().setAttribute("flush","更新が完了しました。");


                request.getSession().removeAttribute("employee_id");


                response.sendRedirect(request.getContextPath() + "/employees/index");
            }
        }
    }

}
