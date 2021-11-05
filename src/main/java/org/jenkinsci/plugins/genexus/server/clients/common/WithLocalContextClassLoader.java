/*
 * The MIT License
 *
 * Copyright 2021 GeneXus S.A..
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.genexus.server.clients.common;

import org.jenkinsci.plugins.genexus.helpers.ThrowingSupplier;

/**
 * Defines a critical section during which the context class loader is set to
 * one obtained from the current class and thus being able to find classes
 * contained in this package or those on which it depends.
 *
 * This fixes class loading errors when accessing web services using JAX-WS.
 * More info about the problem at
 * https://www.eclipse.org/forums/index.php/t/266362/
 *
 * @author jlr
 */
public class WithLocalContextClassLoader {

    public static <T, E extends Throwable> T call(ThrowingSupplier<T, E> s) throws E {
        T result = null;

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(WithLocalContextClassLoader.class.getClassLoader());
        try {
            result = s.get();
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }

        return result;
    }
}
