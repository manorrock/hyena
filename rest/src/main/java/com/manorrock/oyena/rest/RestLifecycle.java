/*
 * Copyright (c) 2002-2020 Manorrock.com. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, 
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of the copyright holder nor the names of its 
 *      contributors may be used to endorse or promote products derived from this
 *      software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.manorrock.oyena.rest;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * The REST life-cycle.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
@ApplicationScoped
@Named("com.manorrock.oyena.rest.RestLifecycle")
public class RestLifecycle extends Lifecycle {

    /**
     * Stores the bean manager.
     */
    @Inject
    private BeanManager beanManager;

    /**
     * Stores the REST mapping matcher.
     */
    @Inject
    private RestMappingMatcher restMappingMatcher;

    /**
     * Stores the REST method executor.
     */
    @Inject
    private RestMethodExecutor restMethodExecutor;

    /**
     * Add a phase listener.
     *
     * <p>
     * This is ignored by the REST lifecycle.
     * </p>
     *
     * @param phaseListener the phase listener.
     */
    @Override
    public void addPhaseListener(PhaseListener phaseListener) {
    }

    /**
     * Perform the execute part of lifecycle.
     *
     * @param facesContext the Faces context.
     * @throws FacesException when a serious error occurs.
     */
    @Override
    public void execute(FacesContext facesContext) throws FacesException {
        RestMappingMatch match = restMappingMatcher.match(facesContext);
        if (match != null) {
            Object result = restMethodExecutor.execute(facesContext, match);
            facesContext.getAttributes().put(
                    RestLifecycle.class.getPackage().getName() + ".RestResult", result);
        } else {
            try {
                facesContext.getExternalContext().responseSendError(404, "Unable to match request");
                facesContext.responseComplete();
            } catch (IOException ioe) {
                throw new FacesException(ioe);
            }
        }
    }

    /**
     * Get the phase listeners.
     *
     * <p>
     * As phase listeners are ignored by the REST life-cycle this will always
     * return a zero length array.
     * </p>
     *
     * @return the empty array of phase listeners.
     */
    @Override
    public PhaseListener[] getPhaseListeners() {
        return new PhaseListener[0];
    }

    /**
     * Get the response writer for the response content type.
     *
     * @param responseContentType the response content type.
     * @return the response writer.
     */
    public RestResponseWriter getResponseWriter(String responseContentType) {
        RestResponseWriter result = null;
        AnnotatedType<RestResponseWriter> type = beanManager.createAnnotatedType(RestResponseWriter.class);
        Set<Bean<?>> beans = beanManager.getBeans(type.getBaseType());
        Iterator<Bean<?>> iterator = beans.iterator();
        while (iterator.hasNext()) {
            Bean<?> bean = iterator.next();
            RestResponseWriterContentType contentType = bean.getBeanClass().getAnnotation(RestResponseWriterContentType.class);
            if (contentType != null && contentType.value().equals(responseContentType)) {
                result = (RestResponseWriter) CDI.current().select(bean.getBeanClass()).get();
                break;
            }
        }
        if (result == null) {
            beans = beanManager.getBeans(type.getBaseType(), new Default.Literal());
            iterator = beans.iterator();
            Bean<?> bean = iterator.next();
            result = (RestResponseWriter) CDI.current().select(bean.getBeanClass()).get();
        }
        return result;
    }

    /**
     * Remove a phase listener.
     *
     * <p>
     * This is ignored by the REST life-cycle.
     * </p>
     *
     * @param phaseListener the phase listener.
     */
    @Override
    public void removePhaseListener(PhaseListener phaseListener) {
    }

    /**
     * Perform the render part of the REST life-cycle.
     *
     * @param facesContext the Faces context.
     * @throws FacesException when a serious error occurs.
     */
    @Override
    public void render(FacesContext facesContext) throws FacesException {
        if (!facesContext.getResponseComplete()) {
            ExternalContext externalContext = facesContext.getExternalContext();
            String responseContentType = externalContext.getResponseContentType();
            if (responseContentType == null) {
                externalContext.setResponseContentType("application/json");
                responseContentType = "application/json";
            }
            getResponseWriter(responseContentType).writeResponse(facesContext);
        }
    }
}
