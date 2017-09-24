package br.com.litecode.web;

import org.omnifaces.exceptionhandler.FullAjaxExceptionHandler;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.FacesContext;

public class AjaxExceptionHandlerFactory extends ExceptionHandlerFactory {
    private final ExceptionHandlerFactory parent;
    public AjaxExceptionHandlerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }
    @Override
    public ExceptionHandler getExceptionHandler() {
        return new FullAjaxExceptionHandler(parent.getExceptionHandler()) {
            @Override
            protected String findErrorPageLocation(FacesContext context, Throwable exception) {
                return "/errorPage.xhtml";
            }
        };
    }
}