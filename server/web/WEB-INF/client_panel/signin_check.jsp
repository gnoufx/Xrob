<%@ page import="com.theah64.xrob.api.database.tables.Clients" %>
<%@ page import="com.theah64.xrob.api.models.Client" %>
<%--
  Created by IntelliJ IDEA.
  User: theapache64
  Date: 30/8/16
  Time: 10:32 PM
  To change this template use File | Settings | File Templates.
--%>
<%

    final Object clientId = session.getAttribute(Clients.COLUMN_ID);

    if (clientId == null) {
        response.sendRedirect("/client/signup");
        return;
    } else {
        final Client client = Clients.getInstance().get(Clients.COLUMN_ID, clientId.toString());
        if (client != null) {
            request.setAttribute(Client.KEY, client);
        } else {
            //Expired
            session.invalidate();
            response.sendRedirect("/client/signin");
            return;
        }
    }

%>