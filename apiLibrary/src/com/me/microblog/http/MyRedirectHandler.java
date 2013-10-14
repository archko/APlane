package com.me.microblog.http;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.protocol.HttpContext;

/**
 * @version 1.00.00
 * @description:
 * @author: archko 12-6-12
 */
public class MyRedirectHandler extends DefaultRedirectHandler {

    public URI lastRedirectedUri;

    @Override
    public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
        return super.isRedirectRequested(response, context);
    }

    @Override
    public URI getLocationURI(HttpResponse response, HttpContext context)
        throws ProtocolException {
        lastRedirectedUri=super.getLocationURI(response, context);
        System.out.println("lastRedirectedUri");

        return lastRedirectedUri;
    }
}
