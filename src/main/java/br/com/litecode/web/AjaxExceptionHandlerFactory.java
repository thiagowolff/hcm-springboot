package br.com.litecode.web;

import org.omnifaces.exceptionhandler.FullAjaxExceptionHandler;

import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.FacesContext;

public class AjaxExceptionHandlerFactory extends ExceptionHandlerFactory {
    private final ExceptionHandlerFactory wrapped;

    public AjaxExceptionHandlerFactory(ExceptionHandlerFactory wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new FullAjaxExceptionHandler(wrapped.getExceptionHandler()) {
            @Override
            protected String findErrorPageLocation(FacesContext context, Throwable exception) {
                if (exception instanceof ViewExpiredException) {
                    return "/index.xhtml";
                }
                return "/errorPage.xhtml";
            }
        };
    }
}