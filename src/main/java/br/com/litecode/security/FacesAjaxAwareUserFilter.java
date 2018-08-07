package br.com.litecode.security;

//public class FacesAjaxAwareUserFilter extends UserFilter {
//    private static final String FACES_REDIRECT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><partial-response><redirect url=\"%s\"></redirect></partial-response>";
//
//    @Override
//    protected void redirectToLogin(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
//        HttpServletRequest request = (HttpServletRequest) servletRequest;
//
//        if ("partial/ajax".equals(request.getHeader("Faces-Request"))) {
//            servletResponse.setContentType("text/xml");
//            servletResponse.setCharacterEncoding("UTF-8");
//            servletResponse.getWriter().printf(FACES_REDIRECT_XML, request.getContextPath() + getLoginUrl());
//        } else {
//            super.redirectToLogin(servletRequest, servletResponse);
//        }
//    }
//}