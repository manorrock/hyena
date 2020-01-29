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
package com.manorrock.oyena.template;

import com.manorrock.piranha.DefaultWebApplicationResponse;
import java.util.List;
import javax.servlet.http.Cookie;

/**
 * The HTTP servlet response implementation for templating.
 * 
 * @author Manfred Riem (mriem@manorrock.com)
 */
public class TemplateHttpServletResponse extends DefaultWebApplicationResponse {

    /**
     * Constructor.
     */
    public TemplateHttpServletResponse() {
        super();
        this.outputStream = new TemplateServletOutputStream();
    }

    /**
     * Get the cookies.
     *
     * @return the cookies.
     */
    public List<Cookie> getCookies() {
        return cookies;
    }

    /**
     * Get the buffer size.
     *
     * @return the buffer size.
     */
    @Override
    public int getBufferSize() {
        return 0;
    }

    /**
     * Get the bytes in the buffer.
     *
     * @return the bytes in the buffer.
     */
    public byte[] getResponseBody() {
        if (this.gotWriter) {
            this.writer.flush();
        }
        TemplateServletOutputStream output = (TemplateServletOutputStream) this.outputStream;
        return output.getBytes();
    }

    /**
     * Reset the buffer.
     */
    @Override
    public void resetBuffer() {
        verifyNotCommitted("resetBuffer");
        TemplateServletOutputStream output = (TemplateServletOutputStream) this.outputStream;
        output.reset();
    }

    /**
     * Set the buffer size.
     *
     * @param size the buffer size.
     */
    @Override
    public void setBufferSize(int size) {
    }
}
