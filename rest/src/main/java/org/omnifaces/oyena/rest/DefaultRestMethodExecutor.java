/*
 * Copyright (c) 2002-2020 OmniFaces. All Rights Reserved.
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
package org.omnifaces.oyena.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 * The default REST method executor.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
@ApplicationScoped
public class DefaultRestMethodExecutor implements RestMethodExecutor {

    /**
     * Stores the REST parameter producer.
     */
    @Inject
    private RestParameterProducer restParameterProducer;

    /**
     * Execute the method.
     *
     * @param facesContext the Faces context.
     * @param restMappingMatch the REST mapping match.
     */
    @Override
    public Object execute(FacesContext facesContext, RestMappingMatch restMappingMatch) {
        Instance instance = CDI.current().select(
                restMappingMatch.getBean().getBeanClass(), Any.Literal.INSTANCE);
        Object result;
        try {
            Object[] parameters = new Object[restMappingMatch.getMethod().getParameterCount()];
            if (parameters.length > 0) {
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = restParameterProducer.produce(
                            facesContext,
                            restMappingMatch,
                            restMappingMatch.getMethod().getParameterTypes()[i],
                            restMappingMatch.getMethod().getParameterAnnotations()[i]);
                }
            }
            result = restMappingMatch.getMethod().invoke(instance.get(), parameters);
        } catch (Throwable throwable) {
            throw new FacesException(throwable);
        }
        return result;
    }
}
